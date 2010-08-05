package com.lingpipe.book.tok;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.analysis.tokenattributes.TermAttribute;

import org.apache.lucene.util.Version;


import java.io.CharArrayReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.io.Serializable;

public class AnalyzerTokenizerFactory 
    implements TokenizerFactory, Serializable {

    private final Analyzer mAnalyzer;
    private final String mFieldName;

    public AnalyzerTokenizerFactory(Analyzer analyzer,
                                    String fieldName) {
        mAnalyzer = analyzer;
        mFieldName = fieldName;
    } 

    public Tokenizer tokenizer(char[] cs, int start, int len) {
        Reader reader = new CharArrayReader(cs,start,len);
        TokenStream tokenStream 
            = mAnalyzer.tokenStream(mFieldName,reader);
        return new TokenStreamTokenizer(tokenStream);
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class TokenStreamTokenizer 
        extends Tokenizer 
        implements Closeable {
        private final TokenStream mTokenStream;
        private final TermAttribute mTermAttribute;
        public TokenStreamTokenizer(TokenStream tokenStream) {
            mTokenStream = tokenStream;
            mTermAttribute 
                = mTokenStream.addAttribute(TermAttribute.class);
        }
        @Override
        public String nextToken() {
            try {
                if (mTokenStream.incrementToken()) {
                    return getNextToken();
                } else {
                    close();
                    return null;
                }
            } catch (IOException e) {
                close();
                return null;
            }
        }
        public void close() {
            try {
                mTokenStream.end();
            } catch (IOException e) {
                /* no op */
            } finally {
                Streams.closeQuietly(mTokenStream);
            }
        }
        String getNextToken() {
            return mTermAttribute.term();
        }    
    }

    public static void main(String[] args) throws IOException {
        String text = args[0];

        StandardAnalyzer analyzer 
            = new StandardAnalyzer(Version.LUCENE_30);
        AnalyzerTokenizerFactory tokFact
            = new AnalyzerTokenizerFactory(analyzer,"text");

        char[] cs = text.toCharArray();
        Tokenizer tokenizer = tokFact.tokenizer(cs,0,cs.length);

        for (String token : tokenizer)
            System.out.println(token);

    }

    static final long serialVersionUID = -1953835346323331784L;

    static class Serializer extends AbstractExternalizable {
        final AnalyzerTokenizerFactory mFactory;
        public Serializer() { 
            this(null); 
        }
        Serializer(AnalyzerTokenizerFactory factory) {
            mFactory = factory;
        }
        @Override
        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {
            @SuppressWarnings("unchecked")
            Analyzer analyzer = (Analyzer) in.readObject();
            String fieldName = in.readUTF();
            return new AnalyzerTokenizerFactory(analyzer,fieldName);
        }
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mFactory.mAnalyzer);
            out.writeUTF(mFactory.mFieldName);
        }
        static final long serialVersionUID = -7760363964471913868L;
    }

}