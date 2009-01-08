package com.aliasi.lingmed.genelinkage;

import com.aliasi.chunk.Chunk;

import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.entrezgene.EntrezGene;
import com.aliasi.lingmed.entrezgene.EntrezGeneCodec;
import com.aliasi.lingmed.entrezgene.EntrezGeneSearcher;
import com.aliasi.lingmed.entrezgene.EntrezGeneSearcherImpl;
import com.aliasi.lingmed.lingblast.Constants;
import com.aliasi.lingmed.medline.MedlineCodec;
import com.aliasi.lingmed.medline.MedlineSearcher;
import com.aliasi.lingmed.medline.MedlineSearcherImpl;
import com.aliasi.lingmed.server.SearchClient;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.medline.Abstract;
import com.aliasi.medline.Article;
import com.aliasi.medline.MedlineCitation;

import com.aliasi.util.NBestSet;
import com.aliasi.util.Pair;
import com.aliasi.util.Strings;

import java.io.*;

import java.sql.SQLException;    

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import org.apache.log4j.Logger;

/**
 * <P>The <code>SearchServlet</code> 
 * allows web searches of the gene_linkage database.
 * geneIds are submitted via and input form,
 * and the servlet returns a listing of the N best-scoring
 * articles which mention this gene.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */
    
public class SearchServlet extends HttpServlet {
    private final Logger mLogger
	= Logger.getLogger(SearchServlet.class);

    private SearchPresentation mPresentation;
    private GeneLinkageSearcher mGeneLinkageSearcher;

    private static final int MAX_ARTICLES = 100;

    public void init() throws ServletException {
	try {
	    InitialContext ic = new InitialContext();
	    Context envContext  = (Context)ic.lookup("java:/comp/env");
	    mPresentation = new SearchPresentation(getServletContext());

	    GeneLinkageDao geneLinkageDao = null;
	    geneLinkageDao = GeneLinkageDaoImpl.getInstance(envContext,"jdbc/GeneLinkDB");
	    mLogger.info("instantiated genelinkage mysql dao");

	    String medlineHost = getInitParameter("medlineHost");
	    String medlineService = getInitParameter("medlineService");
	    if (medlineHost == null) medlineHost = "localhost";
	    if (medlineService == null) medlineService = "medline";
	    SearchClient medlineClient = new SearchClient(medlineService,medlineHost,1099);
	    Searcher medlineRemoteSearcher = medlineClient.getSearcher();
	    MedlineSearcher medlineSearcher = 
		new MedlineSearcherImpl(new MedlineCodec(),medlineRemoteSearcher);

	    String entrezgeneHost = getInitParameter("entrezgeneHost");
	    String entrezgeneService = getInitParameter("entrezgeneService");
	    if (entrezgeneHost == null) entrezgeneHost = "localhost";
	    if (entrezgeneService == null) entrezgeneService = "entrezgene";
	    SearchClient egClient = new SearchClient(entrezgeneService,entrezgeneHost,1099);
	    Searcher egRemoteSearcher = egClient.getSearcher();
	    EntrezGeneSearcher entrezGeneSearcher = 
		new EntrezGeneSearcherImpl(new EntrezGeneCodec(),egRemoteSearcher);

	    mGeneLinkageSearcher = 
		new GeneLinkageSearcher(entrezGeneSearcher,medlineSearcher,geneLinkageDao);
	} catch (Exception e) {
	    String msg = "Unexpected exception: "+e.getMessage();
	    throw new ServletException(msg,e);
	}
	
    }

    public void destroy() {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGetOrPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGetOrPost(request, response);
    }

    private void doGetOrPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
	PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/byGeneId")) {
	    doSearch(request,response);
        } else {
	    out.println(mPresentation.genSearchForm());
	}
	out.close();
   }

    private void doSearch(HttpServletRequest request, HttpServletResponse response) throws IOException {
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	try {
	    String paramGeneId = request.getParameter("geneId");
	    if (paramGeneId == null) {
		getServletContext().log("search request missing parameter geneId");
		out.println(mPresentation.genSearchForm());
		return;
	    }
	    int geneId = 0;
	    try {
		geneId = Integer.parseInt(paramGeneId);
	    } catch (NumberFormatException nfe) {
		getServletContext().log("expecting integer geneId, found: "+paramGeneId);
		out.println(mPresentation.genSearchForm());
		return;
	    }
	    String paramLimit = request.getParameter("limit");
	    int limit = MAX_ARTICLES;
	    if (paramLimit != null) {
		try {
		    limit = Integer.parseInt(paramLimit);
		} catch (NumberFormatException nfe) {
		    getServletContext().log("expecting integer geneId, found: "+paramLimit);
		}
	    }
	    if (limit < 1) {
		getServletContext().log("impossible limit: "+limit);
		out.println(mPresentation.genSearchForm());
		return;
	    }
	    getServletContext().log("find N best articles for geneId: "+paramGeneId
				    +" limit: "+limit);
	    int limitx10 = limit*10;
	    ArticleMention[] mentions = 
		mGeneLinkageSearcher.findTopMentions(paramGeneId,limitx10);
	    out.println(mGeneLinkageSearcher.genHtml(paramGeneId,mentions));
	} catch (DaoException de) {
	    String msg = "search error: "+de.getMessage();
	    out.println(mPresentation.genErrorPage(msg,de));
	} catch (SQLException se) {
	    String msg = "database error: "+se.getMessage();
	    out.println(mPresentation.genErrorPage(msg,se));
	} finally {
	    out.close();
	}
    }
}