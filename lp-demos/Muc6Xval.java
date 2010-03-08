import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TagChunkCodec;

import com.aliasi.crf.ChainCrfChunker;
import com.aliasi.crf.ChainCrfFeatureExtractor;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.IOException;

public class Muc6Xval {

    public static void main(String[] args)
        throws IOException, ClassNotFoundException {

        File muc6XmlDir = new File(args[0]);
        int minFeatureCount = Integer.valueOf(args[1]);
        boolean addIntercept = Boolean.valueOf(args[2]);
        boolean cacheFeatures = Boolean.parseBoolean(args[3]);
        double priorVariance = Double.valueOf(args[4]);
        int priorBlockSize = Integer.parseInt(args[5]);
        double initialLearningRate = Double.valueOf(args[6]);
        double learningRateDecay = Double.valueOf(args[7]);
        double minImprovement = Double.valueOf(args[8]);
        int maxEpochs = Integer.valueOf(args[9]);
        int numFolds = Integer.valueOf(args[10]);

        Muc6XvalCorpus corpus
            = new Muc6XvalCorpus(1,muc6XmlDir);

        xval(corpus,
             minFeatureCount,
             addIntercept,
             cacheFeatures,
             priorVariance,
             priorBlockSize,
             initialLearningRate,
             learningRateDecay,
             minImprovement,
             maxEpochs,
             numFolds);
    }

    static void xval(XValidatingObjectCorpus<Chunking> corpus,
                     int minFeatureCount,
                     boolean addIntercept,
                     boolean cacheFeatures,
                     double priorVariance,
                     int priorBlockSize,
                     double initialLearningRate,
                     double learningRateDecay,
                     double minImprovement,
                     int maxEpochs,
                     int numFolds)
        throws IOException, ClassNotFoundException {

        corpus.setNumFolds(numFolds);

        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;

        // even munged MUC6 doesn't quite line up
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

        Reporter reporter
            = Reporters.stdOut().setLevel(LogLevel.DEBUG);

        System.out.println("\nEstimating");

        for (int fold = 0; fold < numFolds; ++fold) {
            System.out.println("\nFOLD=" + fold);
            corpus.setFold(fold);
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

            System.out.println("\nCompiling");
            @SuppressWarnings("unchecked") // required for serialized compile
                ChainCrfChunker compiledCrfChunker
                = (ChainCrfChunker)
                AbstractExternalizable.serializeDeserialize(crfChunker);
            System.out.println("     compiled");

            System.out.println("\nEvaluating");
            ChunkerEvaluator evaluator
                = new ChunkerEvaluator(compiledCrfChunker);

            corpus.visitTest(evaluator);
            System.out.println("\nEvaluation");
            System.out.println(evaluator);
        }
    }

}