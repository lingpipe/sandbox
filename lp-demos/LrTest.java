import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.ClassifierEvaluator;
import com.aliasi.classify.LogisticRegressionClassifier;

import com.aliasi.util.FeatureExtractor;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;



import java.io.File;

public class LrTest {

    public static void main(String[] args) throws Exception {
        Reporter reporter = Reporters.stdOut().setLevel(LogLevel.DEBUG);

        File corpusDir = new File(args[0]);
        reporter.debug("Building Corpus; Reading Directory=" + corpusDir);
        TwentyNewsgroupsCorpus corpus
            = new TwentyNewsgroupsCorpus(corpusDir);
        corpus.setMaxSupervisedInstancesPerCategory(1000);

        int minFeatureCount = 5;

        boolean addIntercept = true;
        double priorVariance = 0.25;
        RegressionPrior prior
            = RegressionPrior.laplace(priorVariance,addIntercept);

        double initialLearningRate = 0.00005;
        double annealingRate = 0.995;
        AnnealingSchedule annealingSchedule
            = AnnealingSchedule.exponential(initialLearningRate,
                                            annealingRate);
        double minImprovement = 0.000001;
        int minEpochs = 1;
        int maxEpochs = 10000;


        TokenizerFactory baseTokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory tokenizerFactory
            = new LowerCaseTokenizerFactory(baseTokenizerFactory);
        FeatureExtractor<CharSequence> featureExtractor
            = new TokenFeatureExtractor(baseTokenizerFactory);

        reporter.debug("TRAINING");

        LogisticRegressionClassifier<CharSequence> classifier
            = LogisticRegressionClassifier
            .train(featureExtractor,
                   corpus,
                   minFeatureCount,
                   addIntercept,
                   prior,
                   annealingSchedule,
                   reporter,
                   minImprovement,minEpochs,maxEpochs);

        System.out.println("classifier=" + classifier);

        reporter.debug("EVALUATING");
        String[] categories = corpus.categorySet().toArray(new String[0]);
        ClassifierEvaluator<CharSequence,ConditionalClassification> evaluator
            = new ClassifierEvaluator<CharSequence,
                                      ConditionalClassification>(classifier,
                                                                 categories);
        corpus.visitTest(evaluator);
        System.out.println(evaluator);
    }

}