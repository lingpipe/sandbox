package com.aliasi.lingmed.lingblast;

import com.aliasi.lingmed.dao.DaoException;

import com.aliasi.lingmed.entrezgene.EntrezGene;
import com.aliasi.lingmed.entrezgene.EntrezGeneCodec;
import com.aliasi.lingmed.entrezgene.EntrezGeneSearcher;
import com.aliasi.lingmed.entrezgene.EntrezGeneSearcherImpl;

import com.aliasi.lingmed.medline.SearchableMedlineCodec;
import com.aliasi.lingmed.medline.MedlineSearcher;
import com.aliasi.lingmed.medline.MedlineSearcherImpl;

import com.aliasi.lingmed.server.SearchClient;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.LanguageModel;

import com.aliasi.medline.Abstract;
import com.aliasi.medline.Article;
import com.aliasi.medline.MedlineCitation;

import com.aliasi.util.Arrays;
import com.aliasi.util.AbstractExternalizable;
import java.io.IOException;

// import java.util.Arrays; // name conflict with aliasi.util.Arrays
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.search.Searcher;


public class XValidateGenomics {

    public static void main(String[] args) throws Exception {
        String searchHost = args[0];
        int port = Integer.parseInt(args[1]);
        String entrezService = "entrezgene";
        SearchClient egClient = new SearchClient(entrezService,searchHost,port);
        Searcher egSearcher = egClient.getSearcher();
        EntrezGeneSearcher entrezGeneSearcher 
            = new EntrezGeneSearcherImpl(new EntrezGeneCodec(),
                                         egSearcher);

        String medlineService = "medline";
        SearchClient mlClient = new SearchClient(medlineService,searchHost,port);
        Searcher mlSearcher = mlClient.getSearcher();
        MedlineSearcher medlineSearcher
            = new MedlineSearcherImpl(new SearchableMedlineCodec(),
                                      mlSearcher);

        testSearcher(medlineSearcher);
        

        // xvalPositive(entrezGeneSearcher,medlineSearcher);

        xvalAll(entrezGeneSearcher,medlineSearcher);

    }

    static void testSearcher(MedlineSearcher medlineSearcher) throws Exception {
        int count = 0;
        MedlineQueue queue = new MedlineQueue(100,medlineSearcher);
        new Thread(queue).start();
        while (!queue.isDone()) {
            MedlineCitation citation = queue.poll(1000,TimeUnit.MILLISECONDS);
            if (citation == null) {
                System.out.println("null");
                continue; // busy wait
            } 
            String text = getText(citation);
            if (text == null)
                System.out.println("null text for citation=" + citation);
            if (++count % 10000 == 0) System.out.println("count=" + count);
        }
    }


