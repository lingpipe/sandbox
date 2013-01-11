package com.lingpipe.book.webchars;


import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class TranscodeFileServlet extends HttpServlet {

    public static final String DEFAULT_CHARSET = "ISO-8859-1";
    public static final int MAX_FILE_SIZE = 10000;

    public void doPost(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
	System.out.println(this.getClass().getName() + " recieved request");

	// print request header info to console
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

	String reqCharset = DEFAULT_CHARSET;
	String respCharset = DEFAULT_CHARSET;
	byte[] fileBytes = null;

	try {
	    // Create a factory for disk-based file items
	    DiskFileItemFactory factory = new DiskFileItemFactory();
	    // Create a new file upload handler
	    ServletFileUpload upload = new ServletFileUpload(factory);
	    List items = upload.parseRequest(req);
	    Iterator iter = items.iterator();
	    while (iter.hasNext()) {
		DiskFileItem item = (DiskFileItem) iter.next();
		if (item.isFormField()) {
		    String name = item.getFieldName();
		    if (name.equals("request-charset")) {
			reqCharset = item.getString();
		    }
		    else if (name.equals("response-charset")) {
			respCharset = item.getString();
		    }
		} else {  // item is uploaded file
		    fileBytes = item.get();
		}
	    }
	    if (fileBytes == null) {
		res.getWriter().println("error processing form data");
		System.out.println("form missing input file");
		return;
	    }
	} catch (FileUploadException fue) {
	    res.getWriter().println("error processing form data");
	    System.out.println(fue.getMessage());
	    return;
	}

	String text = new String(fileBytes,reqCharset);

	printBytes(text,reqCharset);
	
	res.setContentType("text/plain; charset=" + respCharset);
	PrintWriter out = res.getWriter();
	out.println(text);
    }

    static void printBytes(String string,String encoding) throws IOException {
	System.out.println("** printBytes in string: " + string
			   + " getBytes encoding: " + encoding);
	byte[] bytes = string.getBytes(encoding);
	for (int i=0; i<bytes.length; i++) {
	    System.out.println("byte " + i 
			       + " as hex: " + Integer.toHexString(0xff & bytes[i]));

	}
    }


}
