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

import com.aliasi.lingmed.dao.*;
import com.aliasi.lingmed.entrezgene.*;
import com.aliasi.lingmed.medline.*;
import com.aliasi.lingmed.server.*;

import com.aliasi.medline.*;

import java.io.*;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;

import org.apache.log4j.Logger;

public class TestDateSearch {

    public static void main(String[] args) throws Exception {
	File luceneDir = new File( args[0] );
	Searcher medlineLocalSearcher = new IndexSearcher(FSDirectory.getDirectory(luceneDir));
	MedlineSearcher medlineSearcher = new MedlineSearcherImpl(new MedlineCodec(),medlineLocalSearcher);

	System.out.println("test1a: citations between 1985-1985");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("1985","1985");
	    System.out.println("citations in 1985: "+hits.size());
	    for (MedlineCitation hit : hits){
		PubDate pubDate = hit.article().journal().journalIssue().pubDate();
		if (pubDate.isStructured()) {
		    System.out.println("citation: "+hit.pmid()
				       +"\tpubdate "+pubDate.toString());
		} else {
		    System.out.println("citation: "+hit.pmid()
				       +"\tUNSTRUCTURED "+pubDate.medlineDate());
		}		
	    }
	    System.out.println("citations in 1985: "+hits.size());
	} catch (DaoException de) {
	    System.out.println("test1a failed: "+de.getMessage());
	    de.printStackTrace();
	}

	System.out.println("test1b: citations between 2000-2005");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("2000","2005");
	    System.out.println("citations in 2000-2005: "+hits.size());
	} catch (DaoException de) {
	    System.out.println("test1b failed: "+de.getMessage());
	    de.printStackTrace();
	}

	System.out.println("test1c: citations between 2004-2005");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("2004","2005");
	    System.out.println("citations in 2004-2005: "+hits.size());
	} catch (DaoException de) {
	    System.out.println("test1c failed: "+de.getMessage());
	    de.printStackTrace();
	}

	System.out.println("test1d: citations between 1964-1965");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("1964","1965");
	    System.out.println("citations in 1964-1965: "+hits.size());
	} catch (DaoException de) {
	    System.out.println("test1d failed: "+de.getMessage());
	    de.printStackTrace();
	}

	System.out.println("test1e: citations between 0000-9999");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("0000","9999");
	    System.out.println("citations in 0000-9999: "+hits.size());
	} catch (DaoException de) {
	    System.out.println("test1e failed: "+de.getMessage());
	    de.printStackTrace();
	}

	System.out.println("test1f: citations between 0000-1000");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("0000","1000");
	    System.out.println("citations in 0000-1000: "+hits.size());
	} catch (DaoException de) {
	    System.out.println("test1f failed: "+de.getMessage());
	    de.printStackTrace();
	}

	System.out.println("test1g: citations between 3000-5000");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("3000","5000");
	    System.out.println("citations in 3000-5000: "+hits.size());
	} catch (DaoException de) {
	    System.out.println("test1g failed: "+de.getMessage());
	    de.printStackTrace();
	}

	System.out.println("test2: citations between 1999-1998");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("1999","1998");
	} catch (DaoException de) {
	    System.out.println("test2 passed (i.e. failed): "+de.getMessage());
	}

	System.out.println("test3: citations between foo-bar");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("foo","bar");
	} catch (DaoException de) {
	    System.out.println("test3 passed (i.e. failed): "+de.getMessage());
	}

	System.out.println("test4: citations between empty range");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("","");
	} catch (DaoException de) {
	    System.out.println("test4 passed (i.e. failed): "+de.getMessage());
	}

	System.out.println("test5: citations between empty range");
	try {
	    SearchResults<MedlineCitation> hits = medlineSearcher.getCitationsInYearRange("2000","");
	} catch (DaoException de) {
	    System.out.println("test4 passed (i.e. failed): "+de.getMessage());
	}



	System.exit(0);
    }

}
