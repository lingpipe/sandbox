package stemmers.em;

import com.aliasi.stats.PoissonConstant;
import com.aliasi.stats.PoissonDistribution;
import com.aliasi.stats.Statistics;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import util.StringToDouble;

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

public class TrainEmStemmer implements Compilable {

    final long mStartTime;
    final ObjectToCounterMap mWordCounts;
    final int mMinWordCount;
    final PoissonDistribution mStemLengthDistro;
    final PoissonDistribution mSuffixLengthDistro;
    final double mZeroLengthSuffixProb;
    final int mNumIterations;
    final boolean mViterbi;
    final boolean mIgnoreCount;
    final double mSuffixPerCharProb;
    final double mStemPerCharProb;
    final PrintWriter mPrinter;
    public TrainEmStemmer(ObjectToCounterMap wordCounts,
                          int minWordCount,
                          double meanStemLength,
                          double meanSuffixLength,
                          double zeroSuffixLengthProb,
                          double stemCharProb,
                          double suffixCharProb,
                          int numIterations,
                          File resultsFile) throws IOException {

        FileOutputStream fileOut 
            = new FileOutputStream(resultsFile);
        OutputStreamWriter writer 
            = new OutputStreamWriter(fileOut,Strings.UTF8);
        mPrinter = new PrintWriter(writer);

        // println("#word count file=" + wordCountFile);
        println("#minimum word count=" + minWordCount);
        println("#meanStemLength=" + meanStemLength);
        println("#meanSuffixLength=" + meanSuffixLength);
        println("#zeroSuffxLengthProb=" + zeroSuffixLengthProb);
        println("#stem per char prob=" + stemCharProb);
        println("#suffix per char prob=" + suffixCharProb);
        println("#iterations=" + numIterations);
        println("#results file=" + resultsFile.getCanonicalPath());

        mStartTime = System.currentTimeMillis();
        mWordCounts = wordCounts;
        mMinWordCount = minWordCount;
        mStemLengthDistro = new PoissonConstant(meanStemLength);
        mZeroLengthSuffixProb = zeroSuffixLengthProb;
        mSuffixLengthDistro = new PoissonConstant(meanSuffixLength);
        mSuffixPerCharProb = suffixCharProb;
        mStemPerCharProb = stemCharProb;

        int numWords = wordCounts.keySet().size();
        println("#words in corpus=" + numWords);

        int prunedCount = 0;
        Iterator wordIt1 = wordCounts.keySet().iterator();
        while (wordIt1.hasNext())
            if (wordCounts.getCount(wordIt1.next()) >= mMinWordCount)
                ++prunedCount;
        println("#words with count >= " + minWordCount + " is "
                           + prunedCount);

        ObjectToCounterMap histogram = new ObjectToCounterMap();
        Iterator wordIt2 = wordCounts.keySet().iterator();
        while (wordIt2.hasNext())
            histogram.increment(new Integer(wordCounts.getCount(wordIt2.next())));
        println("#MIN_COUNT  #TOKENS");
        for (int i = 1; i <= 64*1024*1024; ++i) {
            int count = histogram.getCount(new Integer(i));
            if (even(i)) println("#" + i + " " + numWords);
            numWords -= count;
        }
        
        mNumIterations = numIterations;
        mViterbi = false;
        mIgnoreCount = true;
        println("#Viterbi Estimation=" + mViterbi);
        println("#Ignore word counts=" + mIgnoreCount);
    }

    void println(String msg) {
        System.out.println(msg);
        mPrinter.print(msg == null ? "null" : msg);
        mPrinter.print('\n');
        mPrinter.flush();
    }
    
    void close() {
        mPrinter.close();
    }

    static boolean even(int i) {
        if (i == 0) return true;
        while (i > 0) {
            if (i == 1) return true;
            if ((i % 2) != 0) return false;
            i = i/2;
        }
        return false;
    }


    Model createModel() {
        return new CharLmModel();
        // return new MaxLikelihoodModel();
    }

    public void compileTo(ObjectOutput objOut) throws IOException {
        Model model = iterate();
        objOut.writeObject(model);
    }
 

