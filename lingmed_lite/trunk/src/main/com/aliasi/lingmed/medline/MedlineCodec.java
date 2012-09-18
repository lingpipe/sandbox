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

import com.aliasi.lingmed.medline.parser.MedlineCitation;
import com.aliasi.lingmed.medline.parser.MedlineHandler;
import com.aliasi.lingmed.medline.parser.MedlineParser;

import com.aliasi.lingpipe.util.Strings;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.util.Version;

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

    public static final PerFieldAnalyzerWrapper MEDLINE_ANALYZER;
    private static final Map<String,Analyzer> analyzerPerField 
        = new HashMap<String,Analyzer>();
    static {
        KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
        StandardAnalyzer contentAnalyzer = new StandardAnalyzer(Version.LUCENE_36);
        for (String field : Fields.TEXT_FIELDS) {
            analyzerPerField.put(field, 
                                 new LimitTokenCountAnalyzer(contentAnalyzer,
                                                             Integer.MAX_VALUE));
        }
        for (String field : Fields.SIMPLE_FIELDS) {
            analyzerPerField.put(field, 
                                 new LimitTokenCountAnalyzer(contentAnalyzer,
                                                             Integer.MAX_VALUE));
        }
        for (String field : Fields.KEYWORD_FIELDS) {
            analyzerPerField.put(field, keywordAnalyzer); 
        }
        MEDLINE_ANALYZER = new PerFieldAnalyzerWrapper(new KeywordAnalyzer(), analyzerPerField);
    }

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
                                   CompressionTools.compressString(citation.xmlString()),
                                   Field.Store.YES);
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

    public Analyzer getAnalyzer() {
        return MEDLINE_ANALYZER;
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

}
