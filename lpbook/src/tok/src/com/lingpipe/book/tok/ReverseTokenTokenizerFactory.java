package com.lingpipe.book.tok;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.aliasi.util.AbstractExternalizable;

/*x ReverseTokenTokenizerFactory.1 */
public class ReverseTokenTokenizerFactory 
    extends ModifyTokenTokenizerFactory {
    
    public ReverseTokenTokenizerFactory(TokenizerFactory f) {
        super(f);
    }

    public String modifyToken(String tok) {
        return new StringBuilder(tok).reverse().toString();
    }
/*x*/

    Object writeReplace() {
        return new Serializer(this);
    }
    
    static class Serializer extends AbstractExternalizable {
        final ReverseTokenTokenizerFactory mFactory;
        public Serializer() { 
            this(null); 
        }
        public Serializer(ReverseTokenTokenizerFactory f) {
            mFactory = f;
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mFactory.baseTokenizerFactory());
        }
        public Object read(ObjectInput in) throws IOException, ClassNotFoundException {
            @SuppressWarnings("unchecked")
            TokenizerFactory f = (TokenizerFactory) in.readObject();
            return new ReverseTokenTokenizerFactory(f);
        }
        static final long serialVersionUID = -4806605436671620412L;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String text = args[0];
        /*x ReverseTokenTokenizerFactory.2 */
        TokenizerFactory baseTokFact
            = IndoEuropeanTokenizerFactory.INSTANCE;
        ReverseTokenTokenizerFactory tokFact
            = new ReverseTokenTokenizerFactory(baseTokFact);
        DisplayTokens.displayTextPositions(text);
        DisplayTokens.displayTokens(text,tokFact);
        /*x*/

        TokenizerFactory deserFact
            = (TokenizerFactory)
            AbstractExternalizable.serializeDeserialize(fact);
        DisplayTokens.displayTokens(text,deserFact);
    }

    static final long serialVersionUID = -8336080711361974247L;

}