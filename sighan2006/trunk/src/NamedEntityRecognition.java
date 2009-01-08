import com.aliasi.lm.NGramProcessLM;

import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

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

public class NamedEntityRecognition {

    static final TokenizerFactory TOKENIZER_FACTORY 
	= new RegExTokenizerFactory(".");
    static final int NUM_CHUNKINGS_RESCORED = 64;
    static final int NGRAM_ORDER = 5;
    static final int NUM_CHARS = 7500;
    static final double INTERPOLATION_RATIO = NGRAM_ORDER;

    static final String EOS_TOKEN = new String(new char[] { (char) 0x3002 });
    static final String ZERO_CHAR = "0";
    static final String LETTER_O_CHAR = "O";

    public static void main(String[] args) throws Exception {
	System.out.println("SIGHAN 2006: NAMED ENTITY RECOGNITION");
	
	CharLmRescoringChunker chunker 
	    = new CharLmRescoringChunker(TOKENIZER_FACTORY,
					 NUM_CHUNKINGS_RESCORED,
					 NGRAM_ORDER,
					 NUM_CHARS,
					 INTERPOLATION_RATIO);

	// train
	for (int i = 0; i+2 < args.length; ++i) {
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
		String token = line.substring(0,lastSpaceIndex);
		String tag = line.substring(lastSpaceIndex+1);
		if (tag.equals(ZERO_CHAR)) tag = LETTER_O_CHAR;  // convert to LingPipe standard "out"
		tokenList.add(token);
		tagList.add(tag);
		if (token.equals(EOS_TOKEN)) {
		    String[] tags = (String[]) tagList.toArray(new String[0]);
		    String[] toks = (String[]) tokenList.toArray(new String[0]);
		    String[] whites = new String[toks.length+1];
		    Arrays.fill(whites,"");
		    chunker.handle(toks,whites,tags);
		    tagList = new ArrayList();
		    tokenList = new ArrayList();
		}
	    }
	    if (tagList.size() > 0) {
		String[] tags = (String[]) tagList.toArray(new String[0]);
		String[] toks = (String[]) tokenList.toArray(new String[0]);
		String[] whites = new String[toks.length+1];
		Arrays.fill(whites,"");
		chunker.handle(toks,whites,tags);
	    }
	    bufReader.close();
	}
	
	// compile
	System.out.println("Compiling (in memory)");
	Chunker compiledChunker 
	    = (Chunker) AbstractExternalizable.compile(chunker);

	// annotate test
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
	    Chunking chunking = compiledChunker.chunk(line);
	    System.out.println("line=" + k 
			       + " length=" + line.length() 
			       + " #chunks=" 
			       + (chunking == null 
				  ? 0 
				  : chunking.chunkSet().size()));
	    int pos = 0;
	    if (chunking == null) {
		for (int i = 0; i < line.length(); ++i)
		    bufWriter.write(line.charAt(i) + " " + ZERO_CHAR + "\n");
		continue;
	    }
	    CharSequence charSeq = chunking.charSequence();
	    Iterator it = chunking.chunkSet().iterator();
	    while (it.hasNext()) {
		Chunk chunk = (Chunk) it.next();
		int start = chunk.start();
		int end = chunk.end();
		String type = chunk.type();
		while (pos < start)
		    bufWriter.write(charSeq.charAt(pos++) + " " + ZERO_CHAR + "\n");
		bufWriter.write(charSeq.charAt(pos++) + " " + "B-" + type + "\n");
		while (pos < end)
		    bufWriter.write(charSeq.charAt(pos++) + " " + "I-" + type + "\n");
	    }
	    while (pos < charSeq.length()) 
		bufWriter.write(charSeq.charAt(pos++) + " " + ZERO_CHAR + "\n");
	}	    
	bufReader.close();
	bufWriter.close();
    }

}
