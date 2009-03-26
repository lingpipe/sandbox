/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.lingmed.lucene;

import com.aliasi.tokenizer.EnglishStopListFilterTokenizer;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseFilterTokenizer;
import com.aliasi.tokenizer.PorterStemmerFilterTokenizer;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Streams;

import java.io.IOException;
import java.io.Reader;

import java.util.Map;
import java.util.HashMap;

import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.TokenStream;

/**
 * A <code>LuceneAnalyzer</code> is an adapter class which enables us
 * to use LingPipe {@link Tokenizer} and {@link TokenizerFactory}
 * objects with a Lucene analyzer.
 *
 * @author Mitzi Morris, Bob Carpenter
 * @version 1.0
 * @since   LingMed1.0
 */
public class LuceneAnalyzer extends Analyzer {

    private TokenizerFactory mDefaultFactory;

    private final Map<String,TokenizerFactory> mFieldToFactory 
        = new HashMap<String,TokenizerFactory>();

    public LuceneAnalyzer() {
        this(KEYWORD_TOKENIZER_FACTORY);
    }

    public LuceneAnalyzer(TokenizerFactory defaultFactory) {
        mDefaultFactory = defaultFactory;
    }

    public void setTokenizer(String field, TokenizerFactory factory) {
	mFieldToFactory.put(field,factory);
    }

    public TokenStream tokenStream(String fieldName, Reader reader) {
	TokenizerFactory factory = mFieldToFactory.containsKey(fieldName)
	    ? mFieldToFactory.get(fieldName)
	    : mDefaultFactory;
	if (factory == null) {
	    String msg = "Must set factory for field or set default factory."
		+ " Field=" + fieldName
		+ " has no factory and default factory is null.";
	    throw new IllegalArgumentException(msg);
	}
        char[] cs = null;
	try {
	    cs = Streams.toCharArray(reader);
	} catch (IOException e) {
            cs = EMPTY_CHAR_ARRAY;
	}
        Tokenizer tokenizer = factory.tokenizer(cs,0,cs.length);
        return new LuceneTokenStream(tokenizer);
    }

    static final char[] EMPTY_CHAR_ARRAY = new char[0];
    
    public static final TokenizerFactory KEYWORD_TOKENIZER_FACTORY
        = new RegExTokenizerFactory(".+",Pattern.DOTALL);

    public static final TokenizerFactory INDOEUROPEAN_TOKENIZER_FACTORY
        = IndoEuropeanTokenizerFactory.FACTORY;

	static class IndoEuropeanLowerCaseTokenizerFactory implements TokenizerFactory {
		public Tokenizer tokenizer(char[] cs, int start, int length) {
			Tokenizer tokenizer = INDOEUROPEAN_TOKENIZER_FACTORY.tokenizer(cs,start,length);
			tokenizer = new LowerCaseFilterTokenizer(tokenizer);
			return tokenizer;
		}
	}
    public static final TokenizerFactory INDOEUROPEAN_LC_TOKENIZER_FACTORY
        = new IndoEuropeanLowerCaseTokenizerFactory();

	// like Lucene's analysis.SimpleAnalyzer, but with digits, too
	public static final TokenizerFactory SIMPLE_TOKENIZER_FACTORY
		= new RegExTokenizerFactory("\\p{L}+|\\p{Digit}+");

	// STANDARD_TOKENIZER_FACTORY acts like Lucene's StandardAnalyzer
	static class StandardTokenizerFactory implements TokenizerFactory {
		public Tokenizer tokenizer(char[] cs, int start, int length) {
			Tokenizer tokenizer = SIMPLE_TOKENIZER_FACTORY.tokenizer(cs,start,length);
			tokenizer = new LowerCaseFilterTokenizer(tokenizer);
			tokenizer = new EnglishStopListFilterTokenizer(tokenizer);
			tokenizer = new PorterStemmerFilterTokenizer(tokenizer);
			return tokenizer;
		}
	} 
	public TokenizerFactory STANDARD_TOKENIZER_FACTORY
		= new StandardTokenizerFactory();


}
