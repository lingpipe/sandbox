package trec;

import com.aliasi.util.Streams;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;

public class NGramAnalyzer extends Analyzer {
    private static final int DEFAULT_NGRAM_LENGTH = 3;
    private final int mLength;
    public NGramAnalyzer() {
	this(DEFAULT_NGRAM_LENGTH); 
    }
    public NGramAnalyzer(int length) {
	mLength = length;
    }
    public TokenStream tokenStream(String fieldName, Reader reader) {
	return new NGramTokenStream(reader,mLength);
    }
    private static class NGramTokenStream extends TokenStream {
	private String mText;
	private int mStart = 0;
	private final int mNGramSize;
	private NGramTokenStream(Reader reader, int nGramSize) {
	    mNGramSize = nGramSize;
	    try {
		mText = new String(Streams.toCharArray(reader)).toLowerCase();
	    } catch (IOException e) {
		mText = "";
	    }
	    Streams.closeReader(reader);
	}
	public Token next() {
	    if ((mStart + mNGramSize) > (mText.length())) return null;
	    Token result = new Token(mText.substring(mStart,mStart+mNGramSize),
				    mStart,mStart+mNGramSize); 
	    ++mStart;
	    return result;
	}
    }
}
