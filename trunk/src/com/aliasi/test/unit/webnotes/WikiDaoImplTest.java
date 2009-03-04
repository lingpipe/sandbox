package com.aliasi.test.unit.webnotes;

import com.aliasi.webnotes.*;

import com.aliasi.classify.ConfusionMatrix;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;


import javax.sql.DataSource;

public class WikiDaoImplTest extends TestCase {

    private WikiDao dao;

    static private final String DB_USERNAME = "wikiuser";
    static private final String DB_PASSWORD = "hello";

    static private final String TEST_USER = "Mitzi";


    static private final int TEST_ID = 1030;
    static private final int TEST_BAD_ID = 9999999;

    static private final String TEST_SOURCE_EG = "eg";
    static private final String TEST_SOURCE_HG = "hg";
    static private final String TEST_ENTITY_ID = "1000";
    static private final String TEST_ENTITY_ID2 = "h:1000";

    public WikiDaoImplTest() throws NamingException {
	InitialContext context = getMysqlContext("jdbc:mysql://localhost:3306/annotation_wiki",
						 "annotation_wiki",
						 DB_USERNAME,
						 DB_PASSWORD);

	dao = WikiDaoImpl.getInstance(context,"jdbc/mysql",DB_USERNAME,DB_PASSWORD);
    }

    public InitialContext getMysqlContext(String url, String databaseName, String username, String password) 
	throws NamingException {
	InitialContext ic = new InitialContext();
	// Construct Jndi object reference:  arg1:  classname, arg2: factory name, arg3:URL (can be null)
	Reference ref = new Reference("com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource",
				      "com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory", null);
	ref.add(new StringRefAddr("driverClassName","com.mysql.jdbc.Driver"));
	ref.add(new StringRefAddr("url", url));
	ref.add(new StringRefAddr("databaseName",databaseName));
	ref.add(new StringRefAddr("username", username));
	ref.add(new StringRefAddr("password", password));
	ic.rebind("jdbc/mysql", ref);
	return ic;
    }

    public void testGetAllArticles() throws SQLException {
	ArticleRec[] articles = dao.getAllArticles();
	System.out.println("articles found: "+articles.length);
	System.out.flush();
	for (int i=0; i<10 && i<articles.length; i++) {
	    System.out.println(articles[i]);
	}
    }

    public void testGetAllEntities() throws SQLException {
	EntityRec[] entities = dao.getAllEntities();
	System.out.println("entites found: "+entities.length);
	for (int i=0; i<10 && i<entities.length; i++) {
	    System.out.println(entities[i]);
	}
    }

    public void testGetAllUsers() throws SQLException {
	UserRec[] users = dao.getAllUsers();
	System.out.println("users found: "+users.length);
	for (UserRec user : users) {
	    System.out.println(user);
	}
    }

    public void testGetArticleById() throws SQLException {
	for (int i=1; i<10; i++) {
	    ArticleRec rec = dao.getArticleById(i);
	    System.out.println(i+": "+rec.toString());
	}
    }

    public void testGetFirstArticle() throws SQLException {
	ArticleRec rec = dao.getFirstArticle();
	System.out.println("first article: "+rec.toString());
    }


    public void testGetAnnotationsForArticle() throws SQLException {
	UserRec user = dao.getUserByName(TEST_USER);
	ArticleRec lastArt = dao.getLastArticleForUser(user.id());
	AnnotationRec[] annotations = dao.getAnnotationsForArticle(lastArt.id());
	System.out.println("annotations found: "+annotations.length);
	for (int i=0; i<annotations.length; i++) {
	    System.out.println(annotations[i]);
	}
    }

    public void testGetAnnotationsForArticleUser() throws SQLException {
	UserRec user = dao.getUserByName(TEST_USER);
	ArticleRec lastArt = dao.getLastArticleForUser(user.id());
	AnnotationRec[]	annotations = dao.getAnnotationsForArticleUser(lastArt.id(),user.id());
	System.out.println("annotations found: "+annotations.length);
	for (int i=0; i<annotations.length; i++) {
	    System.out.println(annotations[i]);
	}
    }

