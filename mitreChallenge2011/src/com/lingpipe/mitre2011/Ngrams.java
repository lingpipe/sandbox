package com.lingpipe.mitre2011;

import com.aliasi.spell.TfIdfDistance;

import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.ScoredObject;

import java.io.File;
import java.io.IOException;

public class Ngrams {

    public static void main(String[] args) throws IOException {
        File dir = new File(args[0]);
        Corpus corpus = new Corpus(dir);

        System.out.println("Training TF/IDF");
        TokenizerFactory tf = new NGramTokenizerFactory(2,4);
        TfIdfDistance distance = new TfIdfDistance(tf);
        for (String[] fields : corpus.mIndex)
            distance.handle(fieldsToName(fields));
        for (String[] fields : corpus.mQueries)
            distance.handle(fieldsToName(fields));
            
        for (String[] fields1 : corpus.mQueries) {
            String id1 = fields1[0];
            String name1 = fieldsToName(fields1);
            BoundedPriorityQueue<ScoredObject<String>> queue
                = new BoundedPriorityQueue<ScoredObject<String>>(ScoredObject
                                                                 .comparator(),
                                                                 10);
            for (String[] fields2 : corpus.mIndex) {
                String id2 = fields2[0];
                String name2 = fieldsToName(fields2);
                double proximity = distance.proximity(name1,name2);
                if (proximity >= 0.6) {

                    // the print here's for debug
                    System.out.printf("%5.2f  |%s|  |%s|\n",
                                      proximity,name1,name2);
                    queue.offer(new ScoredObject<String>(fields2[0],proximity));
                }
            }

            for (ScoredObject<String> so : queue) {
                String id2 = so.getObject();
                double score = so.score();
                // real print for system output
                System.out.println(id1 + "|" + id2 + "|" + score);
            }
                    
                    
                    
        }
    }

    static String fieldsToName(String[] fields) {
        return (" " + fields[1] + " " + fields[2] + " ").replaceAll("\\s+"," ");
    }

}