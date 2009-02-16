package stemmers.em2;

import com.aliasi.stats.Statistics;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class Trainer extends AbstractCommand {

    private final File mWordCountFile;
    private final int mMinWordCount;
    private final ObjectToCounterMap mWordToCounter;
    private final String[] mWords;

    private final File mResultFile;
    private final PrintWriter mResultPrinter;
    
    private final double mPriorCount;
    private final double mPriorStemAvgLength;
    private final double mPriorSuffixNonZeroAvgLength;
    private final double mPriorSuffixZeroLengthProb;

    private final double mLmInterpolationRatio;
    private final int mNumChars;
    private final int mMaxCharNGram;
    private final double mLmCountMultiplier;

    private final double mEpsilon;
    private final int mMaxIterations;

    private final ModelFactory mModelFactory;

    // for EM
    private boolean mFinished;
    private double mCompleteDataLogLikelihood;

    public Trainer(String[] args) throws IOException {
        super(args);

        mResultFile = getArgumentFile("resultFile");
        mResultPrinter = getPrinter(mResultFile);
        
        println("#Result File=" + mResultFile.getCanonicalPath());

        mWordCountFile = getArgumentExistingNormalFile("wordCountFile");
        mMinWordCount = getArgumentInt("minWordCount");
        mWordToCounter = readCounts(mWordCountFile,mMinWordCount);
        mWords = (String[]) 
            mWordToCounter.keySet().toArray(new String[mWordToCounter.size()]);
        Arrays.sort(mWords);

        println("#Word Count File=" + mWordCountFile);
        println("#Total Words=" + mWordToCounter.size());
        println("#Min Word Count=" + mMinWordCount);
        println("#Words with count >= min count=" + mWords.length);
        
        mPriorCount = getArgumentDouble("priorCount");
        mPriorStemAvgLength = getArgumentDouble("priorStemAvgLength");
        mPriorSuffixNonZeroAvgLength 
            = getArgumentDouble("priorSuffixNonZeroAvgLength");
        mPriorSuffixZeroLengthProb 
            = getArgumentDouble("priorSuffixZeroLengthProb");

        println("#Prior Count=" + mPriorCount);
        println("#Prior Stem Avg Length=" + mPriorStemAvgLength);
        println("#Prior Suffix Non-Zero Avg Length="
                           + mPriorSuffixNonZeroAvgLength);
        println("#Prior Suffix Zero Length Prob="
                           + mPriorSuffixZeroLengthProb);

        mMaxCharNGram = getArgumentInt("maxCharNGram");
        mLmInterpolationRatio 
            = getArgumentDouble("lmInterpolationRatio");
        mNumChars = getArgumentInt("numChars");
        mLmCountMultiplier = getArgumentDouble("lmCountMultiplier");

        println("#Max Char N-Gram=" + mMaxCharNGram);
        println("#LM Interpolation Ratio=" 
                           + mLmInterpolationRatio);
        println("#Num Chars=" + mNumChars);
        println("#LM Count Multiplier=" + mLmCountMultiplier);
        
        mEpsilon = getArgumentDouble("epsilon");
        mMaxIterations = getArgumentInt("maxIterations");

        println("#Epsilon Termination Ratio=" + mEpsilon);
        println("#Max EM Iterations=" + mMaxIterations);

        mModelFactory = new CharLmModelFactory(mMaxCharNGram,
                                               mLmInterpolationRatio,
                                               mPriorStemAvgLength,

                                               mMaxCharNGram,
                                               mLmInterpolationRatio,
                                               mPriorSuffixNonZeroAvgLength,
                                               mNumChars,
                                               mLmCountMultiplier);
    }

    public void run() {
        Model model = train(mWords,mModelFactory,mMaxIterations,mEpsilon);
        for (int i = 0; i < mWords.length; ++i) {
            String word = mWords[i];
            String stem = model.stem(word);
            println(stem + "." + word.substring(stem.length()));
            
        }
        println("#Final Model=" + model);
        mResultPrinter.close();
    }

    void println(String msg) {
        System.out.println(msg);
        if (mResultPrinter == null) {
            System.out.println("result printer = null");
            return;
        }
        mResultPrinter.println(msg);
        mResultPrinter.flush();
    }


    public PrintWriter getPrinter(File file) throws IOException {
        FileOutputStream fileOut = null;
        OutputStreamWriter writer = null;
        PrintWriter printer = null;
        try {
            fileOut = new FileOutputStream(file);
            writer = new OutputStreamWriter(fileOut,Strings.UTF8);
            printer = new PrintWriter(writer);
            return printer;
        } catch (IOException e) {
            Streams.closeWriter(printer);
            Streams.closeWriter(writer);
            Streams.closeOutputStream(fileOut);
            throw e;
        }
    }        

    
    ObjectToCounterMap readCounts(File file, int minCount) 
        throws IOException {

        ObjectToCounterMap otc = new ObjectToCounterMap();
        FileInputStream fileIn = null;
        InputStreamReader isReader = null;
        BufferedReader bufReader = null;
        try {
            fileIn = new FileInputStream(file);
            isReader = new InputStreamReader(fileIn,Strings.UTF8);
            bufReader = new BufferedReader(isReader);
            String line;
            while ((line = bufReader.readLine()) != null)
                readCount(line,otc,minCount);
        } finally {
            Streams.closeReader(bufReader);
            Streams.closeReader(isReader);
            Streams.closeInputStream(fileIn);
        }
        return otc;
    }

    void readCount(String line, ObjectToCounterMap otc, int minCount) {
        if (line.length() == 0) return;
        int i = line.indexOf(' ');
        if (i < 0) {
            println("#Ill-formed line=|" + line + "|");
            return;
        }
        String word = line.substring(0,i);
        int count = Integer.parseInt(line.substring(i+1));
        if (count < minCount) return;
        otc.set(word,count);
    }


    Model train(String[] corpus,
                ModelFactory modelFactory,
                int maxIterations,
                double epsilon) {
        mFinished = false;
        mCompleteDataLogLikelihood = Double.NEGATIVE_INFINITY;
        long startTime = System.currentTimeMillis();
        // initial model
        Model model = modelFactory.create();
        
        // top level EM algorithm iteration
        for (int i = 0; (!mFinished) && i < maxIterations; ++i) {
            println("\n#ITERATION " + (i+1) + " ("
                    + Strings.msToString(System.currentTimeMillis()
                                         - startTime) + ")");
            println("#Model Params=" + model.toString());
            test(model,"runs");
            test(model,"running");
            test(model,"runners");
            test(model,"race");
            test(model,"racing");
            test(model,"eat");
            test(model,"eats");
            test(model,"eating");
            test(model,"zoology");
            test(model,"zoological");
            Model compiledModel = model.compile();
            model = em(corpus,compiledModel,modelFactory,epsilon);
        }
        return model;
    }

    void test(Model model, String in) {
        StemScore[] stemScores = model.nBest(in);
        double[] justScores = new double[stemScores.length];
        for (int i = 0; i < stemScores.length; ++i)
            justScores[i] = stemScores[i].score();
        double[] conds = Statistics.normalize(justScores);
        for (int i = 0; i < conds.length; ++i)
            println("#"
                    + stemScores[i].stem()
                    + "."
                    + in.substring(stemScores[i].stem().length())
                    + ":"
                    + conds[i]);
    }

    ObjectToDoubleMap mStemCounter;
    ObjectToDoubleMap mSuffixCounter;
        
    Model em(String[] corpus, 
             Model oldModel,
             ModelFactory modelFactory,
             double epsilon) {

        mStemCounter = new ObjectToDoubleMap();
        mSuffixCounter = new ObjectToDoubleMap();

        Model newModel = modelFactory.create();

        // train new model one word at a time
        double oldCompleteDataLog2Likelihood = 0.0;
        for (int i = 0; i < corpus.length; ++i)
            oldCompleteDataLog2Likelihood 
                += update(corpus[i],oldModel,newModel);



        println("#     Complete Data Log2 Likelihood (Old)="
                + oldCompleteDataLog2Likelihood);
        println("#                               Word Rate="
                + ( oldCompleteDataLog2Likelihood 
                    / corpus.length ));
        println("#                               Char Rate="
                + ( oldCompleteDataLog2Likelihood 
                    / wordLength(corpus)));
        printTops("stem",mStemCounter,10);
        printTops("suffix",mSuffixCounter,250);


        if (withinEpsilon(oldCompleteDataLog2Likelihood,
                          mCompleteDataLogLikelihood,
                          epsilon)) {
            mFinished = true;
        } else {
            mCompleteDataLogLikelihood = oldCompleteDataLog2Likelihood;
        }
        return newModel;
    }

    boolean withinEpsilon(double x, double y, double epsilon) {
        double diff = Math.abs(x-y) / (Math.abs(x) + Math.abs(y));
        println("#     diff=" + diff);
        return diff < epsilon;
    }

    double update(String word, Model oldModel, Model newModel) {
        // expectation step
        StemScore[] stemScores = oldModel.nBest(word);
        double[] jointProbs = new double[stemScores.length];
        boolean allZeros = true;
        for (int i = 0; i < stemScores.length; ++i)
            if ((jointProbs[i] = stemScores[i].score()) > 0)
                allZeros = false;
        if (allZeros) {
            println("#allZeros=" + word);
            return 0.0;
        }

        // maximization step
        double[] condProbs = Statistics.normalize(jointProbs);
        for (int i = 0; i < condProbs.length; ++i) {
            String stem = stemScores[i].stem();
            newModel.train(word,stem,condProbs[i]);
            mStemCounter.increment(stem,condProbs[i]);
            mSuffixCounter.increment(word.substring(stem.length()),
                                     condProbs[i]);
        }

        // return to track complete data log2 likelihood
        double wordProb = com.aliasi.util.Math.sum(jointProbs);
        return com.aliasi.util.Math.log2(wordProb);
    }

    void printTops(String msg, ObjectToDoubleMap counter, int max) {
        println("     Top " + msg);
        Object[] keysByValue = counter.keysOrderedByValue();
        for (int i = 0; i < max && i < keysByValue.length; ++i) {
            long count = Math.round(counter.getValue(keysByValue[i]));
            if (count < 1L) break;
            println("     " + keysByValue[i]
                    + " " + count);
        }
    }

    int wordLength(String[] corpus) {
        int sum = 0;
        for (int i = 0; i < corpus.length; ++i)
            sum += corpus[i].length();
        return sum;
    }


    public static void main(String[] args) {
        try { 
            new Trainer(args).run();
        } catch (Throwable t) {
            System.out.println("Thrown: " + t);
            t.printStackTrace(System.out);
        }
    }


}
