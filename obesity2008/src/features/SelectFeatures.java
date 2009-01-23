package features;

import com.aliasi.classify.Classification;
import com.aliasi.classify.XValidatingClassificationCorpus;

import com.aliasi.corpus.ClassificationHandler;
import com.aliasi.corpus.Corpus;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Pair;

import java.io.File;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.xml.sax.SAXException;

import classify.RunBaseline;

import patient.Corpora;


public class SelectFeatures {

    // these get reset in the main
    static int MIN_FEATURE_COUNT = 10;
    static int NUM_FEATURES_REPORTED = 100;
    static int MIN_NGRAM = 1;
    static int MAX_NGRAM = 1;

    public static void main(String[] args) throws IOException, SAXException {
        // lots of cut and paste from classify.RunBaseline
        File[] patientFiles = RunBaseline.toFiles(args[0]);
	File[] annotationFiles = RunBaseline.toFiles(args[1]);
        MIN_FEATURE_COUNT = Integer.parseInt(args[2]);
        NUM_FEATURES_REPORTED = Integer.parseInt(args[3]);
        MIN_NGRAM = Integer.parseInt(args[4]);
        MAX_NGRAM = Integer.parseInt(args[5]);

        System.out.println("min feature count=" + MIN_FEATURE_COUNT);
        System.out.println("num features reported=" + NUM_FEATURES_REPORTED);
        System.out.println("min ngram=" + MIN_NGRAM);
        System.out.println("max ngram=" + MAX_NGRAM);

        TokenizerFactory baseFactory = new RegExTokenizerFactory("\\S+");

        TokenizerFactory nGramTokenizerFactory 
            = new RunBaseline.TokenNGramTokenizerFactory(MIN_NGRAM,MAX_NGRAM,
                                                         baseFactory);
        FeatureExtractor<CharSequence> featureExtractor
            = new TokenFeatureExtractor(nGramTokenizerFactory);

        System.out.println("Extracting corpora");
	Corpora corpora = new Corpora(patientFiles,annotationFiles,true);
	for (int i = 6; i < args.length; ++i) {
            System.out.println("\nExtracting features for " + args[i]);
	    String[] sourceXDisease = args[i].split(",");
	    String source = sourceXDisease[0];
	    String disease = sourceXDisease[1];
	    XValidatingClassificationCorpus<CharSequence> corpus
		= corpora.get(source,disease,1); // 1 == NUM_FOLDS is irrelevant for this use
            System.out.println("doc count=" + corpus.numInstances());
            ObjectToDoubleMap<String> featureInfoGain
                = featureInfoGain(corpus,featureExtractor);
            List<String> featuresIG = featureInfoGain.keysOrderedByValueList();
            for (int k = 0; i < featuresIG.size() && k < NUM_FEATURES_REPORTED; ++k) {
                String feature = featuresIG.get(k);
                System.out.println(feature + " " + featureInfoGain.getValue(feature));
            }
        }
    }

    public static <E> ObjectToDoubleMap<String> 
        featureInfoGain(Corpus<ClassificationHandler<E,Classification>> corpus,
                        FeatureExtractor<E> featureExtractor) 
	throws IOException {

	CountHandler<E> countHandler = new CountHandler(featureExtractor);
	corpus.visitCorpus(countHandler);  // visit whole corpus, not just training
        return countHandler.informationGain();
    }


    static class CountHandler<F> implements ClassificationHandler<F,Classification> {
	private final FeatureExtractor mFeatureExtractor;
        private final Map<String,ObjectToCounterMap<String>> mFeatureToCategoryCounter 
            = new HashMap<String,ObjectToCounterMap<String>>();
        private final ObjectToCounterMap<String> mFeatureCounter = new ObjectToCounterMap<String>();
        private final ObjectToCounterMap<String> mGlobalCategoryCounter = new ObjectToCounterMap<String>();
        private int mDocCount = 0;

	public CountHandler(FeatureExtractor<F> featureExtractor) {
	    mFeatureExtractor = featureExtractor;
	}
	public void handle(F input, Classification c) {
            ++mDocCount;
	    String category = c.bestCategory();
	    mGlobalCategoryCounter.increment(category);
	    Map<String,? extends Number> features
		= mFeatureExtractor.features(input);
	    for (String feature : features.keySet()) {
                ObjectToCounterMap<String> featureCategoryCounter = mFeatureToCategoryCounter.get(feature);
                if (featureCategoryCounter == null) {
                    featureCategoryCounter = new ObjectToCounterMap<String>();
                    mFeatureToCategoryCounter.put(feature,featureCategoryCounter);
                }
                featureCategoryCounter.increment(category);
                mFeatureCounter.increment(feature);
	    }
	}


	public ObjectToDoubleMap<String> informationGain() {
	    ObjectToDoubleMap<String> result = new ObjectToDoubleMap<String>();
	    for (String feature : mFeatureCounter.keySet()) {
                if (mFeatureCounter.getCount(feature) < MIN_FEATURE_COUNT) continue;
                if (!Character.isLetter(feature.charAt(0))
                    && !Character.isDigit(feature.charAt(0))) continue;
                if (!Character.isLetter(feature.charAt(feature.length()-1))
                    && !Character.isDigit(feature.charAt(feature.length()-1))) continue;
		result.set(feature,informationGain(feature));
            }
	    return result;
	}

	double informationGain(String feature) {
            ObjectToCounterMap<String> featureCategoryCounter = mFeatureToCategoryCounter.get(feature);
            if (featureCategoryCounter == null)
                return 0.0;

	    double featCount = mFeatureCounter.getCount(feature);
	    double notFeatCount = mDocCount - featCount;
	    double pFeature = (featCount + PRIOR_COUNT) / (2.0 * PRIOR_COUNT + mDocCount);
	    double pNotFeature = 1.0 - pFeature;

            // System.out.println(feature + " posCount=" + featCount + " negCount=" + notFeatCount);

            int numCategories = mGlobalCategoryCounter.size();
            double totalPriorCount = numCategories * PRIOR_COUNT;

	    double entropyFeat = 0.0;
	    double entropyNotFeat = 0.0;
	    for (String category : mGlobalCategoryCounter.keySet()) {
		double featCatCount = featureCategoryCounter.getCount(category);
		double notFeatCatCount = mGlobalCategoryCounter.getCount(category) - featCatCount;
		double pCategoryGivenFeature = (featCatCount + PRIOR_COUNT) / (featCount + totalPriorCount);;
		double pCategoryGivenNotFeature 
		    = (notFeatCatCount + PRIOR_COUNT) / (notFeatCount + totalPriorCount);
		entropyFeat += pCategoryGivenFeature * com.aliasi.util.Math.log2(pCategoryGivenFeature);
		entropyNotFeat += pCategoryGivenNotFeature * com.aliasi.util.Math.log2(pCategoryGivenNotFeature);
	    }
            double infoGain = pFeature * entropyFeat 
                + pNotFeature * entropyNotFeat;
            return infoGain;

	}
    }

    static double PRIOR_COUNT = 0.5;
}