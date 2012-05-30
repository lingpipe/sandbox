package com.aliasi.annotate.corpora;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.LineTokenizerFactory;

public class LineTokenizerWrapper implements TokenizerFactory {
    public LineTokenizerWrapper() {
	System.out.println("instantiated");
    }
    public Tokenizer tokenizer(char[] cs, int start, int length) {
	return LineTokenizerFactory.INSTANCE.tokenizer(cs,start,length);
    }
}