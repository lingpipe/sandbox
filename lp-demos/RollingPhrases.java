import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;


import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.TextHandler;

import com.aliasi.corpus.parsers.GigawordTextParser;

import com.aliasi.lm.IntSeqCounter;
import com.aliasi.lm.NGramBoundaryLM;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.lm.UniformBoundaryLM;

import com.aliasi.stats.BinomialDistribution;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.BoundedPriorityQueue;
import com.aliasi.util.Scored;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

import java.util.zip.GZIPInputStream;

public class RollingPhrases extends AbstractCommand {

    private final TokenizerFactory mTokenizerFactory;

    private final TokenizedLM mBgLm, mFgLm;
    private final int mBgNGramOrder, mFgNGramOrder;
    private final int mBgSamplePeriod, mFgSamplePeriod;
    private final int mBgCharNGramOrder;
    private final double mBgInterpolation;

    private final GigawordTextParser mGigawordParser;
    private final RollingHandler mRollingHandler;

    private final int mFgRescaleThreshold;
    private final int mBgRescaleThreshold;

    private final File mDataDir;

    private int mDocCount;

    private int mMinTermCount;
    private int mMaxTerms;

    private int mMinNGram;
    private int mMaxNGram;

    private final SentenceModel mSentenceModel;
    private final Chunker mSentenceChunker;

    public RollingPhrases(String[] args) {
	super(args);
	mDataDir = getArgumentDirectory("dataDir");

	mBgNGramOrder = getArgumentInt("bgNGramOrder");
	mBgCharNGramOrder = getArgumentInt("bgCharNGramOrder");
	mBgInterpolation = getArgumentDouble("bgInterpolation");
	mBgSamplePeriod = getArgumentInt("bgSamplePeriod");
	mBgRescaleThreshold = getArgumentInt("bgScaleThreshold");
	
	mFgNGramOrder = getArgumentInt("fgNGramOrder");
	mFgSamplePeriod = getArgumentInt("fgSamplePeriod");
	mFgRescaleThreshold = getArgumentInt("fgScaleThreshold");

	mMaxTerms = getArgumentInt("reportMaxTerms");
	mMinTermCount = getArgumentInt("reportMinTermCount");
	mMinNGram = getArgumentInt("reportMinNGram");
	mMaxNGram = getArgumentInt("reportMaxNGram");

	mTokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;

	mBgLm = new TokenizedLM(mTokenizerFactory,
				Math.min(mBgNGramOrder,mMaxNGram),
				new NGramBoundaryLM(5), // char LM backoff
				new UniformBoundaryLM(0.0), // unused
				mBgInterpolation); 
	
	mFgLm = new TokenizedLM(mTokenizerFactory,
				Math.min(mFgNGramOrder,mMaxNGram));

	mRollingHandler = new RollingHandler();
	mGigawordParser = new GigawordTextParser(mRollingHandler);
	
	mSentenceModel = new IndoEuropeanSentenceModel(true,true);
	mSentenceChunker = new SentenceChunker(mTokenizerFactory,
					       mSentenceModel);

	mDocCount = 0;
    }

    public void run() {
	printParameters();
	File[] trainFiles = mDataDir.listFiles();
	for (int i = 0; i < trainFiles.length; ++i)
	    runFile(trainFiles[i],i+1);
    }

    private void runFile(File gzipFile, int round) {
	FileInputStream fileIn = null;
	GZIPInputStream gzipIn = null;
	InputStreamReader inReader = null;
	try {
	    fileIn = new FileInputStream(gzipFile);
	    gzipIn = new GZIPInputStream(fileIn);
	    inReader = new InputStreamReader(gzipIn,"ASCII");
	    char[] cs = Streams.toCharArray(inReader);
	    System.out.println("\nFile " + round + ". " + gzipFile);
	    System.out
		.println("==================================================");
	    System.out.println("  Chars=" + (cs.length/1000000) + "M");
	    mGigawordParser.parseString(cs,0,cs.length);
	    reportNewTerms();
	} catch (IOException e) {
	    System.out.println("IOException=" + e);
	    e.printStackTrace(System.out);
	    System.exit(1);
	} finally {
	    Streams.closeReader(inReader);
	    Streams.closeInputStream(gzipIn);
	    Streams.closeInputStream(fileIn);
	}
    }

    void addSubstrings(Set<String> nGramSet, String[] toks) {
	for (int i = 0; i < toks.length; ++i)
	    for (int j = i+1; j <= toks.length; ++j)
		addSubstring(nGramSet,toks,i,j);
    }

    static String SEPARATOR = " ";

    void addSubstring(Set<String> nGramSet, String[] toks, int start, int end) {
	StringBuilder sb = new StringBuilder();
	for (int i = start; i < end; ++i) {
	    if (i > start) sb.append(SEPARATOR);
	    sb.append(toks[i]);
	}
	nGramSet.add(sb.toString());
    }

