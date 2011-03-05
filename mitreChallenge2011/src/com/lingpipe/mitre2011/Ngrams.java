package com.lingpipe.mitre2011;

import com.aliasi.spell.TfIdfDistance;
import com.aliasi.spell.JaccardDistance;

import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Date;

public class Ngrams {

    static final boolean DEBUG = true;

    static final int INDEX_NGRAM = 4;
    static final int MATCH_NGRAM_MIN = 2;
    static final int MATCH_NGRAM_MAX = 4;

    static final String COMMENT_PREFIX = "# ";

    static final int MAX_RESULTS_INDEX = 1000000;
    static final int MAX_RESULTS = 500;

    static final double MIN_INDEX_PROXIMITY = 0.3;

    public static void main(String[] args) throws IOException {
        File corpusDir = new File(args[0]);
        String outPrefix = args[1];

        if (DEBUG)
            System.out.println("outPrefix=" + outPrefix);

        Corpus corpus = new Corpus(corpusDir);

        File outFile = null;
        for (int i = 0; true; ++i) {
            String path = outPrefix + i;
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

        
        

        TokenizerFactory tf = createMatchTokenizerFactory();

        JaccardDistance distance = new JaccardDistance(tf);

        // if (DEBUG)
        // System.out.println("Training TF/IDF");
        // TfIdfDistance distance = new TfIdfDistance(tf);
        // for (String[] fields : corpus.mIndex)
        // distance.handle(fieldsToName(fields));
        // for (String[] fields : corpus.mQueries)
        // distance.handle(fieldsToName(fields));



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
            if (DEBUG)
                System.out.println(Arrays.asList(fields1));

            if ((count++ % 100) == 0) {
                System.out.println("\n==================================");
                System.out.println("COUNT=" + count);
                System.out.println("==================================\n");
            }
            
            String id1 = fields1[0];
            String name1 = fieldsToName(fields1);
            char[] name1Cs = name1.toCharArray();


            // CANDIDATE SET INDEX

            Set<String[]> candidateSet = new HashSet<String[]>();
            for (String ngram : tfIdx.tokenizer(name1Cs,0,name1Cs.length))
                if (nGramToFields.containsKey(ngram))
                    candidateSet.addAll(nGramToFields.get(ngram));

            // N-GRAM SCORE
            
            BoundedPriorityQueue<Match> queue
                = new BoundedPriorityQueue<Match>(ScoredObject.comparator(),
                                                  MAX_RESULTS_INDEX);
            for (String[] fields2 : candidateSet) {
                String id2 = fields2[0];
                String name2 = fieldsToName(fields2);
                double proximity = distance.proximity(name1,name2);
                if (proximity >= MIN_INDEX_PROXIMITY) {
                    queue.offer(new Match(fields1,fields2,proximity));
                }
            }


            // RESCORE

            BoundedPriorityQueue<ScoredObject<String>> resultQueue
                = new BoundedPriorityQueue<ScoredObject<String>>(
                                                  ScoredObject.comparator(),
                                                  MAX_RESULTS);

            BoundedPriorityQueue<ScoredObject<String>> debugQueue
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
                if (DEBUG)
                    debugQueue.offer(new ScoredObject<String>(fieldsToName(match.mFields2),
                                                              finalScore));
            }
                                     

            if (DEBUG) {
                for (ScoredObject<String> so : debugQueue) {
                    System.out.printf("|%4.2f|%s|%s|\n",
                                      so.score(),
                                      name1,
                                      so.getObject());
                }
            }

            // OUTPUT

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
    // this is a completely ad hoc heuristic
    // rescoring that does a bit better than the raw score
    static double rescore(String[] fields1, String[] fields2, double score) {

        // pull out components
        String forename1 = fields1[1];
        String surname1 = fields1[2];
        String forename2 = fields2[1];
        String surname2 = fields2[2];
        String fullName1 = fieldsToName(fields1);
        String fullName2 = fieldsToName(fields2);

        int k1 = forename1.indexOf(' ');
        if (k1 > 0)
            forename1 = forename1.substring(0,k1);
        int k2 = forename2.indexOf(' ');
        if (k2 > 0)
            forename2 = forename2.substring(0,k2);

        double modifier = 1.0;

        // prefer exact surname matches
        if (!surname1.equals(surname2))
            modifier *= .8;

        // prefer exact forename or weak forename matches
        if (!forename1.equals(forename2))
            modifier *= .95;
        if (!weakMatch(forename1,forename2))
            modifier *= 0.8;

        // penalize forename to surname matches
        if (forename1.equals(surname2) || forename2.equals(surname1))
            if (!surname1.equals(surname2))
                modifier *= 0.9;

        return score * modifier;
    }

    static boolean weakMatch(String s1, String s2) {
        if (s1.equals(s2)) return true;
        return (s1.length() == 1 || s2.length() == 1)
            && s1.charAt(0) == s2.charAt(0);
    }

    static Map<String[],String> FIELDS_TO_NAME_CACHE
        = new HashMap<String[],String>();

    static final String SPACES_REGEX = "\\s+";

    static String removeSpaces(String s) {
        return s.replaceAll(SPACES_REGEX,"");
    }

    static String fieldsToName(String[] fields) {
        String result = FIELDS_TO_NAME_CACHE.get(fields);
        if (result != null) 
            return result;
        // extra start/end space is to get boundaries modeled
        // could remove some of this norm; haven't used it in
        // submissions yet
        result 
            = (" " + fields[1] + " " + fields[2] + " ")
            .toLowerCase()
            .replaceAll("mm","m")
            .replaceAll("nn","n")
            .replaceAll("ss","s")
            .replaceAll("ll","l")
            .replaceAll("dd","d")
            .replaceAll("tt","t")
            .replaceAll("rr","r")
            .replaceAll("pp","p")
            .replaceAll("bb","b")
            // .replaceAll("hs","x")
            //.replaceAll("ch","j")
            .replaceAll("'"," ")
            // .replaceAll("z","s")
            // .replaceAll("[aeiouy]+","a")
            // .replaceAll("k","c")
            // .replaceAll("w","v")
            // .replaceAll("d","t")
            .replaceAll("-"," ")
            .replaceAll("\\s+"," ");
        FIELDS_TO_NAME_CACHE.put(fields,result);
        return result;
    }

    static TokenizerFactory createMatchTokenizerFactory() {
        TokenizerFactory tf1Base
            = new NGramTokenizerFactory(MATCH_NGRAM_MIN,
                                        MATCH_NGRAM_MAX);
        
        TokenizerFactory tf1
            = new ModifyTokenTokenizerFactory(tf1Base) {
                    public String modifyToken(String token) {
                        return "*" + token;
                    }
                };
        
        // whole names
        TokenizerFactory tf2
            = new RegExTokenizerFactory("\\S+");
        
        // initials
        TokenizerFactory tf3
            = new ModifyTokenTokenizerFactory(tf2) {
                    public String modifyToken(String token) {
                        return token.substring(0,1); // long enough by \\S+ pattern
                    }
                };
        
        return new CompoundTokenizerFactory(tf1,tf2,tf3);
    }

    static class CompoundTokenizerFactory implements TokenizerFactory {
        private final List<TokenizerFactory> mTokFacts;
        public CompoundTokenizerFactory(TokenizerFactory... tokFacts) {
            this(Arrays.asList(tokFacts));
        }
        public CompoundTokenizerFactory(List<TokenizerFactory> tokFacts) {
            mTokFacts = tokFacts;
        }
        public Tokenizer tokenizer(char[] cs, int start, int end) {
            List<Tokenizer> toks = new ArrayList<Tokenizer>();
            for (TokenizerFactory tf : mTokFacts)
                toks.add(tf.tokenizer(cs,start,end));
            return new CompoundTokenizer(toks);
        }
    }

    static class CompoundTokenizer extends Tokenizer {
        private final Iterator<Tokenizer> mTokIt;
        private Tokenizer mTokenizer;
        private int mCount = 0;
        public CompoundTokenizer(List<Tokenizer> toks) {
            mTokIt = toks.iterator();
        }
        public String nextToken() {
            while (true) {
                if (mTokenizer == null) {
                    if (!mTokIt.hasNext())
                        return null;
                    mTokenizer = mTokIt.next();
                }
                String token = mTokenizer.nextToken();
                if (token != null)
                    return token;
                mTokenizer = null;
            }
        }
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