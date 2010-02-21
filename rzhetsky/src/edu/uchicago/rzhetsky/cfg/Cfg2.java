package edu.uchicago.rzhetsky.cfg;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class Cfg2 {

    private final int[][] mRuleSyms;
    private final int[][] mLexEntrySyms;
    private final String[] mCats;
    private final String[] mWords;

    private final int[] mRuleStarts;
    private final int[] mLexStarts;

    public Cfg2(String[][] rules,
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
        for (int i = 0; i < lexEntries.length; ++i) {
            mLexEntrySyms[i] = new int[2];
            mLexEntrySyms[i][0] = Arrays.binarySearch(mCats,lexEntries[i][0]);
            mLexEntrySyms[i][1] = Arrays.binarySearch(mWords,lexEntries[i][1]);
        }
        Arrays.sort(mRuleSyms,FIRST_RHS_CAT_COMPARATOR);
        Arrays.sort(mLexEntrySyms,FIRST_RHS_CAT_COMPARATOR);
        mRuleStarts = new int[mCats.length];
        mLexStarts = new int[mCats.length];
        // add start indexes
    }

    public String[][] rules() {
        String[][] rules = new String[mRuleSyms.length][];
        for (int i = 0; i < rules.length; ++i) {
            rules[i] = new String[mRuleSyms[i].length];
            for (int k = 0; k < mRuleSyms[i].length; ++k)
                rules[i][k] = mCats[mRuleSyms[i][k]];
        }
        return rules;
    }

    public String[][] lexEntries() {
        String[][] lexEntries = new String[mLexEntrySyms.length][];
        for (int i = 0; i < lexEntries.length; ++i) {
            lexEntries[i] = new String[2];
            lexEntries[i][0] = mCats[mLexEntrySyms[i][0]];
            lexEntries[i][1] = mWords[mLexEntrySyms[i][1]];
        }
        return lexEntries;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RULES\n");
        for (String[] rule : rules()) {
            sb.append(Arrays.asList(rule));
            sb.append('\n');
        } 
        sb.append("LEX ENTRIES\n");
        for (String[] lexEntry : lexEntries()) {
            sb.append(Arrays.asList(lexEntry));
            sb.append('\n');
        }
        return sb.toString();
    }
    

    static final Comparator<int[]> FIRST_RHS_CAT_COMPARATOR
        = new Comparator<int[]>() {
        public int compare(int[] xs, int[] ys) {
            if (xs[1] < ys[1])
                return -1;
            if (xs[1] > ys[1])
                return 1;
            if (xs[0] < ys[0])
                return -1;
            if (xs[0] > ys[0])
                return 1;
            for (int i = 2; i < Math.min(xs.length,ys.length); ++i) {
                if (xs[i] < ys[i])
                    return -1;
                if (xs[i] > ys[i])
                    return 1;
            }
            if (xs.length < ys.length)
                return -1;
            if (xs.length > ys.length)
                return 1;
            return 0;
        }
    };

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