package com.lingpipe.book.chars;

import com.ibm.icu.text.Transliterator;

import java.util.Enumeration;

public class Transliterate {

    public static void main(String[] args) {
        /*x Transliterate.1 */
        String text = args[0];
        String scheme = args[1];
        Transliterator trans = Transliterator.getInstance(scheme);
        String out = trans.transliterate(text);
        /*x*/

        System.out.println("Scheme=" + scheme);
        System.out.println("Input=" + text);
        System.out.println("Output=" + out);
        System.out.println("Output char values");
        for (char c : out.toCharArray())
            System.out.printf("%5h",c);
    }
    
    // System.setOut(new java.io.PrintStream(System.out,true,"UTF-8"));

}