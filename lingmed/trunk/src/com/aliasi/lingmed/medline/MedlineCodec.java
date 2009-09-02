/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.lingmed.medline;

import com.aliasi.lingmed.dao.Codec;
import com.aliasi.lingmed.lucene.Fields;
import com.aliasi.lingmed.lucene.LuceneAnalyzer;

import com.aliasi.medline.MedlineCitation;
import com.aliasi.medline.MedlineHandler;
import com.aliasi.medline.MedlineParser;

import com.aliasi.tokenizer.EnglishStopListFilterTokenizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseFilterTokenizer;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerFilterTokenizer;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Implmentation  of {@link Codec} for Medline Citation entries.
 *
 * @author  Mitzi Morris, Bob Carpenter
 * @version 1.1
 * @since   LingMed1.0
 */

public class MedlineCodec implements Codec<MedlineCitation> {

    public MedlineCodec() { 
    }

    public Document toDocument(MedlineCitation citation) {
        Document doc = new Document(); 

        // index pubmed id (as keyword)
        Field idField = new Field(Fields.ID_FIELD,
                                  citation.pmid(),
                                  Field.Store.YES,
                                  Field.Index.NOT_ANALYZED_NO_NORMS);
        doc.add(idField);

        // store raw XML
        Field xmlField = new Field(Fields.XML_FIELD,
                                   citation.xmlString(),
                                   Field.Store.COMPRESS,
                                   Field.Index.NO);
        doc.add(xmlField);
        return doc;
    }

    // may return null if the document is a "filename" doc rather
    // than an actual encoding of a MEDLINE object
    public MedlineCitation toObject(Document d) { 
        String xml = d.get(Fields.XML_FIELD);
        if (xml == null) return null; // trying to convert filename doc ??
        InputSource is = new InputSource(new StringReader(xml)); 
        MedlineParser parser = new MedlineParser(false);
        ExtractionHandler handler = new ExtractionHandler();
        try {
            parser.parse(is,handler);
            return handler.mCitation;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        } catch (SAXException e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        return null;
    }

    /* toRecodableObject
	creates MedlineCitation object that contains rawXML field,
	so we can codec can convert it back to a Document
	(document always stores XML_FIELD) 
    */
    public MedlineCitation toRecodableObject(Document d) { 
        String xml = d.get(Fields.XML_FIELD);
        if (xml == null) return null; // trying to convert filename doc ??
        InputSource is = new InputSource(new StringReader(xml)); 
        MedlineParser parser = new MedlineParser(true);
        ExtractionHandler handler = new ExtractionHandler();
        try {
            parser.parse(is,handler);
            return handler.mCitation;
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        } catch (SAXException e) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
        return null;
    }

    public LuceneAnalyzer getAnalyzer() {
        return MedlineAnalyzer.INSTANCE;
    }

    static class ExtractionHandler implements MedlineHandler {
        MedlineCitation mCitation;

        public void handle(MedlineCitation citation) {
            mCitation = citation;
        }

        public void delete(String pmid) {
            String msg = "Cannot handle deleteions.";
            throw new UnsupportedOperationException(msg);
        }
    }

    public static String titleAbstract(MedlineCitation citation) {
        StringBuffer textBuf = new StringBuffer();
        textBuf.append(citation.article().articleTitleText());
        textBuf.append(Strings.NEWLINE_CHAR);
        if (citation.article().abstrct() != null)
            textBuf.append(citation.article().abstrct().textWithoutTruncationMarker());
        return textBuf.toString();
    }

    /**
     * Analyzer used for creating and searching Lucene index of Medline Citation entries.
     * Tokenizes input via a {@link com.aliasi.tokenizer.LineTokenizerFactory}.
     */
    static class MedlineAnalyzer extends LuceneAnalyzer {

        static class StandardTokenizerFactory implements TokenizerFactory {
            public Tokenizer tokenizer(char[] cs, int start, int length) {
                Tokenizer tokenizer = SIMPLE_TOKENIZER_FACTORY.tokenizer(cs,start,length);
                tokenizer = new LowerCaseFilterTokenizer(tokenizer);
                tokenizer = new EnglishStopListFilterTokenizer(tokenizer);
                tokenizer = new PorterStemmerFilterTokenizer(tokenizer);
                return tokenizer;
            }
        } // acts like Lucene's StandardAnalyzer
        public TokenizerFactory TEXT_TOKENIZER_FACTORY
            = new StandardTokenizerFactory();

        // like Lucene's analysis.SimpleAnalyzer, but with digits, too
        public static final TokenizerFactory SIMPLE_TOKENIZER_FACTORY
            = new RegExTokenizerFactory("\\p{L}+|\\p{Digit}+");

        public static final TokenizerFactory EXACT_TEXT_TOKENIZER_FACTORY 
            = IndoEuropeanTokenizerFactory.FACTORY;

        public static final TokenizerFactory NGRAM_TEXT_TOKENIZER_FACTORY
            = new NGramTokenizerFactory(3,3);

        public static final MedlineAnalyzer INSTANCE
            = new MedlineAnalyzer();

        private MedlineAnalyzer() {
            for (String field : SearchableMedlineCodec.TEXT_FIELDS) {
                setTokenizer(field,TEXT_TOKENIZER_FACTORY);
                setTokenizer(field + SearchableMedlineCodec.EXACT_FIELD_SUFFIX,
                             EXACT_TEXT_TOKENIZER_FACTORY);
                setTokenizer(field + SearchableMedlineCodec.NGRAM_FIELD_SUFFIX,
                             NGRAM_TEXT_TOKENIZER_FACTORY);
            }
            for (String field : SearchableMedlineCodec.SIMPLE_FIELDS) {
                setTokenizer(field,SIMPLE_TOKENIZER_FACTORY);
            }
        }

    }


}
