package com.lingpipe.book.tok;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;
import java.io.Serializable;

import java.util.regex.Pattern;

/*x PennTreebankTokenizerFactory.1 */
public class PennTreebankTokenizerFactory
    extends ModifyTokenTokenizerFactory
    implements Serializable {

    public static final TokenizerFactory INSTANCE
        = new PennTreebankTokenizerFactory();

    private PennTreebankTokenizerFactory() {
        super(new RegExTokenizerFactory(BASE_REGEX,
                                        Pattern
                                        .CASE_INSENSITIVE));
    }
/*x*/


    // replace quotes at begin-of-string and after separator or start
    // punctuation (Z,Ps) with open-curly quotes, others with
    // close curlies, then replace directional quote punctuation
    // (Pi,Pf) with open and close.
    // does not change length so spans are correct
    /*x PennTreebankTokenizerFactory.2 */
    @Override 
    public Tokenizer tokenizer(char[] cs, int start, int len) {
        String s = new String(cs,start,len)
            .replaceAll("(^\")","\u201C")
            .replaceAll("(?<=[ \\p{Z}\\p{Ps}])\"","\u201C")
            .replaceAll("\"","\u201D")
            .replaceAll("\\{Pi}","\u201C")
            .replaceAll("\\{Pf}","\u201D");
        return super.tokenizer(s.toCharArray(),0,len);
    }
    /*x*/

    /*x PennTreebankTokenizerFactory.3 */
    @Override
    public String modifyToken(String token) {
        return token
            .replaceAll("\\(","-LRB-").replaceAll("\\)","-RRB-")
            .replaceAll("\\[","-LSB-").replaceAll("\\]","-RSB-")
            .replaceAll("\\{","-LCB-").replaceAll("\\}","-RCB-")
            .replaceAll("\u201C","``").replaceAll("\u201D","''");
    }
    /*x*/

    /*x PennTreebankTokenizerFactory.4 */
    static final String BASE_REGEX
        = "("
        + "\\.\\.\\." + "|" + "--"
        + "|" + "can(?=not\\b)" + "|" + "d'(?=ye\\b)"  
        + "|" + "gim(?=me\\b)" + "|" + "lem(?=me\\b)"  
        + "|" + "gon(?=na\\b)" + "|" + "wan(?=na\\b)"
        + "|" + "more(?='n\\b)" + "|" + "'t(?=is\\b)"  
        + "|" + "'t(?=was\\b)" + "|" + "ha(?=ddya\\b)" 
        + "|" + "dd(?=ya\\b)" + "|" + "ha(?=tcha\\b)"
        + "|" + "t(?=cha\\b)"
        + "|" + "'(ll|re|ve|s|d|m|n)" + "|" + "n't"
        + "|" + "[\\p{L}\\p{N}]+(?=(\\.$|\\.([\\{Pf}\"'])+|n't))"
        + "|" + "[\\p{L}\\p{N}\\.]+"
        + "|" + "[^\\p{Z}]"
        + ")";
    /*x*/

    Object writeReplace() {
        return new Serializer();
    }

    public static void main(String[] args) throws IOException {
        String text = args[0];
        DisplayTokens.displayTextPositions(text);
        TokenizerFactory tf = PennTreebankTokenizerFactory.INSTANCE;
        DisplayTokens.displayTokens(text,tf);

        @SuppressWarnings("unchecked")
        TokenizerFactory deser
            = (PennTreebankTokenizerFactory)
            AbstractExternalizable.serializeDeserialize((Serializable)tf);
        DisplayTokens.displayTokens(text,tf);
            
    }


    static class Serializer extends AbstractExternalizable {
        public Serializer() { }
        public void writeExternal(ObjectOutput out) {
        }
        public Object read(ObjectInput in) {
            return INSTANCE;
        }
        static final long serialVersionUID = 177430127995041985L;
    }

    static final long serialVersionUID = 128686532870207419L;
    
}