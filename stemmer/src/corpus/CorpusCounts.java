package corpus;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.LanguageModel;

import com.aliasi.stats.Statistics;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Counter;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This could all be made much faster by rolling the probability
 * estimates into the big outer loop.  And also by using an explicit
 * ordered merge on the intersection step.  Could also cache the
 * intersections of the top levels.
 */

public class CorpusCounts {

    private static final int MIN_WORD_COUNT = 1;      // 5

    private static final int MIN_STEM_LENGTH = 1;     // 1
    private static final int MAX_SUFFIX_LENGTH = 5;   // 4
    private static final int MIN_STEM_COUNT = 2;      // 2
    private static final int MIN_SUFFIX_COUNT = 10;    // 10
    private static final int MAX_FREQ_SUFFIXES_REPORT = 100;

    private static final int NUM_SUFFIXES_FOR_SIMILARITY = 200;
    private static final int MAX_SIMILAR_SUFFIXES_REPORT = 100;
    private static final int MIN_SIMILAR_SUFFIXES_STEM_OVERLAP = 200;

    private static final int MAX_SUBSTRING_LENGTH = MAX_SUFFIX_LENGTH;

    private final ObjectToCounterMap mWordCounter = new ObjectToCounterMap();
    private final ObjectToCounterMap mStemCounter = new ObjectToCounterMap();
    private final ObjectToCounterMap mSuffixCounter = new ObjectToCounterMap();
    private final ObjectToCounterMap mSuffixCounterWeighted
        = new ObjectToCounterMap();
    private final ObjectToSet mSuffixToStemSet = new ObjectToSet();
    private final ObjectToCounterMap mSubstringCounter 
        = new ObjectToCounterMap();
    private final ObjectToCounterMap mSubstringCounterWeighted
        = new ObjectToCounterMap();

    private final HashSet mCharSet = new HashSet();

    public CorpusCounts(File file) throws IOException {
        readWordCounts(file);
        count();
    }


    void countChars(String w) {
	for (int i = 0; i < w.length(); ++i)
	    mCharSet.add(new Character(w.charAt(i)));
    }

    void count() {
        System.out.println("Counting.");
        Iterator it = mWordCounter.keySet().iterator();
        System.out.println("     Min stem length=" 
                           + MIN_STEM_LENGTH);
        System.out.println("     Max suffix length=" 
			   + MAX_SUFFIX_LENGTH);
        while (it.hasNext()) {
            String word = it.next().toString();
	    countChars(word);
            for (int i = MIN_STEM_LENGTH; i <= word.length(); ++i) {
                String stem = word.substring(0,i);
                String suffix = word.substring(i);
                if (suffix.length() > MAX_SUFFIX_LENGTH) continue;
                mStemCounter.increment(stem);
            }
        }

	System.out.println("Characters Found");
	Character[] cs 
	    = (Character[]) 
	    mCharSet.toArray(new Character[mCharSet.size()]);
	Arrays.sort(cs);
	System.out.print("char[] cs = {");
	for (int i = 0; i < cs.length; ++i) {
	    if (i > 0) System.out.print(", ");
	    if (cs[i] < 128) {
		System.out.print(cs[i]);
	    } else {
		String hexString 
		    = Integer.toHexString(cs[i].charValue()).toUpperCase();
		while (hexString.length() < 4)
		    hexString = "0" + hexString;
		System.out.print("\\u" + hexString);
	    }
	}
	System.out.println(" };");

        System.out.println("     Pruning Stems. Min count=" + MIN_STEM_COUNT);
        mStemCounter.prune(MIN_STEM_COUNT);
        
        it = mWordCounter.keySet().iterator();
        System.out.println("     Counting Suffixes"
                           + " Max suffix length=" + MAX_SUFFIX_LENGTH);
        while (it.hasNext()) {
            String word = it.next().toString();
            int count = mWordCounter.getCount(word);
            for (int i = MIN_STEM_LENGTH; i <= word.length(); ++i) {
                String stem = word.substring(0,i);
                String suffix = word.substring(i);
                if (suffix.length() > MAX_SUFFIX_LENGTH) continue;
                mSuffixToStemSet.addMember(suffix,stem);
                mSuffixCounter.increment(suffix);
                mSuffixCounterWeighted.increment(suffix,count);
            }
        }
        System.out.println("     Pruning Suffixes. Min count=" 
                           + MIN_SUFFIX_COUNT);
        mSuffixCounter.prune(MIN_SUFFIX_COUNT);

        System.out.println("     Counting Substrings"
                           + " Max substring length=" + MAX_SUBSTRING_LENGTH);
        it = mWordCounter.keySet().iterator();
        while (it.hasNext()) {
            String word = it.next().toString();
            int count = mWordCounter.getCount(word);
            for (int i = 0; i < word.length(); ++i) {
                for (int j = i+1; 
                     j <= MAX_SUBSTRING_LENGTH && j <= word.length();
                     ++j) {
                    String substring = word.substring(i,j);
                    mSubstringCounter.increment(substring);
                    mSubstringCounterWeighted.increment(substring,count);
                }
            }
        }
    }

