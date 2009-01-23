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

import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.stats.Statistics;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.FilterTokenizer;
import com.aliasi.tokenizer.TokenFeatureExtractor;

import com.aliasi.util.AbstractExternalizable;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import patient.Corpora;

// just doing explicit now
public class RunBaseline {

    public static void main(String[] args) throws Exception {
	File[] patientFiles = toFiles(args[0]);
	File[] annotationFiles = toFiles(args[1]);

        boolean normalizeCategories = false;
	Corpora corpora = new Corpora(patientFiles,annotationFiles,normalizeCategories);

        System.out.println("# threads=" + NUM_THREADS);
        System.out.println("# folds=" + NUM_FOLDS);
        System.out.println("min feature count=" + MIN_FEATURE_COUNT);
        System.out.println("add intercept=" + ADD_INTERCEPT);
        System.out.println("prior=" + PRIOR);
        System.out.println("annealing schedule=" + ANNEALING_SCHEDULE);
        System.out.println("min improvement=" + MIN_IMPROVEMENT);
        System.out.println("min epochs=" + MIN_EPOCHS);
        System.out.println("max epochs=" + MAX_EPOCHS);
        
	for (int i = 2; i < args.length; ++i) {
	    String[] sourceXDisease = args[i].split(",");
	    String source = sourceXDisease[0];
	    String disease = sourceXDisease[1];


	    XValidatingClassificationCorpus<CharSequence> corpus
		= corpora.get(source,disease,NUM_FOLDS);
	    corpus.permuteCorpus(new Random(42));
	    
            System.out.println("\nSOURCE=" + source + " DISEASE=" + disease);
	    System.out.println("CORPUS(" + source + "," + disease + ")=" + corpus);
	    corpora.reportCategoryCount(source,disease);
	    // corpus.visitTrain(PRINT_HANDLER);

            crossValidate(corpus,NUM_THREADS);
	}
    }

    static void crossValidate(XValidatingClassificationCorpus<CharSequence> corpus,
                              int numThreads) 
        throws IOException, ClassNotFoundException, InterruptedException {
        
        double[] accuracies = new double[corpus.numFolds()];
        Queue<Integer> foldQueue = new LinkedList<Integer>();
        for (int fold = 0; fold < corpus.numFolds(); ++fold) {
            foldQueue.add(fold);
        }
        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < threads.length; ++i)
            threads[i] = new Thread(new CrossValidate(foldQueue,accuracies,corpus));
        for (int i = 0; i < threads.length; ++i)
            threads[i].start();
        for (int i = 0; i < threads.length; ++i)
            threads[i].join();
        
