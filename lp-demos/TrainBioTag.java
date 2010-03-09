import com.aliasi.chunk.*;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Streams;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;

public class TrainBioTag {

    static final int MAX_N_GRAM = 8;
    static final int NUM_CHARS = 256;
    static final double LM_INTERPOLATION = MAX_N_GRAM; // default behavior

    // java TrainBioTag <trainingInputFile> <modelOutputFile>
    public static void main(String[] args) throws IOException {
        File corpusFile = new File(args[0]);
        File modelFile = new File(args[1]);

        System.out.println("Setting up Chunker Estimator");
        TokenizerFactory factory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        HmmCharLmEstimator hmmEstimator
            = new HmmCharLmEstimator(MAX_N_GRAM,NUM_CHARS,LM_INTERPOLATION);
        CharLmHmmChunker chunkerEstimator
            = new CharLmHmmChunker(factory,hmmEstimator);

        System.out.println("Setting up Data Parser");
        PennBioTagParser parser = new PennBioTagParser();
        parser.setHandler(chunkerEstimator);

        System.out.println("Training with Data from File=" + corpusFile);
        parser.parse(corpusFile);

        System.out.println("Compiling and Writing Model to File=" + modelFile);
        AbstractExternalizable.compileTo(chunkerEstimator,modelFile);

        test(chunkerEstimator,"c-kit mutation Asp 816 His and then");
    }

    static void test(CharLmHmmChunker chunker, String text) {
        char[] cs = text.toCharArray();
        int len = cs.length;
        int maxNBest = 10;
        System.out.println("\nTEXT=" + text);
        Chunking chunking = chunker.chunk(text);
        System.out.println("\n1st BEST CHUNKING\n" + chunking);

        System.out.println("\n10 BEST CHUNKS");
        Iterator<Chunk> it = chunker.nBestChunks(cs,0,len,maxNBest);
        for (int i = 0; i < maxNBest && it.hasNext(); ++i)
            System.out.println(i + " " +  it.next());

        System.out.println("\n10 BEST CHUNKINGS");
        Iterator<ScoredObject<Chunking>> it2 = chunker.nBest(cs,0,len,maxNBest);
        for (int i = 0; i < maxNBest && it2.hasNext(); ++i)
            System.out.println(i + " " + it2.next());
    }

}
