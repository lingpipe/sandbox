package com.lingpipe.book.tok;

public class PennTreebankTokenizerFactory
    extends ModifyTokenTokenizerFactory {

    private PennTreebankTokenizerFactory() {
        super(new RegExTokenizerFactory(BASE_REGEX));
    }

    @Override
    public Tokenizer tokenizer(char[] cs, int start, int len) {

        // replace quotes at begin-of-string and after start
        // punctuation (Zs,Ps) with open-curly quotes, others with
        // close curlies, then replace directional punctuation
        // (Pi,Pf) with open and close.
        // does not change length so spans are correct
        String s = new String(cs,start,len)
            .replaceAll("(^\")","\u201C")
            .replaceAll("(?:[\\p{Zs}\\p{Ps}])\"","\u201C")
            .replaceAll("\"","\u201D")
            .replaceAll("\\{Pi}","\u201C")
            .replaceAll("\\{Pf}","\u201D");
        return super.tokenizer(s.toCharArray(),0,len);
    }

    @Override
    public String modifyToken(String token) {
        return token
            .replaceAll("(","-LRB-")
            .replaceAll(")","-RRB-")
            .replaceAll("[","-LSB-")
            .replaceAll("]","-RSB-")
            .replaceAll("{","-LCB-")
            .replaceAll("}","-RCB-")
            .replaceAll("\u201C","``")
            .replaceAll("\u201D","''");
    }

    public static final TokenizerFactory INSTANCE
        = new PennTreebankTokenizerFactory();


    static final String BASE_REGEX = "(\.\.\.";
    

}