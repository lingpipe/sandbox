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

package com.aliasi.lingmed.omim;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.lingmed.dao.Codec;
import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.lucene.Fields;
import com.aliasi.lingmed.lucene.LuceneAnalyzer;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.tokenizer.LineTokenizerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

import org.xml.sax.InputSource;

import org.apache.log4j.Logger;

/**
 * Implmentation  of {@link Codec} for Omim entries.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class OmimCodec implements Codec<OmimRecord> {
    private final Logger mLogger
	= Logger.getLogger(OmimCodec.class);

    public Document toDocument(OmimRecord rec) { 
        Document doc = new Document(); 
	// index Omim id (as keyword)
        Field idField = new Field(Fields.ID_FIELD,String.valueOf(rec.getMimId()),
                                  Field.Store.YES,
                                  Field.Index.TOKENIZED);
        doc.add(idField);
	Field rawTextField = new Field(Fields.RAW_TEXT_FIELD,rec.getRawText(),
				   Field.Store.COMPRESS,
				   Field.Index.NO);
	doc.add(rawTextField);
	return doc;
    }

    public OmimRecord toObject(Document d) { 
	String text = d.get(Fields.RAW_TEXT_FIELD);
	InputSource is = new InputSource(new StringReader(text)); 
	
	OmimParser parser = new OmimParser(false);
	ExtractionHandler handler = new ExtractionHandler();
	parser.setHandler(handler);
	try {
	    parser.parse(is);
	    return handler.mRec;
	} catch (IOException e) {
	    mLogger.warn("parser error: "+e.getMessage());
	    mLogger.warn("stack trace: "+Logging.logStackTrace(e));
	}
	return null;
    }

    public LuceneAnalyzer getAnalyzer() {
	LuceneAnalyzer analyzer = new LuceneAnalyzer();
	return analyzer;
    }


    static class ExtractionHandler implements ObjectHandler<OmimRecord> {
	OmimRecord mRec;
	public void handle(OmimRecord rec) {
	    mRec = rec;
	}
    }


}
