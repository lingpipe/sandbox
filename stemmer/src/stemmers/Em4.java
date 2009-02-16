package stemmers;

import com.aliasi.lm.*;

import com.aliasi.stats.*;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Exceptions;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Scored;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.*;
import java.util.*;

public class Em4 {


    static File sResultFile;
    static File sModelFile;

    static File sWordCountFile;


    static int sMinWordCount = 2;

    static double sPriorCount = 500000;
    static double sPriorStemAvgLength = 4;
    static double sPriorSuffixAvgLength = 2.5;
    static double sPriorSuffixZeroLengthProb = 0.25;

    static int sMaxCharNGram = 8;
    static double sLmInterpolationRatio = 1.0;
    static int sNumChars = 26;
    static double sCountMultiplier = 250.0;
    static int sCharNGramPruneThreshold = 1;

    static double sEpsilon = 0.001;
    static int sMaxIterations = 32;

    static double sBoundaryMatchBoost = 0.1;
    static double sBoundarySmoothing = 0.01;
    static double sBoundaryTotalSmoothing; // see where sValidChars computed
    
    // 1 match boost, smoothing * 1 delete, |chars| substs, |chars| adds
    static int sBoundaryPruneThreshold = 5;


    static char[] sValidChars;
    
    static ObjectToCounterMap sWordCounts;
    static String[] sWords;

    static long sStartTime = System.currentTimeMillis();

    static HashSet<String> sPossibleRoots;

    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

