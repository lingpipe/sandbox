package com.aliasi.annotate.corpora;

import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;

public class TrainChunker {

    public static void main(String[] args)
        throws IOException, SAXException {

        File inputDir = new File(args[0]);
        File modelFile = new File(args[1]);
        Set<String> containingTypes = new HashSet<String>();
        for (int i = 2; i < args.length; ++i)
            containingTypes.add(args[i]);

        System.out.println("Input data directory=" + inputDir);
        System.out.println("Containing chunk types=" + containingTypes);

        System.out.println("Training.");
        CharLmRescoringChunker chunker
            = trainChunker(inputDir,containingTypes);

        System.out.println("Compiling to file=" + modelFile);
        AbstractExternalizable.compileTo(chunker,modelFile);
    }

    public static CharLmRescoringChunker trainChunker(File inputDir,
                                                      Set<String> containingTypes)
        throws IOException, SAXException {

        // shared params
        int numChars = 128;

        // hmm params
        int hmmNGramSize = 8;
        double hmmInterpolation = 8.0;
        boolean hmmSmootheTags = false;

        // chunker params
        int numTaggingsToRescore = 64;
        int rescoringNGramSize = 12;
        double rescoringInterpolation = 12.0;

        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;

        HmmCharLmEstimator lmEstimator
            = new HmmCharLmEstimator(hmmNGramSize,
                                     numChars,
                                     hmmInterpolation,
                                     hmmSmootheTags);


        CharLmRescoringChunker chunkerEstimator
            = new CharLmRescoringChunker(tokenizerFactory,
                                         numTaggingsToRescore,
                                         rescoringNGramSize,
                                         numChars,
                                         rescoringInterpolation);

        handle(inputDir,containingTypes,chunkerEstimator);

        return chunkerEstimator;
    }

    public static void handle(File inputDir,
                              Set<String> containingTypes,
                              ObjectHandler<Chunking> handler)
        throws IOException, SAXException {

        Parser<ObjectHandler<Chunking>> parser = new AnnotatorCorpusParser(containingTypes);
        parser.setHandler(handler);

        File[] files
            = inputDir.listFiles(new FileExtensionFilter(".xml",false));

        for (File file : files)
            parser.parse(file);
    }

}