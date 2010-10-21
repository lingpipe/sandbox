package edu.uchicago.rzhetsky.ne;

import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.AbstractCharLmRescoringChunker;

import com.aliasi.chunk.TokenShapeChunker;
import com.aliasi.chunk.TrainTokenShapeChunker;

import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.tokenizer.TokenCategorizer;
import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;

import java.util.Random;

public class GeneTagChunkerCompiler {

    static final TokenizerFactory TOKENIZER_FACTORY
        = IndoEuropeanTokenizerFactory.INSTANCE;
    // = new RegExTokenizerFactory("[\\p{L}\\p{N}]+");

    static final int NUM_CHUNKINGS_RESCORED 
        = 128;

    static final int N_GRAM
        = 8;

    static final int NUM_CHARS
        = 256;

    static final double INTERPOLATION_RATIO
        = N_GRAM;

    static final boolean SMOOTH_TAGS
        = true;

    static final TokenCategorizer TOKEN_CATEGORIZER
        = IndoEuropeanTokenCategorizer.CATEGORIZER;

    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

        System.out.println("Gene Tag Corpus Evaluator/Compiler");
        System.out.println("     TokenizerFactory=" + TOKENIZER_FACTORY);
        System.out.println("     Num Chunkings Rescored=" + NUM_CHUNKINGS_RESCORED);
        System.out.println("     Ngram=" + N_GRAM);
        System.out.println("     Num chars=" + NUM_CHARS);
        System.out.println("     Interpolation Ratio=" + INTERPOLATION_RATIO);
        System.out.println("     Smooth tags=" + SMOOTH_TAGS);
        System.out.println();

        GeneTagChunkCorpus corpus
            = new GeneTagChunkCorpus(new File(args[0]),
                                     new File(args[1]),
                                     10);
        corpus.permuteCorpus(new Random(125L));
        System.out.println("Corpus size=" + corpus.size() + " chunkings");

        
        // prints out parsed corpus
        corpus.setFold(0);
        corpus.visitTrain(new ObjectHandler<Chunking>() {
                public void handle(Chunking chunking) {
                    for (Chunk chunk : chunking.chunkSet()) {
                        if (chunk.start() < 0
                            || chunk.end() < 0
                            || chunk.end() > chunking.charSequence().length()
                            || chunk.start() >= chunk.end()) {
                            System.out.println("BAD CHUNKING=\n" + chunking);
                            System.exit(0);
                        }
                    }
                }
            });

        for (int fold = 0; fold < corpus.numFolds(); ++fold) {
            System.out.println("\nFold=" + fold);
            corpus.setFold(fold);
            
            CharLmRescoringChunker chunker
                = new CharLmRescoringChunker(TOKENIZER_FACTORY,
                                             NUM_CHUNKINGS_RESCORED,
                                             N_GRAM,
                                             NUM_CHARS,
                                             INTERPOLATION_RATIO,
                                             SMOOTH_TAGS);
            corpus.visitTrain(chunker);

            @SuppressWarnings("unchecked")
            AbstractCharLmRescoringChunker compiledChunker 
                = (AbstractCharLmRescoringChunker) AbstractExternalizable.compile(chunker);

            ChunkerEvaluator evaluator
                = new ChunkerEvaluator(compiledChunker.baseChunker());
            corpus.visitTest(evaluator);
            PrecisionRecallEvaluation prEval = evaluator.evaluation().precisionRecallEvaluation();
            System.out.printf("     BASE:    Prec=%5.3f Recall=%5.3f F=%5.3f\n",
                              prEval.precision(), prEval.recall(), prEval.fMeasure());

            ChunkerEvaluator evaluator2
                = new ChunkerEvaluator(compiledChunker);
            corpus.visitTest(evaluator2);
            PrecisionRecallEvaluation prEval2 = evaluator2.evaluation().precisionRecallEvaluation();
            System.out.printf("     RESCORE: Prec=%5.3f Recall=%5.3f F=%5.3f\n",
                              prEval2.precision(), prEval2.recall(), prEval2.fMeasure());


            TrainTokenShapeChunker tscTrainer 
                = new TrainTokenShapeChunker(TOKEN_CATEGORIZER,TOKENIZER_FACTORY);
            corpus.visitTrain(tscTrainer);
            @SuppressWarnings("unchecked")
            TokenShapeChunker tsChunker 
                = (TokenShapeChunker)
                AbstractExternalizable.compile(tscTrainer);
            
            ChunkerEvaluator evaluator3
                = new ChunkerEvaluator(tsChunker);
            corpus.visitTest(evaluator3);
            PrecisionRecallEvaluation prEval3 = evaluator3.evaluation().precisionRecallEvaluation();
            System.out.printf("     TSC:     Prec=%5.3f Recall=%5.3f F=%5.3f\n",
                              prEval3.precision(), prEval3.recall(), prEval3.fMeasure());

            


        }
    }
    
}