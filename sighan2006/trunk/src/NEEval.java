import com.aliasi.lm.NGramProcessLM;

import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;

import com.aliasi.corpus.ChunkTagHandlerAdapter;

import com.aliasi.tokenizer.CharacterTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.io.IOException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NEEval {

    static final TokenizerFactory TOKENIZER_FACTORY 
	= new RegExTokenizerFactory(".");
    static final int NUM_CHUNKINGS_RESCORED = 64;
    static final int NGRAM_ORDER = 5;
    static final int NUM_CHARS = 7500;
    static final double INTERPOLATION_RATIO = 0.5 * (double) NGRAM_ORDER;

    static final String EOS_TOKEN = new String(new char[] { (char) 0x3002 });

    static ObjectToCounterMap TRAINING_TYPE_DISTRO = new ObjectToCounterMap();
    static ObjectToCounterMap TEST_TYPE_DISTRO = new ObjectToCounterMap();
    static Set TRAINING_ENTITY_SET = new HashSet();
    static Set TEST_ENTITY_SET = new HashSet();
    static void addTrainingEntities(String[] toks, String[] whites, String[] tags,
				    Set entitySet, ObjectToCounterMap typeDistro) {
	Chunking chunking 
	    = com.aliasi.corpus.ChunkTagHandlerAdapter.toChunkingBIO(toks,whites,tags);
	String s = chunking.charSequence().toString();
	Iterator it = chunking.chunkSet().iterator();
	while (it.hasNext()) {
	    Chunk chunk = (Chunk) it.next();
	    String entityText = s.substring(chunk.start(),chunk.end());
	    entitySet.add(entityText);
	    typeDistro.increment(chunk.type());
	}
    }
    
    public static void main(String[] args) throws Exception {
	System.out.println("SIGHAN 2006: WORD SEGMENTATION");
	
	CharLmRescoringChunker chunker 
	    = new CharLmRescoringChunker(TOKENIZER_FACTORY,
					 NUM_CHUNKINGS_RESCORED,
					 NGRAM_ORDER,
					 NUM_CHARS,
					 INTERPOLATION_RATIO);

	// train
	for (int i = 0; i+3 < args.length; ++i) {
	    System.out.println("Training file=" + args[i]);
	    FileInputStream fileIn = new FileInputStream(args[i]);
	    InputStreamReader reader 
		= new InputStreamReader(fileIn,Strings.UTF8);
	    BufferedReader bufReader = new BufferedReader(reader);
	    ArrayList tagList = new ArrayList();
	    ArrayList tokenList = new ArrayList();
	    String line;
	    while ((line = bufReader.readLine()) != null) {
		int lastSpaceIndex = line.lastIndexOf(" ");
		if (lastSpaceIndex != 1) {
		    throw new IllegalArgumentException("fooey, lastSpaceIndex=" + lastSpaceIndex);
		}
		String token = line.substring(0,lastSpaceIndex);
		String tag = line.substring(lastSpaceIndex+1);
		if (tag.equals(ZERO_CHAR)) tag = OH_CHAR;  // convert to our use of the oh (O) char
		tokenList.add(token);
		tagList.add(tag);
		if (token.equals(EOS_TOKEN)) {
		    // System.out.println("tags=" + tagList);
		    String[] tags = (String[]) tagList.toArray(new String[0]);
		    String[] toks = (String[]) tokenList.toArray(new String[0]);
		    String[] whites = new String[toks.length+1];
		    Arrays.fill(whites,"");
		    chunker.handle(toks,whites,tags);

		    addTrainingEntities(toks,whites,tags,TRAINING_ENTITY_SET,TRAINING_TYPE_DISTRO);

		    tagList = new ArrayList();
		    tokenList = new ArrayList();
		}
	    }
	    if (tagList.size() > 0) {
		String[] tags = (String[]) tagList.toArray(new String[0]);
		String[] toks = (String[]) tokenList.toArray(new String[0]);
		String[] whites = new String[toks.length+1];
		Arrays.fill(whites,"");

		addTrainingEntities(toks,whites,tags,TRAINING_ENTITY_SET, TRAINING_TYPE_DISTRO);

		chunker.handle(toks,whites,tags);
	    }
	    bufReader.close();
	}
	
	System.out.println("Training Type Distro\n" + TRAINING_TYPE_DISTRO.toString());
	lengthDistro("TRAIN",TRAINING_ENTITY_SET);

	// compile
	System.out.println("Compiling in memory");
	Chunker compiledChunker 
	    = (Chunker) AbstractExternalizable.compile(chunker);
	chunker = null; // allow GC

	// eval
	ChunkerEvaluator evaluator = new ChunkerEvaluator(compiledChunker);
	File evalFile = new File(args[args.length-3]);
	System.out.println("EVAL FILE=" + evalFile);
	FileInputStream evalFileIn = new FileInputStream(evalFile);
	InputStreamReader evalReader = new InputStreamReader(evalFileIn,Strings.UTF8);
	BufferedReader evalBufReader = new BufferedReader(evalReader);
	ArrayList evalTagList = new ArrayList();
	ArrayList evalTokenList = new ArrayList();
	String evalLine;
	int totalEvalChars = 0;
	for (int k = 0; (evalLine = evalBufReader.readLine()) != null; ) {
	    int lastSpaceIndex = evalLine.lastIndexOf(" ");
	    String token = evalLine.substring(0,lastSpaceIndex);
	    String tag = evalLine.substring(lastSpaceIndex+1);
	    if (tag.equals(ZERO_CHAR)) tag = OH_CHAR;
	    evalTokenList.add(token);
	    evalTagList.add(tag);
	    if (token.equals(EOS_TOKEN)) {
		String[] tags = (String[]) evalTagList.toArray(new String[0]);
		String[] toks = (String[]) evalTokenList.toArray(new String[0]);
		String[] whites = new String[toks.length+1];
		Arrays.fill(whites,"");
		totalEvalChars += toks.length;
		System.out.println("Eval Sentence " + (k++) + " length=" + toks.length + " total eval chars so far=" + totalEvalChars);
		Chunking chunking = ChunkTagHandlerAdapter.toChunkingBIO(toks,whites,tags);
		evaluator.handle(chunking);
		evalTagList = new ArrayList();
		evalTokenList = new ArrayList();
		if (k % 100 == 0) {
		    System.out.println("\nINTERIM EVALUATION");
		    System.out.println(evaluator.evaluation().precisionRecallEvaluation().toString());
		}
	    }
	}
	System.out.println("\nEVALUATION");
	System.out.println(evaluator.evaluation().precisionRecallEvaluation().toString());

	// test
	File testFile = new File(args[args.length-2]);
	File outputFile = new File(args[args.length-1]);
	System.out.println("Test file=" + testFile);
	System.out.println("Writing output to file=" + outputFile);
	FileInputStream fileIn = new FileInputStream(testFile);
	InputStreamReader reader = new InputStreamReader(fileIn,Strings.UTF8);
	BufferedReader bufReader = new BufferedReader(reader);
	FileOutputStream fileOut = new FileOutputStream(outputFile);
	OutputStreamWriter writer = new OutputStreamWriter(fileOut,Strings.UTF8);
	BufferedWriter bufWriter = new BufferedWriter(writer);
	String line;
	for (int k = 0; (line = bufReader.readLine()) != null; ++k) {
	    System.out.println("analyzing line " + k + "; length=" + line.length());
	    Chunking chunking = compiledChunker.chunk(line);
	    int pos = 0;
	    if (chunking == null) {
		for (int i = 0; i < line.length(); ++i)
		    bufWriter.write(line.charAt(i) + " " + ZERO_CHAR + "\n");
		continue;
	    }
	    CharSequence charSeq = chunking.charSequence();
	    if (charSeq.length() != line.length()) 
		throw new IllegalArgumentException("wrong lengths");
	    Iterator it = chunking.chunkSet().iterator();
	    while (it.hasNext()) {
		Chunk chunk = (Chunk) it.next();
		int start = chunk.start();
		int end = chunk.end();
		String type = chunk.type();

		TEST_ENTITY_SET.add(charSeq.toString().substring(start,end));
		TEST_TYPE_DISTRO.increment(type);
		
		
		if (start < pos)
		    throw new IllegalArgumentException("drat. not ordered.");

		while (pos < start)
		    bufWriter.write(charSeq.charAt(pos++) + " " + ZERO_CHAR + "\n");
		bufWriter.write(charSeq.charAt(pos++) + " " + "B-" + type + "\n");
		while (pos < end)
		    bufWriter.write(charSeq.charAt(pos++) + " I-" + type + "\n");

		if (pos != end)
		    throw new IllegalArgumentException("drat.  not pos == end");
	    }
	    while (pos < charSeq.length()) 
		bufWriter.write(charSeq.charAt(pos++) + " " + ZERO_CHAR + "\n");
	    bufWriter.flush(); // just so we can watch output
	}	    
	bufReader.close();
	bufWriter.close();

	// report
	System.out.println("#TRAINING ENTITIES=" + TRAINING_ENTITY_SET.size());
	System.out.println("#TEST ENTITIES=" + TEST_ENTITY_SET.size());
	Set diffSet = new HashSet(TEST_ENTITY_SET);
	diffSet.removeAll(TRAINING_ENTITY_SET);
	System.out.println("#TEST NOT IN TRAINING=" + diffSet.size());
	lengthDistro("TEST",TEST_ENTITY_SET);
	System.out.println("Test Type Distro\n" + TEST_TYPE_DISTRO.toString());
    }

    static void lengthDistro(String msg, Set entitySet) {
	ObjectToCounterMap counter = new ObjectToCounterMap();
	Iterator it = entitySet.iterator();
	while (it.hasNext()) {
	    String entity = it.next().toString();
	    counter.increment(new Integer(entity.length()));
	}
	System.out.println("Length distro " + msg + "\n" + counter);
    }

    static final String ZERO_CHAR = "0";
    static final String OH_CHAR = "O";

}
