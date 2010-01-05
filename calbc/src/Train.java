import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.zip.GZIPInputStream;

import org.xml.sax.InputSource;


public class Train {

    public static void main(String[] args) 
        throws IOException {

        File trainFileGz = new File(args[0]);
        File modelFile = new File(args[1]);

        System.out.println("GZipped Training File=" + trainFileGz);
        System.out.println("Compiled Model file=" + modelFile);

        int nGram = 8;
        int maxCharacters = 128;
        double charLmInterpolation = nGram;
        boolean smoothStates = true;

        HmmCharLmEstimator hmmEstimator
            = new HmmCharLmEstimator(nGram,
                                     maxCharacters,
                                     charLmInterpolation,
                                     smoothStates);

        CharLmHmmChunker chunkerEstimator
            = new CharLmHmmChunker(IndoEuropeanTokenizerFactory.INSTANCE,
                                   hmmEstimator);


        System.out.println("\nTraining");
        InputStream fileIn = new FileInputStream(trainFileGz);
        GZIPInputStream zipIn = new GZIPInputStream(fileIn);
        InputSource in = new InputSource(zipIn);
        CalbcChunkParser chunkParser = new CalbcChunkParser();
        chunkParser.setHandler(chunkerEstimator);
        chunkParser.parse(in);

        System.out.println("\nReporting");
        chunkParser.report();

        System.out.println("\nCompiling");
        AbstractExternalizable.compileTo(chunkerEstimator,modelFile);
   }

}