    void readWordCounts(File file) throws IOException {

        System.out.println("Reading word counts.");
        System.out.println("     file=" + file.getCanonicalPath());
        System.out.println("     Min word count=" + MIN_WORD_COUNT);

        FileInputStream fileIn = null;
        InputStreamReader isReader = null;
        BufferedReader bufReader = null;
        try {
            fileIn = new FileInputStream(file);
            isReader = new InputStreamReader(fileIn,Strings.UTF8);
            bufReader = new BufferedReader(isReader);
            String line;
            while ((line = bufReader.readLine()) != null)
                readCount(line);
            System.out.println("     unique word count=" 
			       + mWordCounter.size());
	    long totalCount = 0L;
	    Iterator it = mWordCounter.values().iterator();
	    while (it.hasNext()) {
		Counter counter = (Counter) it.next();
		totalCount += counter.value();
	    }
	    System.out.println("     total word count=" 
			       + totalCount);
        } finally {
            Streams.closeReader(bufReader);
            Streams.closeReader(isReader);
            Streams.closeInputStream(fileIn);
        }
    }

    void readCount(String line) {
        if (line.length() == 0) return;
        int i = line.lastIndexOf(' ');
        if (i < 0) {
            System.out.println("#Ill-formed line=|" + line + "|");
            return;
        }
        String word = line.substring(0,i);
        int count = Integer.parseInt(line.substring(i+1));
        if (count < MIN_WORD_COUNT) return;
        mWordCounter.set(word,count);
    }
        
    void zipfPlots() {
        zipfPlot("Raw Words",mWordCounter);
        zipfPlot("Suffixes (weighted by word freq)",mSuffixCounterWeighted);
        zipfPlot("Suffixes (unweighted by word freq)",mSuffixCounter);
        zipfPlot("Substrings (weighted by word freq)",
                 mSubstringCounterWeighted);
        zipfPlot("Substrings (unweighted by word freq)",
                 mSubstringCounterWeighted);
    }

    void zipfPlot(String msg, ObjectToCounterMap counter) {
        System.out.println("\nZipf Plot: " + msg);
	System.out.println("rank, count");
        Object[] keys = counter.keysOrderedByCount();
        for (int i = 1; i < keys.length; i *= 2) {
            Object key = keys[i-1];
            System.out.println(i + " " + counter.getCount(key));
        }
        System.out.println(keys.length 
                           + " " + counter.getCount(keys[keys.length-1]));

	System.out.println("\nInverse Zipf Plot: " + msg);
	System.out.println("count, unique words exceeding count");
	for (int i = 1; ; i *= 2) {
	    int count = wordsExceedingCount(counter,keys,i);
	    System.out.println(i + " " + count);
	    if (count == 0) break;
	}

        System.out.println("\nTop Counts: " + msg);
	System.out.println("word, count");
        for (int i = 0; i < keys.length && i < 100; ++i)
            System.out.println(keys[i].toString()
                               + " " + counter.getCount(keys[i]));
    }

    int wordsExceedingCount(ObjectToCounterMap counter, 
			    Object[] keysOrderedByCount, 
			    int count) {
	// could do binary search here
	for (int i = 0; i < keysOrderedByCount.length; ++i)
	    if (counter.getCount(keysOrderedByCount[i]) < count)
		return i;
	return keysOrderedByCount.length;
    }

