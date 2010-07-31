package com.lingpipe.book.tok;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

public class PennTreebankTokenizerFactory
    extends ModifyTokenTokenizerFactory {

    private PennTreebankTokenizerFactory() {
        super(new RegExTokenizerFactory(BASE_REGEX));
    }

    @Override
    public Tokenizer tokenizer(char[] cs, int start, int len) {

        // replace quotes at begin-of-string and after separator or start
        // punctuation (Z,Ps) with open-curly quotes, others with
        // close curlies, then replace directional quote punctuation
        // (Pi,Pf) with open and close.
        // does not change length so spans are correct
        String s = new String(cs,start,len)
            .replaceAll("(^\")","\u201C")
            .replaceAll("(?:[ \\p{Z}\\p{Ps}])\"","\u201C")
            .replaceAll("\"","\u201D")
            .replaceAll("\\{Pi}","\u201C")
            .replaceAll("\\{Pf}","\u201D");
        return super.tokenizer(s.toCharArray(),0,len);
    }

    @Override
    public String modifyToken(String token) {
        return token
            .replaceAll("\\(","-LRB-")
            .replaceAll("\\)","-RRB-")
            .replaceAll("\\[","-LSB-")
            .replaceAll("\\]","-RSB-")
            .replaceAll("\\{","-LCB-")
            .replaceAll("\\}","-RCB-")
            .replaceAll("\u201C","``")
            .replaceAll("\u201D","''");
    }

    public static final TokenizerFactory INSTANCE
        = new PennTreebankTokenizerFactory();

    static final String BASE_REGEX
        = "("
        + "\\.\\.\\."
        + "|" + "--"
        + "|" + "'(?<![\\p{Z}])"
        + "|" + "'(ll|re|ve|s|d|M)" // adding 'm' (only lower) breaks disjunction.  wtf?
        + "|" + "can(?=not)"
        + "|" + "n't|N'T"
        + "|" + "[\\p{L}\\p{N}]+($|(?=(n't|N'T)))"
        + "|" + "[\\p{L}\\p{N}\\.]+"
        + "|" + "[^\\p{Z}]"
        + ")";
    
    public static void main(String[] args) {
        String text = args[0];
        DisplayTokens.displayTextPositions(text);
        TokenizerFactory tf = PennTreebankTokenizerFactory.INSTANCE;
        DisplayTokens.displayTokens(text,tf);
    }




}