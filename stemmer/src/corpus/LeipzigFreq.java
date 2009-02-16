package corpus;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.io.*;
import java.util.Arrays;
import java.util.regex.*;

public class LeipzigFreq {

    static final IndoEuropeanTokenizerFactory TOKENIZER_FACTORY
	= new IndoEuropeanTokenizerFactory();

    public static void main(String[] args) throws Exception {
	File langDir = new File(args[0]);
	File countsOutFile = new File(args[1]);

	String inputCharset = extractCharset(langDir);

	File inFile = new File(langDir,"sentences.txt");
	String contents = Files.readFromFile(inFile,inputCharset);

	ObjectToCounterMap tokenCounter = new ObjectToCounterMap();
	
	System.out.println("Reading Input From file=" + inFile);
	long totalLength = 0L;
	String[] lines = contents.split("\\n");
	for (int i = 0; i < lines.length; ++i) {
	    String line = lines[i];
	    if (line.length() == 0) continue;
	    int index = line.indexOf("\t");
	    String newline = line.substring(index+1);
	    // System.out.println("line=" + line);
	    // System.out.println("New line=" + newline);
	    totalLength += newline.length();
	    addTokens(newline,tokenCounter);
	}

	System.out.println("     # total chars=" + totalLength);
	System.out.println("     # tokens=" + totalLength);
	System.out.println("     # distinct tokens=" + tokenCounter.size());
	
	writeOutput(tokenCounter,countsOutFile);
    }


    static void writeOutput(ObjectToCounterMap tokenCounter, 
			    File countsOutFile) throws Exception {
	System.out.println("Writing Output to file=" + countsOutFile);
	System.out.println("     # distinct tokens=" + tokenCounter.size());
	String[] keys 
	    = (String[]) tokenCounter.keySet().toArray(new String[0]);
	Arrays.sort(keys);
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < keys.length; ++i)
	    sb.append(keys[i] + " " + tokenCounter.getCount(keys[i]) + "\n");
	
	Files.writeStringToFile(sb.toString(),countsOutFile,Strings.UTF8);
    }


    static void addTokens(String s, ObjectToCounterMap tokenCounter) {
	char[] cs = s.toCharArray();
	String[] tokens 
	    = TOKENIZER_FACTORY.tokenizer(cs,0,cs.length).tokenize();
	for (int j = 0; j < tokens.length; ++j) 
	    if (isWord(tokens[j]))
		tokenCounter.increment(tokens[j]);
    }

    static boolean isWord(String token) {
	if (token.length() < 1) return false;
	if (!Character.isLowerCase(token.charAt(0))) return false;
	for (int i = 1; i < token.length(); ++i)
	    if (!Character.isLowerCase(token.charAt(i))) return false;
	return true;
    }
	    
    static String extractCharset(File dir) throws IOException {
	File metaFile = new File(dir,"meta.txt");
	System.out.println("Reading meta file=" + metaFile);
	String metaText = Files.readFromFile(metaFile,"ISO-8859-1");
	Pattern pattern = Pattern.compile("content encoding\\s+(\\S+)");
	Matcher matcher = pattern.matcher(metaText);
	matcher.find();
	String charset = matcher.group(1);
	System.out.println("     charset=" + charset);
	return charset;
    }

}