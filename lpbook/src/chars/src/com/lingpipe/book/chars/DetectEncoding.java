package com.lingpipe.book.chars;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.io.*;

public class DetectEncoding {

    public static void main(String[] args) 
        throws IOException, UnsupportedEncodingException {

        String declared = "Latin1";
        String s = "D\u00E9j\u00E0 vu.";
        String[] encodings = { "UTF-8", "UTF-16", "ISO-8859-1" };
        for (String encoding : encodings) {
            byte[] bs = s.getBytes(encoding);
            CharsetDetector detector = new CharsetDetector();
            detector.setText(bs);
            CharsetMatch[] matches = detector.detectAll();
            System.out.printf("\nencoding=%s # matches=%d\n",encoding,matches.length);
            for (CharsetMatch match : matches) {
                String name = match.getName();
                int conf = match.getConfidence();
                String lang = match.getLanguage();
                String text = "foo"; // match.getString();
                System.out.printf("     guess=%s conf=%d lang=%s text=%s\n",
                                  name,conf,lang,text);
            }
        }
    }

}