package com.lingpipe.book.tok;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class PorterStemmerTokens {

    public static void main(String[] args) {
        String text = args[0];
        
        /*x PorterStemmerTokens.1 */
        TokenizerFactory f = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory tokFact
            = new PorterStemmerTokenizerFactory(f);
        /*x*/

        DisplayTokens.displayTextPositions(text);
        DisplayTokens.displayTokens(text,tokFact);
    }

}