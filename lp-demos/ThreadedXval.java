import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.ChunkingEvaluation;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TagChunkCodec;

import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.XValidatingObjectCorpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.crf.ChainCrfChunker;
import com.aliasi.crf.ChainCrfFeatureExtractor;

import com.aliasi.io.FileLineReader;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadedXval {

    static final int QUEUE_CAPACITY = 1024;

    public static void main(String[] args)
        throws IOException, ClassNotFoundException, InterruptedException {

        File muc6XmlDir = new File(args[0]);

        Muc6XvalCorpus corpus
            = new Muc6XvalCorpus(10,muc6XmlDir);

        File paramsFile = new File(args[1]);
        int numThreads = Integer.parseInt(args[2]);

        System.out.println("Corpus Dir=" + muc6XmlDir);
        System.out.println("Jobs File=" + paramsFile);
        System.out.println("Number of Threads=" + numThreads);

        threadedEval(corpus,paramsFile,numThreads,System.out);
    }

    static void threadedEval(Corpus<ObjectHandler<Chunking>> corpus,
                             File paramsFile,
                             int numThreads,
                             OutputStream out)
        throws IOException, ClassNotFoundException, InterruptedException {

        ArrayBlockingQueue<Runnable> queue
            = new ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY);
        ThreadPoolExecutor executor
            = new ThreadPoolExecutor(numThreads,
                                     numThreads,
                                     Long.MAX_VALUE,
                                     TimeUnit.SECONDS,
                                     queue);

        FileLineReader lineReader = new FileLineReader(paramsFile,"ASCII");
        int id = 1;
        for (String line : lineReader) {
            Runnable evaluator = evaluator(corpus,line,out);
            if (evaluator != null) {
                System.out.println("Enqueueing Job=" + line);
                executor.execute(evaluator);
            }
        }
        System.out.println("Enqueued all jobs.");
        System.out.println("Waiting for termination.");
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE,TimeUnit.SECONDS);
    }

    // returns reporter output
    static ByteArrayOutputStream
        eval(Corpus<ObjectHandler<Chunking>> corpus,
             int minFeatureCount,
             boolean addIntercept,
             boolean cacheFeatures,
             double priorVariance,
             int priorBlockSize,
             double initialLearningRate,
             double learningRateDecay,
             double minImprovement,
             int maxEpochs)
        throws IOException, ClassNotFoundException {

        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;

        boolean enforceConsistency = false;
        TagChunkCodec tagChunkCodec
            = new BioTagChunkCodec(tokenizerFactory,
                                   enforceConsistency);

        ChainCrfFeatureExtractor<String> featureExtractor
            = new ChunkerFeatureExtractor();

        boolean uninformativeIntercept = addIntercept;
        RegressionPrior prior
            = RegressionPrior.laplace(priorVariance,
                                      uninformativeIntercept);

        AnnealingSchedule annealingSchedule
            = AnnealingSchedule.exponential(initialLearningRate,
                                            learningRateDecay);

        int minEpochs = 1;

        ByteArrayOutputStream bytesOut
            = new ByteArrayOutputStream();
        Reporter reporter
            = Reporters.stream(bytesOut,Strings.UTF8);
        reporter.setLevel(LogLevel.DEBUG);

        ChainCrfChunker crfChunker
            = ChainCrfChunker.estimate(corpus,
                                       tagChunkCodec,
                                       tokenizerFactory,
                                       featureExtractor,
                                       addIntercept,
                                       minFeatureCount,
                                       cacheFeatures,
                                       prior,
                                       priorBlockSize,
                                       annealingSchedule,
                                       minImprovement,
                                       minEpochs,
                                       maxEpochs,
                                       reporter);

        @SuppressWarnings("unchecked") // required for serialized compile
        ChainCrfChunker compiledCrfChunker
            = (ChainCrfChunker)
            AbstractExternalizable.serializeDeserialize(crfChunker);

        ChunkerEvaluator evaluator
            = new ChunkerEvaluator(compiledCrfChunker);
        corpus.visitTest(evaluator);
        ChunkingEvaluation evaluation
            = evaluator.evaluation();
        PrecisionRecallEvaluation prEvaluation
            = evaluation.precisionRecallEvaluation();

        reporter.debug("Precision=" + prEvaluation.precision());
        reporter.debug("Recall=" + prEvaluation.recall());
        reporter.debug("F(1)=" + prEvaluation.fMeasure());

        reporter.close();

        return bytesOut;
    }

    static Runnable evaluator(Corpus<ObjectHandler<Chunking>> corpus,
                              String line,
                              OutputStream out) {
        if (line.length() == 0)
            return null;
        if (line.startsWith("#"))
            return null;
        return new Evaluator(corpus,line,out);
    }

    static class Evaluator implements Runnable {
        private final Corpus<ObjectHandler<Chunking>> mCorpus;
        private final int mId;
        private final int mMinFeatureCount;
        private final boolean mAddIntercept;
        private final boolean mCacheFeatures;
        private final double mPriorVariance;
        private final int mPriorBlockSize;
        private final double mInitialLearningRate;
        private final double mLearningRateDecay;
        private final double mMinImprovement;
        private final int mMaxEpochs;
        private final OutputStream mOut;
        public Evaluator(Corpus<ObjectHandler<Chunking>> corpus,
                         String line,
                         OutputStream out) {
            String[] fields = line.split(",");
            mCorpus = corpus;
            mOut = out;
            mId = Integer.parseInt(fields[0].trim());
            mMinFeatureCount = Integer.parseInt(fields[1].trim());
            mAddIntercept = Boolean.parseBoolean(fields[2].trim());
            mCacheFeatures = Boolean.parseBoolean(fields[3].trim());
            mPriorVariance = Double.parseDouble(fields[4].trim());
            mPriorBlockSize = Integer.parseInt(fields[5].trim());
            mInitialLearningRate = Double.parseDouble(fields[6].trim());
            mLearningRateDecay = Double.parseDouble(fields[7].trim());
            mMinImprovement = Double.parseDouble(fields[8].trim());
            mMaxEpochs = Integer.parseInt(fields[9].trim());
        }
        public void run() {
            try {
                runWithExceptions();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
        void runWithExceptions()
            throws IOException, ClassNotFoundException {

            ByteArrayOutputStream bytesOut
                = eval(mCorpus,
                       mMinFeatureCount,
                       mAddIntercept,
                       mCacheFeatures,
                       mPriorVariance,
                       mPriorBlockSize,
                       mInitialLearningRate,
                       mLearningRateDecay,
                       mMinImprovement,
                       mMaxEpochs);

            synchronized (mOut) {
                bytesOut.write(("\nRUN ID=" + Integer.toString(mId)).getBytes());
                bytesOut.writeTo(mOut);
                mOut.flush();
            }
        }
    }

}