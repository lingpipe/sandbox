package com.lingpipe.book.webchars;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TranscodingServlet extends HttpServlet {

    public static final String DEFAULT_CHARSET = "ISO-8859-1";

    public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
	System.out.println(this.getClass().getName() + " recieved request");

	String reqCharset=req.getParameter("request-charset");
	if (reqCharset == null) reqCharset = DEFAULT_CHARSET;
	System.out.println("request charset: "+reqCharset);

	String respCharset=req.getParameter("response-charset");
	if (respCharset == null) respCharset = DEFAULT_CHARSET;
	System.out.println("response charset: "+respCharset);

	String codedText = req.getParameter("text");
	byte[] rawBytes = codedText.getBytes("ISO-8859-1");
	String text = new String(rawBytes,reqCharset);

	res.setContentType("text/plain; charset=" + respCharset);
	PrintWriter out = res.getWriter();
	out.println(text);
    }
}
