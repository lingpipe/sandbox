package com.lingpipe.book.tok;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.ArrayList;
import java.util.List;

class FragmentsTok {

    public static void frag1() { 
        char[] cs = new char[0];
        TokenizerFactory tokFactory 
            = IndoEuropeanTokenizerFactory.INSTANCE;

        /*x FragmentsTok.1 */
        Tokenizer tokenizer = tokFactory.tokenizer(cs,0,cs.length);
        String[] tokens = tokenizer.tokenize();
        /*x*/
    }

    public static void frag2() {
        char[] cs = new char[0];
        TokenizerFactory tokFactory 
            = IndoEuropeanTokenizerFactory.INSTANCE;
        
        /*x FragmentsTok.2 */
        Tokenizer tokenizer = tokFactory.tokenizer(cs,0,cs.length);
        List<String> tokens = new ArrayList<String>();
        List<String> whitespaces = new ArrayList<String>();
        tokenizer.tokenize(tokens,whitespaces);
        /*x*/
    }
    

}