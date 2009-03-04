package com.aliasi.webnotes;

import java.io.*;

import java.sql.SQLException;    

import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;    
import java.util.HashSet;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
    
public class WikiServlet extends HttpServlet {

    private WikiDao mWikiDao;
    private WikiPresentation mPresentation;

    public void init() throws ServletException {
	// get Lucene Entrez DAO

	// instantiate MySQL AnnotationDb DAO
	try {
	    InitialContext ic = new InitialContext();
	    Context envContext  = (Context)ic.lookup("java:/comp/env");
	    mWikiDao = WikiDaoImpl.getInstance(envContext,"jdbc/AnnotationDB");
	    getServletContext().log("instantiated database");
	    mPresentation = new WikiPresentation(getServletContext());
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
        } else if (pathInfo.startsWith("/annotate")) {
	    doRecord(request,response);
        } else if (pathInfo.startsWith("/help")) {
	    doHelp(request,response);
        } else if (pathInfo.startsWith("/list")) {
	    doList(request,response);
        } else if (pathInfo.startsWith("/logout")) {
	    PrintWriter out = response.getWriter();
	    out.println(mPresentation.genLogin());
	    out.close();
        } else if (pathInfo.startsWith("/revise")) {
	    doRevise(request,response);
        } else {
	    PrintWriter out = response.getWriter();
	    out.println(mPresentation.genLogin());
	    out.close();
	}
    }

    // called from link on all pages
    // validate user info, print help page
    private void doHelp(HttpServletRequest request, HttpServletResponse response) throws IOException {
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	try {
	    if (request.getParameter("username") == null) {
		getServletContext().log("help request missing parameters");
		out.println(mPresentation.genError("cannot process request",new IllegalStateException()));
	    } else {
		String pUsername = request.getParameterValues("username")[0];
		UserRec user = mWikiDao.getUserByName(pUsername);
		getServletContext().log("user: "+user);
		out.println(mPresentation.genHelp(user));
	    }
	} catch (SQLException se) {
	    out.println(mPresentation.genError(se.getMessage(),se));
	} finally {
	    out.close();
	}
    }

    // validate user info
    // list all articles already annotated by this user
    private void doList(HttpServletRequest request, HttpServletResponse response) throws IOException {
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	try {
	    if (request.getParameter("username") == null) {
		getServletContext().log("review articles request missing parameters");
		out.println(mPresentation.genError("cannot process request",new IllegalStateException()));
	    } else {
		String pUsername = request.getParameterValues("username")[0];
		UserRec user = mWikiDao.getUserByName(pUsername);
		getServletContext().log("user: "+user);

		ArticleRec[] articles = mWikiDao.getArticlesByUser(user.id());
		Comparator<ArticleRec>  byOrdering = new ArticleOrderComparator();
		Arrays.sort(articles,byOrdering);
		int[] counts = new int[articles.length];
		for (int i=0; i<articles.length; i++) {
		    counts[i] = mWikiDao.getUserArticleCtEntities(articles[i].id(),user.id());
		}
		out.println(mPresentation.genList(user,articles,counts));
	    }
	} catch (SQLException se) {
	    out.println(mPresentation.genError(se.getMessage(),se));
	} finally {
	    out.close();
	}
    }


