package classify;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classifier;
import com.aliasi.classify.ClassifierEvaluator;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;
import com.aliasi.classify.LogisticRegressionClassifier;
import com.aliasi.classify.NaiveBayesClassifier;
import com.aliasi.classify.ScoredClassification;
import com.aliasi.classify.TfIdfClassifierTrainer;
import com.aliasi.classify.XValidatingClassificationCorpus;

import com.aliasi.corpus.ClassificationHandler;
import com.aliasi.corpus.Corpus;

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Pair;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import patient.Corpora;

// just doing explicit now
public class Train {

    public static void main(String[] args) throws Exception {
	File[] patientFiles = RunBaseline.toFiles(args[0]);
	File[] annotationFiles = RunBaseline.toFiles(args[1]);
        File modelsDir = new File(args[2]);

        System.out.println("PATIENT FILES=" + Arrays.asList(patientFiles));
        System.out.println("ANNOTATION FILES=" + Arrays.asList(annotationFiles));
        System.out.println("MODELS DIR=" + modelsDir);
        System.out.println();

	Corpora corpora = new Corpora(patientFiles,annotationFiles,true);
        System.out.println("Source set=" + corpora.sourceSet());
        System.out.println("Disease set=" + corpora.diseaseSet());
        System.out.println();

        for (String source : corpora.sourceSet()) {
            System.out.println("SOURCE=" + source);
            File sourceDir = new File(modelsDir,source);
            sourceDir.mkdirs();
            for (String disease : corpora.diseaseSet()) {
                File modelFile = new File(sourceDir,disease+".Classifier");
                XValidatingClassificationCorpus<CharSequence> corpus
                    = corpora.get(source,disease,2);
                System.out.println("     DISEASE=" + disease + " file=" + modelFile.getCanonicalPath() + " #instances=" + corpus.numInstances());
                Compilable classifier = createClassifier(corpus);
                AbstractExternalizable.compileTo(classifier,modelFile);
            }

        }
    }        
    
    public static Compilable createClassifier(final XValidatingClassificationCorpus<CharSequence> corpus) 
        throws IOException {
        
        Corpus<ClassificationHandler<CharSequence,Classification>> trainingCorpus
            = new Corpus<ClassificationHandler<CharSequence,Classification>>() {
            public void visitTrain(ClassificationHandler<CharSequence,Classification> handler) throws IOException {
                corpus.visitTrain(handler);
                corpus.visitTest(handler);
            }
        };

        double minImprovement = 0.0000000001; // more precision doesn't help much
        int minEpochs = 1;
        int maxEpochs = 100; // ~ 5 minutes

        int minFeatureCount = 10;
        boolean addIntercept = true;

        double priorVariance = 0.5;
        boolean noninformativeIntercept = true; 
        RegressionPrior prior = RegressionPrior.laplace(priorVariance,noninformativeIntercept); 
            
        PrintWriter progressWriter = null;
        // = new PrintWriter(new OutputStreamWriter(System.out,Strings.UTF8),true);

        AnnealingSchedule annealingSchedule 
            = AnnealingSchedule.inverse(0.001,1000);

        LogisticRegressionClassifier<CharSequence> classifier
            = LogisticRegressionClassifier.train(RunBaseline.FEATURE_EXTRACTOR,
                                                 trainingCorpus,
                                                 minFeatureCount,
                                                 addIntercept,
                                                 prior,
                                                 annealingSchedule,
                                                 minImprovement,
                                                 minEpochs,maxEpochs,
                                                 progressWriter);
        return classifier;
    }
    



}