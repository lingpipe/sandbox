package corpus;


import com.aliasi.corpus.TextHandler;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import java.util.Arrays;

public class WordFreqTextHandler implements TextHandler {

    private long mNumChars = 0;
    private long mNumToks = 0;
    private long mNumValidToks = 0;
    
    private final int mMaxTokenLength;
    private final TokenizerFactory mTokenizerFactory;
    private final ObjectToCounterMap mWordCounter
	= new ObjectToCounterMap();


    
    public WordFreqTextHandler(TokenizerFactory factory,
			       int maxTokenLength) {
	mTokenizerFactory = factory;
	mMaxTokenLength = maxTokenLength;
    }

    public WordFreqTextHandler() {
	this(new IndoEuropeanTokenizerFactory(),32);
    }

    public void handle(char[] cs, int start, int length) {
	mNumChars += length;
	Tokenizer tokenizer 
	    = mTokenizerFactory.tokenizer(cs,start,length);
	String token;
	while ((token = tokenizer.nextToken()) != null) {
	    ++mNumToks;
	    if (validToken(token)) {
		++mNumValidToks;
		mWordCounter.increment(token);
	    }
	}
    }
    
    public boolean validToken(String token) {
	return Strings.allLowerCase(token)
	    && token.length() <= mMaxTokenLength;
    }

    public void writeCountsTo(File file) throws IOException {
	String[] toks 
	    = (String[]) mWordCounter.keySet().toArray(new String[0]);
	Arrays.sort(toks);
	FileOutputStream fileOut = null;
	OutputStreamWriter osWriter = null;
	BufferedWriter bufWriter = null;
	try {
	    fileOut = new FileOutputStream(file);
	    osWriter = new OutputStreamWriter(fileOut,Strings.UTF8);
	    bufWriter = new BufferedWriter(osWriter);
	    for (int i = 0; i < toks.length; ++i) {
		bufWriter.write(toks[i]);
		bufWriter.write(' ');
		bufWriter.write(Long.toString(mWordCounter.getCount(toks[i])));
		bufWriter.write('\n');
	    }
	} finally {
	    Streams.closeWriter(bufWriter);
	    Streams.closeWriter(osWriter);
	    Streams.closeOutputStream(fileOut);
	}
    }

}