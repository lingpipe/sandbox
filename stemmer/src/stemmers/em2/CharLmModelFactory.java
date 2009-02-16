package stemmers.em2;

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramBoundaryLM;

import com.aliasi.stats.PoissonConstant;
import com.aliasi.stats.PoissonDistribution;
import com.aliasi.stats.PoissonEstimator;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

public class CharLmModelFactory implements ModelFactory {

    private final double mSuffixZeroLengthProb = 0.20;
    private final int mStemNGram;
    private final double mStemLambda;
    private final double mStemMeanLength;
    private final int mSuffixNGram;
    private final double mSuffixLambda;
    private final double mSuffixMeanLength;
    private final double mCountMultiplier;
    private final int mNumCharacters;

    public CharLmModelFactory(int stemNGram,
                              double stemLambda,
                              double stemMeanLength,
                              int suffixNGram,
                              double suffixLambda,
                              double suffixMeanLength,
                              int numCharacters,
                              double countMultiplier) {
        mStemNGram = stemNGram;
        mStemLambda = stemLambda;
        mStemMeanLength = stemMeanLength;
        mSuffixNGram = suffixNGram;
        mSuffixLambda = suffixLambda;
        mSuffixMeanLength = suffixMeanLength;
        mNumCharacters = numCharacters;
        mCountMultiplier = countMultiplier;
    }
    
    public Model create() {
        return new CharLmModelEstimator(mSuffixZeroLengthProb);
    }

    private static class CharLmModel implements Model {
        final LanguageModel mStemLm;
        final PoissonDistribution mStemLengthDistribution;
        final LanguageModel mSuffixLm;
        final PoissonDistribution mSuffixLengthDistribution;
        final double mSuffixZeroLengthProb;
        CharLmModel(LanguageModel stemLm,
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
        public Model compile() {
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

    private class CharLmModelEstimator 
        extends CharLmModel
        implements Compilable {
        private CharLmModelEstimator(double suffixZeroLengthProb) {
            super(new NGramBoundaryLM(mStemNGram,
                                      mNumCharacters,
                                      mCountMultiplier*mStemLambda,
                                      ' '),
                  new PoissonEstimator(500000,mStemMeanLength-1),
                  new NGramBoundaryLM(mSuffixNGram,
                                      mNumCharacters,
                                      mCountMultiplier*mSuffixLambda,
                                      ' '),
                  new PoissonEstimator(500000,mSuffixMeanLength-1),
                  suffixZeroLengthProb);
        }
        public void train(String word, String stem, double weight) {
            String suffix = (stem.length() == word.length())
                ? ""
                : word.substring(stem.length());
            int count = (int) Math.round(mCountMultiplier * weight);
            if (count < 1) return;
            ((NGramBoundaryLM)mStemLm).train(stem,count);
            ((NGramBoundaryLM)mSuffixLm).train(suffix,count);
            ((PoissonEstimator)mStemLengthDistribution)
                .train(stem.length()-1,weight);
            if (suffix.length() > 0)
                ((PoissonEstimator)mSuffixLengthDistribution)
                    .train(suffix.length()-1,weight);
        }
        public String report() {
            return "Avg Stem Length=" 
                + (mStemLengthDistribution.mean()+1)
                + " Avg Non-Zero Suffix Length=" 
                + (mSuffixLengthDistribution.mean()+1);
        }
        public String toString() {
            return report();
        }
        public Model compile() {
            try {
                return (Model) AbstractExternalizable.compile(this);
            } catch (Exception e) {
                System.out.println("Could not compile.  Exception=" + e);
                e.printStackTrace(System.out);
                return this;
            }
        }
        public void compileTo(ObjectOutput objOut) throws IOException {
            objOut.writeObject(new Externalizer(this));
        }

    }

        // can't be static because container's not static
        private static class Externalizer extends AbstractExternalizable {
            static final long serialVersionUID = 1623859317152451545L;
            final CharLmModel mModel;
            public Externalizer() { 
                this(null);
            }
            public Externalizer(CharLmModel model) {
                mModel = model;
            }
            public Object read(ObjectInput in) 
                throws ClassNotFoundException, IOException {
                
                LanguageModel stemLm = (LanguageModel) in.readObject();
                double stemMeanLength = in.readDouble();
                LanguageModel suffixLm = (LanguageModel) in.readObject();
                double suffixMeanLength = in.readDouble();
                double suffixZeroLengthProb = in.readDouble();
                return new CharLmModel(stemLm,
                                       new PoissonConstant(stemMeanLength),
                                       suffixLm,
                                       new PoissonConstant(suffixMeanLength),
                                       suffixZeroLengthProb);
            }
            public void writeExternal(ObjectOutput out) throws IOException {
                ((NGramBoundaryLM)mModel.mStemLm).compileTo(out);
                out.writeDouble(mModel.mStemLengthDistribution.mean());
                ((NGramBoundaryLM)mModel.mSuffixLm).compileTo(out);
                out.writeDouble(mModel.mSuffixLengthDistribution.mean());
                out.writeDouble(mModel.mSuffixZeroLengthProb);
            }
        }


}
