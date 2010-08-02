package com.lingpipe.book.tok;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class PorterStemmerTokens {

    public static void main(String[] args) {
        String text = args[0];
        
        /*x PorterStemmerTokens.1 */
        TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory f2 = new PorterStemmerTokenizerFactory(f1);
        /*x*/

        DisplayTokens.displayTextPositions(text);
        DisplayTokens.displayTokens(text,f2);
    }

}