package com.lingpipe.book.tok;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.regex.Pattern;

public class PennTreebankTokenizerFactory
    extends ModifyTokenTokenizerFactory {

    private PennTreebankTokenizerFactory() {
        super(new RegExTokenizerFactory(BASE_REGEX,
                                        Pattern.CASE_INSENSITIVE));
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
        + "|" + "can(?=not )"
        + "|" + "d'(?=ye )"
        + "|" + "gim(?=me )"
        + "|" + "lem(?=me )"
        + "|" + "gon(?=na )"
        + "|" + "wan(?=na )"
        + "|" + "more(?='n )"
        + "|" + "'t(?=is )"
        + "|" + "'t(?=was )"
        + "|" + "ha(?=ddya )"
        + "|" + "dd(?=ya )" // (?<ha)
        + "|" + "ha(?=tcha )"
        + "|" + "t(?=cha )" // (?<ha)
        + "|" + "'(ll|re|ve|s|d|m|n)"
        + "|" + "n't"
        + "|" + "'(?<![\\p{Z}])"
        + "|" + "[\\p{L}\\p{N}]+($|(?=(n't)))"
        + "|" + "[\\p{L}\\p{N}\\.]+"
        + "|" + "[^\\p{Z}]"
        + ")";

    // c'mon, this needs to be richer -- what about y'all?  

    public static void main(String[] args) {
        String text = args[0];
        DisplayTokens.displayTextPositions(text);
        TokenizerFactory tf = PennTreebankTokenizerFactory.INSTANCE;
        DisplayTokens.displayTokens(text,tf);
    }


    static followedBy(String tok, String next) {
        return tok + "(?=" + next + ")"; // needs escapes!
    }

    static triple(String token, String tokenNext, String token3) {
        return followedBy(token,tokenNext+token3)
            + "|"
            + followedBy(tokenNext,token3);
    }




}