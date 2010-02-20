package edu.uchicago.rzhetsky.cfg;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Cfg {

    private final int[][] mRuleSyms;
    private final int[][] mLexEntrySyms;
    private final String[] mCats;
    private final String[] mWords;

    public Cfg(int[][] ruleSyms,
               int[][] lexEntrySyms,
               String[] cats,
               String[] words) {
        mRuleSyms = copy(ruleSyms);
        mLexEntrySyms = copy(lexEntrySyms);
        mCats = copy(cats);
        mWords = copy(words);
    }

    public Cfg(String[][] rules,
               String[][] lexEntries) {

        for (int i = 0; i < rules.length; ++i) {
            if (rules[i].length < 2) {
                String msg = "Rules must be at least length 2."
                    + "rules[" + i + "]=" + Arrays.asList(rules[i]);
                throw new IllegalArgumentException(msg);
            }
        }
        for (int i = 0; i < lexEntries.length; ++i) {
            if (lexEntries[i].length != 2) {
                String msg = "Lex entries must be length 2."
                    + " Found lexEntries[" + i + "]=" + Arrays.asList(lexEntries[i]);
                throw new IllegalArgumentException(msg);
            }
        }
            

        Set<String> catSet = new HashSet<String>();
        for (String[] rule : rules)
            for (String cat : rule)
                catSet.add(cat);
        for (String[] lex : lexEntries)
            catSet.add(lex[0]);
        
        Set<String> wordSet = new HashSet<String>();
        for (String[] lex : lexEntries)
            wordSet.add(lex[1]);


        mCats = toSortedArray(catSet);
        mWords = toSortedArray(wordSet);
        mRuleSyms = new int[rules.length][];
        for (int i = 0; i < rules.length; ++i) {
            mRuleSyms[i] = new int[rules[i].length];
            for (int k = 0; k < rules[i].length; ++k)
                mRuleSyms[i][k] = Arrays.binarySearch(mCats,rules[i][k]);
        }
        mLexEntrySyms = new int[lexEntries.length][];
        for (int i = 0; i < rules.length; ++i) {
            mLexEntrySyms[i] = new int[2];
            mLexEntrySyms[i][0] = Arrays.binarySearch(mCats,lexEntries[i][0]);
            mLexEntrySyms[i][1] = Arrays.binarySearch(mWords,lexEntries[i][1]);
        }
        

    }

    static String[] toSortedArray(Set<String> xSet) {
        String[] xs = xSet.toArray(new String[xSet.size()]);
        Arrays.sort(xs);
        return xs;
    }

    static int[][] copy(int[][] xs) {
        int[][] copy = new int[xs.length][];
        for (int i = 0; i < xs.length; ++i)
            copy[i] = copy(xs[i]);
        return copy;
    }

    static String[] copy(String[] xs) {
        String[] copy = new String[xs.length];
        for (int i = 0; i < xs.length; ++i)
            copy[i] = xs[i];
        return copy;
    }

    static int[] copy(int[] xs) {
        int[] copy = new int[xs.length];
        for (int i = 0; i < xs.length; ++i)
            copy[i] = xs[i];
        return copy;
    }


}