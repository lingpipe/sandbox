package stemmers.hammarstrom;

import com.aliasi.util.ObjectToCounterMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import java.io.Serializable;

public class Stemmer implements Serializable {
    HashMap mSuffixToCount;
    double mUnigramPerplexity;
    long mNumTokens;
    Set mSuffixSet;
    public Stemmer() { }
    public Stemmer(ObjectToCounterMap suffixCounter,
		   double unigramPerplexity,
		   long numTokens,
		   Set suffixSet) {
	mSuffixToCount = new HashMap();
	Iterator it = suffixCounter.keySet().iterator();
	while (it.hasNext()) {
	    Object suffix = it.next();
	    double count = suffixCounter.getCount(suffix);
	    mSuffixToCount.put(suffix,new Double(count));
	}
	mUnigramPerplexity = unigramPerplexity;
	mNumTokens = numTokens;
	mSuffixSet = suffixSet;
    }
    public String stem(String word) {
	double maxVal = Double.NEGATIVE_INFINITY;
	int maxIndex = -1;
	for (int i = 0; i < word.length(); ++i) {
	    String suffix = word.substring(i);
	    if (!mSuffixSet.contains(suffix)) continue;
	    double f_ws = f_w(suffix);
	    double e_ws = e_w(suffix);
	    double fp_ws = fPrime_w(suffix);
	    double z_ws = z_w(suffix,word);
	    if (z_ws > maxVal) {
		maxVal = z_ws;
		maxIndex = i;
	    }
	}
	return maxIndex > 0
	    ? word.substring(0,maxIndex)
	    : word;
    }
    double f_w(String s) {
	return ((Double) mSuffixToCount.get(s)).doubleValue();
    }

    double p_expected(String s) {
	return 1.0 / Math.pow(mUnigramPerplexity,s.length());
    }

    double e_w(String s) {
	return  p_expected(s) * mNumTokens;
    }

    double fPrime_w(String s) {
	double fp_w = f_w(s) - e_w(s);
	return fp_w;
    }

    double z_w(String suffix, String word) {
	if (!word.endsWith(suffix)) return 0.0;
	if (word.length() == suffix.length()) return 0.0;
	    
	String suffixPlus1 = word.substring(word.length()-suffix.length()-1);
	double fp_w = fPrime_w(suffix);
	double fp_w2 = fPrime_w(suffixPlus1);
	return (fp_w - fp_w2) / Math.abs(fp_w2); 
    }
}
    
