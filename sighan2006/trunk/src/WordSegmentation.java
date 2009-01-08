import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class WordSegmentation {

    static final int MAX_N_GRAM = 5;
    static final int NUM_CHARS = 7500;
    static final double INTERPOLATION_RATIO = MAX_N_GRAM;
    static final int MAX_N_BEST = 1024;
    static final String SPACE_PATTERN = "(\\s|\u3000)+";
    static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static void main(String[] args) throws Exception {
	System.out.println("SIGHAN 2006: WORD SEGMENTATION");

	// train segmenter
	NGramProcessLM lm 
	    = new NGramProcessLM(MAX_N_GRAM,NUM_CHARS,INTERPOLATION_RATIO);
	WeightedEditDistance distance = CompiledSpellChecker.TOKENIZING;
	TrainSpellChecker trainer 
	    = new TrainSpellChecker(lm,distance,null); 
	for (int i = 0; i+2 < args.length; ++i) {
	    System.out.println("Training file=" + args[i]);
	    FileInputStream fileIn = new FileInputStream(args[i]);
	    InputStreamReader reader 
		= new InputStreamReader(fileIn,Strings.UTF8);
	    BufferedReader bufReader = new BufferedReader(reader);
	    String line;
	    while ((line = bufReader.readLine()) != null) {
		String normalizedLine 
		    = (line.trim() + " ")
		    .replaceAll("\u3000"," ") // half space counts as space
		    .replaceAll("\\s+"," "); // any other space sequence
		trainer.train(normalizedLine);
	    }
	    bufReader.close();
	}

	// compile and configure segmenter
	CompiledSpellChecker segmenter
	    = (CompiledSpellChecker) AbstractExternalizable.compile(trainer);
	segmenter.setAllowInsert(true);
	segmenter.setAllowMatch(true);
	segmenter.setAllowDelete(false);
	segmenter.setAllowSubstitute(false);
	segmenter.setAllowTranspose(false);
	segmenter.setNumConsecutiveInsertionsAllowed(1);
	segmenter.setNBest(MAX_N_BEST);

	// run segmentation on test file
	File testFile = new File(args[args.length-2]);
	File outputFile = new File(args[args.length-1]);
	System.out.println("Test file=" + testFile);
	System.out.println("Writing output to file=" + outputFile);
	FileInputStream fileIn = new FileInputStream(testFile);
	InputStreamReader reader = new InputStreamReader(fileIn,Strings.UTF8);
	BufferedReader bufReader = new BufferedReader(reader);
	FileOutputStream fileOut = new FileOutputStream(outputFile);
	OutputStreamWriter writer 
	    = new OutputStreamWriter(fileOut,Strings.UTF8);
	BufferedWriter bufWriter = new BufferedWriter(writer);
	String line;
	while ((line = bufReader.readLine()) != null) {
	    String response = segmenter.didYouMean(line);
	    bufWriter.write(response + " \n");
	}	    
	bufReader.close();
	bufWriter.close();
    }

}
