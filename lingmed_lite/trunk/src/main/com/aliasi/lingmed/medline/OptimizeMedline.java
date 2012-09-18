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

import com.aliasi.lingmed.utils.FileUtils;
//import com.aliasi.lingmed.utils.Logging;

import com.aliasi.lingpipe.util.AbstractCommand;

import java.io.File;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.util.Version;

// import org.apache.log4j.Logger;

/** 
 * <P>The <code>Optimize</code> command processes a
 * optimized a Lucene Index on the local filesystem.
 */

public class OptimizeMedline extends AbstractCommand {

    //    private final Logger mLogger
    //        = Logger.getLogger(IndexMedline.class);

    private File mIndex;
    private String mIndexName;
    private final static String LUCENE_INDEX = "index";
    private final static Properties DEFAULT_PARAMS = new Properties();

    // Instantiate IndexMedline object and 
    // initialize instance variables per command line args
    private OptimizeMedline(String[] args) throws Exception {
        super(args,DEFAULT_PARAMS);
        mIndexName = getExistingArgument(LUCENE_INDEX);
	mIndex = FileUtils.checkIndex(mIndexName,false);
    }

    /**
     * Run the command.  See class documentation above for details on
     * arguments and behavior.
     */
    public void run() {
        //        mLogger.info("start run");
        try {
            Directory fsDir = FSDirectory.open(mIndex);
            IndexWriterConfig iwConf 
                = new IndexWriterConfig(Version.LUCENE_36,
                                        new StandardAnalyzer(Version.LUCENE_36));
            iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

            IndexWriter indexWriter = new IndexWriter(fsDir,iwConf);
            indexWriter.forceMerge(1);
            indexWriter.commit();
            //            mLogger.info("Processing complete.");
        } catch (Exception e) {
            //            mLogger.warn("Unexpected Exception: "+e.getMessage());
            //            mLogger.warn("stack trace: "+Logging.logStackTrace(e));
            //            mLogger.warn("Aborting this run");
            IllegalStateException e2 
                = new IllegalStateException(e.getMessage());
            e2.setStackTrace(e.getStackTrace());
            throw e2;
        }
    }

    public static void main(String[] args) throws Exception {
        OptimizeMedline optimizer = new OptimizeMedline(args);
	optimizer.run();
    }

}