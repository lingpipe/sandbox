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
 
package com.aliasi.lingmed.wormbase;

import com.aliasi.corpus.Handler;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

import com.aliasi.lingmed.dao.*;
import com.aliasi.lingmed.medline.*;
import com.aliasi.lingmed.server.*;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Files;

import java.io.*;
import java.util.Properties;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

/** 
 * map Wormbase literature citations to their Pubmed IDs
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.3
 */

public class WormbaseCitations extends AbstractCommand {
    private final Logger mLogger
        = Logger.getLogger(WormbaseCitations.class);

    private String mEndnoteFileName;
    private String mMedlineIndex;
    private MedlineSearcher mMedlineSearcher;
    private File mEndnoteFile;

    private final static String ENDNOTE_FILE = "endnote";
    private final static String MEDLINE_INDEX = "medline";
    private final static Properties DEFAULT_PARAMS = new Properties();

    private WormbaseCitations(String[] args) throws IOException {
        super(args,DEFAULT_PARAMS);
        mMedlineIndex = getExistingArgument(MEDLINE_INDEX);
        FileUtils.checkIndex(mMedlineIndex,false);
        Searcher medlineLocalSearcher = new IndexSearcher(mMedlineIndex);
        mEndnoteFileName = getExistingArgument(ENDNOTE_FILE);
        mEndnoteFile = FileUtils.checkInputFile(mEndnoteFileName);
    }


    public void run() {
        mLogger.info("\nBegin");
        try {
            Parser<ObjectHandler<EndnoteRecord>> parser = new EndnoteParser();
            SimpleHandler handler = new SimpleHandler(mMedlineSearcher);
            parser.setHandler(handler);
            InputSource inSource = new InputSource();
            inSource.setCharacterStream(new FileReader(mEndnoteFile));
            parser.parse(inSource);
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
        WormbaseCitations wbc = new WormbaseCitations(args);
        wbc.run();

    }

    static class SimpleHandler implements ObjectHandler<EndnoteRecord> {
        private MedlineSearcher mMedlineSearcher;

        public SimpleHandler(MedlineSearcher searcher) {
            mMedlineSearcher = searcher;
        }

        public void handle(EndnoteRecord rec) {
            System.out.println("processing record: "+rec.toString());
            //            String pubmedId = mMedlineSearcher.getPubmedIdForEndnote(rec);
            //            if (pubmedId == null) {
            //                // log not found;
            //                return;
            //            }
            //            rec.setPubmedId(pubmedId);
        }
    }

}