    void reportSimilarSuffixes() {
        int numSuffixes = NUM_SUFFIXES_FOR_SIMILARITY;

        System.out.println("\nExtracting Similar Suffixes");
        System.out.println("     topSuffixes=" + numSuffixes);
        System.out.println("     min shared stems=" + MIN_SIMILAR_SUFFIXES_STEM_OVERLAP); 
        double numStems = mStemCounter.keySet().size();
        Object[] keys = mSuffixCounter.keysOrderedByCount();
        int numTopSuffixes = Math.min(numSuffixes,keys.length);
        String[] topSuffixes = new String[numTopSuffixes];
        for (int i = 0; i < numTopSuffixes; ++i)
            topSuffixes[i] = keys[i].toString();
        
        BoundedPriorityQueue[] queues = new BoundedPriorityQueue[32];
        for (int i = 0; i < queues.length; ++i)
            queues[i] = new BoundedPriorityQueue(ScoredObject.SCORE_COMPARATOR,
                                                 1000*MAX_SIMILAR_SUFFIXES_REPORT);

        String[] suffixes = new String[16];
        for (int i = 0; i < numSuffixes; ++i) {
            suffixes[0] = topSuffixes[i];
            System.out.println("suffix=|" + suffixes[0] + "|");
            Set set0 = mSuffixToStemSet.getSet(suffixes[0]);
            Set setAll0 = set0;
            double pIndy0 = setAll0.size()/numStems;
            for (int j = i + 1; j < numSuffixes; ++j) {
                suffixes[1] = topSuffixes[j];
                Set set1 = mSuffixToStemSet.getSet(suffixes[1]);
                Set setAll1 = intersect(setAll0,set1);
                double pIndy1 = pIndy0 * (set1.size()/numStems);
                if (!add(queues[1],suffixes,2,pIndy1,setAll1,numStems)) continue;
                for (int k = j + 1; k < numSuffixes; ++k) {
                    suffixes[2] = topSuffixes[k];
                    Set set2 = mSuffixToStemSet.getSet(suffixes[2]);
                    Set setAll2 = intersect(setAll1,set2);
                    double pIndy2 = pIndy1 * (set2.size()/numStems);
                    if (!add(queues[2],suffixes,3,pIndy2,setAll2,numStems)) continue;
                    for (int m = k + 1; m < numSuffixes; ++m) {
                        suffixes[3] = topSuffixes[m];
                        Set set3 = mSuffixToStemSet.getSet(suffixes[3]);
                        Set setAll3 = intersect(setAll2,set3);
                        double pIndy3 = pIndy2 * (set3.size()/numStems);
                        if (!add(queues[3],suffixes,4,pIndy3,setAll3,numStems)) continue;
                        for (int n = m + 1; n < numSuffixes; ++n) {
                            suffixes[4] = topSuffixes[n];
                            Set set4 = mSuffixToStemSet.getSet(suffixes[4]);
                            Set setAll4 = intersect(setAll3,set4);
                            double pIndy4 = pIndy3 * (set4.size()/numStems);
                            if (!add(queues[4],suffixes,5,pIndy4,setAll4,numStems)) continue;
                            for (int p = n+1; p < numSuffixes; ++p) {
                                suffixes[5] = topSuffixes[p];
                                Set set5 = mSuffixToStemSet.getSet(suffixes[5]);
                                Set setAll5 = intersect(setAll4,set5);
                                double pIndy5 = pIndy4 * (set5.size()/numStems);
                                if (!(add(queues[5],suffixes,6,pIndy5,setAll5,numStems))) continue;
                                for (int q = p+1; q < numSuffixes; ++q) {
                                    suffixes[6] = topSuffixes[q];
                                    Set set6 = mSuffixToStemSet.getSet(suffixes[6]);
                                    Set setAll6 = intersect(setAll5,set6);
                                    double pIndy6 = pIndy5 * (set6.size()/numStems);
                                    if (!(add(queues[6],suffixes,7,pIndy6,setAll6,numStems))) continue;
                                    for (int r = q + 1; r < numSuffixes; ++r) {
                                        suffixes[7] = topSuffixes[r];
                                        Set set7 = mSuffixToStemSet.getSet(suffixes[7]);
                                        Set setAll7 = intersect(setAll6,set7);
                                        double pIndy7 = pIndy6 * (set7.size()/numStems);
                                        if (!(add(queues[7],suffixes,8,pIndy7,setAll7,numStems))) continue;
                                        for (int s = r + 1; s < numSuffixes; ++s) {
                                            suffixes[8] = topSuffixes[s];
                                            Set set8 = mSuffixToStemSet.getSet(suffixes[8]);
                                            Set setAll8 = intersect(setAll7,set8);
                                            double pIndy8 = pIndy7 * (set8.size()/numStems);
                                            if (!(add(queues[8],suffixes,9,pIndy8,setAll8,numStems))) continue;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < queues.length; ++i) {
            BoundedPriorityQueue queue = queues[i];
            int count = 0;
            while (count < MAX_SIMILAR_SUFFIXES_REPORT) {
                ScoredObject so = (ScoredObject) queue.pop();
                if (so == null) break;
                String[] toks = (String[]) so.getObject();
                double score = so.score();
                if (!sharedInitialChar(toks))
                    System.out.println((++count)
                                       + " " + Strings.concatenate(toks,", ") 
                                       + " " + score);
            }
        }
    }

    static final char EOS = '\uFFFF';
    static final String EOS_STR = new String(new char[] { EOS });

    // z score of found vs. expected (found=p(word #), expected=p(word)*p(#)
    public void significantSuffixes(boolean weighted) throws Exception {
	System.out.println("\nSignificant Suffixes.");
	System.out.println("     type=" + (weighted ? "" : "un") + "weighted");
	
	NGramProcessLM lm = new NGramProcessLM(MAX_SUFFIX_LENGTH);
	
	int numWords = 0;
	Iterator it = mWordCounter.keySet().iterator();
	while (it.hasNext()) {
	    String word = it.next().toString();
	    int count = weighted ? mWordCounter.getCount(word) : 1;
	    numWords += count;
	    lm.train(word + EOS,count);
	}

	LanguageModel lmCompiled 
	    = (LanguageModel) AbstractExternalizable.compile(lm);

	BoundedPriorityQueue queue 
	    = new BoundedPriorityQueue(Scored.SCORE_COMPARATOR,500);
	
	Iterator it2 = mSuffixCounterWeighted.keySet().iterator();
	while (it2.hasNext()) {
	    String suffix = it2.next().toString();
	    int count = mSuffixCounterWeighted.getCount(suffix);
	    if (count < MIN_SUFFIX_COUNT) continue;
	    double log2P = lmCompiled.log2Estimate(suffix + EOS);
	    double log2PExp
		= lmCompiled.log2Estimate(suffix)
		+ lmCompiled.log2Estimate(EOS_STR);
	    double p = Math.pow(2.0,log2P);
	    double pExp = Math.pow(2.0,log2PExp);
	    double z 
		= Math.sqrt(numWords / (pExp * (1.0 - pExp)))
		* (p - pExp);
	    ScoredObject zScoredSuffix = new ScoredObject(suffix,z);
	    queue.add(zScoredSuffix);
	}
	
	Object scoredSuffix = null;
	while ((scoredSuffix = queue.pop()) != null)
	    System.out.println(scoredSuffix);
    }




    public static void main(String[] args) throws Exception {
	for (int i = 0; i < args.length; ++i) {
	    File corpusFile = new File(args[i]);
	    System.out.println("\n================================================================================");
	    System.out.println("Input Corpus=" + corpusFile);
	    CorpusCounts counts = new CorpusCounts(corpusFile);
	    counts.zipfPlots();
	    counts.reportSimilarSuffixes();
	    counts.significantSuffixes(false); // unweighted
	    counts.significantSuffixes(true);  // weighted by count
	}
	    System.out.println("================================================================================");
    }


    static boolean add(BoundedPriorityQueue queue,
                       String[] suffixes, int len, double pIndy, 
                       Set setAll, double numStems) {
        if (setAll.size() < MIN_SIMILAR_SUFFIXES_STEM_OVERLAP) 
            return false;
        double pAll = setAll.size() / numStems;
        double score 
            = Math.sqrt(((pAll - pIndy)
                         * ( numStems * (pAll-pIndy)))
                        / pIndy);
        String[] suffixesCopy = new String[len];
        for (int i = 0; i < len; ++i)
            suffixesCopy[i] = suffixes[i];
        queue.add(new ScoredObject(suffixesCopy,score));
        return true;
    }
        
    public static boolean sharedInitialChar(String[] toks) {
        if (toks.length == 0) return false;
        if (toks[0].length() == 0) return false;
        char c = toks[0].charAt(0);
        for (int i = 1; i < toks.length; ++i) {
            String tok = toks[i];
            if (tok.length() == 0) return false;
            if (c != tok.charAt(0)) return false;
        }
        return true;
    }


    static void add(BoundedPriorityQueue queue, String[] suffixes, int len,
                    double score) {
        String[] suffixesCopy = new String[len];
        for (int i = 0; i < len; ++i)
            suffixesCopy[i] = suffixes[i];
        queue.add(new ScoredObject(suffixesCopy,score));
    }

    static Set intersect(Set s1, Set s2) {
        if (s1.size() < s2.size()) 
            return intersect2(s1,s2);
        else
            return intersect2(s2,s1);
    }
    
    static Set intersect2(Set s1, Set s2) {
        HashSet result = new HashSet();
        Iterator it = s1.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (s2.contains(next))
                result.add(next);
        }
        return result;
    }

    static double scoreChiSquare(String suffix1, String suffix2,
                                 ObjectToSet mSuffixToStemSet,
                                 int numStems) {
        Set s1 = mSuffixToStemSet.getSet(suffix1);
        Set s2 = mSuffixToStemSet.getSet(suffix2);
        HashSet both = new HashSet(s1);
        both.addAll(s2);
        int bothCount = both.size();

        if (bothCount < 100) return Double.NEGATIVE_INFINITY;

        HashSet only1 = new HashSet(s1);
        only1.removeAll(s2);
        int only1Count = only1.size();

        HashSet only2 = new HashSet(s2);
        only2.removeAll(s1);
        int only2Count = only2.size();
        
        int neitherCount = numStems - only1Count - only2Count - bothCount;
        return chiSquare(bothCount,only1Count,only2Count,neitherCount);
    }

    static double chiSquare(double both, double only1, double only2,
                            double neither) {
        return Statistics.chiSquaredIndependence(both,only1,only2,neither);
    }


    
}

