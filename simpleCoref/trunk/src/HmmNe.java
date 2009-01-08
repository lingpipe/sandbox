import com.aliasi.classify.ScoredPrecisionRecallEvaluation;

import com.aliasi.chunk.HmmChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkingEvaluation;
import com.aliasi.chunk.CharLmHmmChunker;

import com.aliasi.corpus.ChunkHandler;
import com.aliasi.corpus.ChunkHandlerAdapter;
import com.aliasi.corpus.TagHandler;
import com.aliasi.corpus.ChunkTagHandlerAdapter;

import com.aliasi.hmm.HmmCharLmEstimator;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aliasi.util.AbstractCommand;


public abstract class HmmNe extends AbstractCommand {

    int mMaxNGram;
    int mNumChars;
    double mLambdaFactor;

    int mMaxNBest;
    int mNBestPrint;

    int mMaxConfNBest;

    double mTestFraction;

    
    CharLmHmmChunker mChunker;
    HmmChunker mCompiledChunker;

    TokenizerFactory mTokenizerFactory;

    public HmmNe(String[] args) {
	super(args);

	mMaxNGram = getArgumentInt("maxNGram");
	mNumChars = getArgumentInt("numChars");
	mLambdaFactor = getArgumentDouble("lambda");
	mTestFraction = getArgumentDouble("testFrac");
	mMaxNBest = getArgumentInt("nBestEval");
	mNBestPrint = getArgumentInt("nBestPrint");
	mMaxConfNBest = getArgumentInt("nBestConf");

	mTokenizerFactory 
	    = new IndoEuropeanTokenizerFactory();
	// = new com.aliasi.tokenizer.RegExTokenizerFactory("\\S+"); // debug on MUC6

	HmmCharLmEstimator estimator 
	    = new HmmCharLmEstimator(mMaxNGram,mNumChars,mLambdaFactor);

	mChunker = new CharLmHmmChunker(mTokenizerFactory,estimator);

	System.out.println("\nCOMMAND PARAMS");
	System.out.println("     max n-gram=" + mMaxNGram);
	System.out.println("     num chars=" + mNumChars);
	System.out.println("     lambda fact=" + mLambdaFactor);
	System.out.println("     test fraction=" + mTestFraction);
	System.out.println("     max n-best=" + mMaxNBest);
	System.out.println("     max n-best print=" + mNBestPrint);
	System.out.println("     max conf n-best=" + mMaxConfNBest);
    }

    public void run() {
	try {
	    run2();
	} catch (Throwable t) {
	    t.printStackTrace(System.out);
	}
    }

    public void run2() throws ClassNotFoundException, IOException {
	System.out.println("\nTRAINING");
	visitTraining(mChunker);

	System.out.println("\nCOMPILING");
	HmmChunker compiledChunker
	    = (HmmChunker) AbstractExternalizable.compile(mChunker);

	ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
	ObjectOutputStream dataOut = new ObjectOutputStream(bytesOut);
	mChunker.compileTo(dataOut);
	byte[] bytes = bytesOut.toByteArray();
	System.out.println("     model size =" + (bytesOut.toByteArray().length/1000) + " kb");
	
	com.aliasi.util.Files.writeBytesToFile(bytes,
					       new File("../../models/ne-en-news-muc.chunk"));

	System.out.println("\nTESTING");
	MultiEval eval = new MultiEval(compiledChunker,
				       mMaxNBest,mMaxConfNBest,
				       mNBestPrint,
				       mTokenizerFactory);
	visitTest(eval);
	eval.finalReport();
    }
    
    public abstract void visitTraining(CharLmHmmChunker chunker) 
	throws IOException;

    public abstract void visitTest(ChunkHandler chunkHandler) 
	throws IOException;


    static Chunk toUnscoredChunk(Chunk c) {
	return ChunkFactory.createChunk(c.start(),
					c.end(),
 					c.type());
    }

