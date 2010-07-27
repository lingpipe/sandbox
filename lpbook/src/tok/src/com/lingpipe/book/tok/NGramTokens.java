package com.lingpipe.book.tok;

import com.aliasi.tokenizer.NGramTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.regex.Pattern;

public class NGramTokens {

    public static void main(String[] args) {
        String minNGram = args[0];
        String maxNGram = args[1];
        String text = args[2];

        int minNGramInt = Integer.valueOf(minNGram);
        int maxNGramInt = Integer.valueOf(maxNGram);
        
        System.out.println("minNGram=" + minNGram);
        System.out.println("maxNGram=" + maxNGram);
        System.out.println();
        DisplayTokens.displayTextPositions(text);
        
        /*x NGramTokens.1 */
        NGramTokenizerFactory tokFact
            = new NGramTokenizerFactory(minNGramInt,maxNGramInt);

        DisplayTokens.displayTokens(text,tokFact);
        /*x*/
    }

}