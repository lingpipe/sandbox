import com.aliasi.chunk.AbstractCharLmRescoringChunker;
import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.HmmChunker;
import com.aliasi.chunk.TokenShapeChunker;

import com.aliasi.hmm.HmmDecoder;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.FastCache;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.File;

import java.util.Iterator;

public class ChunkerSpeedTest {

    public static void main(String[] args) throws Exception {
        String baseModelFileName = args[0];
        File dataFile = new File(args[1]);
        String charset = args[2];

        System.out.println("Reading chars from file=" + dataFile);
        char[] cs = Files.readCharsFromFile(dataFile,charset);
        System.out.println("     #chars=" + cs.length);

        TokenizerFactory tokenizer
            = IndoEuropeanTokenizerFactory.INSTANCE;
        IndoEuropeanSentenceModel sentenceModel
            = new IndoEuropeanSentenceModel(true,false); // +end, -balan paren
        Chunker sentenceChunker
            = new SentenceChunker(tokenizer,sentenceModel);

        long startTime = System.currentTimeMillis();

        System.out.println("\nSentence Detecting Corpus.");
        Chunking sentenceChunking
            = sentenceChunker.chunk(cs,0,cs.length);
        System.out.println("     Finished Sentence Detection.");

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        report("Sentence Detection",elapsedTime,cs.length);

        // evaluate(sentenceChunking,baseModelFileName,"CharLmHmmChunker",
        // cs);
        evaluate(sentenceChunking,baseModelFileName,"CharLmRescoringChunker",
        cs);
        // evaluate(sentenceChunking,baseModelFileName,"TokenShapeChunker",
        // cs);
    }

    static void evaluate(Chunking sentenceChunking,
                         String baseFile,
                         String modelType,
                         char[] cs) throws Exception {
        System.out.println("\nEvaluating model type=" + modelType);
        File modelFile = new File(baseFile + "." + modelType);
        System.out.println("     Reading chunker from file=" + modelFile);
        Chunker neChunker = createChunker(modelFile);
        System.out.println("          Finished Read.");

        long startTime = System.currentTimeMillis();

        System.out.println("Beginning Speed Test");
        Iterator<Chunk> it = sentenceChunking.chunkSet().iterator();
        for (int i = 0; it.hasNext(); ++i) {
            if (i % 5000 == 0) System.out.println("     " + i + " sents");
            Chunk chunk = it.next();
            int start = chunk.start();
            int end = chunk.end();
            Chunking neChunking = neChunker.chunk(cs,start,end);
        }

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        report("Entity tagging",elapsedTime,cs.length);
    }

    static void report(String msg, long elapsedMs, int numChars) {
        long rate = numChars / elapsedMs;
        System.out.println("     " + msg
                           + " time=" + Strings.msToString(elapsedMs)
                           + " #chars=" + numChars
                           + " rate=" + rate + " kilochars/second");
    }

    static Chunker createChunker(File modelFile) throws Exception {
        Chunker chunker
            = (Chunker) AbstractExternalizable.readObject(modelFile);
        configure(chunker);
        return chunker;
    }

    @SuppressWarnings("rawtypes") // required for instanceof
    static void configure(Chunker chunker) {
        if (chunker instanceof HmmChunker) {
            System.out.println("     Configuring HmmChunker");
            HmmChunker hmmChunker = (HmmChunker) chunker;
            HmmDecoder decoder = hmmChunker.getDecoder();
            int cacheSize = 1000000;
            double beam = 15.0;
            double emissionBeam = 15.0;

            System.out.println("     emission cache=" + cacheSize);
            System.out.println("     beam=" + beam);
            System.out.println("     emit beam=" + emissionBeam);
            decoder.setEmissionLog2Cache(new FastCache<String,double[]>(cacheSize));
            decoder.setEmissionCache(new FastCache<String,double[]>(cacheSize));
            decoder.setLog2Beam(beam);
            decoder.setLog2EmissionBeam(emissionBeam);

        } else if (chunker instanceof TokenShapeChunker) {
            System.out.println("     Configuring TokenShapeChunker");
            TokenShapeChunker tsChunker = (TokenShapeChunker) chunker;
            double beam = 15.0;
            System.out.println("          Setting beam=" + beam);
            tsChunker.setLog2Beam(beam);
        } else if (chunker instanceof AbstractCharLmRescoringChunker) {
            @SuppressWarnings("unchecked")
            AbstractCharLmRescoringChunker<?,?,?> clrChunker = (AbstractCharLmRescoringChunker<?,?,?>) chunker;
            @SuppressWarnings("unchecked")
            HmmChunker baseChunker = (HmmChunker) clrChunker.baseChunker();
            System.out.println("     Configuring CharLmRescoringChunker");

            int numChunkingsRescored = 256;
            clrChunker.setNumChunkingsRescored(numChunkingsRescored);
            System.out.println("     #rescored=" + numChunkingsRescored);

            System.out.println("     Configure Underlying HmmChunker:");
            configure(baseChunker);
        }
    }



}