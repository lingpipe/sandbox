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

package com.aliasi.lingmed.entrezgene;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.lingmed.dao.Codec;
import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.dao.SearchResults;
import com.aliasi.lingmed.lucene.Fields;
import com.aliasi.lingmed.lucene.LuceneAnalyzer;

import com.aliasi.tokenizer.EnglishStopListFilterTokenizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseFilterTokenizer;
import com.aliasi.tokenizer.PorterStemmerFilterTokenizer;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import org.apache.lucene.analysis.Analyzer;

import org.xml.sax.InputSource;

/**
 * Implmentation  of {@link Codec} for Entrez Gene entries.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class EntrezGeneCodec implements Codec<EntrezGene> {

    public Document toDocument(EntrezGene e) { 
        Document doc = new Document(); 

        Field idField 
			= new Field(Fields.ID_FIELD,
						e.getGeneId(),
						Field.Store.YES,
						Field.Index.TOKENIZED);
        doc.add(idField);

		if (e.getSpeciesTaxName() != null) {
			Field speciesField 
				= new Field(Fields.ENTREZGENE_SPECIES_FIELD,
							e.getSpeciesTaxName(),
							Field.Store.YES,
							Field.Index.TOKENIZED);
			doc.add(speciesField);
		}
		if (e.getOfficialSymbol() != null) {
			Field symbolField
				= new Field(Fields.ENTREZGENE_SYMBOL_FIELD,
							e.getOfficialSymbol(),
							Field.Store.YES,
							Field.Index.TOKENIZED);
			doc.add(symbolField);
		}

        if (e.getGeneTrackStatus() != null) {
			Field statusField
				= new Field(Fields.ENTREZGENE_STATUS_FIELD,
							e.getGeneTrackStatus(),
							Field.Store.YES,
							Field.Index.TOKENIZED);
			doc.add(statusField);
		}
        if (e.getEntrezgeneType() != null) {
			Field typeField
				= new Field(Fields.ENTREZGENE_TYPE_FIELD,
							e.getEntrezgeneType(),
							Field.Store.YES,
							Field.Index.TOKENIZED);
			doc.add(typeField);
		}

        if (e.getTextData() != null) {
			String texts = e.getTextData().trim();
			if (texts.length() > 1) {
				Field textsField
					= new Field(Fields.ENTREZGENE_TEXTS_FIELD,
								texts,
								Field.Store.YES,
								Field.Index.TOKENIZED);
				doc.add(textsField);
			}
		}


		// index pubmed article ids (if any) which reference this gene
		String[] pmidRefs = e.getUniquePubMedRefs();
		for (String pmid : pmidRefs) {
			Field pmidField = 
				new Field(Fields.ENTREZGENE_PMID_FIELD,pmid,
						  Field.Store.YES,
						  Field.Index.TOKENIZED);
			doc.add(pmidField);
		}

		Field xmlField 
			= new Field(Fields.XML_FIELD,e.xmlString(),
						Field.Store.COMPRESS,
						Field.Index.NO);
		doc.add(xmlField);
		return doc;
    }

    public EntrezGene toObject(Document d) { 
		String xml = d.get(Fields.XML_FIELD);
		InputSource is = new InputSource(new StringReader(xml)); 
		EntrezParser parser = new EntrezParser(false);
		ExtractionHandler handler = new ExtractionHandler();
		parser.setHandler(handler);
		try {
			parser.parse(is);
			return handler.mGene;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}
		return null;
    }


    public LuceneAnalyzer getAnalyzer() {
        return EntrezGeneAnalyzer.INSTANCE;
    }


    /**
     * Analyzer used for creating and searching Lucene index of 
	 * Entrez Gene entries.
     * Per-field analyzers:
	 * <ul>
	 *  <li>numeric ids:  Entrez Gene, PubMed (Medline Citation)</li>
	 *  <li>keywords:  species, status, geneType</li>
	 *  <li>text: </li>
	 * </ul>
     */
    static class EntrezGeneAnalyzer extends LuceneAnalyzer {

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


        public static final EntrezGeneAnalyzer INSTANCE
            = new EntrezGeneAnalyzer();

        private EntrezGeneAnalyzer() {
			setTokenizer(Fields.ID_FIELD,SIMPLE_TOKENIZER_FACTORY);
			setTokenizer(Fields.ENTREZGENE_PMID_FIELD,SIMPLE_TOKENIZER_FACTORY);
			setTokenizer(Fields.ENTREZGENE_TEXTS_FIELD,TEXT_TOKENIZER_FACTORY);
        }

    }

    static class ExtractionHandler implements ObjectHandler<EntrezGene> {
		EntrezGene mGene;
		public void handle(EntrezGene gene) {
			mGene = gene;
		}
    }

}
