package com.lingpipe.book.webchars;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that echos POST data, using encoding set in request */

public class EncodingServlet extends HttpServlet {

    public static final String DEFAULT_CHARSET = "ISO-8859-1";

    public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {

	// print request header info to console
	System.out.println(this.getClass().getName() + " recieved request");
	System.out.println("** request headers **");
	Enumeration names = req.getHeaderNames();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    Enumeration values = req.getHeaders(name);  // support multiple values
	    if (values != null) {
		while (values.hasMoreElements()) {
		    String value = (String) values.nextElement();
		    System.out.println(name + ": " + value);
		}
	    }
	}
	System.out.println("** end request headers **");

	// process request header
	String reqContentType = req.getHeader("Content-type");
	String reqCharset = parseCharset(reqContentType);
	System.out.println("content-type is: "+reqContentType);
	System.out.println("using charset: "+reqCharset);

	// get request data as bytes
	InputStream in = req.getInputStream();
	ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
	int b;
	while ((b = in.read()) != -1) {
	    bytesOut.write((byte)b);
	}
	byte[] bytes = bytesOut.toByteArray();

	// convert bytes to string
	String respCharset = reqCharset;
	String respText = new String(bytes,respCharset);

	// echo request data to server console
	System.out.println("** POST data **");
	for (int i = 0; i < bytes.length; i++) {
	    System.out.println("byte " + i 
			       + " as hex: " + Integer.toHexString(0xff & bytes[i]));
	}
	System.out.println("** end POST data **");
	System.out.println("** response string **");
	System.out.println(respText);
	System.out.println("** end response string **");

	// send response
	res.setContentType("text/plain; charset="+respCharset);
	PrintWriter out = res.getWriter();
	out.print(respText);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
	doPost(req,res);
    }

    static String parseCharset(String contentType) {
	if (contentType == null) return DEFAULT_CHARSET;
	Pattern pattern = Pattern.compile(".*charset=(.*)");
	Matcher matcher = pattern.matcher(contentType);
	if (matcher.matches()) return matcher.group(1);
	return DEFAULT_CHARSET;
    }

}
