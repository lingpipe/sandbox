package com.lingpipe.book.chars;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class ByteToString {

    public static void main(String[] args) 
        throws UnsupportedEncodingException, IOException {

        String dv = "D\u00E9j\u00E0a vu";  

        String[] encodings = { "UTF-8", "UTF-16", "Latin1", "ASCII" };

        for (String enc : encodings)
            dumpBytes(dv,enc);

        for (String enc1 : encodings)
            for (String enc2 : encodings)
                codec(dv,enc1,enc2);
    }

    static void dumpBytes(String s, String encoding) 
        throws IOException {
        byte[] bs = s.getBytes(encoding);
        System.out.println("\nEncoding=" + encoding);
        for (int i = 0; i < bs.length; ++i)
            System.out.print(bs[i] + " ");
        System.out.println();
    }

    static void codec(String s, String enc1, String enc2) 
        throws IOException {
        byte[] bs = s.getBytes(enc1);
        String s2 = new String(bs,enc2);
        System.out.println("\n" + enc1 + "->" + enc2 + "  OK=" + (s.equals(s2)));
        dump(s);
        dump(s2);
    }

    static void dump(String s) {
        for (int i = 0; i < s.length(); ++i)
            System.out.printf("%6h", (int)s.charAt(i));
        System.out.println();
    }

}