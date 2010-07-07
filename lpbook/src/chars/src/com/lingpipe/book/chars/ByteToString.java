package com.lingpipe.book.chars;

import java.io.UnsupportedEncodingException;

public class ByteToString {

    /*x ByteToString.1 */
    public static void main(String[] args) 
        throws UnsupportedEncodingException {

        System.setOut(new java.io.PrintStream(System.out,true,"UTF-8"));

        String s = "D\u00E9j\u00E0 vu";
        System.out.println(s);

        String encode = args[0];
        String decode = args[1];

        byte[] bs = s.getBytes(encode);
        String t = new String(bs,decode);
    /*x*/

        System.out.println("char[] from string");
        dumpString(s);
        System.out.println("byte[] from encoding with " + encode);
        dumpBytes(bs);
        System.out.println("char[] from decoding with " + decode);
        dumpString(t);
    }

    static void dumpBytes(byte[] bs) {
        for (byte b : bs)
            System.out.printf("%4h  ",b >= 0 ? b : 256+b);
        System.out.println();
    }

    static void dumpString(String s) {
        for (char c : s.toCharArray())
            System.out.printf("%4h  ",c);
        System.out.println();
    }

}