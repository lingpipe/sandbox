package com.aliasi.lingmed.server;

import com.aliasi.lingmed.entrezgene.*;
import com.aliasi.lingmed.medline.*;
import com.aliasi.lingmed.dao.SearchResults;
import com.aliasi.medline.MedlineCitation;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import java.util.Iterator;

import org.apache.lucene.search.Searcher;

import org.apache.log4j.Logger;


/**
 * Simple timing tests for search client.
 * Runs a set of queries on remote Medline index.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class TestClient {
    private final Logger mLogger
	= Logger.getLogger(TestClient.class);

    private final static int SECOND = 1000;
    private final static int MINUTE = 60*SECOND;


    public static void main(String[] args) throws Exception {

	if (args.length < 2) {
	    System.out.println("args: <hostname> <pmidsfile>");
	    System.exit(-1);
	}

	String hostname = args[0];
	    
	String filename = args[1];
	File file = new File(filename);
	if (!file.exists()) {
	    System.out.println("no such file: "+filename);
	    System.exit(-1);
	}

	System.out.println("test remote search on host: "+hostname);
	TestClient client = new TestClient();
	client.doSearch(hostname,filename);
    }

    public void doSearch(String hostname,String fileName) throws Exception {
	MedlineCodec medlineCodec = new MedlineCodec();
	SearchClient medlineClient = new SearchClient("medline",hostname,1099);
	while (true) {
	    String id;
	    LineNumberReader in = new LineNumberReader(new BufferedReader(new FileReader(fileName)));
	    while ((id = in.readLine()) != null) {
		mLogger.info("\n"+in.getLineNumber()+"\tid: "+id);

		Searcher medlineRemoteSearcher = medlineClient.getSearcher();
		MedlineSearcher medlineSearcher = new MedlineSearcherImpl(medlineCodec,medlineRemoteSearcher);

		int numHits = medlineSearcher.numExactPhraseMatches("gene");
		mLogger.info("num hits for \"gene\": "+numHits);

		long startPubmedTime = System.currentTimeMillis();
		MedlineCitation mc = medlineSearcher.getById(id);
		if (mc == null) {
		    mLogger.info("id not found: "+id);
		} else {
		    mLogger.info(id+": "+mc.status());
		    if (mc.article() != null) 
			mLogger.info(mc.article().articleTitle());
		}
		updatePubmedTimes(System.currentTimeMillis()-startPubmedTime);
		mLogger.info("mean pubmed lookup time in ms: "+meanPubmed);
		mLogger.info("dev pubmed lookup time in ms: "+devPubmed);
	    }
	    Thread.sleep(10*SECOND);
	}
    }

    // timing utils

    double PERIOD = 1000;

    double meanPubmed = 0;
    double varPubmed = 0;
    double devPubmed = 0;

    public void updatePubmedTimes(double x) {
	meanPubmed = meanPubmed*(PERIOD-1)/PERIOD + x/PERIOD;
	varPubmed = varPubmed*(PERIOD-1)/PERIOD + (x-meanPubmed)*(x-meanPubmed)/PERIOD;
	devPubmed = Math.sqrt(varPubmed);
    }

    double meanEntrez = 0;
    double varEntrez = 0;
    double devEntrez = 0;

    public void updateEntrezTimes(double x) {
	meanEntrez = meanEntrez*(PERIOD-1)/PERIOD + x/PERIOD;
	varEntrez = varEntrez*(PERIOD-1)/PERIOD + (x-meanEntrez)*(x-meanEntrez)/PERIOD;
	devEntrez = Math.sqrt(varEntrez);
    }




}