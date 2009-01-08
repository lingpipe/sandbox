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

import org.apache.lucene.search.RemoteSearchable;
import org.apache.lucene.search.Searcher;

import java.rmi.AlreadyBoundException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * <P>A <code>SearchServer</code> registers and unregisters
 * Lucene {@link org.apache.lucene.search.RemoteSearchable} objects
 * with the local RMI registry.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class SearchServer {
    private final Logger mLogger
	= Logger.getLogger(SearchServer.class);

    private final Registry mRegistry;
    private int mPort;

    private static final HashMap<String,RemoteSearchable> sSearchablesMap 
	= new HashMap<String,RemoteSearchable>();

    public SearchServer(int port) throws RemoteException {
	mPort = port;
	mRegistry = LocateRegistry.getRegistry(port);
	mLogger.debug("located registry");
	mLogger.debug("using registry: "+mRegistry
		      +"services: "+java.util.Arrays.asList(mRegistry.list()));
	}

    public void registerSearcher(String name, Searcher searcher) throws RemoteException {
	if (sSearchablesMap.containsKey(name)) {
	    unregisterSearcher(name);
	}
	RemoteSearchable server = new RemoteSearchable(searcher);
	mLogger.debug("instantiated server: "+server);
	try {
	    mRegistry.bind(name, server);
	} catch (AlreadyBoundException abe) {
	    mLogger.warn("rmi registry exception - name already bound: "+name);
	    mRegistry.rebind(name,server);
	}
	sSearchablesMap.put(name,server);
	mLogger.info("registered remote searchable: "+name);
    }

    public void unregisterSearcher(String name) throws RemoteException {
	mLogger.debug("unregister old remote searchable: "+name);
	RemoteSearchable old = sSearchablesMap.get(name);
	UnicastRemoteObject.unexportObject(old,true);
	sSearchablesMap.remove(name);
	try {
	    mRegistry.unbind(name);
	} catch (NotBoundException nbe) {
	    mLogger.warn("rmi registry unbind failed, name: "+name);
	}
    }

}