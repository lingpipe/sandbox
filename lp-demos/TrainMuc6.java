import com.aliasi.chunk.TrainTokenShapeChunker;
import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.corpus.parsers.Muc6ChunkParser;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.tokenizer.TokenCategorizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Streams;

import java.io.File;
import java.io.IOException;

public class TrainMuc6 {

    static final int NUM_CHUNKINGS_RESCORED = 256;

    static final int MAX_N_GRAM = 8;
    static final int NUM_CHARS = 128;
    static final double INTERPOLATION_RATIO = MAX_N_GRAM;

    public static void main(String[] args) throws Exception {
        String baseFileName = args[0];

        File trainDir = new File(args[1]);

        System.out.println("Training Data Directory="
                           + trainDir.getCanonicalPath());
        File[] trainingFiles = trainDir.listFiles();
        System.out.println("    #files=" + trainingFiles.length);


        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        HmmCharLmEstimator hmmEstimator
            = new HmmCharLmEstimator(MAX_N_GRAM,
                                     NUM_CHARS,
                                     INTERPOLATION_RATIO);
        CharLmHmmChunker hmmChunkerEstimator
            = new CharLmHmmChunker(factory,hmmEstimator);
        trainCompile(hmmChunkerEstimator,trainingFiles,
                     "CharLmHmmChunker",baseFileName);


        TokenCategorizer categorizer
            = IndoEuropeanTokenCategorizer.CATEGORIZER;
        TrainTokenShapeChunker shapeChunkerEstimator
            = new TrainTokenShapeChunker(categorizer,factory);
        trainCompile(shapeChunkerEstimator,trainingFiles,
                     "TokenShapeChunker",baseFileName);


        CharLmRescoringChunker rescoringChunkerEstimator
            = new CharLmRescoringChunker(factory,
                                         NUM_CHUNKINGS_RESCORED,
                                         MAX_N_GRAM,
                                         NUM_CHARS,
                                         INTERPOLATION_RATIO);
        trainCompile(rescoringChunkerEstimator,trainingFiles,
                     "CharLmRescoringChunker", baseFileName);

    }

    static void trainCompile(ObjectHandler<Chunking> trainingHandler,
                             File[] trainingFiles,
                             String modelType,
                             String baseFileName) throws Exception {
        System.out.println("\nTraining modelType=" + modelType);
        Muc6ChunkParser parser = new Muc6ChunkParser();
        parser.setHandler(trainingHandler);
        for (int i = 0; i < trainingFiles.length; ++i)
            parser.parse(trainingFiles[i]);
        File modelFile = new File(baseFileName + "." + modelType);
        System.out.println("     Compiling to file=" + modelFile);
        AbstractExternalizable.compileTo((Compilable) trainingHandler,
                                         modelFile);
    }

}
