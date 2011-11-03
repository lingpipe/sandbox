package com.lingpipe.twpos;

import com.aliasi.classify.*;
import com.aliasi.corpus.*;
import com.aliasi.io.*;
import com.aliasi.hmm.*;
import com.aliasi.tag.*;

import java.io.*;
import java.util.*;

public class Eval {

    public static void main(String[] args) throws IOException {
        System.out.println("Reading Corpus");
        TwitterPosCorpus corpus = new TwitterPosCorpus(new File(args[0]));
        System.out.println("    #test cases=" + corpus.testCases().size());
        System.out.println("    #train cases=" + corpus.trainCases().size());

        System.out.println("Training Tagger");
        HmmCharLmEstimator hmm = new HmmCharLmEstimator();
        corpus.visitTrain(hmm);
        HmmDecoder tagger = new HmmDecoder(hmm);

        System.out.println("Evaluating");
        boolean storeTokens = true;
        TaggerEvaluator<String> evaluator
            = new TaggerEvaluator<String>(tagger,storeTokens);
        corpus.visitTest(evaluator);
        System.out.println(evaluator.tokenEval());


    }

    static List<Tagging<String>> parse(File f) throws IOException {
        List<Tagging<String>> taggings = new ArrayList<Tagging<String>>();
        FileLineReader reader = new FileLineReader(f,"UTF-8");
        List<String> tokens = new ArrayList<String>();
        List<String> tags = new ArrayList<String>();
        for (String line : reader) {
            String[] tokTag = line.split("\\s+");
            if (tokTag.length != 2) {
                taggings.add(new Tagging<String>(tokens,tags));
                // System.out.println("tokens=" + tokens);
                // System.out.println("tags=" + tags);
                tokens = new ArrayList<String>();
                tags = new ArrayList<String>();
            } else {
                tokens.add(tokTag[0]);
                tags.add(tokTag[1]);
            }
        }           
        return taggings;
    }

    static class TwitterPosCorpus extends ListCorpus<Tagging<String>> {
        public TwitterPosCorpus(File path) throws IOException {
            for (Tagging<String> t : parse(new File(path,"train")))
                addTrain(t);
            for (Tagging<String> t : parse(new File(path,"dev")))
                addTrain(t);
            for (Tagging<String> t : parse(new File(path,"test")))
                addTest(t);
        }
    }




}

