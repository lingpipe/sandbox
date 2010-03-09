import com.aliasi.chunk.TrainTokenShapeChunker;

import com.aliasi.corpus.parsers.GeniaEntityChunkParser;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenCategorizer;
import com.aliasi.tokenizer.TokenCategorizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;

import java.io.File;
import java.io.IOException;

public class TrainGenia {

    public static void main(String[] args) throws Exception {
        File modelFile = new File(args[0]);
        File trainFile = new File(args[1]);

        System.out.println("Setting up Chunker Estimator");
        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenCategorizer categorizer
            = IndoEuropeanTokenCategorizer.CATEGORIZER;

        TrainTokenShapeChunker chunkerEstimator
            = new TrainTokenShapeChunker(categorizer,factory);

        System.out.println("Setting up Data Parser");
        GeniaEntityChunkParser parser
            = new GeniaEntityChunkParser();
        parser.setHandler(chunkerEstimator);

        System.out.println("Training with Data from File=" + trainFile);
        parser.parse(trainFile);

        System.out.println("Compiling and Writing Model to File=" + modelFile);
        AbstractExternalizable.compileTo(chunkerEstimator,modelFile);

    }

}
