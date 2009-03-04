package com.aliasi.webnotes;

import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;    
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

public class AdminPresentation {

    private final ServletContext mContext;
    private static final String HTMLDIR = "/WEB-INF/pages/";
    private static final String OPEN_HTML = "<HTML><HEAD><TITLE>AnnotationWiki Administrator</TITLE></HEAD><BODY>";
    private static final String CLOSE_HTML = "</BODY></HTML>";

    private static final String PUBMED_URL_A1 = "<A href=\"http://www.ncbi.nlm.nih.gov/sites/entrez?Db=pubmed&amp;Cmd=DetailsSearch&amp;Term=";
    private static final String PUBMED_URL_A2 = "%5Buid%5D\">";

    AdminPresentation(ServletContext context) {
	mContext = context;
    }

    // generate HTML for list of all articles annotated by user
    public String genReport(HashMap<ArticleRec, HashMap> articleMap) {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	Set<ArticleRec> articleSet = articleMap.keySet();
	ArticleRec[] articles = new ArticleRec[articleSet.size()];
	articles = articleSet.toArray(articles);
	Comparator<ArticleRec>  byOrdering = new ArticleOrderComparator();
	Arrays.sort(articles,byOrdering);
	html.append("articles found: "+articles.length);
	html.append("<TABLE BORDER=\"1\" CELLPADDING=\"5\" WIDTH=\"92%\">");
	for (int i=0; i<articles.length; i++) {
	    html.append("<TR><TD>");
	    html.append("<TABLE BORDER=\"1\" CELLPADDING=\"5\" WIDTH=\"90%\">");
	    html.append("<TR><TD COLSPAN=\"2\">");
	    html.append("Article: "+articles[i].ordering()+": "+PUBMED_URL_A1+articles[i].pmid()+PUBMED_URL_A2+articles[i].pmid()+"</A> ");
	    html.append("</TD></TR><TR>");
	    HashMap<UserRec,ArrayList<EntityRec>> userMap = (HashMap<UserRec,ArrayList<EntityRec>>)articleMap.get(articles[i]);
	    for (Iterator it=userMap.entrySet().iterator(); it.hasNext(); ) {
		Map.Entry entry = (Map.Entry)it.next();
		UserRec user = (UserRec)entry.getKey();
		html.append("<TD>");
		html.append("<A href=\"http://192.168.1.105:8080/annotation/wiki/revise?username="+user.name()
			    +"&article="+articles[i].id()+"\"> "
			    +user.name()+"</A>");
		ArrayList<EntityRec> entities = (ArrayList<EntityRec>)entry.getValue();
		html.append(": annotations: "+entities.size()+"<BR>");
		for (Iterator it2=entities.iterator(); it2.hasNext(); ) {
		    EntityRec entity = (EntityRec)it2.next();
		    html.append("\t"+entity.sourceId()+":"+entity.entityId());
		    html.append("<BR>");
		}
		html.append("</TD>");
	    }
	    html.append("</TR></TABLE>");
	}
	html.append("</TABLE>");
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

    // generate HTML for user login page
    public String genLogin() throws IOException {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append("<h1>AnnotationWiki Administration</h1>");
	html.append("<TABLE WIDTH=\"75%\" CELLPADDING=10>");
	html.append("<TR>");
	html.append("<TD>");
	html.append("<FORM NAME=\"login\" ACTION=\"/annotation/admin/login\" METHOD=\"POST\">");
	html.append("Name: <INPUT TYPE=\"TEXT\" NAME=\"username\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Login\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("</TR>");
	html.append("</TABLE>");
	html.append("<HR>");
	html.append(CLOSE_HTML);
	return html.toString();
    }

    // generate HTML for report params page
    public String genReportForm(UserRec user) throws IOException {
	StringBuffer html = new StringBuffer();
	html.append(OPEN_HTML);
	html.append("<h1>AnnotationWiki Administration</h1>");
	html.append("<TABLE WIDTH=\"75%\" CELLPADDING=10>");
	html.append("<TR>");
	html.append("<TD>");
	html.append("<FORM NAME=\"report\" ACTION=\"/annotation/admin/report\" METHOD=\"POST\">");
	html.append("<INPUT TYPE=\"HIDDEN\" NAME=\"username\" VALUE=\""+user.name()+"\">");
	html.append("<INPUT TYPE=\"SUBMIT\" VALUE=\"Get Report\">");
	html.append("</FORM>");	
	html.append("</TD>");
	html.append("</TR>");
	html.append("</TABLE>");
	html.append("<HR>");
	html.append(CLOSE_HTML);
	return html.toString();
    }


}