    // creates estimates weighted by 
    Model initialModel() {
        Model model = createModel();

        Iterator it = mWordCounts.keySet().iterator();
        double uniformProb = 1.0/(5.0 * mWordCounts.size());
        while (it.hasNext()) {
            String word = it.next().toString();
            double wordCount = mWordCounts.getCount(word);
            if (wordCount < mMinWordCount) continue;
            if (mIgnoreCount) 
                wordCount = 1;
            double[] splitProbRatios = new double[word.length()+1];
            for (int i = 0; i <= word.length(); ++i) {
                String stem = word.substring(0,i);
                String suffix = word.substring(i);
                double stemPrior = stemPrior(stem.length(), uniformProb);
                double suffixPrior = suffixPrior(suffix.length(), uniformProb);
                splitProbRatios[i] = stemPrior * suffixPrior;
            }
            double[] splitProbs = Statistics.normalize(splitProbRatios);
            for (int i = 0; i < splitProbs.length; ++i)
                model.increment(word,i,wordCount*splitProbs[i]);
        }
        return model;
    }
        
    double suffixPrior(int length, double p) {
        double lengthFactor
            = (length == 0) 
            ? mZeroLengthSuffixProb
            : ( (1.0 - mZeroLengthSuffixProb) 
                * mSuffixLengthDistro.probability(length-1) );
        if (1+1 == 2) return lengthFactor;
        double charEmissionFactor = Math.pow(mSuffixPerCharProb,length);
        double numInstances = mWordCounts.size() * p;
        return Math.pow(lengthFactor * charEmissionFactor,1.0/numInstances);
    }

    double stemPrior(int length, double p) {
        double lengthFactor 
            = (length == 0) ? 0.0 : mStemLengthDistro.probability(length-1);
        if (1+1 == 2) return lengthFactor;
        double charEmissionFactor = Math.pow(mStemPerCharProb,length);
        double numInstances = mWordCounts.size() * p;
        return Math.pow(lengthFactor * charEmissionFactor,1.0/numInstances);
        
    }

    Model iterate() {
        println("#INITIALIZING MODEL");
        Model model = initialModel();
        for (int i = 0; i < mNumIterations; ++i) {
            println("#ITERATION=" + (i+1) 
                    + "  (t=" + Strings.msToString(System.currentTimeMillis() 
                                                   - mStartTime) + ")" );
            model = nextModel(model);
        }
        return model;
    }

    void analyze(Model model) {
        Iterator it = new TreeSet(mWordCounts.keySet()).iterator();
        while (it.hasNext()) {
            String word = it.next().toString();
            int count = mWordCounts.getCount(word);
            if (count < mMinWordCount) continue;
            String stem = model.stem(word);
            String suffix = word.substring(stem.length());
            println(stem + "." + suffix);
        }
    }
   
    Model nextModel(Model lastModel) {
        Model model = createModel();
        double jointEntropy = 0.0;
        double jointBestEntropy = 0.0;
        double conditionalBestEntropy = 0.0;
        Iterator it = new TreeSet(mWordCounts.keySet()).iterator();
        while (it.hasNext()) {
            String word = it.next().toString();
            int wordCount = mWordCounts.getCount(word);
            if (wordCount < mMinWordCount) continue;
            if (mIgnoreCount) wordCount = 1;
            double[] splitProbRatios = new double[word.length()+1];
            splitProbRatios[0] = 0.0;
            double wordProb = 0.0;
            for (int i = 1; i <= word.length(); ++i) {
                double estimate = lastModel.estimate(word,i);
                wordProb += estimate;
                splitProbRatios[i] = estimate
                    * stemPrior(i,estimate)
                    * suffixPrior(word.length()-i,estimate)
                    ;
            }
            int maxIndex = maxIndex(splitProbRatios);
            if (maxIndex == -1) continue;
            
            jointEntropy -= com.aliasi.util.Math.log2(wordProb);
            jointBestEntropy -= com.aliasi.util.Math.log2(splitProbRatios[maxIndex]);

            if (sum(splitProbRatios) <= 0.0) continue;
            double[] splitProbs = Statistics.normalize(splitProbRatios);
            conditionalBestEntropy 
                -= com.aliasi.util.Math.log2(splitProbs[maxIndex]);
            if (mViterbi) {  // Viterbi estimate
                if (maxIndex != -1) {
                    double incr = wordCount * splitProbs[maxIndex];
                    model.increment(word,maxIndex,incr);
                }
            } else {  // full EM
                for (int i = 1; i < splitProbs.length; ++i) {
                    double incr = wordCount * splitProbs[i];
                    model.increment(word,i,incr);
                }
            }
        }       
        printAgreement(lastModel,model);
        double numWords = mWordCounts.size();
        double jointEntropyRate = jointEntropy/numWords;
        println("#    Joint Entropy Rate=" + jointEntropyRate);
        double jointBestEntropyRate = jointBestEntropy/numWords;
        println("#    Joint Best Entropy Rate=" 
                           + jointBestEntropyRate);
        double conditionalEntropyRate =  conditionalBestEntropy/numWords;
        double geometricAverageConfidence 
            = Math.pow(2.0,-conditionalEntropyRate);
        println("#    Conditional Best Entropy Rate="
                           + conditionalEntropyRate);
        println("#    Geometric Average Confidence="
                           + geometricAverageConfidence);
        return model;
    }

