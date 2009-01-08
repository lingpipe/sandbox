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

package com.aliasi.lingmed.server;

import com.aliasi.lingmed.utils.FileUtils;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.rmi.RemoteException;

import java.util.Date;
import java.util.Properties;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import org.apache.log4j.Logger;

/**
 * <P>The <code>SearchService</code> command instantiates
 * a Lucene {@link org.apache.lucene.search.RemoteSearchable}
 * object for a local Lucene index and registers
 * this object with RMI, via a {@link SearchServer}.
 *
 * <P>The following arguments are all required:
 *
 * <dl>
 * <dt><code>-port</code></dt>
 * <dd>Port on which RMI registry service is listening.
 * </dd>
 *
 * <dt><code>-service</code></dt>
 * <dd>Name of service.  Used by RMI clients.
 * </dd>
 *
 * <dt><code>-index</code></dt>
 * <dd>Path to the Lucene index file.
 * </dd>
 * </dl>
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class SearchService extends AbstractCommand {
    private final Logger mLogger
	= Logger.getLogger(SearchService.class);

    private String mIndexName;
    private String mService;
    private int mPort;

    private SearchServer mServer;
    private File mIndex;
    private long mVersion;

    private final static String LUCENE_INDEX = "index";
    private final static String PORT = "port";
    private final static String SERVICE = "service";

    private final static Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(PORT,"1099");
    }

    private final static int SECOND = 1000;
    private final static int MINUTE = 60*SECOND;
    private final static int HOUR = 60*60*SECOND;

    // Instantiate SearchService object and 
    // initialize instance variables per command line args
    private SearchService(String[] args) throws IOException, 
						RemoteException {
        super(args,DEFAULT_PARAMS);
        mIndexName = getExistingArgument(LUCENE_INDEX);
        mService = getExistingArgument(SERVICE);
        mPort = getArgumentInt(PORT);
	reportParameters();

	mServer = new SearchServer(mPort);
	mLogger.info("search server instantiated on port: "+mPort);

	mIndex = FileUtils.checkIndex(mIndexName,false);
	mVersion = IndexReader.getCurrentVersion(mIndex);
	IndexSearcher searcher = new IndexSearcher(FSDirectory.getDirectory(mIndex));
	mLogger.info("instantiated index searcher, version: "+mVersion);
	mServer.registerSearcher(mService,searcher);
	mLogger.info("registered service: "+mService+" version: "+mVersion);
    }

    public void run() { 
	try {
	    while (true) {
		mLogger.info("check index versions");
		if (IndexReader.isLocked(mIndexName)) {
		    mLogger.info("index locked: "+mIndexName);
		} else if (mVersion != IndexReader.getCurrentVersion(mIndex)) {
		    mLogger.info("found new version of index for service: "+mService);
		    mVersion = IndexReader.getCurrentVersion(mIndex);
		    IndexSearcher searcher = new IndexSearcher(FSDirectory.getDirectory(mIndex));
		    mServer.registerSearcher(mService,searcher);
		    mLogger.info("re-registered service: "+mService+" version: "+mVersion);
		} else {
		    mLogger.info("index unchanged: "+mIndexName+" version: "+mVersion);
		}
		Thread.sleep(15*MINUTE);
	    }
	} catch (Exception e) {
	    String msg = "Unexpected exception=" + e.getMessage();
	    mLogger.warn(msg);
	    try {
		mServer.unregisterSearcher(mService);
	    } catch (RemoteException re) { 
		mLogger.warn(re.getMessage());
	    }
	    IllegalStateException e2 
		= new IllegalStateException(msg);
	    e2.setStackTrace(e.getStackTrace());
	    throw e2;
	}
    }

    public static void main(String[] args) throws Exception {
        SearchService server = new SearchService(args);
	server.run();
    }

    private void reportParameters() {
        mLogger.info("Remote search server"
		     + "\n\tStart time=" + Strings.msToString(startTimeMillis())
		     + "\n\tIndex=" + mIndexName
		     + "\n\tService=" + mService
		     + "\n\tPort=" + mPort);
    }

}
