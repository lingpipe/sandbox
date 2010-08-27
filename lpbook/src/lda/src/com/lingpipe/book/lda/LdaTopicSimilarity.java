package com.lingpipe.book.lda;

import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.cluster.LatentDirichletAllocation.GibbsSample;

import com.aliasi.stats.Statistics;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class LdaTopicSimilarity {

    /*x LdaTopicSimilarity.1 */
    public static void main(String[] args) 
        throws IOException, InterruptedException {
    /*x*/

        File corpusFile = new File(args[0]);
        int minTokenCount = 5;

        CharSequence[] articleTexts = LdaWorm.readCorpus(corpusFile);
        SymbolTable symbolTable = new MapSymbolTable();
        TokenizerFactory tokenizerFactory 
            = LdaWorm.wormbaseTokenizerFactory();
        int[][] docTokens
            = LatentDirichletAllocation
            .tokenizeDocuments(articleTexts,tokenizerFactory,
                               symbolTable,minTokenCount);


    /*x LdaTopicSimilarity.2 */
        long seed1 = 42L;  
        long seed2 = 43L;
        LdaRunnable runnable1 = new LdaRunnable(docTokens,seed1);
        LdaRunnable runnable2 = new LdaRunnable(docTokens,seed2);
        Thread thread1 = new Thread(runnable1);
        Thread thread2 = new Thread(runnable2);
        thread1.start();  
        thread2.start();
        thread1.join();   
        thread2.join();
        LatentDirichletAllocation lda0 = runnable1.mLda;
        LatentDirichletAllocation lda1 = runnable2.mLda;

        double[] scores = similarity(lda0,lda1);
    /*x*/
        for (int i = 0; i < scores.length; ++i)
            System.out.printf("%4d %15.1f\n",i,scores[i]);
    }


    /*x LdaTopicSimilarity.3 */
    static double[] similarity(LatentDirichletAllocation lda0,
                               LatentDirichletAllocation lda1) {
        int numTopics = lda0.numTopics();
        List<TopicSim> pairs = new ArrayList<TopicSim>();
        for (int i = 0; i < numTopics; ++i) {
            double[] pi = lda0.wordProbabilities(i);
            for (int j = 0; j < numTopics; ++j) {
                double[] pj = lda1.wordProbabilities(j);
                double divergence
                    = Statistics.symmetrizedKlDivergence(pi,pj);
                TopicSim ts = new TopicSim(i,j,divergence);
                pairs.add(ts);
            }
        }
        Collections.sort(pairs,ScoredObject.comparator());
        boolean[] taken0 = new boolean[numTopics];
        boolean[] taken1 = new boolean[numTopics];
        double[] scores = new double[numTopics];
        int scorePos = 0;
        for (TopicSim ts : pairs) {
            if (!taken0[ts.mI] && !taken1[ts.mJ]) {
                taken0[ts.mI] = taken1[ts.mJ] = true;
                scores[scorePos++] = ts.score();
            }
        }
        return scores;
    }
    /*x*/

    /*x LdaTopicSimilarity.4 */
    static class LdaRunnable implements Runnable {

        LatentDirichletAllocation mLda;
        final int[][] mDocTokens;
        final Random mRandom;

        LdaRunnable(int[][] docTokens, long seed) {
            mDocTokens = docTokens;
            mRandom = new Random(seed);
        }

        public void run() {
            short numTopics = 50;
            double topicPrior = 0.1;
            double wordPrior = 0.001;
            int numSamples = 200;
            Iterator<GibbsSample> it
                = LatentDirichletAllocation
                .gibbsSample(mDocTokens,numTopics,topicPrior,
                             wordPrior,mRandom);
            for (int i = 1; i < numSamples; ++i)
                it.next();
            mLda = it.next().lda();
        }
    }
    /*x*/

    /*x LdaTopicSimilarity.5 */
    static class TopicSim implements Scored {

        final int mI, mJ;
        final double mScore;

        public TopicSim(int i, int j, double score) {
            mI = i; 
            mJ = j;
            mScore = score;
        }

        public double score() {
            return mScore;
        }
    }
    /*x*/

}
