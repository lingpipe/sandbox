package com.lingpipe.book.tok;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import com.ibm.icu.text.Transliterator;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/*x UnicodeNormTokenizerFactory.1 */
public class UnicodeNormTokenizerFactory 
    extends ModifyTokenTokenizerFactory {

    private final String mScheme;
    private final Transliterator mTransliterator;

    public UnicodeNormTokenizerFactory(String scheme,
                                       TokenizerFactory f) {
        super(f);
        mScheme = scheme;
        mTransliterator = Transliterator.getInstance(scheme);
    }

    public String modifyToken(String in) {
        return mTransliterator.transliterate(in);
    }
/*x*/

    Object writeReplace() {
        return new Serializer(this);
    }

    static class Serializer extends AbstractExternalizable {
        final UnicodeNormTokenizerFactory mFactory;
        public Serializer() { 
            this(null);
        }
        public Serializer(UnicodeNormTokenizerFactory f) {
            mFactory = f;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeUTF(mFactory.mScheme);
            out.writeObject(mFactory.baseTokenizerFactory());
        }

        public Object read(ObjectInput in) 
            throws IOException, ClassNotFoundException {
            String scheme = in.readUTF();
            @SuppressWarnings("unchecked")
            TokenizerFactory f =
                (TokenizerFactory) in.readObject();
            return new UnicodeNormTokenizerFactory(scheme,f);
        }
        static final long serialVersionUID = -8867574612350665755L;
    }

    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

        String translitScheme = args[0];
        String text = args[1];

        System.setOut(new java.io.PrintStream(System.out,true,"UTF-8"));

        TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
        UnicodeNormTokenizerFactory f2
            = new UnicodeNormTokenizerFactory(translitScheme,f1);

        System.out.println("translitScheme=" + translitScheme);
        DisplayTokens.displayTextPositions(text);
        DisplayTokens.displayTokens(text,f2);

        TokenizerFactory deserF
            = (TokenizerFactory)
            AbstractExternalizable.serializeDeserialize(f2);
        System.out.println("\nSerialized/Deserialized Output");
        DisplayTokens.displayTokens(text,deserF);


    }

    static final long serialVersionUID = 8989033400387209930L;

}