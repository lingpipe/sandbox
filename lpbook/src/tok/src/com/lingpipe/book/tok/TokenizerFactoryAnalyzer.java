package com.lingpipe.book.tok;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.Streams;

import org.apache.lucene.analysis.Analyzer;
// import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.miscellaneous.EmptyTokenStream;

import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TokenizerFactoryAnalyzer  extends Analyzer {

    private final Map<String,TokenizerFactory> mFieldToTokenizerFactory;
    private final TokenizerFactory mDefaultTokenizerFactory;

    // use same tokenizer for all fields
    public TokenizerFactoryAnalyzer(TokenizerFactory tokenizerFactory) {
        this(Collections.<String,TokenizerFactory>emptyMap(),
             tokenizerFactory);
    }
        
    // reset per field
    public TokenizerFactoryAnalyzer(Map<String,TokenizerFactory> fieldToTokenizerFactory,
                                    TokenizerFactory defaultTokenizerFactory) {
        mFieldToTokenizerFactory 
            = new HashMap<String,TokenizerFactory>(fieldToTokenizerFactory);
        mDefaultTokenizerFactory = defaultTokenizerFactory;
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {

        TokenizerTokenStream tokenizer = new TokenizerTokenStream();
        tokenizer.setField(fieldName);
        try {
            tokenizer.reset(reader);
            return tokenizer;
        } catch (IOException e) {
            return new EmptyTokenStream();
        }
    }

    
    @Override
    public TokenStream reusableTokenStream(String fieldName, Reader reader) 
            throws IOException {
        
        TokenStream prevTokenStream = (TokenStream) getPreviousTokenStream();

        if (prevTokenStream == null) {
            TokenStream tokenizer = tokenStream(fieldName,reader);
            setPreviousTokenStream(tokenizer);
        }

        if (!(prevTokenStream instanceof TokenizerTokenStream))
            return prevTokenStream;

        TokenizerTokenStream tokenizer = (TokenizerTokenStream) prevTokenStream;
        tokenizer.setField(fieldName);
        tokenizer.reset(reader);
        return tokenizer;
    }

    class TokenizerTokenStream 
        extends org.apache.lucene.analysis.Tokenizer {

        private TermAttribute mTermAttribute;
        private OffsetAttribute mOffsetAttribute;
        private PositionIncrementAttribute mPositionAttribute;
        
        private String mFieldName;
        private TokenizerFactory mTokenizerFactory;
        private char[] mCs;
        private Tokenizer mTokenizer;
        private int mPosition;

        TokenizerTokenStream() {
            mOffsetAttribute = addAttribute(OffsetAttribute.class);
            mTermAttribute = addAttribute(TermAttribute.class);
            mPositionAttribute = addAttribute(PositionIncrementAttribute.class);
        }
        
        public void setField(String fieldName) {
            mFieldName = fieldName;
        }

        @Override
        public boolean incrementToken() {
            String token = mTokenizer.nextToken();
            if (token == null)
                return false;
            char[] cs = mTermAttribute.termBuffer();
            token.getChars(0,token.length(),cs,0);
            mTermAttribute.setTermLength(token.length());
            mPositionAttribute.setPositionIncrement(mPosition++);
            mOffsetAttribute.setOffset(mTokenizer.lastTokenStartPosition(),
                                       mTokenizer.lastTokenEndPosition());
            return true;

        }
        @Override
        public void end() {
            mOffsetAttribute.setOffset(mTokenizer.lastTokenEndPosition(),
                                       mTokenizer.lastTokenEndPosition());
        }
        @Override
        public void close() {
            mCs = null;
        }

        @Override
        public void reset(Reader reader) throws IOException {
            mCs = Streams.toCharArray(reader);
            mTokenizerFactory
                = mFieldToTokenizerFactory.containsKey(mFieldName)
                ? mFieldToTokenizerFactory.get(mFieldName)
                : mDefaultTokenizerFactory;
            reset();
        }

        @Override
        public void reset() throws IOException {
            if (mCs == null) {
                String msg = "Cannot reset after close().";
                throw new IOException(msg);
            }
            mPosition = 0;
            mTokenizer = mTokenizerFactory.tokenizer(mCs,0,mCs.length);
        }
    }

    public static void main(String[] args) throws IOException {
        String text = args[0];

        Map<String,TokenizerFactory> fieldToTokenizerFactory
            = new HashMap<String,TokenizerFactory>();
        fieldToTokenizerFactory.put("foo",IndoEuropeanTokenizerFactory.INSTANCE);
        fieldToTokenizerFactory.put("bar",new NGramTokenizerFactory(3,3));
        TokenizerFactory defaultFactory
            = new RegExTokenizerFactory("\\S+");
        TokenizerFactoryAnalyzer analyzer
            = new TokenizerFactoryAnalyzer(fieldToTokenizerFactory,
                                           defaultFactory);

        DisplayTokens.displayTextPositions(text);
        
        displayTokens(analyzer,"foo",text);
        displayTokens(analyzer,"bar",text);
        displayTokens(analyzer,"jib",text);
    }

    static void displayTokens(Analyzer analyzer,
                              String fieldName,
                              String text) throws IOException {
        System.out.println("\nField=" + fieldName);
        Reader textReader = new StringReader(text);
        TokenStream tokStream 
            = analyzer.tokenStream(fieldName,textReader);
        TermAttribute termAtt
            = tokStream.addAttribute(TermAttribute.class);
        PositionIncrementAttribute posAtt
            = tokStream.addAttribute(PositionIncrementAttribute.class);
        OffsetAttribute offsetAtt
            = tokStream.addAttribute(OffsetAttribute.class);
        System.out.printf("%5s %5s %5s\n","Pos","Start","End","Token");
        while (tokStream.incrementToken()) {
            System.out.printf("%5d %5d %5d %s\n",
                              posAtt.getPositionIncrement(),
                              offsetAtt.startOffset(),
                              offsetAtt.endOffset(),
                              termAtt.term());
        }
    }
    

}