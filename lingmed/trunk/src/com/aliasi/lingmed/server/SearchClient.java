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

import com.aliasi.lingmed.dao.DaoException;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Searcher;

import org.apache.log4j.Logger;

/**
 * A <code>SearchClient</code> contains a Searcher
 * for a remote Lucene Index.
 */

public class SearchClient {
    private final Logger mLogger
	= Logger.getLogger(SearchClient.class);

    private MultiSearcher mSearcher;

    public SearchClient(String name, String server, int port) throws DaoException {
	String url = "//"+server+":"+port+"/"+name;
	mLogger.info("url: "+url);
	//	System.setProperty("java.rmi.server.hostname",server);
	//	mLogger.info("rmi name: "+System.getProperty("java.rmi.server.hostname"));
	try {
	    Searchable searchable = (Searchable)Naming.lookup(url);
	    mLogger.info("naming lookup succeeded, searchable: "+searchable);
	    mSearcher = new MultiSearcher(new Searchable[]{ searchable });
	} catch (IOException ioe) {
	    throw new DaoException(ioe.getMessage(), ioe);
	} catch (NotBoundException nbe) {
	    throw new DaoException(nbe.getMessage(), nbe);
	}
    }

    /** Returns the searcher over the remote index. */
    // check version?
    // error handling?
    public Searcher getSearcher() {
	return mSearcher;
    }

}
