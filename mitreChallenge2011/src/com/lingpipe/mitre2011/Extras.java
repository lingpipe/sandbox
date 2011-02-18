package com.lingpipe.mitre2011;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.util.HashMap;
import java.util.Map;

// WARNING: NOT DEBUGGED

public class Extras {

    static double exactMatchScore(String forename1, String surname1,
                                  String forename2, String surname2) {
        boolean forenameExactMatch = forename1.equals(forename2);
        boolean surnameExactMatch = surname1.equals(surname2);
        double exactMatchScore = 0.0;
        if (forenameExactMatch)
            exactMatchScore += 0.3;
        if (surnameExactMatch)
            exactMatchScore += 0.7;
        return exactMatchScore;
    }

    static double tokenJaccard(String[] tokens1, String[] tokens2) {
        int both = 0;
        int just1 = 0;
        int just2 = 0;
        for (int i = 0; i < tokens1.length; ++i) {
            if (contains(tokens1[i],tokens2))
                ++both;
            else
                ++just1;
        }
        for (int j = 0; j < tokens2.length; ++j) {
            if (!contains(tokens2[j],tokens1))
                ++just2;
        }
        return ((double) both) / (double) (just1 + just2 + both);
    }

    static boolean contains(Object x, Object[] xs) {
        for (int i = 0; i < xs.length; ++i)
            if (xs[i].equals(x))
                return true;
        return false;
    }

    static final TokenizerFactory SPACE_TOK_FACT
        = new RegExTokenizerFactory("[\\p{L}]+");

    static final Map<String,String[]> SPACE_TOK_FACT_CACHE
        = new HashMap<String,String[]>();
    static String[] spaceTokenize(String text) {
        String[] result = SPACE_TOK_FACT_CACHE.get(text);
        if (result != null)
            return result;
        char[] cs = text.toCharArray();
        result = SPACE_TOK_FACT.tokenizer(cs,0,cs.length).tokenize();
        SPACE_TOK_FACT_CACHE.put(text,result);
        return result;
    }


}