    boolean containsSuperstring(Set<String> nGramSet, String[] toks) {
	return nGramSet.contains(Strings.concatenate(toks,SEPARATOR));
    }

    void reportNewTerms() {
	rescale();
	BoundedPriorityQueue<ScoredObject<String[]>> termPQ 
	    = new BoundedPriorityQueue<ScoredObject<String[]>>(ScoredObject.comparator(),mMaxTerms);
	Set<String> substrings = new HashSet<String>();
	for (int n = mMaxNGram; n >= mMinNGram; --n) {
	    SortedSet<ScoredObject<String[]>> newTerms 
		= mFgLm.newTermSet(n,mMinTermCount,mMaxTerms*10,mBgLm);
            for (ScoredObject<String[]> term : newTerms) {
		String[] toks = term.getObject();
		if (n < mMaxNGram && containsSuperstring(substrings,toks))
		    continue;
		if (n > 1) 
		    addSubstrings(substrings,toks);
		if (acceptable(toks)) {
		    termPQ.offer(term);
		}
	    }
	}
	Iterator<ScoredObject<String[]>> it = termPQ.iterator();
	System.out.println("  New Terms");
	System.out.println("      "
			   + "  Rank"
			   + "   " 
			   + " Score"
			   + "    "
			   + " Phrase");
	System.out.println("      "
			   + "------------------------------------------");
	for (int i = 1; it.hasNext(); ++i) {
	    ScoredObject<String[]> newTerm = it.next();
	    double score = newTerm.score();
	    double log2Score = com.aliasi.util.Math.log2(score);
	    String[] toks = newTerm.getObject();
	    System.out.printf("      %6d %6.1f %s\n",
                              i, log2Score,printNormalize(toks));
	}
    }

    String printNormalize(String[] toks) {
	return 
	    Strings.concatenate(toks," ")
	    .replaceAll("' s","'s")
	    .replaceAll("O ' ","O'")
	    .replaceAll(" '","'")
	    .replaceAll(" - ","-")
	    .replaceAll(" \\.",".");
    }

    boolean acceptable(String[] toks) {
	return isCapitalized(toks[0])
	    && containsLowerCase(toks)
	    && (toks.length == 1 
		|| isCapitalized(toks[toks.length-1]));
    }

    boolean containsLowerCase(String[] toks) {
	for (int i = 0; i < toks.length; ++i)
	    if (containsLowerCase(toks[i]))
		return true;
	return false;
    }

    boolean containsLowerCase(String tok) {
	for (int i = 0; i < tok.length(); ++i)
	    if (Character.isLowerCase(tok.charAt(i)))
		return true;
	return false;
    }

    boolean isCapitalized(String tok) {
	return Strings.containsDigits(tok.toCharArray())
	    || ( (tok.length() > 0)
		 && Character.isUpperCase(tok.charAt(0)) );
    }

    private void printParameters() {
	System.out.println("NEW PHRASE EXTRACTION DEMO");
	System.out.println();

	System.out.println("Command Parameters");
	System.out.println("  Corpus Directory=" + mDataDir);
	System.out.println("  Background Language Model");
	System.out.println("      Token N-Gram Order=" + mBgNGramOrder);
	System.out.println("      Character N-Gram Order=" + mBgCharNGramOrder);
	System.out.println("      Interpolation Param=" + mBgInterpolation);
	System.out.println("      Sample Period=" + mBgSamplePeriod);
	System.out.println("      Rescale Threshold=" + mBgRescaleThreshold);
	System.out.println("  Foreground Language Model");
	System.out.println("      Token N-Gram Order=" + mFgNGramOrder);
	System.out.println("      Sample Period=" + mFgSamplePeriod);
	System.out.println("      Rescale Threshold=" + mFgRescaleThreshold);

	System.out.println("  Analysis");
	System.out.println("      Tokenizer Factory=" 
			   + mTokenizerFactory.getClass());
	System.out.println("      Sentence Model="
			   + mSentenceModel.getClass());

	System.out.println("  Reporting");
	System.out.println("      Max terms=" + mMaxTerms);
	System.out.println("      Min term count=" + mMinTermCount);
	System.out.println("      Min n-gram=" + mMinNGram);
	System.out.println("      Max n-gram=" + mMaxNGram);
    }

    void rescale() {
	System.out.println("  Rescaling ");
	rescale(mFgLm,mFgRescaleThreshold,"FG");
	rescale(mBgLm,mBgRescaleThreshold,"BG");
    }

    void rescale(TokenizedLM lm, int threshold, String name) {
	while (true) {
	    int size = lm.sequenceCounter().trieSize();
	    System.out.println("      " + name + " Nodes=" + size);
	    if (size < threshold) return; 
	    lm.sequenceCounter().rescale(0.5);
	}
    }

