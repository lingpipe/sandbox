package com.lingpipe.mitre2011;

import com.aliasi.spell.TfIdfDistance;

import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Date;

public class Ngrams {

    static final boolean DEBUG = true;

    static final int INDEX_NGRAM = 4;
    static final int MATCH_NGRAM_MIN = 2;
    static final int MATCH_NGRAM_MAX = 3;

    static final String COMMENT_PREFIX = "# ";

    static final int MAX_RESULTS_INDEX = 200;
    static final int MAX_RESULTS = 50;

    static final double MIN_INDEX_PROXIMITY = 0.5;

    public static void main(String[] args) throws IOException {
        File corpusDir = new File(args[0]);
        String outPrefix = args[1];

        if (DEBUG)
            System.out.println("outPrefix=" + outPrefix);

        Corpus corpus = new Corpus(corpusDir);

        File outFile = null;
        for (int i = 0; true; ++i) {
            String path = outPrefix + i;
            System.out.println("path=" + path);
            outFile = new File(path);
            if (!outFile.exists())
                break;
        }
        outFile.getParentFile().mkdirs();
        if (DEBUG) {
            System.out.println("outFile=" + outFile);
            System.out.println("outFile.getParentFile()=" + outFile.getParentFile());
        }
        
        PrintWriter printer
            = new PrintWriter(
                  new OutputStreamWriter(
                      new BufferedOutputStream(
                          new FileOutputStream(outFile)),
                      "ASCII"));
        printer.write(COMMENT_PREFIX + outPrefix + "\n");
        printer.write(COMMENT_PREFIX + "INDEX_NGRAM=" + INDEX_NGRAM + "\n");
        printer.write(COMMENT_PREFIX + "MATCH_NGRAM_MIN=" + MATCH_NGRAM_MIN + "\n");
        printer.write(COMMENT_PREFIX + "MATCH_NGRAM_MAX=" + MATCH_NGRAM_MAX + "\n");
        printer.write(COMMENT_PREFIX + "MAX_RESULTS_INDEX=" + MAX_RESULTS_INDEX + "\n");
        printer.write(COMMENT_PREFIX + "MAX_RESULTS=" + MAX_RESULTS + "\n");
        printer.write(COMMENT_PREFIX + "MIN_INDEX_PROXIMITY=" + MIN_INDEX_PROXIMITY + "\n");
        printer.write(COMMENT_PREFIX + new Date() + "\n");

        if (DEBUG)
            System.out.println("Training TF/IDF");
        TokenizerFactory tf 
            = new NGramTokenizerFactory(MATCH_NGRAM_MIN,
                                        MATCH_NGRAM_MAX);
        TfIdfDistance distance = new TfIdfDistance(tf);
        for (String[] fields : corpus.mIndex)
            distance.handle(fieldsToName(fields));
        for (String[] fields : corpus.mQueries)
            distance.handle(fieldsToName(fields));

        if (DEBUG)
            System.out.println("Building Reverse Index");
        TokenizerFactory tfIdx = new NGramTokenizerFactory(INDEX_NGRAM,
                                                           INDEX_NGRAM);
        Map<String,Set<String[]>> nGramToFields 
            = new HashMap<String,Set<String[]>>();
        for (String[] fields: corpus.mIndex) {
            String name = fieldsToName(fields);
            char[] cs = name.toCharArray();
            for (String ngram : tfIdx.tokenizer(cs,0,cs.length)) {
                if (!nGramToFields.containsKey(ngram)) 
                    nGramToFields.put(ngram,new HashSet<String[]>());
                nGramToFields.get(ngram).add(fields);
            }
        }

        if (DEBUG)
            System.out.println("Matching");
        int count = 0;
        for (String[] fields1 : corpus.mQueries) {
            if ((count++ % 10) == 0) {
                if (DEBUG) {
                    System.out.println("\n==================================");
                    System.out.println("\nCOUNT=" + count);
                    System.out.println("==================================\n");
                }
            }
            
            String id1 = fields1[0];
            String name1 = fieldsToName(fields1);
            char[] name1Cs = name1.toCharArray();

            BoundedPriorityQueue<Match> queue
                = new BoundedPriorityQueue<Match>(ScoredObject.comparator(),
                                                  MAX_RESULTS_INDEX);
            Set<String[]> candidateSet = new HashSet<String[]>();
            for (String ngram : tfIdx.tokenizer(name1Cs,0,name1Cs.length))
                if (nGramToFields.containsKey(ngram))
                    candidateSet.addAll(nGramToFields.get(ngram));
            
            for (String[] fields2 : candidateSet) {
                String id2 = fields2[0];
                String name2 = fieldsToName(fields2);
                double proximity = distance.proximity(name1,name2);
                if (proximity >= MIN_INDEX_PROXIMITY) {
                    queue.offer(new Match(fields1,fields2,proximity));
                }
            }

            BoundedPriorityQueue<ScoredObject<String>> resultQueue
                = new BoundedPriorityQueue<ScoredObject<String>>(
                                                  ScoredObject.comparator(),
                                                  MAX_RESULTS);
                
            for (Match match : queue) {
                double finalScore = rescore(match.mFields1,match.mFields2,
                                            match.mScore);
                ScoredObject<String> result
                    = new ScoredObject<String>(match.mFields2[0],
                                               finalScore);
                resultQueue.offer(result);

                if (DEBUG) {
                    String name2 = fieldsToName(match.mFields2);
                    double origScore = match.mScore;
                    System.out.printf("%5.3f -> %5.3f|%s|%s|\n",
                                      origScore, finalScore,
                                      name1,name2);
                }
            }

            for (ScoredObject<String> so : resultQueue) {
                String id2 = so.getObject();
                double score = so.score();
                // real print for system output
                printer.printf("%s|%s|%4.3f\n",id1,id2,score);
            }
        }

        printer.close();
        if (DEBUG) 
            System.out.println("FINISHED SUCCESSFULLY");
    }

    // use this for rescoring a hypothesis match
    static double rescore(String[] fields1, String[] fields2, double score) {
        return score;
    }

    static Map<String[],String> FIELDS_TO_NAME_CACHE
        = new HashMap<String[],String>();

    static String fieldsToName(String[] fields) {
        String result = FIELDS_TO_NAME_CACHE.get(fields);
        if (result != null) 
            return result;
        result = (" " + fields[1] + " " + fields[2] + " ").replaceAll("\\s+"," ");
        FIELDS_TO_NAME_CACHE.put(fields,result);
        return result;
    }


    static class Match implements Scored {
        final String[] mFields1;
        final String[] mFields2;
        final double mScore;
        public Match(String[] fields1, 
                     String[] fields2,
                     double score) {
            mFields1 = fields1;
            mFields2 = fields2;
            mScore = score;
        }
        public double score() {
            return mScore;
        }
        public void print(PrintWriter writer) {
            writer.printf("|%5.3f|%s|%s|\n",
                          mScore,
                          Strings.concatenate(mFields1,"+"),
                          Strings.concatenate(mFields2,"+"));
        }
    }



}