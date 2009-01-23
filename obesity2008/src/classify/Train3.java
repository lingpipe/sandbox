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
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import patient.Corpora;

// just doing explicit now
public class Train3 {

    public static void main(String[] args) throws Exception {
        String patientFileNames = "python/train.xml,python/train2.xml";
        String annotationFileNames = "data/obesity_standoff_annotations_training.xml,data/obesity_standoff_annotations_training_addendum.xml,data/obesity_standoff_annotations_training_addendum2.xml,data/obesity_standoff_annotations_training_addendum3.xml";
        String modelFileName = "non-cvs/models/run3";

	File[] patientFiles = RunBaseline.toFiles(patientFileNames);
	File[] annotationFiles = RunBaseline.toFiles(annotationFileNames);
        File modelsDir = new File(modelFileName);

        System.out.println("PATIENT FILES=" + Arrays.asList(patientFiles));
        System.out.println("ANNOTATION FILES=" + Arrays.asList(annotationFiles));
        System.out.println("MODELS DIR=" + modelsDir);
        System.out.println();

	Corpora corpora = new Corpora(patientFiles,annotationFiles,false); // true = normalize
        System.out.println("Source set=" + corpora.sourceSet());
        System.out.println("Disease set=" + corpora.diseaseSet());
        System.out.println();

        for (String source : corpora.sourceSet()) {
            if ("textual".equals(source)) continue; // added to just train intuitive
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
        int maxEpochs = 30000; 

        int minFeatureCount = 5;
        boolean addIntercept = true;

        double priorVariance = 0.1;
        boolean noninformativeIntercept = true; 
        RegressionPrior prior = RegressionPrior.laplace(priorVariance,noninformativeIntercept); 
            
        PrintWriter progressWriter 
            = null;
            // = new PrintWriter(new OutputStreamWriter(System.out,Strings.UTF8),true);

        AnnealingSchedule annealingSchedule 
            = AnnealingSchedule.exponential(0.001,0.9999);

        LogisticRegressionClassifier<CharSequence> classifier
            = LogisticRegressionClassifier.train(Train3.FEATURE_EXTRACTOR,
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

    static Set<String> readLegalFeatures(String fileName) {
        String[] elements = null;
        try {
            elements = Files.readLinesFromFile(new File(fileName),"ISO-8859-1");
        } catch (IOException e) {
            System.out.println("BOMBED.  File=" + fileName);
            System.out.println("exception = e");
            e.printStackTrace(System.out);
            System.exit(0);
        }
        Set<String> result = new HashSet<String>();
        for (String element : elements) {
            if (element.length() > 0) {
                result.add(element);
            }
        }
        System.out.println("# of feats=" + result.size());
        return result;
    }

    static String FEATURE_SOURCE = "intuitive";

    static final TokenizerFactory SIMPLE_TOKENIZER_FACTORY
        = new RegExTokenizerFactory("\\S+");
    
    static Set<String> LEGAL_FEATURE_SET = readLegalFeatures("data/reduced_lexicons/" + FEATURE_SOURCE + "_top_15.txt");

    public static final FeatureExtractor<CharSequence> FEATURE_EXTRACTOR
        = new FeatureExtractorFilter(Train3.LEGAL_FEATURE_SET,
                                     new TokenFeatureExtractor(Train3.SIMPLE_TOKENIZER_FACTORY));
    



}