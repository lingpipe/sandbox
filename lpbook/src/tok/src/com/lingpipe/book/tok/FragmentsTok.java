package com.lingpipe.book.tok;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.RegExFilteredTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import java.util.regex.Pattern;

class FragmentsTok {

    void frag1() { 
        char[] cs = new char[0];
        TokenizerFactory tokFactory 
            = IndoEuropeanTokenizerFactory.INSTANCE;

        /*x FragmentsTok.1 */
        Tokenizer tokenizer = tokFactory.tokenizer(cs,0,cs.length);
        String[] tokens = tokenizer.tokenize();
        /*x*/
    }

    void frag2() {
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
    

    void frag3() {
        TokenizerFactory tokFac = null;
        char[] cs = null;
        int start = 0;
        int length = 0;
        /*x FragmentsTok.3 */
        Tokenizer tokenizer = tokFac.tokenizer(cs,start,length);
        for (String token : tokenizer) { 
            // do something
        }
        /*x*/
    }

    TokenizerFactory baseTokenizerFactory() {
        return null;
    }

    void frag4() {
        /*x FragmentsTok.5 */
        TokenizerFactory f = IndoEuropeanTokenizerFactory.INSTANCE;
        Locale loc = Locale.GERMAN;
        TokenizerFactory fact = new LowerCaseTokenizerFactory(f,loc);
        /*x*/
    }

    void frag5() {
        /*x FragmentsTok.6 */
        Set<String> stopSet = CollectionUtils.asSet("the","be");
        TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory f2 = new LowerCaseTokenizerFactory(f1);
        TokenizerFactory f3 = new StopTokenizerFactory(f2,stopSet);
        /*x*/
    }

    /*x FragmentsTok.7 */
    static <E> HashSet<E> asSet(E... es) {
    /*x*/
        return null;
    }


    void frag6() {
        TokenizerFactory f = null;
        /*x FragmentsTok.8 */
        Pattern p = Pattern.compile("\\p{Lu}.*");
        TokenizerFactory f2 = new RegExFilteredTokenizerFactory(f,p);
        /*x*/
    }

    void frag7() {
        /*x FragmentsTok.9 */
        /*x*/
    }

    abstract class Bar {
        /*x FragmentsTok.4 */
        protected abstract Tokenizer modify(Tokenizer tokenizer);

        public Tokenizer tokenizer(char[] cs, int start, int length) {
            TokenizerFactory tokFact = baseTokenizerFactory();
            Tokenizer tok = tokFact.tokenizer(cs,start,length);
            return modify(tok);
        }
        /*x*/
    }



}