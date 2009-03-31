package com.aliasi.lingmed.genelinkage;

import com.aliasi.chunk.Chunk;

import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.utils.Logging;

import java.sql.SQLException;    

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;


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
    
public class DbTestServlet extends HttpServlet {

    GeneLinkageDao mGeneLinkageDao;

    public void init() throws ServletException {
        try {
            InitialContext ic = new InitialContext();
            Context envContext  = (Context)ic.lookup("java:/comp/env");
            mGeneLinkageDao = GeneLinkageDaoImpl.getInstance(envContext,"jdbc/GeneLinkDB");
            getServletContext().log("instantiated mysql dao");
            mGeneLinkageDao.getArticleMentionsForGeneId(101);
            getServletContext().log("database search successful");
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
        out.println("<html><body><p>Hi Mom</p></body></html>");
        out.close();
	}

}