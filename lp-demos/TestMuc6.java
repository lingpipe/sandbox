import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TrainTokenShapeChunker;

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

public class TestMuc6 {

    static final double TRAIN_FRACTION = 0.9;

    static final int NUM_CHUNKINGS_RESCORED = 256;

    static final int MAX_N_GRAM = 8;
    static final int NUM_CHARS = 128;
    static final double INTERPOLATION_RATIO = MAX_N_GRAM;

    public static void main(String[] args) throws Exception {
	File trainDir = new File(args[0]);

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
	trainTest(hmmChunkerEstimator,trainingFiles,
		  "CharLmHmmChunker");


	TokenCategorizer categorizer
	    = IndoEuropeanTokenCategorizer.CATEGORIZER;
	TrainTokenShapeChunker shapeChunkerEstimator
	    = new TrainTokenShapeChunker(categorizer,factory);
	trainTest(shapeChunkerEstimator,trainingFiles,
		  "TokenShapeChunker");


	CharLmRescoringChunker rescoringChunkerEstimator
	    = new CharLmRescoringChunker(factory,
					 NUM_CHUNKINGS_RESCORED,
					 MAX_N_GRAM,
					 NUM_CHARS,
					 INTERPOLATION_RATIO);
	trainTest(rescoringChunkerEstimator,trainingFiles,
		  "CharLmRescoringChunker");

    }

    static void trainTest(ObjectHandler<Chunking> trainingHandler,
			  File[] files,
			  String modelType) throws Exception {
	System.out.println("\nType=" + modelType);
	
	System.out.println("     Training.");
	Muc6ChunkParser parser = new Muc6ChunkParser();
	parser.setHandler(trainingHandler);
	int numTrainingFiles = (int) (TRAIN_FRACTION * files.length);
	System.out.println("     #training files=" + numTrainingFiles);
        for (int i = 0; i < numTrainingFiles; ++i)
	    parser.parse(files[i]);

        System.out.println("     Compiling.");
	Chunker chunker 
	    = (Chunker) AbstractExternalizable.compile((Compilable) trainingHandler);
	// ChunkerSpeedTest.configure(chunker);

        System.out.println("     Evaluating.");
        ChunkerEvaluator evaluator = new ChunkerEvaluator(chunker);
        evaluator.setVerbose(false);
        evaluator.setMaxNBest(128);
        evaluator.setMaxNBestReport(1);
        evaluator.setMaxConfidenceChunks(8);

	Muc6ChunkParser testParser = new Muc6ChunkParser();
	testParser.setHandler(evaluator);
	for (int i = numTrainingFiles; i < files.length; ++i)
	    testParser.parse(files[i]); // charset = default = ISO-8859-1

        System.out.println("     Results");
        System.out.println(evaluator.toString());

	
    }

}
