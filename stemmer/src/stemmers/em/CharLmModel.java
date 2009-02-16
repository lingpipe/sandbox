package stemmers.em;

import com.aliasi.lm.NGramBoundaryLM;

public class CharLmModel extends AbstractModel {

    private final NGramBoundaryLM mStemLM;
    private final NGramBoundaryLM mSuffixLM;
    
    private static final int STEM_NGRAM_LENGTH = 8;
    private static final int SUFFIX_NGRAM_LENGTH = 4;
    private static final int NUM_CHARS = 26;
    private static final char BOUNDARY_CHAR = ' '; 

    private static final double COUNT_MULTIPLIER = 100;

    public CharLmModel() {
        mStemLM = new NGramBoundaryLM(STEM_NGRAM_LENGTH,NUM_CHARS,
                                      COUNT_MULTIPLIER*STEM_NGRAM_LENGTH,
                                      BOUNDARY_CHAR);
        mSuffixLM = new NGramBoundaryLM(SUFFIX_NGRAM_LENGTH,NUM_CHARS,
                                        COUNT_MULTIPLIER*SUFFIX_NGRAM_LENGTH,
                                        BOUNDARY_CHAR);
    }
    
    public void increment(String word, int beginSuffixIndex, double delta) {
        int count = (int) java.lang.Math.round(COUNT_MULTIPLIER * delta);
        if (count == 0) return;
        String stem = word.substring(0,beginSuffixIndex);
        String suffix = word.substring(beginSuffixIndex);
        for (int i = 0; i < count; ++i) {
            mStemLM.train(stem);
            mSuffixLM.train(suffix);
        }
    }

    public double estimate(String word, int beginSuffixIndex) {
        String stem = word.substring(0,beginSuffixIndex);
        String suffix = word.substring(beginSuffixIndex);
        double stemProb 
            = java.lang.Math.pow(2.0,mStemLM.log2Estimate(stem));
        double suffixProb 
            = java.lang.Math.pow(2.0,mSuffixLM.log2Estimate(suffix));
        return stemProb * suffixProb;
    }

    public String toString() {
        return "CharLmModel does not do a final report on prefix/suffix prob yet.";
    }


}