    public void testGetAnnotationsForUser() throws SQLException {
	UserRec user = dao.getUserByName(TEST_USER);
	AnnotationRec[]	annotations = dao.getAnnotationsForUser(user.id());
	System.out.println("annotations found: "+annotations.length);
	for (int i=0; i<10 && i<annotations.length; i++) {
	    System.out.println(annotations[i]);
	}
    }


    public void testGetArticlesByUser() throws SQLException {
	UserRec user = dao.getUserByName(TEST_USER);
	ArticleRec[] articles = dao.getArticlesByUser(user.id());
	System.out.println("articles found: "+articles.length);
	for (int i=0; i<10 && i<articles.length; i++) {
	    System.out.println(articles[i]);
	}
    }

    public void testGetArticlesForEntity() throws SQLException {
	ArticleRec[] articles = dao.getArticlesForEntity(TEST_ID);
	System.out.println("articles found: "+articles.length);
	for (int i=0; i<10 && i<articles.length; i++) {
	    System.out.println(articles[i]);
	}
    }

    public void testGetArticlesWithEntity() throws SQLException {
	ArticleRec[] articles = dao.getArticlesWithEntity();
	System.out.println("articles found: "+articles.length);
	for (int i=0; i<10 && i<articles.length; i++) {
	    System.out.println(articles[i]);
	}
    }

    public void testGetEntityById() throws SQLException {
	EntityRec entity = dao.getEntityById(TEST_ID);
	if (entity != null) System.out.println("found entity: "+entity);
	else System.out.println("not found in db");
    }

    public void testAnnotationDetail() throws SQLException {
	HashMap<ArticleRec, HashMap> articleMap = dao.getAnnotationDetail();
	Set<ArticleRec> articleSet = articleMap.keySet();
	ArticleRec[] articles = new ArticleRec[articleSet.size()];
	articles = articleSet.toArray(articles);
	Comparator<ArticleRec>  byOrdering = new ArticleOrderComparator();
	Arrays.sort(articles,byOrdering);
	System.out.println("articles found: "+articles.length);
	for (int i=0; i<articles.length; i++) {
	    System.out.println(articles[i]);
	    HashMap<UserRec,ArrayList<EntityRec>> userMap = (HashMap<UserRec,ArrayList<EntityRec>>)articleMap.get(articles[i]);
	    for (Iterator it=userMap.entrySet().iterator(); it.hasNext(); ) {
		Map.Entry entry = (Map.Entry)it.next();
		UserRec user = (UserRec)entry.getKey();
		System.out.print("\tuser: "+user);
		ArrayList<EntityRec> entities = (ArrayList<EntityRec>)entry.getValue();
		System.out.print("\t #entities: "+entities.size());
		for (Iterator it2=entities.iterator(); it2.hasNext(); ) {
		    EntityRec entity = (EntityRec)it2.next();
		    System.out.print("\tentity: "+entity);
		}
		System.out.println();
	    }
	}
    }


    public void test2GetEntityById() throws SQLException {
	try {
	    EntityRec entity = dao.getEntityById(TEST_BAD_ID);
	    fail();
	} catch (SQLException se) {
	    System.out.println("correct exception raised");
	}
    }

    public void testGetEntityByOtherIds1() throws SQLException {
	EntityRec entity = dao.getEntityByOtherIds(TEST_SOURCE_EG,TEST_ENTITY_ID);
	if (entity != null) System.out.println("found eg entity: "+entity);
	else System.out.println("not found in db");
    }

    public void testGetEntityByOtherIds2() throws SQLException {
	String sourceId = "hg";
	String entityId = null;
	int idx = TEST_ENTITY_ID2.indexOf(":");
	entityId = TEST_ENTITY_ID2.substring(idx+1);

	EntityRec entity = dao.getEntityByOtherIds(sourceId,entityId);
	if (entity != null) System.out.println("found hg entity: "+entity);
	else System.out.println("not found in db");
    }

    public void testGetLastArticleForUser() throws SQLException {
	UserRec user = dao.getUserByName(TEST_USER);
	ArticleRec lastArt = dao.getLastArticleForUser(user.id());
	System.out.println("last article: "+lastArt);
    }

