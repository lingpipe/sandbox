package com.lingpipe.book.intro;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class Toast {
    public static String CHEERS = "cheers";
    public static String SANTE = "sant\u00e9";
    public static String SKOL = "sk\u00e5l";
    public static String NAZDROV = "\u041d\u0430 \u0437\u0434\u043e\u0440\u0432\u044c";
    public static String KANPAI = "\u4e73\u676f";
    public static String GAMBAE = "\uac74\ubc30";

    public static void main(String[] args) 
        throws IOException {

        OutputStream outStream = new FileOutputStream("toast.txt");
        OutputStreamWriter outWriter = new OutputStreamWriter(outStream,"UTF-8");
        PrintWriter out = new PrintWriter(outWriter,true);
	out.println("in English: " + CHEERS);
	out.println("in French:  " + SANTE);
	out.println("in Norwegian: " + SKOL);
	out.println("in Russian: " + NAZDROV);
	out.println("in Japanese: " + KANPAI);
	out.println("in Korean: " + GAMBAE);
	out.close();
    }
}
