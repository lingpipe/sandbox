package com.lingpipe.book.tok;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.regex.Pattern;

public class RegexTokens {

    public static void main(String[] args) {
        String regex = args[0];
        String flags = args[1];
        String text = args[2];

        
        System.out.println("regex=|" + regex + "|");
        System.out.println("flags=" + flags + " (binary " + Integer.toBinaryString(Integer.valueOf(flags)) + ")");
        System.out.println();
        DisplayTokens.displayTextPositions(text);

        /*x RegexTokens.1 */
        int flagInt = Integer.valueOf(flags);
        TokenizerFactory tokFact
            = new RegExTokenizerFactory(regex,flagInt);

        DisplayTokens.displayTokens(text,tokFact);
        /*x*/
    }

}