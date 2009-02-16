package stemmers.em2;

import com.aliasi.util.Scored;

public class StemScore implements Scored {

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
