package com.lingpipe.book.tok;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.SoundexTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class SoundexTokens {

    public static void main(String[] args) {
        String text = args[0];
        
        /*x SoundexTokens.1 */
        TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory f2 = new SoundexTokenizerFactory(f1);
        /*x*/

        DisplayTokens.displayTextPositions(text);
        DisplayTokens.displayTokens(text,f2);
    }

}