    public void testGetNextArticleForUser() throws SQLException {
	UserRec user = dao.getUserByName(TEST_USER);
	ArticleRec lastArt = dao.getLastArticleForUser(user.id());
	ArticleRec nextArt = dao.getNextArticle(lastArt.id());
	System.out.println("next article: "+nextArt);
    }

    public void testGetUserByName() throws SQLException {
	UserRec user = dao.getUserByName(TEST_USER);
	System.out.println("found user record: "+user);
    }



    static private final String TEST_USER1 = "Mitzi";
    static private final String TEST_USER2 = "Breck";

    public void testCtTruePositiveAnnotations() throws SQLException {
	UserRec user1 = dao.getUserByName(TEST_USER1);
	UserRec user2 = dao.getUserByName(TEST_USER2);
	int count = dao.ctTruePositiveAnnotations(user1.id(),user2.id());
	System.out.println("ctTruePositives: "+count+" user1: "+TEST_USER1+" user2: "+TEST_USER2);
    }

    public void testCtFalsePositiveAnnotations() throws SQLException {
	UserRec user1 = dao.getUserByName(TEST_USER1);
	UserRec user2 = dao.getUserByName(TEST_USER2);
	int count = dao.ctFalsePositiveAnnotations(user1.id(),user2.id());
	System.out.println("ctFalsePositives: "+count+" user1: "+TEST_USER1+" user2: "+TEST_USER2);
    }

    public void testCtFalseNegativeAnnotations() throws SQLException {
	UserRec user1 = dao.getUserByName(TEST_USER1);
	UserRec user2 = dao.getUserByName(TEST_USER2);
	int count = dao.ctFalseNegativeAnnotations(user1.id(),user2.id());
	System.out.println("ctFalseNegatives: "+count+" user1: "+TEST_USER1+" user2: "+TEST_USER2);
    }


    public void testInterAnnotatorAgreement() throws SQLException {
	UserRec user1 = dao.getUserByName(TEST_USER1);
	UserRec user2 = dao.getUserByName(TEST_USER2);
	ConfusionMatrix cm = dao.interAnnotatorAgreement(user1.id(),user2.id());
	System.out.println("interannotator agreement between  user1: "+user1.name()+" user2: "+user2.name());
	System.out.println(cm);
    }


// need to undo changes to db after adds - or else test on mock db

//    public void testPutArticleAnnotations() throws SQLException {
//	UserRec user = dao.getUserByName(TEST_USER);
//	ArticleRec lastArt = dao.getLastArticleForUser(user.id());
//	ArticleRec nextArt = dao.getNextArticle(lastArt.id());
//	System.out.println("next article: "+nextArt);
//
//	AnnotationRec[]	annotations = dao.getAnnotationsForArticleUser(nextArt.id(),user.id());
//	int ctBefore = annotations.length;
//	System.out.println("# annotations before update: "+ctBefore);
//
//	EntityRec[] entities = dao.getAllEntities();
//	int len = entities.length/10;
//	if (len == 0) len = entities.length; 
//	if (len == 0) fail();
//
//	EntityRec[] e2 = Arrays.copyOfRange(entities,0,len);
//	dao.putArticleAnnotations(user.id(),nextArt.id(),e2);
//
//	annotations = dao.getAnnotationsForArticleUser(nextArt.id(),user.id());
//	System.out.println("# annotations after update: "+annotations.length);
//    }

//    public void testPutArticleAnnotations2() throws SQLException {
//	UserRec user = dao.getUserByName(TEST_USER);
//	ArticleRec lastArt = dao.getLastArticleForUser(user.id());
//	ArticleRec nextArt = dao.getNextArticle(lastArt.id());
//
//	AnnotationRec[]	annotations = dao.getAnnotationsForArticleUser(nextArt.id(),user.id());
//	int ctBefore = annotations.length;
//	System.out.println("# annotations before update: "+ctBefore);
//
//	EntityRec[] entities = new EntityRec[0];
//	dao.putArticleAnnotations(user.id(),nextArt.id(),entities);
//
//	annotations = dao.getAnnotationsForArticleUser(nextArt.id(),user.id());
//	System.out.println("# annotations after update: "+annotations.length);
//     }


}