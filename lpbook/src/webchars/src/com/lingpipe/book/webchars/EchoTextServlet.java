package com.lingpipe.book.webchars;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EchoTextServlet extends HttpServlet {

    public static final String DEFAULT_CHARSET = "ISO-8859-1";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGetOrPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGetOrPost(request, response);
    }

    private void doGetOrPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	System.out.println(this.getClass().getName() + " recieved request");

	String reqCharset=request.getParameter("request-charset");
	if (reqCharset == null) reqCharset = DEFAULT_CHARSET;
	System.out.println("request charset: "+reqCharset);

	String codedText = request.getParameter("text");
	byte[] rawBytes = codedText.getBytes("ISO-8859-1");
	String text = new String(rawBytes,reqCharset);

	System.out.println("codedText: "+codedText);
	printBytes(codedText,"ISO-8859-1");
	System.out.println("text: "+text);
	printChars(text);

	response.setContentType("text/plain; charset=" + reqCharset);
	PrintWriter out = response.getWriter();
	out.println(text);
    }

    static void printChars(String string) throws IOException {
        System.out.println("** printChars in string: " + string);
	for (int i=0; i<string.length(); i++) {
	    System.out.println("char " + i 
			       + " as char: " + string.charAt(i)
			       + " as hex: " + Integer.toHexString((int)string.charAt(i))
			       + " as int: " + (int)string.charAt(i)
			       );
	}
	System.out.println("");
    }


    static void printBytes(String string,String encoding) throws IOException {
	System.out.println("** printBytes in string: " + string
			   + " getBytes encoding: " + encoding);
	byte[] bytes = string.getBytes(encoding);
	for (int i=0; i<bytes.length; i++) {
	    System.out.println("byte " + i 
			       + " as hex: " + Integer.toHexString(0xff & bytes[i]));

	}
	System.out.println("");
    }
}