    private static CharSequence reconsititute(String[] tokens, 
					      String[] whites) {
	if (whites == null) {
	    whites = new String[tokens.length+1];
	    Arrays.fill(whites," ");
	    whites[0] = "";
	    whites[whites.length-1] = "";
	}
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < tokens.length; ++i) {
	    sb.append(whites[i]);
	    sb.append(tokens[i]);
	}
	sb.append(whites[whites.length-1]);
	return sb;
    }

    static class MultiEval implements ChunkHandler {
	NBestEvaluator mNBestEval;
	ChunkHandler mNBestEvalChunk;

	ConfidenceEvaluator mConfEval;
	ChunkHandler mConfEvalChunk;

	FirstBestEvaluator mFirstBestEval;

	int mNumCase = 0;

	MultiEval(HmmChunker chunker,
		  int maxNBest, int maxConfNBest,
		  int maxNBestPrint,
		  TokenizerFactory factory) {

	    mNBestEval = new NBestEvaluator(chunker,maxNBest,maxNBestPrint);
	    mNBestEvalChunk = new ChunkHandlerAdapter(mNBestEval,factory,false);

	    mConfEval = new ConfidenceEvaluator(chunker,maxConfNBest);
	    mConfEvalChunk = new ChunkHandlerAdapter(mConfEval,factory,false);

	    mFirstBestEval = new FirstBestEvaluator(chunker);
	}
	void finalReport() {
	    mFirstBestEval.finalReport();
	    mNBestEval.finalReport();
	    mConfEval.finalReport();
	}
	public void handle(Chunking chunking) {
	    System.out.println();
	    for (int i = 0; i < 160; ++i)
		System.out.print('=');
	    System.out.println("\nCASE=" + (++mNumCase));
	    mFirstBestEval.handle(chunking);
	    mNBestEvalChunk.handle(chunking);
	    mConfEvalChunk.handle(chunking);
	}
    }

    static class FirstBestEvaluator implements ChunkHandler {
	ChunkerEvaluator mEvaluator;
	FirstBestEvaluator(HmmChunker chunker) {
	    mEvaluator = new ChunkerEvaluator(chunker);
	}
	public void handle(Chunking chunking) {
	    System.out.println("\nFIRST-BEST EVAL");
	    mEvaluator.handle(chunking);
	}
	public void finalReport() {
	    System.out.println("\n\nFIRST-BEST FINAL REPORT");
	    System.out.println(mEvaluator
			       .evaluation()
			       .precisionRecallEvaluation());
	}
    }

    static class ConfidenceEvaluator implements TagHandler {
	final ScoredPrecisionRecallEvaluation mPrEval
	    = new ScoredPrecisionRecallEvaluation();
	final HmmChunker mChunker;
	final int mMaxNBest;
	ConfidenceEvaluator(HmmChunker chunker, int maxNBest) {
	    mChunker = chunker;
	    mMaxNBest = maxNBest;
	}
	public void handle(String[] tokens, String[] whites, String[] tags) {
	    CharSequence input = reconsititute(tokens,whites);
	    char[] cs = Strings.toCharArray(input);

	    Chunking refChunking 
		= ChunkTagHandlerAdapter.toChunkingBIO(tokens,whites,tags);
	    Set refChunks = new HashSet();
	    Iterator it = refChunking.chunkSet().iterator();
	    while (it.hasNext()) {
		Chunk nextChunk = (Chunk) it.next();
		Chunk zeroChunk = toUnscoredChunk(nextChunk);
		refChunks.add(zeroChunk);
	    }
	    ChunkingEvaluation.printHeader(new String(cs));
	    System.out.println("\nCONFIDENCE EVAL");
	    System.out.println("     Ref Chunks=" + refChunking.chunkSet());

	    Iterator nBestIt = mChunker.nBestChunks(cs,0,cs.length,mMaxNBest);
	    while (nBestIt.hasNext()) {
		Chunk nextChunk = (Chunk) nBestIt.next();
		double score = nextChunk.score();
		Chunk zeroedChunk = toUnscoredChunk(nextChunk);
		// System.out.println("  nextChunk=" + nextChunk + " score=" + score);
		boolean correct = refChunks.contains(zeroedChunk);
		System.out.println("     " 
				   + (correct ? "TRUE " : "false") 
				   + " : " + nextChunk);
		mPrEval.addCase(correct,score);
	    }
	}
	public void finalReport() {
	    System.out.println("\nFINAL CONFIDENCE REPORT");
	    System.out.println(mPrEval.toString());
	    double[][] prCurve = mPrEval.prCurve(true); // true = interpolated
	    for (int i = 0; i < prCurve.length; ++i)
		System.out.println(" " + prCurve[i][0] + " / " + prCurve[i][1]);
	}
    }

    static class NBestEvaluator implements TagHandler {
	ObjectToCounterMap mCorrectRanks = new ObjectToCounterMap();
	ArrayList nBests = new ArrayList();
	HmmChunker mChunker;
	int mMaxNBest;
	int mNBestPrint;

	NBestEvaluator(HmmChunker chunker, int maxNBest, 
		       int nBestPrint) {
	    mChunker = chunker;
	    mMaxNBest = maxNBest;
	    mNBestPrint = nBestPrint;
	}
	public void handle(String[] tokens, String[] whites, String[] tags) {
	    CharSequence input = reconsititute(tokens,whites);
	    char[] cs = Strings.toCharArray(input);

	    Chunking refChunking 
		= ChunkTagHandlerAdapter.toChunkingBIO(tokens,whites,tags);
	    System.out.println("N-BEST EVAL");
	    System.out.println("             " + new String(cs));
	    // ChunkingEvaluation.printHeader(new String(cs));
	    ChunkingEvaluation.printChunks("  Ref        ",refChunking);

	    Iterator nBestIt = mChunker.nBest(cs,0,cs.length,mMaxNBest);
	    int i;
	    double score = Double.NEGATIVE_INFINITY;
	    int foundRank = -1;
	    for (i = 0; i < mMaxNBest; ++i) {
		if (!nBestIt.hasNext()) {
		    System.out.println("Out of hypotheses at rank=" + i);
		    break;
		}
		ScoredObject so = (ScoredObject) nBestIt.next();
		score = so.score();
		Chunking responseChunking = (Chunking) so.getObject();
		if (i < mNBestPrint) {
		    System.out.print(Strings.decimalFormat(i,"#,##0",5));
		    System.out.print(" ");
		    ChunkingEvaluation
			.printChunks(Strings.decimalFormat(score,"#,##0",6)
				     + " ",
				     responseChunking);
		}
		if (responseChunking.equals(refChunking)) {
		    System.out.println("  -----------");
		    foundRank = i;
		}
	    }
	    if (foundRank < 0) 
		System.out.println("Correct Rank >=" + mMaxNBest);
	    else
		System.out.println("Correct Rank=" + foundRank);
	    System.out.println();
	    mCorrectRanks.increment(new Integer(foundRank));
	}
	public void finalReport() {
	    System.out.println("\nFINAL N-BEST REPORT");
	    System.out.println(mCorrectRanks.toString());
	}
    }

}
    
