package com.lingpipe.book.tok;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Strings;

public class DisplayTokens {

    public static void main(String[] args) {
        String text = args[0];
        /*x DisplayTokens.1 */
        TokenizerFactory tokFact
            = IndoEuropeanTokenizerFactory.INSTANCE;
        displayTextPositions(text);
        displayTokens(text,tokFact);
        /*x*/
    }

    public static void displayTokens(CharSequence in,
                                     TokenizerFactory tokFact) {
        System.out.printf("\n%5s %5s %s\n","START", "END", "TOKEN");

        /*x DisplayTokens.2 */
        char[] cs = Strings.toCharArray(in);
        Tokenizer tokenizer = tokFact.tokenizer(cs,0,cs.length);

        for (String token : tokenizer) {
            int start = tokenizer.lastTokenStartPosition();
            int end = tokenizer.lastTokenEndPosition();
        /*x*/
            System.out.printf("%5d %5d  %s\n",start,end,"|" + token + "|");
        }
    }

    static void displayTextPositions(CharSequence in) {
        System.out.println(in);
        for (int i = 0; i < in.length(); ++i)
            System.out.print(i%10);
        System.out.println();
        if (in.length() < 10) return;
        for (int i = 0; i < in.length(); ++i)
            System.out.print(i % 10 == 0 
                             ? Integer.toString(i/10)
                             : " ");
        System.out.println();
        if (in.length() < 100) return;
        for (int i = 0; i < in.length(); ++i)
            System.out.print(i % 100 == 0
                             ? Integer.toString(i/100)
                             : " ");
        System.out.println();
    }

}