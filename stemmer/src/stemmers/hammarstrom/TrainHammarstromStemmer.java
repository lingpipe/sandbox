package stemmers.hammarstrom;

import util.StringToDouble;

import com.aliasi.corpus.Parser;
import com.aliasi.corpus.TextHandler;

import com.aliasi.corpus.parsers.GigawordTextParser;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Compilable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.IOException;
import java.io.ObjectOutput;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.util.zip.GZIPInputStream;

import org.xml.sax.InputSource;

public class TrainHammarstromStemmer implements Compilable {

    ObjectToCounterMap mTokenCounter;
    ObjectToCounterMap mSuffixCounter;

    double mUnigramPerplexity;

    Set mSuffixSet;


    int mNumSuffixes;

    public TrainHammarstromStemmer(ObjectToCounterMap tokenCounter,
				   int numSuffixes) {
	mTokenCounter = tokenCounter;
	mNumSuffixes = numSuffixes;

	mUnigramPerplexity = computeUnigramPerplexity(tokenCounter);
	mSuffixCounter = collectSuffixes(tokenCounter);
	StringToDouble suffixScores = extractSuffixes(tokenCounter);
	mSuffixSet = prunedSuffixes(suffixScores,numSuffixes);
    }
    
    public void compileTo(ObjectOutput objOut) throws IOException {
	Stemmer stemmer = new Stemmer(mSuffixCounter,mUnigramPerplexity,mTokenCounter.size(),
				      mSuffixSet);
	objOut.writeObject(stemmer);
    }


    public static double computeUnigramPerplexity(ObjectToCounterMap tokenCounter) {
	System.out.println("\nUNIGRAM ENTROPY");
	ObjectToCounterMap charCounts = new ObjectToCounterMap();
	Iterator it = tokenCounter.keySet().iterator();
	long totalCharCount = 0;
	while (it.hasNext()) {
	    String token = it.next().toString();
	    long count = tokenCounter.getCount(token);
	    totalCharCount += token.length() * count;
	    for (int i = 0; i < token.length(); ++i)
		charCounts.increment(new Character(token.charAt(i)),
				     (int) count);
	}
	double unigramEntropy = 0.0;
	Iterator it2 = new TreeSet(charCounts.keySet()).iterator();
	while (it2.hasNext()) {
	    Character c = (Character) it2.next();
	    double count = charCounts.getCount(c);
	    double prob = count
		/ (double) totalCharCount;
	    double log2Prob = com.aliasi.util.Math.log2(prob);
	    System.out.println("     " + c + "  " + " count=" + count
			       + " prob=" + prob
			       + " log2 prob=" + log2Prob);
	    unigramEntropy -= prob * log2Prob;
	}
	return Math.pow(2.0,unigramEntropy);
    }


    
    public static ObjectToCounterMap collectSuffixes(ObjectToCounterMap tokenCounter) {
	ObjectToCounterMap suffixCounter = new ObjectToCounterMap();
	Iterator it = tokenCounter.keySet().iterator();
	while (it.hasNext()) {
	    String token = it.next().toString();
	    long count = tokenCounter.getCount(token);
	    int len = token.length();
	    for (int j = 0; j < len; ++j)
		suffixCounter.increment(token.substring(j),
					(int) count);
	}
	return suffixCounter;
    }



    StringToDouble extractSuffixes(ObjectToCounterMap tokenCounter) {
	StringToDouble suffixScores = new StringToDouble();
	HashSet unpurgedSuffixSet = new HashSet();
	Iterator it = tokenCounter.keySet().iterator();
	while (it.hasNext()) {
	    String token = it.next().toString();
	    double maxValue = Double.NEGATIVE_INFINITY;
	    String bestSuffix = "";
	    for (int i = 0; i <= token.length(); ++i) {
		String suffix = token.substring(i);
		double z_w = z_w(suffix,token);
		suffixScores.increment(suffix,z_w);
		if (z_w > maxValue) {
		    maxValue = z_w;
		    bestSuffix = suffix;
		}
	    }
	    unpurgedSuffixSet.add(bestSuffix);
	}

	Iterator it2 = suffixScores.keySet().iterator();
	while (it2.hasNext()) 
	    if (!unpurgedSuffixSet.contains(it2.next()))
		it2.remove();

	return suffixScores;
    }

    static Set prunedSuffixes(StringToDouble suffixScoreMap, int maxSuffixes) {
	Set entrySet = suffixScoreMap.entrySet();
	Map.Entry[] suffixScores 
	    = (Map.Entry[]) entrySet.toArray(new Map.Entry[0]);
	Arrays.sort(suffixScores,SCORE_COMPARATOR);

	HashSet suffixSet = new HashSet();
	for (int i = 0; i < maxSuffixes && i < suffixScores.length; ++i) {
	    String term = suffixScores[i].getKey().toString();
	    suffixSet.add(term);
	}
	return suffixSet;
    }

    double f_w(String s) {
	return mSuffixCounter.getCount(s);
    }

    double p_expected(String s) {
	return 1.0 / Math.pow(mUnigramPerplexity,s.length());
    }

    double e_w(String s) {
	return  p_expected(s) * (double)mTokenCounter.size();
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


    void segment(String word) {
	System.out.println("\nSEGMENT=" + word);
	double[] f_ws = new double[word.length()];
	double[] e_ws = new double[word.length()];
	double[] fp_ws = new double[word.length()];
	double[] z_ws = new double[word.length()];
	double maxVal = Double.NEGATIVE_INFINITY;
	int maxIndex = -1;
	for (int i = 0; i < word.length(); ++i) {
	    String suffix = word.substring(i);
	    f_ws[i] = f_w(suffix);
	    e_ws[i] = e_w(suffix);
	    fp_ws[i] = fPrime_w(suffix);
	    z_ws[i] = z_w(suffix,word);
	    if (z_ws[i] > maxVal) {
		maxVal = z_ws[i];
		maxIndex = i;
	    }
	}
	System.out.println("char       f(s)         eW(s)    f(s)-eW(s)        z(s,w)");
	System.out.println("------------------------------------------------");
	for (int i = 0; i < word.length(); ++i) {
	    if (i == maxIndex) 
		System.out.println("-----");
	    System.out.println(word.charAt(i)
			       + format(f_ws[i])
			       + format(e_ws[i])
			       + format(fp_ws[i])
			       + format(z_ws[i]));
	}
    }

    String format(double x) {
	try {
	    return "  " + Strings.decimalFormat(x,"#,###.00",12);
	} catch (IllegalArgumentException e) {
	    return "  ????????????";
	}
    }


    static Comparator SCORE_COMPARATOR
	= new Comparator() {
		public int compare(Object o1, Object o2) {
		    Map.Entry entry1 = (Map.Entry) o1;
		    Map.Entry entry2 = (Map.Entry) o2;
		    return -((Double)entry1.getValue()).compareTo((Double)entry2.getValue());
		}
	    };
    

}