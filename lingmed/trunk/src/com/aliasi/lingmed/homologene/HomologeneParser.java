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
import com.aliasi.corpus.XMLParser;

import com.aliasi.util.Strings;

import com.aliasi.xml.DelegatingHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Parser for the XML version of the 
 * <A href="http://www.ncbi.nlm.nih.gov/sites/entrez?db=homologene">NCBI Homologene database</A>.
 * 
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class HomologeneParser 
    extends XMLParser<ObjectHandler<HomologeneGroup>> {
    private boolean mSaveXml;

    public HomologeneParser() {
	mSaveXml = false;
    }

    public HomologeneParser(boolean saveXml) {
	mSaveXml = saveXml;
    }

    public DefaultHandler getXMLHandler() {
	if (mSaveXml) {
	    try {
		return new HomologeneSetFilterHandler(getHandler());
	    } catch (IOException ioe) {
	        throw new IllegalStateException(ioe);
	    }
	}
	return new HomologeneSetHandler(getHandler());
    }

    static class HomologeneSetFilterHandler extends DelegatingHandler {
	ObjectHandler<HomologeneGroup> mHandler;
	HomologeneHandler mHomologeneHandler;
	ByteArrayOutputStream mBytesOut = new ByteArrayOutputStream();

	public HomologeneSetFilterHandler(ObjectHandler<HomologeneGroup> handler) throws IOException {
	    mHandler=handler;
	    mHomologeneHandler = new HomologeneHandler();
	    setDelegate(HomologeneTags.HG_ENTRY_ELT, mHomologeneHandler);
	}
	
	public void finishDelegate(String qName, DefaultHandler handler) {
	    if (qName.equals(HomologeneTags.HG_ENTRY_ELT)) {
		HomologeneGroup hg = mHomologeneHandler.getGeneGroup();
		try {
		    byte[] rawXmlBytes = mBytesOut.toByteArray();
		    String rawXml = new String(rawXmlBytes,0,rawXmlBytes.length,Strings.UTF8);
		    hg.setXmlString(rawXml);
		} catch (IOException ioe) {
		    throw new IllegalStateException(ioe);
		}
		mHandler.handle(hg);
		mBytesOut.reset();
	    }
	}
    }    

    static class HomologeneSetHandler extends DelegatingHandler {
	ObjectHandler<HomologeneGroup> mHandler;
	private final HomologeneHandler mHomologeneHandler;

	HomologeneSetHandler(ObjectHandler<HomologeneGroup> handler) {
	    mHandler=handler;
	    mHomologeneHandler = new HomologeneHandler();
	    setDelegate(HomologeneTags.HG_ENTRY_ELT, mHomologeneHandler);
	}

	public void finishDelegate(String qName, DefaultHandler handler) {
	    if (qName.equals(HomologeneTags.HG_ENTRY_ELT)) {
		HomologeneGroup hg = mHomologeneHandler.getGeneGroup();
		mHandler.handle(hg);
	    }
	}

    }    
}