    private class RollingHandler implements TextHandler {
	public void handle(char[] cs, int start, int length) {
	    Chunking sentenceChunking 
		= mSentenceChunker.chunk(cs,start,start+length);
	    CharSequence docCharSeq = sentenceChunking.charSequence();
	    Set<Chunk> chunkSet = sentenceChunking.chunkSet();
	    Iterator<Chunk> chunkIt = chunkSet.iterator();
	    while (chunkIt.hasNext()) {
		Chunk chunk = chunkIt.next();
		int chunkStart = chunk.start();
		int chunkEnd = chunk.end();
		while (chunkStart < chunkEnd 
		       && nonAlphaNum(docCharSeq.charAt(chunkStart)))
		    ++chunkStart;
		while (chunkEnd > chunkStart 
		       && nonAlphaNum(docCharSeq.charAt(chunkEnd-1)))
		    --chunkEnd;
		CharSequence sentCharSeq = 
		    docCharSeq.subSequence(chunkStart,chunkEnd);
		String sent = sentCharSeq.toString();
		// System.out.println("SENTENCE: " + sent);
		String[] phrases
		    = sent.split("``|''|,|;|\\(|\\)|--|---|\\.\\.\\.|:");
		for (int i = 0; i < phrases.length; ++i) {
		    String phrase = phrases[i].trim();
		    if (phrase.length() == 0) continue;
		    // System.out.println("     PHRASE: /" + phrase + "/");
		    train(mFgLm,mFgSamplePeriod,phrase);
		    train(mBgLm,mBgSamplePeriod,phrase);
		}
	    }
	    ++mDocCount;
	}
	void train(TokenizedLM lm, int period, 
		   CharSequence cSeq) {
	    if (mDocCount % period != 0) return;
	    lm.train(cSeq);
	}
	boolean nonAlphaNum(char c) {
	    return !Character.isLetter(c)
		&& !Character.isDigit(c);
	}

    }

    public static void main(String[] args) {
	new RollingPhrases(args).run();
    }
    
    abstract class Collector implements ObjectHandler<String[]> {
	final BoundedPriorityQueue<ScoredObject<String[]>> mBPQ;
	Collector() { 
	    this(true); 
	}
	Collector(boolean biggerIsBetter) {
	    Comparator<ScoredObject<String[]>> comp 
		= biggerIsBetter
		? ScoredObject.<ScoredObject<String[]>>comparator()
		: ScoredObject.<ScoredObject<String[]>>reverseComparator();
	    mBPQ = new BoundedPriorityQueue<ScoredObject<String[]>>(comp,mMaxTerms);
	}
	ScoredObject<String[]>[] nGrams() {
            @SuppressWarnings({"unchecked","rawtypes"})  // ok but should refactor to better style
            ScoredObject<String[]>[] result = (ScoredObject<String[]>[]) new ScoredObject[mBPQ.size()];
	    mBPQ.toArray(result);
	    return result;
	}
	public void handle(String[] nGram) {
	    // don't include boundaries, which have no symbol
	    for (int i = 0; i < nGram.length; ++i)
		if (nGram[i] == null) return;  
	    
	    // check if acceptable
	    if (!accept(nGram)) return;
	
	    // score and add to queue
	    double score = score(nGram);
	    ScoredObject<String[]> so = new ScoredObject<String[]>(nGram,score);
	    mBPQ.offer(so);
	}
	public boolean accept(String[] nGram) {
	    for (int i = 0; i < nGram.length; ++i)
		if (nGram[i] == null) 
		    return false;
	    return true;
	}
	abstract double score(String[] nGram);

    }


    abstract class NewTermCollector extends Collector {
	public double score(String[] nGram) {
	    IntSeqCounter counter  = mFgLm.sequenceCounter();
	    int[] nGramIds = symbolsToIds(nGram);
	    int sampleCount = counter.count(nGramIds,0,nGram.length);
	    int totalSampleCount = counter.count(nGramIds,0,0);
	    double bgProb 
		= mBgLm.tokenProbability(nGram,0,nGram.length);
	    double score 
		= BinomialDistribution.z(bgProb, sampleCount, totalSampleCount);
	    if (Double.isInfinite(score))
		System.out.println("bgProb=" + bgProb
				   + "\n    sampleCount=" 
				   + sampleCount
				   + "\n    totalSampleCount=" 
				   + totalSampleCount
				   + "\n    z=" + score);
	    return score;
	}
	int[] symbolsToIds(String[] syms) {
	    SymbolTable table = mFgLm.symbolTable();
	    int[] ids = new int[syms.length];
	    for (int i = 0; i < syms.length; ++i)
		ids[i] = table.symbolToID(syms[i]);
	    return ids;
	}
    }


}
