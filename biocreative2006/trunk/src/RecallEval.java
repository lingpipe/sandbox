import com.aliasi.corpus.ChunkHandler;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.CharLmHmmChunker;

import com.aliasi.classify.ScoredPrecisionRecallEvaluation;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.util.AbstractExternalizable;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import java.io.File;

import java.util.HashSet;
import java.util.Set;


public class RecallEval {

    static final int NUM_FOLDS = 20;
    
    static final int MAX_CONFIDENCE_CHUNKS = 1024;

    static final int MAX_NGRAM = 5;
    static final int MAX_CHARS = 256;
    static final double INTERPOLATION = 5.0;
    static final boolean SMOOTH_STATES = true;

    public static void main(String[] args) throws Exception {
        File geneEvalFile = new File(args[0]);
        File trainFile = new File(args[1]);


        System.out.println("ngram=" + MAX_NGRAM);
        System.out.println("chas=" + MAX_CHARS);
        System.out.println("interpolation=" + INTERPOLATION);
        System.out.println("smooth states=" + SMOOTH_STATES);
        System.out.println("max conf chunks=" + MAX_CONFIDENCE_CHUNKS);
        System.out.println("# folds=" + NUM_FOLDS);

        System.out.println("Parsing Data");
        Biocreative2006ChunkParser parser 
            = new Biocreative2006ChunkParser(geneEvalFile);
        final Set<Chunking> chunkingSet = new HashSet<Chunking>();
        ChunkHandler collector = new ChunkHandler() {
                public void handle(Chunking chunking) {
                    chunkingSet.add(chunking);
                }
            };
        parser.setHandler(collector);
        parser.parse(trainFile);
        Chunking[] chunkings = chunkingSet.toArray(new Chunking[0]);
        int numChunkings = chunkings.length;
        System.out.println("# chunkings=" + numChunkings);

        ChunkerEvaluator evaluator = new ChunkerEvaluator(null);
        evaluator.setMaxConfidenceChunks(MAX_CONFIDENCE_CHUNKS);
        evaluator.setMaxNBest(1);
        evaluator.setVerbose(false);
        int numChunks = 0;
        int mbs = 0;
        for (Chunking chunking : chunkings) {
            numChunks += chunking.chunkSet().size();
            mbs += chunking.charSequence().length();
        }
        System.out.println("TOTALS TEXT=" + mbs + "MB   #CHUNKS=" + numChunks);

        for (int fold = 0; fold < NUM_FOLDS; ++fold) {
            HmmCharLmEstimator hmmEstimator
                = new HmmCharLmEstimator(MAX_NGRAM,
                                         MAX_CHARS,
                                         INTERPOLATION,
                                         SMOOTH_STATES);
            CharLmHmmChunker chunkerEstimator
                = new CharLmHmmChunker(IndoEuropeanTokenizerFactory.INSTANCE,
                                       hmmEstimator);
            int startTest = (fold * numChunkings)/NUM_FOLDS;
            int endTest = ((fold + 1) * numChunkings)/NUM_FOLDS;
            System.out.println("Fold=" + fold + " (" + startTest + ", " + endTest + ")");
            System.out.println("     training");
            int count = 0;
            for (int i = 0; i < numChunkings; ++i) {
                if (i >= startTest && i < endTest) continue;
                ++count;
                // System.out.println("Chunkings[" + i + "]=" + chunkings[i]);
                chunkerEstimator.handle(chunkings[i]);
            }
            System.out.println("     compiling, training count=" + count);
            Chunker chunker = (Chunker) AbstractExternalizable.compile(chunkerEstimator);
            System.out.println("     testing");
            
            evaluator.setChunker(chunker);
            for (int i = startTest; i < endTest; ++i)
                evaluator.handle(chunkings[i]);
            ScoredPrecisionRecallEvaluation prEval
                = evaluator.confidenceEvaluation();
            System.out.println("maximum f measure=" + prEval.maximumFMeasure());
        }
        
        ScoredPrecisionRecallEvaluation eval = evaluator.confidenceEvaluation();
        double[][] prCurve = eval.prCurve(true);
        System.out.printf("%8s %8s\n","REC","PREC");
        for (double[] rp : prCurve) {
            if (rp[0] < 0.99) continue;
            System.out.printf("%8.6f %8.6f\n",rp[0],rp[1]);
        }
    }

}