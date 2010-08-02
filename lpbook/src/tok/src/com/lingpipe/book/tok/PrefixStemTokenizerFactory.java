package com.lingpipe.book.tok;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public class PrefixStemTokenizerFactory 
    extends ModifyTokenTokenizerFactory {

    private final int mPrefixLength;

    public PrefixStemTokenizerFactory(TokenizerFactory f, int prefixLength) {
        super(f);
        mPrefixLength = prefixLength;
    }

    @Override
    /*x PrefixStemmer.1 */
    public String modifyToken(String token) {
        return token.length() <= mPrefixLength
            ? token
            : token.substring(0,mPrefixLength);
    /*x*/
    }

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer extends AbstractExternalizable {
        private final PrefixStemTokenizerFactory mStemmer;
        public Serializer() { 
            this(null);
        }
        Serializer(PrefixStemTokenizerFactory stemmer) {
            mStemmer = stemmer;
        }

        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(mStemmer.mPrefixLength);
            out.writeObject(mStemmer.baseTokenizerFactory());
        }
        public Object read(ObjectInput in)
            throws IOException, ClassNotFoundException {
            int prefixLength = in.readInt();
            @SuppressWarnings("unchecked")
            TokenizerFactory f = (TokenizerFactory) in.readObject();
            return new PrefixStemTokenizerFactory(f,prefixLength);
        }
        static final long serialVersionUID = 7337750796142781843L;
    }

    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

        String prefixLen = args[0];
        String text = args[1];

        int prefixLenInt = Integer.valueOf(prefixLen);
        TokenizerFactory f = IndoEuropeanTokenizerFactory.INSTANCE;
        PrefixStemTokenizerFactory tokFact
            = new PrefixStemTokenizerFactory(f,prefixLenInt);
        DisplayTokens.displayTextPositions(text);
        DisplayTokens.displayTokens(text,tokFact);

        @SuppressWarnings("unchecked")
        TokenizerFactory deserTokFact
            = (TokenizerFactory) 
            AbstractExternalizable.serializeDeserialize(tokFact);

        System.out.println("\nAfter Serialization");
        DisplayTokens.displayTokens(text,deserTokFact);
    }

    static final long serialVersionUID = 8257231905932729718L;
}