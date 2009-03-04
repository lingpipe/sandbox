package com.aliasi.webnotes;

import java.io.*;

import java.sql.SQLException;    
import javax.servlet.ServletContext;

public class WikiPresentation {

    private final ServletContext mContext;

    private static final String HTMLDIR = "/WEB-INF/pages/";
    private static final String OPEN_HTML = "<HTML><HEAD><TITLE>AnnotationWiki</TITLE></HEAD><BODY>";
    private static final String CLOSE_HTML = "</BODY></HTML>";

    WikiPresentation(ServletContext context) {
	mContext = context;
    }

    // generate HTML for article, user's first pass at annotation
    public String genArticle(UserRec user, ArticleRec article) throws IOException {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append(htmlJs());
	html.append(html5Buttons(user,article,""));
	String filename = HTMLDIR+article.ordering()+".html";
	InputStream in = mContext.getResourceAsStream(filename);
	html.append(getContent(in));
	html.append(CLOSE_HTML);
	return html.toString();
    }

    // re-generate HTML for article, user's first pass at annotation
    public String genArticle(UserRec user, ArticleRec article, String msg) throws IOException {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append(htmlJs());
	html.append(html5Buttons(user,article,""));
	html.append("<P>");
	html.append(msg);
	html.append("<P>");
	String filename = HTMLDIR+article.ordering()+".html";
	InputStream in = mContext.getResourceAsStream(filename);
	html.append(getContent(in));
	html.append(CLOSE_HTML);
	return html.toString();
    }

    // generate HTML for error page
    public String genError(String message, Throwable cause) {
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

    // generate HTML for help page
    public String genHelp(UserRec user) throws IOException {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append(html3Buttons(user));
	html.append("<h1>Article Annotation Help</h1>");
	String filename = HTMLDIR+"help.html";
	InputStream in = mContext.getResourceAsStream(filename);
	html.append(getContent(in));
	html.append(CLOSE_HTML);
	return html.toString();
    }

    // generate HTML for list of all articles annotated by user
    public String genList(UserRec user, ArticleRec[] articles, int[] cts) {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append(html3Buttons(user));
	html.append("<h3>Articles annotated by "+user.name()+"</h3>");
	html.append("<UL>");
	for (int i=0; i<articles.length; i++) {
	    html.append("<LI>"+articles[i].ordering()+": "
			+"<A HREF=\"/annotation/wiki/revise?username="
			+user.name()+"&article="+articles[i].id()+"\"> "
			+articles[i].pmid()+" </A>"
			+"( annotations: "+cts[i]+")");
	}
	html.append(CLOSE_HTML);
	return html.toString();
    }

    // generate HTML for user login page
    public String genLogin() throws IOException {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append("<h1>AnnotationWiki</h1>");
	html.append("<TABLE WIDTH=\"75%\" CELLPADDING=10>");
	html.append("<TR>");
	html.append("<TD>");
	html.append("<FORM NAME=\"login\" ACTION=\"/annotation/wiki/login\" METHOD=\"POST\">");
	html.append("Name: <INPUT TYPE=\"TEXT\" NAME=\"username\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Login\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("</TR>");
	html.append("</TABLE>");
	html.append("<HR>");
	String filename = HTMLDIR+"help.html";
	InputStream in = mContext.getResourceAsStream(filename);
	html.append(getContent(in));
	html.append(CLOSE_HTML);
	return html.toString();
    }

    // generate HTML for article, user revision of existing annotation
    public  String genReviseArticle(UserRec user, ArticleRec article, String annotations) throws IOException { 
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append(htmlJs());
	html.append(html5Buttons(user,article,annotations));
	html.append("<P>Previous annotations: "+annotations+" <P>");
	String filename = HTMLDIR+article.ordering()+".html";
	InputStream in = mContext.getResourceAsStream(filename);
	html.append(getContent(in));
	html.append(CLOSE_HTML);
	return html.toString();
    }

    // generate HTML for task completion page
    public  String genThankYou(UserRec user) {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append(html3Buttons(user));
	html.append("<P>");
	html.append(user.name()+"there are no more articles for you to annotate.<br>");
	html.append("Thank you for your help.");
	html.append("<P>");
	html.append(CLOSE_HTML);
	return html.toString();
    }

    public String getContent(InputStream in) throws IOException {
	StringBuffer html = new StringBuffer();
	byte[] buf = new byte[8096];
	int numBytesRead;
	while ((numBytesRead = in.read(buf)) >= 0) {
	    String s = new String(buf,0,numBytesRead);
	    html.append(s);
	}
	html.append("\n");
	return html.toString();
    }

    public String htmlJs() throws IOException {
	StringBuffer html = new StringBuffer();
	html.append("<SCRIPT TYPE=\"TEXT/JAVASCRIPT\">");
	String filename = HTMLDIR+"wz_tooltip.js";
	InputStream in = mContext.getResourceAsStream(filename);
	html.append(getContent(in));
	html.append("</SCRIPT>");
	return html.toString();
    }

    public String html3Buttons(UserRec user) {
	StringBuffer html = new StringBuffer();
	html.append("<TABLE WIDTH=\"75%\">");
	html.append("<TR>");
	html.append("<TD>");
	html.append("<FORM NAME=\"list\" ACTION=\"/annotation/wiki/list\" METHOD=\"POST\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"username\" VALUE=\""+user.name()+"\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Review Annotations\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("<TD>");
	html.append("<FORM NAME=\"logout\" ACTION=\"/annotation/wiki/logout\" METHOD=\"POST\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"username\" VALUE=\""+user.name()+"\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Logout\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("<TD>");
	html.append("<FORM NAME=\"help\" ACTION=\"/annotation/wiki/help\" METHOD=\"POST\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"username\" VALUE=\""+user.name()+"\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Help\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("</TR>");
	html.append("</TABLE>");
	return html.toString();
    }

    public String html5Buttons(UserRec user, ArticleRec article, String textGenes) {
	StringBuffer html = new StringBuffer();
	html.append("<TABLE WIDTH=\"75%\">");
	html.append("<TR>");
	html.append("<TD>");
	html.append("<FORM NAME=\"list\" ACTION=\"/annotation/wiki/list\" METHOD=\"POST\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"username\" VALUE=\""+user.name()+"\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Review Annotations\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("<TD>");
	html.append("<FORM NAME=\"annotate\" ACTION=\"/annotation/wiki/annotate\" METHOD=\"POST\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"username\" VALUE=\""+user.name()+"\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"article\" VALUE=\""+article.id()+"\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"article_pmid\" VALUE=\""+article.pmid()+"\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"No Genes\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("<TD>");
	html.append("<FORM NAME=\"annotate\" ACTION=\"/annotation/wiki/annotate\" METHOD=\"POST\">");
	html.append("Genes: <INPUT TYPE=\"TEXT\" VALUE=\""+textGenes+"\" NAME=\"genes\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"username\" VALUE=\""+user.name()+"\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"article\" VALUE=\""+article.id()+"\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"article_pmid\" VALUE=\""+article.pmid()+"\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Record\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("<TD>");
	html.append("<FORM NAME=\"logout\" ACTION=\"/annotation/wiki/logout\" METHOD=\"POST\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"username\" VALUE=\""+user.name()+"\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Logout\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("<TD>");
	html.append("<FORM NAME=\"help\" ACTION=\"/annotation/wiki/help\" METHOD=\"POST\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"username\" VALUE=\""+user.name()+"\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Help\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("</TR>");
	html.append("</TABLE>");
	return html.toString();
    }

}