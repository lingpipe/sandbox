package com.lingpipe.book.webchars;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that reports request */

public class EchoRequestServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGetOrPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGetOrPost(request, response);
    }

    private void doGetOrPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	// print request header
        PrintWriter out = response.getWriter();
	//	out.println(this.getClass().getName() + " recieved request");
	out.println("** request headers **");
	Enumeration names = request.getHeaderNames();
	while (names.hasMoreElements()) {
	    String name = (String) names.nextElement();
	    Enumeration values = request.getHeaders(name);  // support multiple values
	    if (values != null) {
		while (values.hasMoreElements()) {
		    String value = (String) values.nextElement();
		    out.println(name + ": " + value);
		}
	    }
	}
	out.println("** done **");
        out.close();
    }

}