    // called upon login form submit
    // validate user info
    // get next article to annotate
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
		getServletContext().log("find article");
		ArticleRec nextArticle = null;
		ArticleRec lastArticle = mWikiDao.getLastArticleForUser(user.id());
		if (lastArticle != null) {
		    nextArticle = mWikiDao.getNextArticle(lastArticle.id());
		} else {
		    nextArticle = mWikiDao.getFirstArticle();
		}
		getServletContext().log("next article id: "+nextArticle.id()+" pmid: "+nextArticle.pmid());
		out.println(mPresentation.genArticle(user,nextArticle));
	    }
	} catch (NullPointerException npe) {
	    out.println(mPresentation.genError(npe.getMessage(),npe));
	} catch (SQLException se) {
	    out.println(mPresentation.genError(se.getMessage(),se));
	} finally {
	    out.close();
	}
    }

    // called upon article annotation form submit
    // record annotation for user
    // get next article to annotate (if any)
    private void doRecord(HttpServletRequest request, HttpServletResponse response) throws IOException {
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	try {
	    if (request.getParameter("username") == null
		|| request.getParameter("article") == null) {
		getServletContext().log("record annotation request missing parameters");
		out.println(mPresentation.genError("cannot process request",new IllegalStateException()));
	    } else {
		String pUsername = request.getParameterValues("username")[0];
		UserRec user = mWikiDao.getUserByName(pUsername);
		getServletContext().log("user: "+user);

		String pArticle = request.getParameterValues("article")[0];
		int articleId = Integer.parseInt(pArticle);
		ArticleRec article = mWikiDao.getArticleById(articleId);
		getServletContext().log("article: "+article);

		String pGenes[] = new String[0];
		EntityRec entities[] = new EntityRec[0];
		if (request.getParameter("genes") != null) {
		    pGenes = request.getParameterValues("genes");
		    getServletContext().log("genes: "+pGenes[0]);
		    try {
			entities = parseGenes(pGenes[0]);
		    } catch (SQLException se) {
			String msg = "Error: unknown entity id "+pGenes[0];
			out.println(mPresentation.genArticle(user,article,msg));
			return;
		    }
		}
		mWikiDao.putArticleAnnotations(user.id(),article.id(),entities);

		ArticleRec nextArticle = null;
		ArticleRec lastArticle = mWikiDao.getLastArticleForUser(user.id());
		if (lastArticle != null) {
		    nextArticle = mWikiDao.getNextArticle(lastArticle.id());
		}
		if (nextArticle != null) {
		    getServletContext().log("next article id: "+nextArticle.id()+" pmid: "+nextArticle.pmid());
		    out.println(mPresentation.genArticle(user,nextArticle));
		}  else {
		    getServletContext().log("done - nextArticle == null");
		    out.println(mPresentation.genThankYou(user));
		}
	    }
	} catch (NumberFormatException nfe) {
	    out.println(mPresentation.genError("request error",nfe));
	} catch (SQLException se) {
	    out.println(mPresentation.genError(se.getMessage(),se));
	} finally {
	    out.close();
	}
    }

    // invoked via links on the list of annotated articles page
    // present article with current annotations by user
    private void doRevise(HttpServletRequest request, HttpServletResponse response) throws IOException {
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	try {
	    if (request.getParameter("username") == null
		|| request.getParameter("article") == null) {
		getServletContext().log("do annotation request missing parameters");
		out.println(mPresentation.genError("cannot process request",new IllegalStateException()));
	    } else {
		String pUsername = request.getParameterValues("username")[0];
		UserRec user = mWikiDao.getUserByName(pUsername);
		getServletContext().log("user: "+user);

		String pArticle = request.getParameterValues("article")[0];
		int articleId = Integer.parseInt(pArticle);
		ArticleRec article = mWikiDao.getArticleById(articleId);
		getServletContext().log("article: "+article);

		AnnotationRec[] annotations = mWikiDao.getAnnotationsForArticleUser(articleId,user.id());
		getServletContext().log("num annotations: "+annotations.length);
		String geneIds = listGeneIds(annotations);
		out.println(mPresentation.genReviseArticle(user,article,geneIds));
	    }
	} catch (SQLException se) {
	    out.println(mPresentation.genError(se.getMessage(),se));
	} finally {
	    out.close();
	}
    }

    private String listGeneIds(AnnotationRec[] annotations) throws SQLException {
	StringBuffer sb = new StringBuffer();
	for (int i=0; i<annotations.length; i++) {
	    EntityRec entity = mWikiDao.getEntityById(annotations[i].entityId());
	    if (i>0) sb.append(", ");
	    if (entity.sourceId().equalsIgnoreCase("eg")) {
		sb.append(entity.entityId());
	    } else {
		sb.append(entity.sourceId()+":"+entity.entityId());
	    }
	}
	return sb.toString();
    }
		
    private EntityRec[] parseGenes(String input) throws SQLException, NumberFormatException {
	HashSet<EntityRec> entities = new HashSet<EntityRec>();
	String[] params = input.split(",");
	for (String param : params) {
	    if (param.trim() == "") continue;
	    try {
		String entityId = param.trim();
		String sourceId = "eg";
		if (entityId.startsWith("h")) { 
		    sourceId = "hg";
		    int idx = entityId.indexOf(":");
		    entityId = entityId.substring(idx+1);
		    getServletContext().log("hg id: entityId: "+entityId);
		}
		EntityRec entity = mWikiDao.getEntityByOtherIds(sourceId,entityId);
		getServletContext().log("entity: "+entity);
		entities.add(entity);
	    } catch (NumberFormatException nfe) {
		String msg = "bad entity id: "+param;
		getServletContext().log(msg);
		throw new NumberFormatException(msg);
	    } catch (SQLException se) {
		String msg = "entity not found in database: "+param;
		getServletContext().log(msg);
		throw new SQLException(msg);
	    }
	}
	EntityRec[] arr = new EntityRec[entities.size()];
	return entities.toArray(arr);
    }

}

