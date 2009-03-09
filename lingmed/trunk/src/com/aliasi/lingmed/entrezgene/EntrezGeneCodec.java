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

    public static final String SPECIES_FIELD = "species";
    public static final String PMID_FIELD = "PMID";

    public Document toDocument(EntrezGene e) { 
        Document doc = new Document(); 
		// index EntrezGene id (as keyword)
        Field idField = new Field(Fields.ID_FIELD,e.getGeneId(),
                                  Field.Store.YES,
                                  Field.Index.TOKENIZED);
        doc.add(idField);
		// index Species (latin name: "Homo Sapiens")
		if (e.getSpeciesTaxName() != null) {
			Field speciesField = new Field(SPECIES_FIELD,e.getSpeciesTaxName(),
										   Field.Store.YES,
										   Field.Index.TOKENIZED);
			doc.add(speciesField);
		}
		// index pubmed article ids (if any) which reference this gene
		String[] pmidRefs = e.getUniquePubMedRefs();
		for (String pmid : pmidRefs) {
			Field pmidField = new Field(PMID_FIELD,pmid,
										Field.Store.YES,
										Field.Index.TOKENIZED);
			doc.add(pmidField);
		}
		Field xmlField = new Field(Fields.XML_FIELD,e.xmlString(),
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


    /* 
     * EntrezGene analyzer uses keyword analyzer on ID field,
     * numeric id analyzer on PMID field
     */
    public LuceneAnalyzer getAnalyzer() {
		LuceneAnalyzer analyzer = new LuceneAnalyzer();
		analyzer.setTokenizer(PMID_FIELD,NUMBER_TOKENIZER_FACTORY);
		return analyzer;
    }

    static class ExtractionHandler implements ObjectHandler<EntrezGene> {
		EntrezGene mGene;
		public void handle(EntrezGene gene) {
			mGene = gene;
		}
    }

    public static final TokenizerFactory NUMBER_TOKENIZER_FACTORY
        = new RegExTokenizerFactory("\\d+");


}
