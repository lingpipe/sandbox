package com.aliasi.lingmed.server;

import com.aliasi.lingmed.entrezgene.*;
import com.aliasi.lingmed.medline.*;
import com.aliasi.lingmed.dao.SearchResults;
import com.aliasi.lingmed.parser.MedlineCitation;


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

    private final static String TEST_MEDLINE = "medline";
    private final static String TEST_ENTREZGENE = "entrezgene";
    private final static String TEST_BOTH = "both";

    private static boolean doMedline = false;
    private static boolean doEntrezGene = false;

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.out.println("args: <type> <hostname> <pmidsfile>");
            System.exit(-1);
        }

        String type = args[0];
        if (TEST_MEDLINE.equalsIgnoreCase(type)) {
            doMedline = true;
        } else if (TEST_ENTREZGENE.equalsIgnoreCase(type)) {
            doEntrezGene = true;
        } else if (TEST_BOTH.equalsIgnoreCase(type)) {
            doMedline = true;
            doEntrezGene = true;
        } else {
            System.out.println("args: <type> <hostname> <pmidsfile>");
            System.out.println("type is one of {medline, entrezgene, both}");
            System.exit(-1);
        }

        String hostname = args[1];
            
        String filename = args[2];
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

        EntrezGeneCodec entrezGeneCodec = new EntrezGeneCodec();
        SearchClient entrezgeneClient = new SearchClient("entrezgene",hostname,1099);

        MedlineSearcher medlineSearcher = null;
        EntrezGeneSearcher entrezGeneSearcher = null;

        Timing medlineTimes = new Timing();
        Timing entrezgeneTimes = new Timing();

        int ct = 0;
        while (true) {
            if (doMedline) {
                Searcher medlineRemoteSearcher = medlineClient.getSearcher();
                medlineSearcher = new MedlineSearcherImpl(medlineCodec,medlineRemoteSearcher);

                int numHits = medlineSearcher.numExactPhraseMatches("gene");
                mLogger.info("num articles which contain phrase: \"gene\": "+numHits);
            }
            if (doEntrezGene) {
                Searcher entrezGeneRemoteSearcher = entrezgeneClient.getSearcher();
                entrezGeneSearcher = new EntrezGeneSearcherImpl(entrezGeneCodec,entrezGeneRemoteSearcher);
            }
            String id;
            LineNumberReader in = new LineNumberReader(new BufferedReader(new FileReader(fileName)));
            while ((id = in.readLine()) != null) {
                ++ct;
                if (mLogger.isDebugEnabled()) {
                    mLogger.debug("test: " + ct + ", line: " + in.getLineNumber() + "\tid: "+id);
                }
                if (doMedline) {
                    long startTime = System.currentTimeMillis();
                    MedlineCitation mc = medlineSearcher.getById(id);
                    medlineTimes.update(System.currentTimeMillis()-startTime);
                    if (mc == null) {
                        if (mLogger.isDebugEnabled()) {
                            mLogger.debug("id not found: " + id);
                        }
                    } else {
                        if (mLogger.isDebugEnabled()) {
                            mLogger.debug(id + ": " + mc.status());
                            if (mc.article() != null) 
                                mLogger.debug(mc.article().articleTitle());
                        }
                    }
                    if (mLogger.isDebugEnabled()) {
                        mLogger.debug("mean pubmed lookup time in millis: " + medlineTimes.getMean());
                        mLogger.debug("stddev pubmed lookup time in millis: " + medlineTimes.getStdDev());
                    } else if ((ct%Timing.REPORT_FREQ) == 0) {
                        mLogger.info("\nlookups: " + ct);
                        mLogger.info("mean pubmed lookup time in millis: " + medlineTimes.getMean());
                        mLogger.info("stddev pubmed lookup time in millis: " + medlineTimes.getStdDev());
                    }
                }
                if (doEntrezGene) {
                    long startTime = System.currentTimeMillis();
                    SearchResults<EntrezGene> hits = entrezGeneSearcher.getGenesForPubmedId(id);
                    entrezgeneTimes.update(System.currentTimeMillis()-startTime);
                    if (hits.size() == 0) {
                        if (mLogger.isDebugEnabled()) {
                            mLogger.debug("no genes reference article: " + id);
                        }
                    } else {
                        if (mLogger.isDebugEnabled()) {
                            mLogger.debug("article: " + id + " referenced by " + hits.size() + " genes");
                        }
                    }
                    if (mLogger.isDebugEnabled()) {
                        mLogger.debug("mean entrezgene lookup time in millis: " + entrezgeneTimes.getMean());
                        mLogger.debug("stddev entrezgene lookup time in millis: " + entrezgeneTimes.getStdDev());
                    } else if ((ct%Timing.REPORT_FREQ) == 0) {
                        mLogger.info("\nlookups: " + ct);
                        mLogger.info("mean entrezgene lookup time in millis: " + entrezgeneTimes.getMean());
                        mLogger.info("stddev entrezgene lookup time in millis: " + entrezgeneTimes.getStdDev());
                    }
                }

            }
            mLogger.info("\nlookups: " + ct);
            if (doMedline) {
                mLogger.info("mean pubmed lookup time in millis: " + medlineTimes.getMean());
                mLogger.info("dev pubmed lookup time in millis: " + medlineTimes.getStdDev());
            }
            if (doEntrezGene) {
                mLogger.info("mean entrezgene lookup time in millis: " + entrezgeneTimes.getMean());
                mLogger.info("stddev entrezgene lookup time in millis: " + entrezgeneTimes.getStdDev());
            }
            Thread.sleep(10*SECOND);
        }
    }


    // timing util
    static class Timing {

        static final double PERIOD = 1000;
        static final int REPORT_FREQ = 100;

        private double mean = 0;
        private double var = 0;
        private double stddev = 0;
        

        public void update(double x) {
            mean = mean*(PERIOD-1)/PERIOD + x/PERIOD;
            var = var*(PERIOD-1)/PERIOD + (x-mean)*(x-mean)/PERIOD;
            stddev = Math.sqrt(var);
        }

        public double getMean() { return mean; }
        public double getStdDev() { return stddev; }

    }


}