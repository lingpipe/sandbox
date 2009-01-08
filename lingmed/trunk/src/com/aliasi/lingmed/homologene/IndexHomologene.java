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
import com.aliasi.corpus.Parser;

import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.util.AbstractCommand;

import java.io.File;
import java.io.IOException;

import java.util.Properties;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import org.xml.sax.InputSource;

import org.apache.log4j.Logger;

/** 
 * <P>The <code>IndexHomologene</code> command processes
 * a file of XML-formatted HomologeneGroup entries and adds
 * them to a Lucene Index on the local filesystem.
 *
 * <P>The following arguments are required:
 *
 * <dt><code>-index</code></dt>
 * <dd>Path to the Lucene index file.
 * </dd>
 *
 * <dt><code>-distFile</code></dt>
 * <dd>Path to the XML file.
 * </dd>
 * </dl>
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class IndexHomologene extends AbstractCommand {
    private final Logger mLogger
	= Logger.getLogger(IndexHomologene.class);

    private File mDistFile;
    private String mDistFileName;
    private File mIndex;
    private String mIndexName;

    private HomologeneCodec mCodec = new HomologeneCodec();

    private final static String DIST_FILE = "distFile";
    private final static String LUCENE_INDEX = "index";
    private final static Properties DEFAULT_PARAMS = new Properties();

    /*
     * Instantiate IndexHomologene object and 
     * initialize instance variables per command line args
     */
    private IndexHomologene(String[] args) throws Exception {
        super(args,DEFAULT_PARAMS);
        mIndexName = getExistingArgument(LUCENE_INDEX);
	mDistFileName = getExistingArgument(DIST_FILE);
	reportParameters();
	mIndex = FileUtils.checkIndex(mIndexName,true);
	mDistFile = FileUtils.checkInputFile(mDistFileName);
    }

    private void reportParameters() {
        mLogger.info("Indexing Homologene "
		     + "\n\tIndex=" + mIndexName
		     + "\n\tHomologene distribution=" + mDistFileName
		     );
    }

    public void run() {
	mLogger.info("Begin indexing");
	try {
	    IndexWriter indexWriter = new IndexWriter(mIndex,mCodec.getAnalyzer());
	    HomologeneIndexer indexer = new HomologeneIndexer(indexWriter,mCodec);

	    Parser<ObjectHandler<HomologeneGroup>> parser = new HomologeneParser(true);  
	    parser.setHandler(indexer);
	    InputSource inSource = new InputSource(mDistFileName);
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
        IndexHomologene indexer = new IndexHomologene(args);
	indexer.run();
    }

    static class HomologeneIndexer implements ObjectHandler<HomologeneGroup> {
	final IndexWriter mIndexWriter;
	final HomologeneCodec mCodec;

        public HomologeneIndexer(IndexWriter indexWriter, HomologeneCodec codec) {
	    mIndexWriter = indexWriter;
	    mCodec = codec;
        }

        public void handle(HomologeneGroup hg) {
	    Logger.getLogger(IndexHomologene.class).debug("homologene entry: " 
							  + hg.toString());
	    Document doc = mCodec.toDocument(hg);
	    try { 
		mIndexWriter.addDocument(doc);  
	    } catch (IOException ioe) {
		Logger.getLogger(IndexHomologene.class).warn("Exception indexing Homologene: " + ioe);
	    }
        }

        public void close() throws IOException {
            mIndexWriter.optimize();  // merges segments
            mIndexWriter.close();     // commits to disk
        }
    }

}
