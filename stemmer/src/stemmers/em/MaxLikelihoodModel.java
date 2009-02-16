package stemmers.em;

import util.StringToDouble;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

public class MaxLikelihoodModel extends AbstractModel implements Serializable {

    final StringToDouble mStemCounts = new StringToDouble();
    final StringToDouble mSuffixCounts = new StringToDouble();
    double mTotalCount = 0.0;

    public void increment(String word, int beginSuffixIndex, double delta) {
	mTotalCount += delta;
        String stem = word.substring(0,beginSuffixIndex);
        String suffix = word.substring(beginSuffixIndex);
        mStemCounts.increment(stem,delta);
        mSuffixCounts.increment(suffix,delta);
    }

    public double estimate(String word, int beginSuffixIndex) {
        String stem = word.substring(0,beginSuffixIndex);
        String suffix = word.substring(beginSuffixIndex);
        double stemProb = mStemCounts.maxLikelihood(stem);
        double suffixProb = mSuffixCounts.maxLikelihood(suffix);
        return stemProb * suffixProb;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("MODEL COUNTS\n");
        toString(sb,"SUFFIXES",mSuffixCounts,50);
        toString(sb,"STEMS",mStemCounts,50);
        return sb.toString();
    }

    void toString(StringBuffer sb, String msg, 
                  StringToDouble counter, int max) {
        String[] ss = counter.stringsByFrequency();
        int len = Math.min(max,ss.length);
        sb.append(msg + ": TOP " + len + "/" + mTotalCount + " counts\n");
        double last = Double.POSITIVE_INFINITY;
        for (int i = 0; i < len; ++i) {
            double val = counter.getDouble(ss[i]);
            if (val > last) {
                String errorMsg = "last=" + last + " val=" + val;
                System.out.println("BUG: " + errorMsg);
            }
            last = val;
            sb.append("     " + ss[i] 
                      + " = " + val + "\n"); 
        }
    }


}
