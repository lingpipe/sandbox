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

package com.aliasi.lingmed.homologene;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.lingmed.dao.Codec;
import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.dao.SearchResults;
import com.aliasi.lingmed.lucene.Fields;
import com.aliasi.lingmed.lucene.LuceneAnalyzer;

import com.aliasi.tokenizer.LineTokenizerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;

import org.xml.sax.InputSource;

/**
 * Implmentation  of {@link Codec} for Homologene HomologeneGroup entries.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class HomologeneCodec implements Codec<HomologeneGroup> {

    public static final String TAXON_FIELD = "gene_taxon_id";
    public static final String ALIAS_FIELD = "alias";

    public Document toDocument(HomologeneGroup hg) { 
        Document doc = new Document(); 

	// index Homologene id (as keyword)
        Field idField = new Field(Fields.DEFAULT_FIELD,hg.getGroupId(),
                                  Field.Store.YES,
                                  Field.Index.TOKENIZED);
        doc.add(idField);

	// index HomologeneGroup by EntrezId,TaxonId
	String[] ids = hg.getGeneTaxonIds();
	for (int i = 0; i < ids.length; ++i) {
	    Field geneTaxonIdField = new Field(TAXON_FIELD,ids[i],
					Field.Store.YES,
					Field.Index.TOKENIZED);
	    doc.add(geneTaxonIdField);
	}

	// index HomologeneGroup by Aliases
	String[] aliases = hg.getUniqueAliases();
	for (int i = 0; i < aliases.length; ++i) {
	    Field aliasField = new Field(ALIAS_FIELD,aliases[i],
					Field.Store.YES,
					Field.Index.TOKENIZED);
	    doc.add(aliasField);
	}

	// store raw XML
	Field xmlField = new Field(Fields.XML_FIELD,hg.getXmlString(),
				   Field.Store.COMPRESS,
				   Field.Index.NO);
	doc.add(xmlField);

	return doc;
    }

    public HomologeneGroup toObject(Document d) { 
	String xml = d.get(Fields.XML_FIELD);
	InputSource is = new InputSource(new StringReader(xml)); 
	HomologeneParser parser = new HomologeneParser();
	ExtractionHandler handler = new ExtractionHandler();
	parser.setHandler(handler);
	try {
	    parser.parse(is);
	    return handler.mGroup;
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace(System.err);
	}
	return null;
    }

    static class ExtractionHandler implements ObjectHandler<HomologeneGroup> {
	HomologeneGroup mGroup;
	public void handle(HomologeneGroup hg) {
	    mGroup = hg;
	}
    }

    public LuceneAnalyzer getAnalyzer() {
	return new LuceneAnalyzer();
    }

}