	try {
        for (int i = 0; i < args.length; ++i) {
	    System.out.println("\n================================================================================");
	    sWordCountFile = new File(args[i]);
	    String corpusName = sWordCountFile.getName();
	    sModelFile = new File("models/" + corpusName + ".Stemmer");
	    sResultFile = new File("results/" + corpusName + ".em4-results");
	    printParams();
	    readWordCounts();
	    Model model = em();
	    stemAll(model);
	}
	} catch (Throwable t) {
	    System.out.println("Main threw t=" + t);
	    t.printStackTrace(System.out);
	    System.err.println("Main threw t=" + t);
	    t.printStackTrace(System.err);
	}
    }

    static void readWordCounts() throws IOException {
        System.out.println("\nReading Word Counts");
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
        System.out.println("     #words=" + sWords.length);

        sPossibleRoots = new HashSet<String>(300000);
        for (int i = 0; i < sWords.length; ++i) {
            String word = sWords[i];
            for (int k = 1; k <= word.length(); ++k)
                sPossibleRoots.add(word.substring(0,k));
        }
        System.out.println("     #possible roots=" + sPossibleRoots.size());

        HashSet<Character> charSet = new HashSet<Character>();
        for (int i = 0; i < sWords.length; ++i) {
            String word = sWords[i];
            for (int k = 0; k < word.length(); ++k)
                charSet.add(new Character(word.charAt(k)));
        }

        char[] foundChars = new char[charSet.size()];
        Iterator<Character> it = charSet.iterator();
	for (int i = 0; it.hasNext(); ++i)
            foundChars[i] = it.next().charValue();
        Arrays.sort(foundChars);
	sValidChars = foundChars;
	sBoundaryTotalSmoothing
	    = sBoundaryMatchBoost 
	    + sBoundarySmoothing * (1 + 2 * sValidChars.length);

        print("     found chars=" + new String(sValidChars));
    }

    static void readCount(String line) {
        if (line.length() == 0) return;
        int i = line.lastIndexOf(' ');
        if (i < 0) {
            print("#Ill-formed line=|" + line + "|");
            return;
        }
        String word = line.substring(0,i);
	if (invalid(word)) return;
        int count = Integer.parseInt(line.substring(i+1));
        if (count < sMinWordCount) return;
        sWordCounts.set(word,count);
    }

    static boolean invalid(String word) {
	return word.indexOf(LM_BOUNDARY_CHAR) >= 0;
    }

    static void stemAll(Model model) {
        print("\nFinal Analyses");
        for (int i = 0; i < sWords.length; ++i) {
            String word = sWords[i];
	    if (word.length() == 0) continue;
            StemScore stemScore = model.firstBest(word);
            print(stemScore.toString());
        }
    }

    static Model em() throws IOException, ClassNotFoundException {
        Model oldModel = (new CharLmModel()).compile();
        double lastTotalProb = Double.NEGATIVE_INFINITY;
        for (int iteration = 0; iteration < sMaxIterations; ++iteration) {
            print("\nEM Iteration " + iteration);
            CharLmModel newModel = new CharLmModel();
            double totalProb = 0.0;
            for (int i = 0; i < sWords.length; ++i) {
                if (i % 10000 == 0) print("     #words so far=" + i);
                totalProb += update(sWords[i],oldModel,newModel);
            }
            oldModel = newModel.compile();
            print("     total log Prob=" + totalProb);
            print("     elapsed time=" + elapsedTime());
            if (relativeDifference(totalProb,lastTotalProb) < sEpsilon) {
                print("     within epsilon=" + sEpsilon);

                print("\nWriting Model.");
                AbstractExternalizable.compileTo(newModel,sModelFile);

                break;
            }
            lastTotalProb = totalProb;
        }
        return oldModel;
    }
    

    static double update(String word, Model oldModel, CharLmModel newModel) {
        // expectation step
        ArrayList<StemScore> stemScores = oldModel.nBest(word);
        double[] jointProbs = new double[stemScores.size()];
        boolean allZeros = true;
        for (int i = 0; i < jointProbs.length; ++i) {
            jointProbs[i] = stemScores.get(i).score();
            if (jointProbs[i] > 0)
                allZeros = false;
        }
        if (allZeros) {
            print("#allZeros=" + word);
            return 0.0;
        }

        // maximization step
        double[] condProbs = Statistics.normalize(jointProbs);
        for (int i = 0; i < condProbs.length; ++i) {
            StemScore stemScore = stemScores.get(i);
            String stem = stemScore.stem();
            String boundary = stemScore.boundary();
            String suffix = stemScore.suffix();
            newModel.train(stem,boundary,suffix,condProbs[i]);
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

    static char lastChar(String s) {
        return s.charAt(s.length()-1);
    }


    static class Model {
        final LanguageModel mStemLm;
        final PoissonDistribution mStemLengthDistribution;
        final LanguageModel mSuffixLm;
        final PoissonDistribution mSuffixLengthDistribution;
        final double mSuffixZeroLengthProb;
        final ObjectToDoubleMap mContextCounts;
        final ObjectToDoubleMap mBoundaryCounts;
        Model(LanguageModel stemLm,
              PoissonDistribution stemLengthDistribution,
              LanguageModel suffixLm,
              PoissonDistribution suffixLengthDistribution,
              double suffixZeroLengthProb,
              ObjectToDoubleMap contextCounts,
              ObjectToDoubleMap boundaryCounts) {
            mStemLm = stemLm;
            mStemLengthDistribution = stemLengthDistribution;
            mSuffixLm = suffixLm;
            mSuffixLengthDistribution = suffixLengthDistribution;
            mSuffixZeroLengthProb = suffixZeroLengthProb;
            mContextCounts = contextCounts;
            mBoundaryCounts = boundaryCounts;
        }
        StemScore firstBest(String word) {
            ArrayList<StemScore> stemScores = nBest(word);
            StemScore best = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            for (StemScore stemScore : stemScores) {
                double score = stemScore.score();
                if (Double.isInfinite(score)) continue;
                if (score > bestScore) {
                    best = stemScore;
                    bestScore = score;
                }
            }
            return best;
        }
        boolean possibleRoot(String stem) {
            return sPossibleRoots == null
                || sPossibleRoots.contains(stem);
        }

        ArrayList<StemScore> nBest(String word) {
            ArrayList<StemScore> nBest 
                = new ArrayList<StemScore>(100*word.length());
            
            // delete
            // run + nn + ing = running
            for (int i = 1; i + 1 < word.length(); ++i) {
                // dup only
                // if (word.charAt(i-1) != word.charAt(i)) continue;
                String suffix = word.substring(i+1);
                String stem = word.substring(0,i);
                if (!possibleRoot(stem)) continue;
                String boundary = word.substring(i-1,i+1);
                add(nBest,stem,boundary,suffix);
            }

            // match/subst
            // eat + t + ing = eating    [match]
            // happy + i + ly = happily  [subst]
            for (int i = 0; i < word.length(); ++i) {
                String suffix = word.substring(i+1);
                for (int k = 0; k < sValidChars.length; ++k) {
                    char x = sValidChars[k];
                    // match only
                    // if (x != word.charAt(i)) continue;
                    String stem = word.substring(0,i) + x;
                    if (!possibleRoot(stem)) continue;
                    String boundary = word.substring(i,i+1);
                    add(nBest,stem,boundary,suffix);
                }
            }

            // insert
            // pace + () + ing = pacing
            //                   012345  i=2; z='e'
            for (int i = 0; i < word.length(); ++i) {
                String suffix = word.substring(i+1);
                for (int k = 0; k < sValidChars.length; ++k) {
                    char z = sValidChars[k];
                    String boundary = "";
                    String stem = word.substring(0,i+1) + z;
                    if (!possibleRoot(stem)) continue;
                    add(nBest,stem,boundary,suffix);
                }
            }
            return nBest;
        }
        void add(ArrayList<StemScore> nBest, String stem, String boundary, 
                 String suffix) {
            double estimate = estimate(stem,boundary,suffix);
            StemScore stemScore 
                = new StemScore(stem,boundary,suffix,estimate);
            nBest.add(stemScore);
        }
        double stemEstimate(String stem) {
            double stemLengthEstimate 
                = mStemLengthDistribution.probability(stem.length()-1);
            double stemCharEstimate = Math.pow(2.0,mStemLm.log2Estimate(stem));
            return stemLengthEstimate * Math.pow(stemCharEstimate,1.5);
        }
        double boundaryEstimate(String stem, String boundary, String suffix) {
            String contextKey = contextKey(stem,suffix);
            String boundaryKey = boundaryKey(stem,boundary,suffix);
            double boundaryCount 
                = mBoundaryCounts.getValue(boundaryKey) + sBoundarySmoothing;
            // if (lastChar(stem).equals(boundary))
            if (boundary.length() == 1 && lastChar(stem) == boundary.charAt(0))
                boundaryCount += sBoundaryMatchBoost;
            if (boundary.length() == 2 
                && boundary.charAt(0) == boundary.charAt(1))
                boundaryCount += sBoundaryMatchBoost;
            if (boundary.length() == 0 && lastChar(stem) == 'e')
                boundaryCount += sBoundaryMatchBoost;
            double contextCount
                = mContextCounts.getValue(contextKey) 
                + sBoundaryTotalSmoothing;
            double boundaryCharEstimate = boundaryCount / contextCount;
            if (Double.isInfinite(boundaryCharEstimate)) {
                System.out.println("boundaryCount=" + boundaryCount + " contextCount=" + contextCount);
            }
            double boundaryLengthEstimate 
                = boundary.length() == 0
                ? 0.2
                : (boundary.length() == 1
                   ? 0.8
                   : 0.1 );
            return boundaryCharEstimate * boundaryLengthEstimate;
        }
        double suffixEstimate(String suffix) {
            double suffixLengthEstimate
                =  suffix.length() == 0
                ? mSuffixZeroLengthProb
                : ( (1 - mSuffixZeroLengthProb)
                    * mSuffixLengthDistribution.probability(suffix.length()
                                                            -1) );
            double suffixCharEstimate 
                = Math.pow(2.0,mSuffixLm.log2Estimate(suffix));;
            return suffixLengthEstimate * Math.pow(suffixCharEstimate,1.5);
        }
        double estimate(String stem, String boundary, String suffix) {
            if (stem.length() == 0) return 0.0;
            return stemEstimate(stem) 
                * boundaryEstimate(stem,boundary,suffix)
                * suffixEstimate(suffix);
        }
        static String contextKey(String stem, String suffix) {
            return lastChar(stem) + suffix;
        }
        static String boundaryKey(String stem, String boundary, 
                                  String suffix) {
            return boundary + "." + lastChar(stem) + suffix;
        }
    }

    static final char LM_BOUNDARY_CHAR = ' ';

    static class CharLmModel extends Model implements Compilable {
        CharLmModel() {
            super(new NGramBoundaryLM(sMaxCharNGram,
                                      sNumChars,
                                      sCountMultiplier * sLmInterpolationRatio,
				      LM_BOUNDARY_CHAR),
                  new PoissonEstimator(sPriorCount,sPriorStemAvgLength-1),
                  new NGramBoundaryLM(sMaxCharNGram,
                                      sNumChars,
                                      sCountMultiplier * sLmInterpolationRatio,
                                      LM_BOUNDARY_CHAR),
                  new PoissonEstimator(sPriorCount,sPriorSuffixAvgLength-1),
                  sPriorSuffixZeroLengthProb,
                  new ObjectToDoubleMap(),
                  new ObjectToDoubleMap());
        }
        public void compileTo(ObjectOutput objOut) throws IOException {
            objOut.writeObject(new Externalizer(this));
        }
        void train(String stem, String boundary, String suffix, 
                   double weight) {
            String contextKey = contextKey(stem,suffix);
            String boundaryKey = boundaryKey(stem,boundary,suffix);
            mContextCounts.increment(contextKey,weight);
            mBoundaryCounts.increment(boundaryKey,weight);
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
        void prune() {
            ((NGramBoundaryLM)mStemLm).substringCounter().prune(sCharNGramPruneThreshold);
            ((NGramBoundaryLM)mSuffixLm).substringCounter().prune(sCharNGramPruneThreshold);
            prune(mBoundaryCounts,sBoundaryPruneThreshold);
            prune(mContextCounts,sBoundaryPruneThreshold);
            
        }
        Model compile() throws IOException, ClassNotFoundException {
            return (Model) AbstractExternalizable.compile(this);
        }
        Model compile2() throws IOException, ClassNotFoundException {
            prune();
            NGramBoundaryLM oldStemLm = (NGramBoundaryLM) mStemLm;
            LanguageModel stemLm 
                = (LanguageModel) AbstractExternalizable.compile(oldStemLm);
            double stemMeanLength = mStemLengthDistribution.mean();
            NGramBoundaryLM oldSuffixLm = (NGramBoundaryLM) mSuffixLm;
            LanguageModel suffixLm 
                = (LanguageModel) AbstractExternalizable.compile(oldSuffixLm);
            double suffixMeanLength = mSuffixLengthDistribution.mean();
            double suffixZeroLengthProb = mSuffixZeroLengthProb;
            return new Model(stemLm,
                             new PoissonConstant(stemMeanLength),
                             suffixLm,
                             new PoissonConstant(suffixMeanLength),
                             suffixZeroLengthProb,
                             mContextCounts,
                             mBoundaryCounts);
        }
        void prune(ObjectToDoubleMap map, double minVal) {
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                Double val = (Double) entry.getValue();
                double dVal = val.doubleValue();
                if (dVal < minVal) it.remove();
            }
        }
        static class Externalizer extends AbstractExternalizable {
            private static final long serialVersionUID = 15646748848459L;
            final CharLmModel mCharLmModel;
            public Externalizer(CharLmModel charLmModel) {
                mCharLmModel = charLmModel;
            }
            public Externalizer() {
                this(null);
            }
            public Object read(ObjectInput in) throws IOException {
                try {
                    LanguageModel stemLm = (LanguageModel) in.readObject();
                    double stemMeanLength = in.readDouble();
                    LanguageModel suffixLm = (LanguageModel) in.readObject();
                    double suffixMeanLength = in.readDouble();
                    double suffixZeroLengthProb = in.readDouble();
                    ObjectToDoubleMap contextCounts = (ObjectToDoubleMap) in.readObject();
                    ObjectToDoubleMap boundaryCounts = (ObjectToDoubleMap) in.readObject();
                    return new Model(stemLm,
                                     new PoissonConstant(stemMeanLength),
                                     suffixLm,
                                     new PoissonConstant(suffixMeanLength),
                                     suffixZeroLengthProb,contextCounts,boundaryCounts);
                    
                } catch (ClassNotFoundException e) {
                    throw Exceptions.toIO("Em4.CharLmModel.Externalizer.read(ObjectInput)",e);
                }
            }
            public void writeExternal(ObjectOutput objOut) throws IOException {
                mCharLmModel.prune();
                ((NGramBoundaryLM)(mCharLmModel.mStemLm)).compileTo(objOut);
                objOut.writeDouble(mCharLmModel.mStemLengthDistribution.mean());
                ((NGramBoundaryLM)(mCharLmModel.mSuffixLm)).compileTo(objOut);
                objOut.writeDouble(mCharLmModel.mSuffixLengthDistribution.mean());
                objOut.writeDouble(mCharLmModel.mSuffixZeroLengthProb);
                objOut.writeObject(mCharLmModel.mContextCounts);
                objOut.writeObject(mCharLmModel.mBoundaryCounts);
            }
            
        }
    }


    static class StemScore implements Scored {
        private final String mStem;
        private final String mBoundary;
        private final String mSuffix;
        private final double mScore;
        StemScore(String stem, String boundary, String suffix, double score) {
            mStem = stem;
            mBoundary = boundary;
            mSuffix = suffix;
            mScore = score;
        }
        public double score() {
            return mScore;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(stem().substring(0,stem().length()-1));
            char lastStemChar = lastChar(stem());
            if (boundary().length() == 0) {
                sb.append('(');
                sb.append(lastStemChar);
                sb.append(')');
                sb.append('.');
            } else if (boundary().length() == 1) {
                if (boundary().charAt(0) == lastStemChar) {
                    sb.append(lastStemChar);
                    sb.append('.');
                } else {
                    sb.append('(');
                    sb.append(lastStemChar);
                    sb.append(')');
                    sb.append('.');
                    sb.append('[');
                    sb.append(boundary());
                    sb.append(']');
                }
            } else if (boundary().length() == 2) {
                if (boundary().charAt(0) == lastStemChar) {
                    sb.append(lastStemChar);
                    sb.append('.');
                    sb.append('[');
                    sb.append(boundary().charAt(1));
                    sb.append(']');
                } else {
                    sb.append('(');
                    sb.append(lastStemChar);
                    sb.append(')');
                    sb.append('.');
                    sb.append('[');
                    sb.append(boundary());
                    sb.append(']');
                }
            }
            sb.append(suffix());
            return sb.toString()
                + " = " + word()
                + "     {" + score() + '}'
                + " stem=" + stem()
                + " boundary=" + boundary()
                + " suffix=" + suffix()
                + " lastStemChar=" + lastStemChar;
        }
        String stem() {
            return mStem;
        }
        String boundary() {
            return mBoundary;
        }
        String suffix() {
            return mSuffix;
        }
        String word() {
            return stem().substring(0,stem().length()-1)
                + boundary()
                + suffix();
        }
    }

}
    
