package com.aliasi.lingmed.genelinkage;

import java.io.*;

import java.sql.SQLException;    
import javax.servlet.ServletContext;

/**
 * Presentation layer for {@link SearchServlet}.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class SearchPresentation {

    private final ServletContext mContext;

    private static final String OPEN_HTML = "<HTML><HEAD><TITLE>LingMed</TITLE></HEAD><BODY>";
    private static final String CLOSE_HTML = "</BODY></HTML>";

    SearchPresentation(ServletContext context) {
	mContext = context;
    }

    // generate HTML for user search
    public String genSearchForm() throws IOException {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append("<H1>Article Search</H1>");
	html.append("<P>Find articles for gene by EntrezGene id.</P>");
	html.append("<TABLE WIDTH=\"75%\" CELLPADDING=10>");
	html.append("<TR>");
	html.append("<TD>");
	html.append("<FORM NAME=\"search\" ACTION=\"/genelinkage/search/byGeneId\" METHOD=\"POST\">");
	html.append("Name: <INPUT TYPE=\"TEXT\" NAME=\"geneId\">");
	html.append("<BR>");
	html.append("Max articles: <INPUT TYPE=\"TEXT\" NAME=\"limit\">");
	html.append("<BR>");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"search\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("</TR>");
	html.append("</TABLE>");
	return html.toString();
    }


    // generate HTML for error page
    public String genErrorPage(String message, Throwable cause) {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append("<P>");
	html.append("error: "+message);
	html.append("<P>");
	StackTraceElement[] stack = cause.getStackTrace();
	for (StackTraceElement e : stack) html.append(e.toString()+"\n");
	html.append(CLOSE_HTML);
	return html.toString();
    }
}