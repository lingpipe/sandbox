import com.aliasi.chunk.CharLmRescoringChunker;
import com.aliasi.chunk.Chunker;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.ArrayList;

public class Train {

    static final TokenizerFactory TOKENIZER_FACTORY 
	= new RegExTokenizerFactory("\\S");  // Originally this was "."
    static final int NUM_CHUNKINGS_RESCORED = 128; // setting to 1 reduces
    static final int NGRAM_ORDER = 4;
    static final int NUM_CHARS = 7500;
    static final double INTERPOLATION_RATIO = NGRAM_ORDER;

    static final char CIRCLE_EOS_CHAR = '\u3002';

    static final String CIRCLE_EOS_TOKEN = new String(new char[] { CIRCLE_EOS_CHAR });
    static final String ZERO_CHAR = "0";
    static final String LETTER_O_CHAR = "O";


    public static void main(String[] args) throws Exception {
	System.out.println("Training Model");
        File modelFile = new File(args[0]);
	
	CharLmRescoringChunker chunker 
	    = new CharLmRescoringChunker(TOKENIZER_FACTORY,
					 NUM_CHUNKINGS_RESCORED,
					 NGRAM_ORDER,
					 NUM_CHARS,
					 INTERPOLATION_RATIO);

	// train
	for (int i = 1; i < args.length; ++i) {
	    System.out.println("Training file=" + args[i]);
            
            String[] lines = Files.readLinesFromFile(new File(args[i]),Strings.UTF8);
	    ArrayList tagList = new ArrayList();
	    ArrayList tokenList = new ArrayList();
            for (String line : lines) {
		int lastSpaceIndex = line.lastIndexOf(" ");
		String token = line.substring(0,lastSpaceIndex);
		String tag = line.substring(lastSpaceIndex+1);
		if (tag.equals(ZERO_CHAR)) tag = LETTER_O_CHAR;  // convert to LingPipe standard "out"
		tokenList.add(token);
		tagList.add(tag);
                // sentence chunk ends
		if (token.equals(CIRCLE_EOS_TOKEN)) {
		    String[] tags = (String[]) tagList.toArray(new String[0]);
		    String[] toks = (String[]) tokenList.toArray(new String[0]);
		    String[] whites = new String[toks.length+1];
		    Arrays.fill(whites,"");
		    chunker.handle(toks,whites,tags);
		    tagList = new ArrayList();
		    tokenList = new ArrayList();
		}
	    }
            // handle any dangling sentence material with no EOS
	    if (tagList.size() > 0) {
		String[] tags = (String[]) tagList.toArray(new String[0]);
		String[] toks = (String[]) tokenList.toArray(new String[0]);
		String[] whites = new String[toks.length+1];
		Arrays.fill(whites,"");
		chunker.handle(toks,whites,tags);
	    }
	}
	
	// compile
	System.out.println("Compiling Model");
        System.out.println("     modelFile=" + modelFile);
        AbstractExternalizable.compileTo(chunker,modelFile);
    }

}