        double mean = Statistics.mean(accuracies);
        double variance = Statistics.variance(accuracies);
        System.out.printf("mean=%5.3f dev=%5.3f\n",mean,Math.sqrt(variance));
    }


    static class CrossValidate implements Runnable {
        
        private final XValidatingClassificationCorpus<CharSequence> mCorpus;
        private final Queue<Integer> mFoldQueue;
        private final double[] mAccuracies;

        public CrossValidate(Queue<Integer> foldQueue,
                             double[] accuracies,
                             XValidatingClassificationCorpus<CharSequence> corpus) {
            mFoldQueue = foldQueue;
            mAccuracies = accuracies;
            mCorpus = corpus;
        }

        public void run() {
            while (true && !Thread.currentThread().isInterrupted()) {
                int fold = -1;
                synchronized (mFoldQueue) {
                    if (mFoldQueue.size() == 0)
                        return;
                    fold = mFoldQueue.remove();
                }
                XValidatingClassificationCorpus<CharSequence> corpusCopy
                    = new XValidatingClassificationCorpus<CharSequence>(mCorpus);
                corpusCopy.setFold(fold);

                try {
                    LogisticRegressionClassifier<CharSequence> classifier
                        = LogisticRegressionClassifier.train(Train3.FEATURE_EXTRACTOR,
                                                             corpusCopy,
                                                             MIN_FEATURE_COUNT,
                                                             ADD_INTERCEPT,
                                                             PRIOR,
                                                             ANNEALING_SCHEDULE,
                                                             MIN_IMPROVEMENT,
                                                             MIN_EPOCHS,MAX_EPOCHS,
                                                             PROGRESS_WRITER);

                    if (fold == 0) System.out.println("#features=" + classifier.featureSymbolTable().numSymbols());
                    // don't really need to compile; just testing
                    Classifier<CharSequence,Classification> compiledClassifier
                        = (Classifier<CharSequence,Classification>) AbstractExternalizable.compile(classifier);

                    ClassifierEvaluator evaluator = new ClassifierEvaluator(compiledClassifier, TEXTUAL_CATEGORIES);
                    corpusCopy.visitTest(evaluator);

                    System.out.printf("fold=%5d,      acc=%5.3f      kappa=%5.3f\n",
                                      fold,
                                      evaluator.confusionMatrix().totalAccuracy(),
                                      evaluator.confusionMatrix().kappa());
                    mAccuracies[fold] = evaluator.confusionMatrix().totalAccuracy();
                } catch (Exception e) {
                    System.out.println("exception=" + e);
                    e.printStackTrace(System.out);
                }
            }
	    
        }
    }

         
    public static File[] toFiles(String fileNamesString){ 
	String[] fileNames = fileNamesString.split(",");
	File[] files = new File[fileNames.length];
	for (int i = 0; i < files.length; ++i)
	    files[i] = new File(fileNames[i]);
	return files;
    }
    

    static final int NUM_FOLDS = 4;
    static final int NUM_THREADS = 1;
    
    static final double MIN_IMPROVEMENT = 0.0000000001; // more precision doesn't help much
    static final int MIN_EPOCHS = 1;
    static final int MAX_EPOCHS = 30000;

    static final int MIN_FEATURE_COUNT = 5;
    static final boolean ADD_INTERCEPT = true;

    static final double PRIOR_VARIANCE = 0.5;
    static final boolean NONINFORMATIVE_INTERCEPT = true; 
    static final RegressionPrior PRIOR = RegressionPrior.laplace(PRIOR_VARIANCE,NONINFORMATIVE_INTERCEPT); 

    static final PrintWriter PROGRESS_WRITER 
        = null;
    // = new PrintWriter(new OutputStreamWriter(System.out),true);


    static final AnnealingSchedule ANNEALING_SCHEDULE
        = AnnealingSchedule.exponential(0.001,0.9999); // exp(0.001,0.9999) works well for ngram(5,5) min=20, prior=0.5, large # iterations

    // 8 folds, 5-grams, 0.5 prior Laplace 
    // 0: exp(0.005, 0.999)  1999=-46.4106

    static final TokenizerFactory NGRAM_TOKENIZER_FACTORY
        = new NGramTokenizerFactory(6,6);

    static final TokenizerFactory SIMPLE_TOKENIZER_FACTORY 
	= new RegExTokenizerFactory("\\S+"); // 
    // = new RegExTokenizerFactory("(\\p{L}|\\d)+"); // letter+ | digit+ | non-white-space
    
    public static final TokenizerFactory BIGRAM_TOKENIZER_FACTORY
    // = new BigramTokenizerFactory(SIMPLE_TOKENIZER_FACTORY);
        = new TokenNGramTokenizerFactory(1,4,SIMPLE_TOKENIZER_FACTORY);

    public static final FeatureExtractor<CharSequence> FEATURE_EXTRACTOR
        = new TokenFeatureExtractor(SIMPLE_TOKENIZER_FACTORY);

    static final String[] NORMALIZED_CATEGORIES = new String[] { "Y", "U" };
    static final String[] TEXTUAL_CATEGORIES = new String[] {"Y","N","U","Q"};
    static final String[] INTUITIVE_CATEGORIES = new String[] {"Y","N","Q"};


    static final ClassificationHandler<CharSequence,Classification> PRINT_HANDLER
	= new ClassificationHandler<CharSequence,Classification>() {
	public void handle(CharSequence input, Classification c) {
	    System.out.println("\n" + c + "\n" + input.subSequence(0,Math.min(100,input.length())));
	}
    };

    static class BigramTokenizerFactory implements TokenizerFactory {
        final TokenizerFactory mTokenizerFactory;
        BigramTokenizerFactory(TokenizerFactory tokenizerFactory) {
            mTokenizerFactory = tokenizerFactory;
        }
        public Tokenizer tokenizer(char[] cs, int start, int end) {
            Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end);
            return new BigramFilterTokenizer(tokenizer);
        }
    }

    static class BigramFilterTokenizer extends FilterTokenizer {
        String mLastToken = "BEGIN_STREAM";
        BigramFilterTokenizer(Tokenizer tokenizer) {
            super(tokenizer);
        }
        public String nextToken() {
            String token = mTokenizer.nextToken();
            if (token == null) return null;
            String bigramToken = mLastToken + " " + token;
            mLastToken = token;
            return bigramToken;
        }
    }

    static public class TokenNGramTokenizerFactory implements TokenizerFactory {
        final TokenizerFactory mTokenizerFactory;
        final int mMinNGram;
        final int mMaxNGram;
        public TokenNGramTokenizerFactory(int minNGram, int maxNGram, TokenizerFactory tokenizerFactory) {
            mTokenizerFactory = tokenizerFactory;
            mMinNGram = minNGram;
            mMaxNGram = maxNGram;
        }
        public Tokenizer tokenizer(char[] cs, int start, int end) {
            Tokenizer tokenizer = mTokenizerFactory.tokenizer(cs,start,end);
            return new TokenNGramFilterTokenizer(mMinNGram,mMaxNGram,tokenizer);
        }
    }

    static class TokenNGramFilterTokenizer extends FilterTokenizer {
        final LinkedList<String> mTokenList = new LinkedList();
        final int mMinNGram;
        final int mMaxNGram;
        int mNextNGram;
        TokenNGramFilterTokenizer(int minNGram, int maxNGram, Tokenizer tokenizer) {
            super(tokenizer);
            mMinNGram = minNGram;
            mMaxNGram = maxNGram;
            mNextNGram = mMaxNGram + 1; // force rollover right away
            for (int i = 0; i < maxNGram; ++i)
                mTokenList.add("BEGIN_STREAM");
        }
        public String nextToken() {
            if (mNextNGram > mMaxNGram) {
                String nextToken = mTokenizer.nextToken();
                if (nextToken == null) return null;
                mTokenList.pop();
                mTokenList.addLast(nextToken);
                mNextNGram = mMinNGram;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = mMaxNGram - mNextNGram; i < mMaxNGram; ++i) {
                sb.append(mTokenList.get(i)); // bad quadratic juju here
                sb.append(' ');
            }
            ++mNextNGram;
            return sb.toString().trim().toLowerCase();
        }
    }
    
}