    void printAgreement(Model lastModel, Model model) {
        Iterator it = mWordCounts.keySet().iterator();
        int count = 0;
        int mismatchCount = 0;
        while (it.hasNext()) {
            String token = it.next().toString();
            if (mWordCounts.getCount(token) < mMinWordCount) continue;
            String lastStem = lastModel.stem(token);
            String stem = model.stem(token);
            ++count;
            if (lastStem == null || !lastStem.equals(stem))
                ++mismatchCount;
        }
        println("#    Mismatches=" + mismatchCount + "/" + count);
    }

    static double sum(double[] xs) {
        double sum = 0.0;
        for (int i = 0; i < xs.length; ++i)
            sum += xs[i];
        return sum;
    }

    static int maxIndex(double[] xs) {
        double maxVal = 0.0;
        int maxIndex = -1;
        for (int i = 0; i < xs.length; ++i) {
            if (xs[i] > maxVal) {
                maxVal = xs[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }


    // java stemmers.em.TrainEmStemmer 
    //      wordcountFile
    //      min word count
    //      meanStemLength
    //      meanNonZeroSuffixLength
    //      zeroSuffixLengthprob
    //      stemCharEntropyRate
    //      suffixCharEntropyRate
    //      numIterations
    //      outputFile
    public static void main(String[] args) throws IOException {
        int arg = 0;
        File wordCountFile = new File(args[arg++]);
        int minWordCount = Integer.parseInt(args[arg++]);
        double meanStemLength = Double.parseDouble(args[arg++]);
        double meanSuffixLength = Double.parseDouble(args[arg++]);
        double zeroLengthProb = Double.parseDouble(args[arg++]);
        double stemPerCharProb 
            = Math.pow(2.0,-Double.parseDouble(args[arg++]));
        double suffixPerCharProb
            = Math.pow(2.0,-Double.parseDouble(args[arg++]));
        int numIterations = Integer.parseInt(args[arg++]);
        File resultsFile = new File(args[arg++]);

        ObjectToCounterMap wordCounts = readCounts(wordCountFile);
        TrainEmStemmer finder 
            = new TrainEmStemmer(wordCounts,
                                 minWordCount,
                                 meanStemLength,
                                 meanSuffixLength,
                                 zeroLengthProb,
                                 stemPerCharProb,
                                 suffixPerCharProb,
                                 numIterations,
                                 resultsFile);

        Model model = finder.iterate();
        finder.analyze(model);
        finder.close();
        // println(model.toString());
    }

    static ObjectToCounterMap readCounts(File file) throws IOException {
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
                readCount(line,otc);
        } finally {
            Streams.closeReader(bufReader);
            Streams.closeReader(isReader);
            Streams.closeInputStream(fileIn);
        }
        return otc;
    }

    static void readCount(String line, ObjectToCounterMap otc) {
        if (line.length() == 0) return;
        int i = line.indexOf(' ');
        if (i < 0) {
            System.out.println("Ill-formed line=|" + line + "|");
            return;
        }
        String word = line.substring(0,i);
        int count = Integer.parseInt(line.substring(i+1));
        otc.set(word,count);
    }

}

