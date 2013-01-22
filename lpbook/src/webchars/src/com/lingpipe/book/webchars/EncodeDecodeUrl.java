package com.lingpipe.book.webchars;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;

import java.net.URLEncoder;
import java.net.URLDecoder;

public class EncodeDecodeUrl {

    public static void main(String[] args)  
        throws IOException, UnsupportedEncodingException {
        /*x EncodeDecodeUrl.1 */
        String s1 = "\u00c0 votre sant\u00e9!";
        String s2 = URLEncoder.encode(s1,"UTF-8");
        String s3 = URLDecoder.decode(s2,"UTF-8");
        /*x*/
        OutputStreamWriter outWriter 
	    = new OutputStreamWriter(System.out,"UTF-8");
        PrintWriter out 
	    = new PrintWriter(outWriter,true);
        out.println("string: " + s1);
        out.println("url encoded: " + s2);
        out.println("decoded: " + s3);
	out.close();
    }
}