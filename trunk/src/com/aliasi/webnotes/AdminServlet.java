package com.aliasi.webnotes;

import java.io.*;

import java.sql.SQLException;    

import java.util.HashMap;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
    
public class AdminServlet extends HttpServlet {

    private WikiDao mWikiDao;
    private AdminPresentation mPresentation;

    public void init() throws ServletException {
	// get Lucene Entrez DAO

	// instantiate MySQL AnnotationDb DAO
	try {
	    InitialContext ic = new InitialContext();
	    Context envContext  = (Context)ic.lookup("java:/comp/env");
	    mWikiDao = WikiDaoImpl.getInstance(envContext,"jdbc/AnnotationDB");
	    getServletContext().log("instantiated database");
	    mPresentation = new AdminPresentation(getServletContext());
	} catch (Exception e) {
	    String msg = "Cannot connect to database, "+e.getMessage();
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
        String pathInfo = request.getPathInfo();
        if (pathInfo.startsWith("/login")) {
	    doLogin(request,response);
        } else if (pathInfo.startsWith("/logout")) {
	    PrintWriter out = response.getWriter();
	    out.println(mPresentation.genLogin());
	    out.close();
        } else if (pathInfo.startsWith("/report")) {
	    doReport(request,response);
        } else {
	    PrintWriter out = response.getWriter();
	    out.println(mPresentation.genLogin());
	    out.close();
	}
    }


    // called upon login form submit
    // validate user info
    private void doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	try {
	    if (request.getParameter("username") == null) {
		getServletContext().log("login request missing parameters");
	        out.println(mPresentation.genLogin());
	    } else {
		String pUsername = request.getParameterValues("username")[0];
		UserRec user = mWikiDao.getUserByName(pUsername);
		if (user == null) {
		    getServletContext().log("login attempt by unknown user: "+pUsername);
		    out.println(mPresentation.genLogin());
		    return;
		}
		getServletContext().log("successful login by : "+user);
		out.println(mPresentation.genReportForm(user));
	    }
	} catch (NullPointerException npe) {
	    out.println(mPresentation.genError(npe.getMessage(),npe));
	} catch (SQLException se) {
	    out.println(mPresentation.genError(se.getMessage(),se));
	} finally {
	    out.close();
	}
    }

    // called upon report form submit
    private void doReport(HttpServletRequest request, HttpServletResponse response) throws IOException {
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	try {
	    String pUsername = request.getParameterValues("username")[0];
	    UserRec adminUser = mWikiDao.getUserByName(pUsername);
	    getServletContext().log("adminUser: "+adminUser);
	    HashMap<ArticleRec, HashMap> articleMap = mWikiDao.getAnnotationDetail();
	    out.println(mPresentation.genReport(articleMap));
	} catch (SQLException se) {
	    out.println(mPresentation.genError(se.getMessage(),se));
	} finally {
	    out.close();
	}
    }

}

