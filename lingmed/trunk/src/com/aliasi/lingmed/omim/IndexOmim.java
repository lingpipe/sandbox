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

import com.aliasi.corpus.Handler;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.util.AbstractCommand;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Properties;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import org.xml.sax.InputSource;

import org.apache.log4j.Logger;


/** 
 * <P>The <code>IndexOmim</code> command processes a
 * file of OMIM records and adds
 * them to a Lucene Index on the local filesystem.
 * 
 * <P>The following arguments are required:
 *
 * <dt><code>-index</code></dt>
 * <dd>Path to the Lucene index file.
 * </dd>
 *
 * <dt><code>-distFile</code></dt>
 * <dd>Path to the OMIM distribution text file.
 * </dd>
 * </dl>
 *
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class IndexOmim extends AbstractCommand {
    private final Logger mLogger
	= Logger.getLogger(IndexOmim.class);

    private File mDistFile;
    private String mDistFileName;
    private File mIndex;
    private String mIndexName;

    private final static String DIST_FILE = "distFile";
    private final static String LUCENE_INDEX = "index";
    private final static Properties DEFAULT_PARAMS = new Properties();


    private OmimCodec mCodec = new OmimCodec();

    // Instantiate IndexOmim object and 
    // initialize instance variables per command line args
    private IndexOmim(String[] args) throws Exception {
        super(args,DEFAULT_PARAMS);
        mIndexName = getExistingArgument(LUCENE_INDEX);
	mDistFileName = getExistingArgument(DIST_FILE);
	reportParameters();
	mIndex = FileUtils.checkIndex(mIndexName,true);
	mDistFile = FileUtils.checkInputFile(mDistFileName);
    }

    private void reportParameters() {
        mLogger.info("Indexing OMIM "
		     + "\n\tIndex=" + mIndexName
		     + "\n\tOmim distribution=" + mDistFileName
		     );
    }

    public void run() {
	mLogger.info("Begin indexing");
	try {
	    IndexWriter indexWriter = new IndexWriter(mIndex,mCodec.getAnalyzer());
	    OmimIndexer indexer = new OmimIndexer(indexWriter);
	    Parser<ObjectHandler<OmimRecord>> parser = new OmimParser(true);
	    parser.setHandler(indexer);
	    InputSource inSource = new InputSource();
	    inSource.setCharacterStream(new FileReader(mDistFile));
	    parser.parse(inSource);
	    mLogger.info("Parsed index, now optimize.");
	    indexer.close();
	    mLogger.info("Processing complete.");
	} catch (Exception e) {
	    mLogger.warn("Unexpected Exception: "+e.getMessage());
	    mLogger.warn("stack trace: "+Logging.logStackTrace(e));
	    IllegalStateException e2 
		= new IllegalStateException(e.getMessage());
	    e2.setStackTrace(e.getStackTrace());
	    throw e2;
	}
    }

    public static void main(String[] args) throws Exception {
        IndexOmim indexer = new IndexOmim(args);
	indexer.run();
    }

    static class OmimIndexer implements ObjectHandler<OmimRecord> {
        IndexWriter mIndexWriter;
	final OmimCodec mOmimCodec;

        public OmimIndexer(IndexWriter indexWriter) {
            mIndexWriter = indexWriter;
	    mOmimCodec = new OmimCodec();
        }

        public void handle(OmimRecord rec) {
	    if (!rec.isMoved()) {
		Logger logger = Logger.getLogger(IndexOmim.class);
		if (logger.isDebugEnabled()) {
		    logger.debug("Adding OmimRecord, MimId=" + rec.getMimId()
				 +"\ttitle=" + rec.getTitle());
		    String[] alts = rec.getAltTitles();
		    for (String alt : alts) {
			logger.debug("\talt title=" + alt);
		    }
		}
		Document doc = mOmimCodec.toDocument(rec);
		try { 
		    mIndexWriter.addDocument(doc);  
		} catch (IOException ioe) {
		    Logger.getLogger(IndexOmim.class).warn("Exception indexing OMIM: " + ioe);
		}
	    }
        }

        public void close() throws IOException {
            mIndexWriter.optimize();  // merges segments
            mIndexWriter.close();     // commits to disk
        }
    }


}