    static class MedlineQueue extends LinkedBlockingQueue<MedlineCitation> implements Runnable {
        boolean mDone = false;
        final MedlineSearcher mSearcher;
        public MedlineQueue(int size, MedlineSearcher searcher) {
            super(size);
            mSearcher = searcher;
        }
        public void run() {
            try {
                for (MedlineCitation citation : mSearcher) {
                    if (Thread.currentThread().isInterrupted())
                        break;
                    put(citation);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            synchronized(this) {
                mDone = true;
            }
        }
        public boolean isDone() {
            synchronized(this) {
                return mDone;
            }
        }
    }

                           

    static void xvalAll(EntrezGeneSearcher entrezGeneSearcher,
                        MedlineSearcher medlineSearcher) 
        throws IOException, DaoException, ClassNotFoundException {
        
        System.out.println("XValidate All");
        Set<String> genePmidSet = getEntrezMedlineLinks(entrezGeneSearcher);

        System.out.println("     training language model");
        NGramProcessLM lm = createLm();
        int lmCount = 0;
        for (String pmid : genePmidSet) {
            String text = getText(pmid,medlineSearcher);
            if (text == null) {
                System.out.println("null text, pmid=" + pmid);
                continue;
            }
            lm.train(text);
            if ((lmCount++ % 1000) == 0)
                System.out.println("     " + lmCount);
        }
        
        System.out.println("     compiling");
        LanguageModel compiledLm = (LanguageModel) AbstractExternalizable.compile(lm);

        List<Double> crossEntropyRateList = new ArrayList<Double>();
        int count = 0;

        MedlineQueue queue = new MedlineQueue(100,medlineSearcher);
        new Thread(queue).start();
        while (!queue.isDone()) {
            MedlineCitation citation = queue.poll();
            if (citation == null) continue;
            if (genePmidSet.contains(citation.pmid())) continue;
            String text = getText(citation);
            if (text == null) continue;
            if ((count++ % 1000) == 0)
                System.out.println("          " + count);
            double crossEntropyRate = -compiledLm.log2Estimate(text)/text.length();
            crossEntropyRateList.add(crossEntropyRate);
        }
        
        double[] xs = new double[crossEntropyRateList.size()];
        for (int i = 0; i < xs.length; ++i)
            xs[i] = crossEntropyRateList.get(i);
        java.util.Arrays.sort(xs);
        for (double x : xs)
            System.out.println(x);

    }

    static NGramProcessLM createLm() {
        return new NGramProcessLM(NGRAM,NUM_CHARS,INTERPOLATION_RATIO);
    }

    // shouldn't Entrez gene iterator throw an exception?
    static Set<String> getEntrezMedlineLinks(EntrezGeneSearcher entrezGeneSearcher) {
        System.out.println("\nFinding Entrez References to PMIDs");
        Set<String> pmidSet = new HashSet<String>();
        int geneCounter = 0;
        for (EntrezGene entrezGene : entrezGeneSearcher) {
            if (++geneCounter % 100 == 0) System.out.println(geneCounter);
            String[] pubMedIds = entrezGene.getUniquePubMedRefs();
            for (String pmid : pubMedIds) 
                pmidSet.add(pmid);
        }
        System.out.println("#genes found=" + geneCounter);
        System.out.println("#pmids found=" + pmidSet.size());
        return pmidSet;
    }

    static void xvalPositive(EntrezGeneSearcher entrezGeneSearcher,
                             MedlineSearcher medlineSearcher) 
        throws IOException, DaoException, ClassNotFoundException {
                             
        Set<String> pmidSet = getEntrezMedlineLinks(entrezGeneSearcher);

        System.out.println("\nDownloading MEDLINE citations");
        List<String> textList = new ArrayList<String>();
        for (String pmid : pmidSet) {
            String text = getText(pmid,medlineSearcher);
            if (text == null) continue;
            textList.add(text);
            if (textList.size() % 100 == 0) System.out.println(textList.size());
        }
        System.out.println("#texts=" + textList.size());
        int charCount = 0;
        for (String text : textList)
            charCount += text.length();
        System.out.println("#chars=" + charCount);

        String[] texts = textList.<String>toArray(new String[0]);
        crossValidate(texts);
    }

    static void crossValidate(String[] texts) throws ClassNotFoundException, IOException {
        System.out.println("\nCross-validating");
        Arrays.permute(texts, new Random(42));
        List<Double> scoreList = new ArrayList<Double>();
        for (int fold = 0; fold < NUM_FOLDS; ++fold) {
            System.out.println("fold=" + fold);
            NGramProcessLM lm = createLm();
            System.out.println("     training");
            for (int i = 0; i < texts.length; ++i)
                if ((i % 10) != fold)
                    lm.train(texts[i]);
            System.out.println("     testing");
            LanguageModel compiledLm = (LanguageModel) AbstractExternalizable.compile(lm);
            for (int i = 0; i < texts.length; ++i) {
                if ((i % 10) == fold) {
                    double crossEntropyRate = compiledLm.log2Estimate(texts[i])/texts[i].length();
                    scoreList.add(crossEntropyRate);
                }
            }
        }
        double[] scores = new double[scoreList.size()];
        for (int i = 0; i < scores.length; ++i)
            scores[i] = scoreList.get(i);
        java.util.Arrays.sort(scores);
        for (double xEntropy : scores) 
            System.out.println(xEntropy);
    }

    static String getText(String pmid, MedlineSearcher medlineSearcher) throws DaoException {
        MedlineCitation citation = medlineSearcher.getById(pmid);
        return getText(citation);
    }

    static String getText(MedlineCitation citation) {
        if (citation == null)
            return null;
        Article article = citation.article();
        if (article == null) {
            System.out.println("null article");
            return null;
        }
        String title = article.articleTitleText();
        if (title == null) {
            System.out.println("null title");
            title="";
        }
        Abstract abs = article.abstrct();
        return abs != null 
            ? (title + "\n" + abs.textWithoutTruncationMarker())
            : title;
    }

    static final int NUM_FOLDS = 10;
    static final int NGRAM = 8;
    static final int NUM_CHARS = 128;
    static final double INTERPOLATION_RATIO = 8.0;

}