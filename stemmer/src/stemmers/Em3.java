package stemmers;

import com.aliasi.lm.*;

import com.aliasi.stats.*;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Scored;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.*;
import java.util.*;

public class Em3 {

    static File sResultFile = new File("results/results.txt");
    static File sWordCountFile = new File("data/gigawords-nyt.txt");
    
    static int sMinWordCount = 32;

    static double sPriorCount = 2;
    static double sPriorStemAvgLength = 4;
    static double sPriorSuffixAvgLength = 2.5;
    static double sPriorSuffixZeroLengthProb = 0.20;

    static int sMaxCharNGram = 8;
    static int sLmInterpolationRatio = 8;
    static int sNumChars = 26;
    static double sCountMultiplier = 100.0;

    static double sEpsilon = 0.00001;
    static int sMaxIterations = 128;
    
    static ObjectToCounterMap sWordCounts;
    static String[] sWords;

    static long sStartTime = System.currentTimeMillis();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        printParams();
        readWordCounts();
        Model model = em();
        stemAll(model);
    }

    static void readWordCounts() throws IOException {
        sWordCounts = new ObjectToCounterMap();
        FileInputStream fileIn = new FileInputStream(sWordCountFile);
        InputStreamReader isReader 
            = new InputStreamReader(fileIn,Strings.UTF8);
        BufferedReader bufReader = new BufferedReader(isReader);
        String line;
        while ((line = bufReader.readLine()) != null)
            readCount(line);
        Streams.closeReader(bufReader);
        sWords = (String[]) 
            sWordCounts.keySet().toArray(new String[sWordCounts.size()]);
        Arrays.sort(sWords);
    }

    static void readCount(String line) {
        if (line.length() == 0) return;
        int i = line.indexOf(' ');
        if (i < 0) {
            print("#Ill-formed line=|" + line + "|");
            return;
        }
        String word = line.substring(0,i);
        int count = Integer.parseInt(line.substring(i+1));
        if (count < sMinWordCount) return;
        sWordCounts.set(word,count);
    }

    static void stemAll(Model model) {
        print("\nFinal Analyses");
        for (int i = 0; i < sWords.length; ++i) {
            String word = sWords[i];
            String stem = model.stem(word);
            print(stem + "." + word.substring(stem.length()));
        }
    }

    static Model em() throws IOException, ClassNotFoundException {
        Model oldModel = new CharLmModel();
        double lastTotalProb = Double.NEGATIVE_INFINITY;
        for (int iteration = 0; iteration < sMaxIterations; ++iteration) {
            print("\nEM Iteration " + iteration);
            Model newModel = new CharLmModel();
            double totalProb = 0.0;
            for (int i = 0; i < sWords.length; ++i)
                totalProb += update(sWords[i],oldModel,newModel);
            oldModel = newModel.compile();
            print("     total log Prob=" + totalProb);
            if (relativeDifference(totalProb,lastTotalProb) < sEpsilon) {
                print("     within epsilon=" + sEpsilon);
                break;
            }
            lastTotalProb = totalProb;
        }
        return oldModel;
    }
    

    static double update(String word, Model oldModel, Model newModel) {
        // expectation step
        StemScore[] stemScores = oldModel.nBest(word);
        double[] jointProbs = new double[stemScores.length];
        boolean allZeros = true;
        for (int i = 0; i < stemScores.length; ++i)
            if ((jointProbs[i] = stemScores[i].score()) > 0)
                allZeros = false;
        if (allZeros) {
            print("#allZeros=" + word);
            return 0.0;
        }

        // maximization step
        double[] condProbs = Statistics.normalize(jointProbs);
        for (int i = 0; i < condProbs.length; ++i) {
            String stem = stemScores[i].stem();
            newModel.train(word,stem,condProbs[i]);
            // mStemCounter.increment(stem,condProbs[i]);
            // mSuffixCounter.increment(word.substring(stem.length()),
            // condProbs[i]);
        }

        // return to track complete data log2 likelihood
        double wordProb = com.aliasi.util.Math.sum(jointProbs);
        return com.aliasi.util.Math.log2(wordProb);
    }

    static double relativeDifference(double x, double y) {
        return 2.0 * Math.abs(x-y) / (Math.abs(x) + Math.abs(y));
    }

    static void printParams() {
        print("UNSUPERVISED MORPHOLOGY INDUCTION WITH EM");
        print("\nResults");
        print("     file=" + sResultFile);
        print("\nWord Counts");
        print("     file=" + sWordCountFile);
        print("     min word count=" + sMinWordCount);
        print("\nPriors");
        print("     word count=" + sPriorCount);
        print("     stem avg length=" + sPriorStemAvgLength);
        print("     suffix avg length=" + sPriorSuffixAvgLength);
        print("     suffix zero prob=" + sPriorSuffixZeroLengthProb);
        print("\nChar LMs");
        print("     max ngram=" + sMaxCharNGram);
        print("     num chars=" + sNumChars);
        print("     interpolation ratio=" + sLmInterpolationRatio);
        print("\nEM");
        print("     max iterations=" + sMaxIterations);
        print("     min epsilon=" + sEpsilon);
    }

    static void print(String msg) {
        System.out.println(msg);
    }

    static String elapsedTime() {
        return Strings.msToString(System.currentTimeMillis() - sStartTime);
    }


    static class Model {
        final LanguageModel mStemLm;
        final PoissonDistribution mStemLengthDistribution;
        final LanguageModel mSuffixLm;
        final PoissonDistribution mSuffixLengthDistribution;
        final double mSuffixZeroLengthProb;
        Model(LanguageModel stemLm,
              PoissonDistribution stemLengthDistribution,
              LanguageModel suffixLm,
              PoissonDistribution suffixLengthDistribution,
              double suffixZeroLengthProb) {
            mStemLm = stemLm;
            mStemLengthDistribution = stemLengthDistribution;
            mSuffixLm = suffixLm;
            mSuffixLengthDistribution = suffixLengthDistribution;
            mSuffixZeroLengthProb = suffixZeroLengthProb;
        }
        public String stem(String word) {
            return firstBest(word).stem();
        }
        public StemScore firstBest(String word) {
            double max = Double.NEGATIVE_INFINITY;
            int index = word.length(); // default to whole word
            for (int i = 1; i <= word.length(); ++i) {
                double estimate = estimate(word,i);
                if (estimate > max) {
                    max = estimate;
                    index = i;
                }
            }
            return new StemScore(word.substring(0,index),max);
        }
        public Model compile() throws IOException, ClassNotFoundException {
            return this;
        }
        public StemScore[] nBest(String word) {
            StemScore[] nBest = new StemScore[word.length()];
            for (int i = 0; i < word.length(); ++i)
                nBest[i] = new StemScore(word.substring(0,i+1),
                                         estimate(word,i+1));
            return nBest;
        }
        private double estimate(String word, int i) {
            if (i == 0) return 0.0; // stem must be non-zero length
            if (word.length() == 0) 
                throw new IllegalArgumentException("word empty");
            String stem = word.substring(0,i);
            String suffix = word.substring(i);
            double stemProb 
                = mStemLengthDistribution.probability(stem.length()-1)
                * Math.pow(2.0,mStemLm.log2Estimate(stem));
            double suffixProb
                =  ( suffix.length() == 0
                     ? mSuffixZeroLengthProb
                     : ( (1 - mSuffixZeroLengthProb)
                         * mSuffixLengthDistribution.probability(suffix.length()-1) ) )
                * Math.pow(2.0,mSuffixLm.log2Estimate(suffix));
            return stemProb * suffixProb;
        }
        public void train(String word, String stem, double weight) {
            throw new UnsupportedOperationException("");
        }
        
    }

    static class CharLmModel extends Model {

        CharLmModel() {
            super(new NGramBoundaryLM(sMaxCharNGram,
                                      sNumChars,
                                      sCountMultiplier * sLmInterpolationRatio,
                                      ' '),
                  new PoissonEstimator(500000,sPriorStemAvgLength-1),
                  new NGramBoundaryLM(sMaxCharNGram,
                                      sNumChars,
                                      sCountMultiplier * sLmInterpolationRatio,
                                      ' '),
                  new PoissonEstimator(500000,sPriorSuffixAvgLength-1),
                  sPriorSuffixZeroLengthProb);
        }
        public void train(String word, String stem, double weight) {
            String suffix = (stem.length() == word.length())
                ? ""
                : word.substring(stem.length());
            int count = (int) Math.round(sCountMultiplier * weight);
            if (count < 1) return;
            ((NGramBoundaryLM)mStemLm).train(stem,count);
            ((NGramBoundaryLM)mSuffixLm).train(suffix,count);
            ((PoissonEstimator)mStemLengthDistribution)
                .train(stem.length()-1,weight);
            if (suffix.length() > 0)
                ((PoissonEstimator)mSuffixLengthDistribution)
                    .train(suffix.length()-1,weight);
        }
        public Model compile() throws IOException, ClassNotFoundException {
            LanguageModel stemLm 
                = (LanguageModel) AbstractExternalizable.compile((NGramBoundaryLM)mStemLm);
            double stemMeanLength = mStemLengthDistribution.mean();
            LanguageModel suffixLm 
                = (LanguageModel) AbstractExternalizable.compile((NGramBoundaryLM)mSuffixLm);
            double suffixMeanLength = mSuffixLengthDistribution.mean();
            double suffixZeroLengthProb = mSuffixZeroLengthProb;
            return new Model(stemLm,
                             new PoissonConstant(stemMeanLength),
                             suffixLm,
                             new PoissonConstant(suffixMeanLength),
                             suffixZeroLengthProb);
        }
    }


    static class StemScore implements Scored {
        private final String mStem;
        private final double mScore;
        public StemScore(String stem, double score) {
            mStem = stem;
            mScore = score;
        }
        public String stem() {
            return mStem;
        }
        public double score() {
            return mScore;
        }
    }


}
    
