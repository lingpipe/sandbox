package com.aliasi.webnotes;

import com.aliasi.classify.ConfusionMatrix;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;


import javax.sql.DataSource;

public class WikiDaoImpl implements WikiDao {

    static final String CT_ANNOTATIONS_SQL 
	= "SELECT COUNT(*) FROM annotation ";

    static final String CT_ARTICLES_SQL 
	= "SELECT COUNT(*) FROM article ";

    static final String CT_ENTITIES_SQL 
	= "SELECT COUNT(*) FROM entity ";

    static final String CT_USERS_SQL 
	= "SELECT COUNT(*) FROM web_user ";

    static final String CT_ANNOTATIONS_FOR_ARTICLE_USER_SQL 
 	= "SELECT COUNT(annotation.entity_id) "
	+ " FROM annotation "
	+ " WHERE annotation.article_id = ? "
	+ " AND annotation.user_id = ? ";

    static final String CT_FALSE_ANNOTATIONS_FOR_USER_USER_SQL 
 	= "SELECT COUNT(*) "
	+ " FROM annotation a1 LEFT JOIN annotation a2 "
	+ " ON a1.article_id = a2.article_id AND a1.entity_id = a2.entity_id AND a1.user_id != a2.user_id "
	+ " WHERE a1.user_id = ? AND a1.article_id < 1000 "
	+ " AND (a2.user_id IS NULL OR a2.user_id != ?) "; 

    static final String CT_TRUE_ANNOTATIONS_FOR_USER_USER_SQL 
 	= "SELECT COUNT(*) "
	+ " FROM annotation a1, annotation a2 "
	+ " WHERE a1.article_id < 1000 AND a1.article_id = a2.article_id AND a1.entity_id = a2.entity_id "
	+ " AND a1.user_id = ? "
	+ " AND a2.user_id = ? ";

    static final String DELETE_ANNOTATIONS_FOR_ARTICLE_USER_SQL 
 	= "DELETE FROM annotation "
	+ " WHERE annotation.article_id = ? "
	+ " AND annotation.user_id = ? ";

    static final String GET_ALL_ANNOTATIONS_SQL 
	= "SELECT annotation.article_id, annotation.entity_id, annotation.user_id, "
	+ " FROM annotation ";

    static final String GET_ALL_ARTICLES_SQL 
	= "SELECT article.id, article.pubmed_id, article.ordering "
	+ " FROM article ";

    static final String GET_ALL_ENTITIES_SQL 
	= "SELECT entity.id, entity.source_id, entity.entity_id "
	+ " FROM entity ";

    static final String GET_ALL_USERS_SQL 
	= "SELECT web_user.id, web_user.name FROM web_user ";

    static final String GET_ANNOTATION_DETAIL_SQL 
 	= "SELECT annotation.article_id, article.pubmed_id, article.ordering, "
	+ " entity.id, entity.source_id, entity.entity_id, "
	+ " annotation.user_id, web_user.name "
	+ " FROM annotation, article, entity, web_user "
	+ " WHERE annotation.article_id = article.id "
	+ " AND annotation.entity_id = entity.id "
	+ " AND annotation.user_id = web_user.id ";

    static final String GET_ANNOTATIONS_FOR_ARTICLE_SQL 
 	= "SELECT annotation.entity_id, annotation.user_id "
	+ " FROM annotation "
	+ " WHERE annotation.article_id = ? ";

    static final String GET_ANNOTATIONS_FOR_ARTICLE_USER_SQL 
 	= "SELECT annotation.entity_id "
	+ " FROM annotation "
	+ " WHERE annotation.article_id = ? "
	+ " AND annotation.user_id = ? ";

    static final String GET_ANNOTATIONS_FOR_ENTITY_SQL 
 	= "SELECT annotation.article_id, annotation.user_id "
	+ " FROM annotation "
	+ " WHERE annotation.entity_id = ? ";

    static final String GET_ANNOTATIONS_FOR_USER_SQL 
 	= "SELECT annotation.article_id, annotation.entity_id "
	+ " FROM annotation "
	+ " WHERE annotation.user_id = ? ";

    static final String GET_ARTICLE_BY_ID_SQL 
	= "SELECT article.pubmed_id, article.ordering "
	+ " FROM article "
	+ " WHERE article.id = ? ";

    static final String GET_ARTICLE_BY_PMID_SQL 
	= "SELECT article.id, article.ordering "
	+ " FROM article "
	+ " WHERE article.pubmed_id = ? ";

    static final String GET_ARTICLES_FOR_ENTITY_SQL 
 	= "SELECT DISTINCT(annotation.article_id), article.pubmed_id, article.ordering "
	+ " FROM annotation, article "
	+ " WHERE annotation.entity_id = ? "
	+ " AND annotation.article_id = article.id ";

    static final String GET_ARTICLES_BY_USER_SQL 
 	= "SELECT article.id, article.pubmed_id, article.ordering "
	+ " FROM user_article_count, article "
	+ " WHERE user_article_count.user_id = ? "
	+ " AND user_article_count.article_id = article.id ";

    static final String GET_ARTICLES_WITH_ENTITY_SQL 
 	= "SELECT article.id, article.pubmed_id, article.ordering "
	+ " FROM user_article_count, article "
	+ " WHERE user_article_count.article_id = article.id "
	+ " AND user_article_count.ct_entities > 0 ";


    static final String GET_ENTITY_BY_ID_SQL 
	= "SELECT entity.source_id, entity.entity_id "
	+ " FROM entity "
	+ " WHERE entity.id = ? ";

    static final String GET_ENTITY_BY_OTHER_IDS_SQL 
	= "SELECT entity.id "
	+ " FROM entity "
	+ " WHERE entity.source_id = ? "
	+ " AND entity.entity_id = ? ";

    static final String GET_FIRST_ARTICLE_SQL
	= "SELECT article.id, article.pubmed_id, article.ordering "
	+ " FROM article "
	+ " ORDER BY article.ordering ASC limit 1 ";

    static final String GET_LAST_ARTICLE_FOR_USER_SQL 
	= "SELECT article.id, article.pubmed_id, article.ordering "
	+ " FROM user_article_count, article "
	+ " WHERE user_article_count.user_id = ? "
	+ " AND user_article_count.article_id = article.id "
	+ " ORDER BY article.ordering DESC limit 1 ";

    static final String GET_NEXT_ARTICLE_SQL 
	= "SELECT article.id, article.pubmed_id, article.ordering "
	+ " FROM article "
	+ " WHERE ordering > ? "
	+ " ORDER BY article.ordering ASC LIMIT 1 ";

    static final String GET_USER_ARTICLE_COUNT_SQL 
 	= "SELECT ct_entities FROM user_article_count "
	+ " WHERE article_id = ? "
	+ " AND user_id = ? ";

    static final String GET_USER_BY_ID_SQL 
 	= "SELECT web_user.name "
	+ " FROM web_user "
	+ " WHERE web_user.id = ? ";

    static final String GET_USER_BY_NAME_SQL 
	= "SELECT web_user.id "
	+ " FROM web_user "
	+ " WHERE web_user.name = ? ";

    static final String INSERT_ANNOTATION_SQL 
 	= "INSERT INTO  annotation(article_id,entity_id,user_id) "
	+ " VALUES(?,?,?) " ;

    static final String INSERT_USER_ARTICLE_COUNT_SQL 
 	= "INSERT INTO  user_article_count(article_id, user_id, ct_entities) "
	+ " VALUES(?,?,?) " ;

    static final String DELETE_USER_ARTICLE_COUNT_SQL 
 	= "DELETE FROM user_article_count "
	+ " WHERE article_id=? "
	+ " AND user_id=? ";

    private static WikiDao sInstance;
    private static DataSource sDs;

    private static boolean isStandalone;
    private static String sUserName;
    private static String sPassword; 

    public static synchronized WikiDao getInstance(Context context,String name) throws NamingException {
	if (sInstance == null) {
	    isStandalone = false;
	    sDs = (DataSource)context.lookup(name);
	    if ( sDs == null ) {
		throw new IllegalStateException("Data source not found!");
	    }
	    sInstance = new WikiDaoImpl();
	}
	return sInstance;
    }


    public static synchronized WikiDao getInstance(Context context,String name, 
						   String userName, String password) throws NamingException {
	if (sInstance == null) {
	    sUserName = userName;
	    sPassword = password;
	    isStandalone = true;
	    sDs = (DataSource)context.lookup(name);
	    if ( sDs == null ) {
		throw new IllegalStateException("Data source not found!");
	    }
	    sInstance = new WikiDaoImpl();
	}
	return sInstance;
    }

    public ConfusionMatrix interAnnotatorAgreement(int user1Id, int user2Id) throws SQLException {
	int tp = ctTruePositiveAnnotations(user1Id, user2Id);
	int fp = ctFalsePositiveAnnotations(user1Id, user2Id);
	int fn = ctFalseNegativeAnnotations(user1Id, user2Id);
	return getConfusionMatrix(tp,fp,fn);
    }

    /** count true positive annotations for pair of users */
    public int ctTruePositiveAnnotations(int user1Id, int user2Id) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return ctTruePositiveAnnotations(user1Id,user2Id,con);
	} catch (SQLException e) {
	    String msg = "Could not get annotations";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }


    /** count false positive annotations for pair of users */
    public int ctFalsePositiveAnnotations(int user1Id, int user2Id) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    // note query is symmteric w/ FalsePositive query - swap order of user ids!
	    return ctFalseAnnotations(user1Id,user2Id,con);
	} catch (SQLException e) {
	    String msg = "Could not get annotations";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** count false negative annotations for pair of users */
    public int ctFalseNegativeAnnotations(int user1Id, int user2Id) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    // note query is symmteric w/ FalsePositive query - swap order of user ids!
	    return ctFalseAnnotations(user2Id,user1Id,con);
	} catch (SQLException e) {
	    String msg = "Could not get annotations";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** count number of entities annotated for article, user
     */
    public int ctAnnotationsForArticleUser(int articleId, int userId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return ctAnnotationsForArticleUser(articleId,userId,con);
	} catch (SQLException e) {
	    String msg = "Could not get annotations";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get all articles in db
     */
    public ArticleRec[] getAllArticles() throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getAllArticles(con);
	} catch (SQLException e) {
	    String msg = "Could not get articles";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get all entities in db
     */
    public EntityRec[] getAllEntities() throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getAllEntities(con);
	} catch (SQLException e) {
	    String msg = "Could not get entities";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get all users in db
     */
    public UserRec[] getAllUsers() throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getAllUsers(con);
	} catch (SQLException e) {
	    String msg = "Could not get users";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get annotations for article
     */
    public AnnotationRec[] getAnnotationsForArticle(int articleId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getAnnotationsForArticle(articleId,con);
	} catch (SQLException e) {
	    String msg = "Could not get annotations";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }


    /** get annotations for article, user
     */
    public AnnotationRec[] getAnnotationsForArticleUser(int articleId, int userId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getAnnotationsForArticleUser(articleId,userId,con);
	} catch (SQLException e) {
	    String msg = "Could not get annotations";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get annotations for entity
     */
    public AnnotationRec[] getAnnotationsForEntity(int entityId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getAnnotationsForEntity(entityId,con);
	} catch (SQLException e) {
	    String msg = "Could not get annotations";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get annotations for user
     */
    public AnnotationRec[] getAnnotationsForUser(int userId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getAnnotationsForUser(userId,con);
	} catch (SQLException e) {
	    String msg = "Could not get annotations";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get article for id
     */
    public ArticleRec getArticleById(int id) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getArticleById(id,con);
	} catch (SQLException e) {
	    String msg = "Could not get article";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get article for pubmed_id
     */
    public ArticleRec getArticleByPmid(int pmid) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getArticleByPmid(pmid,con);
	} catch (SQLException e) {
	    String msg = "Could not get article";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get articles which have annotations by user
     */
    public ArticleRec[] getArticlesByUser(int userId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getArticlesByUser(userId,con);
	} catch (SQLException e) {
	    String msg = "Could not get articles";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get articles which have annotations for entity
     */
    public ArticleRec[] getArticlesForEntity(int entityId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getArticlesForEntity(entityId,con);
	} catch (SQLException e) {
	    String msg = "Could not get articles";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get articles which have annotations */
    public ArticleRec[] getArticlesWithEntity() throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getArticlesWithEntity(con);
	} catch (SQLException e) {
	    String msg = "Could not get articles";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    public HashMap<ArticleRec, HashMap> getAnnotationDetail() throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getAnnotationDetail(con);
	} catch (SQLException e) {
	    String msg = "Could not get articles";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get entity by id
     */
    public EntityRec getEntityById(int id) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getEntityById(id,con);
	} catch (SQLException e) {
	    String msg = "Could not get entity";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }


    /** get entity record by source_id, entity_id */
    public EntityRec getEntityByOtherIds(String sourceId, String entityId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getEntityByOtherIds(sourceId,entityId,con);
	} catch (SQLException e) {
	    String msg = "Could not get entity";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get first article for annotation
     */
    public ArticleRec getFirstArticle() throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getFirstArticle(con);
	} catch (SQLException e) {
	    String msg = "Could not get article for annotation";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get last article annotation for user
     */
    public ArticleRec getLastArticleForUser(int userId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getLastArticleForUser(userId,con);
	} catch (SQLException e) {
	    String msg = "Could not get article for user (userId="+userId+")";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get next article according to article ordering
     */
    public ArticleRec getNextArticle(int articleId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getNextArticle(articleId,con);
	} catch (SQLException e) {
	    String msg = "Could not find article (after articleId="+articleId+")";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /**   get count of entities in article annotated by user
     */
    public int getUserArticleCtEntities(int articleId, int userId) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getUserArticleCtEntities(articleId,userId,con);
	} catch (SQLException e) {
	    String msg = "Could not get user article count";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get user by name
     */
    public UserRec getUserByName(String name) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getUserByName(name,con);
	} catch (SQLException e) {
	    String msg = "Could not get user";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** get user by id
     */
    public UserRec getUserById(int id) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    return getUserById(id,con);
	} catch (SQLException e) {
	    String msg = "Could not get user";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    /** annotate article
     */
    public void putArticleAnnotations(int userId, int articleId, EntityRec[] entities) throws SQLException {
	Connection con = null;
	try {
	    con = getConnection();
	    putArticleAnnotations(userId,articleId,entities,con);
	} catch (SQLException e) {
	    String msg = "Could not record annotations";
	    System.out.println(e.getMessage());
	    e.printStackTrace(System.out);
	    throw new SQLException(msg);
	} finally {
	    closeConnection(con);
	    con = null;
	}
    }

    private int ctAnnotationsForArticleUser(int articleId, int userId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(CT_ANNOTATIONS_FOR_ARTICLE_USER_SQL);
	    pstmt.setInt(1,articleId);
	    pstmt.setInt(2,userId);
	    ResultSet rs = null;
	    int total = 0;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    total = rs.getInt(1);
		}
		return total;
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private int ctTruePositiveAnnotations(int user1Id, int user2Id, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(CT_TRUE_ANNOTATIONS_FOR_USER_USER_SQL);
	    pstmt.setInt(1,user1Id);
	    pstmt.setInt(2,user2Id);
	    ResultSet rs = null;
	    int total = 0;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    total = rs.getInt(1);
		}
		return total;
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private int ctFalseAnnotations(int user1Id, int user2Id, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(CT_FALSE_ANNOTATIONS_FOR_USER_USER_SQL);
	    pstmt.setInt(1,user1Id);
	    pstmt.setInt(2,user2Id);
	    ResultSet rs = null;
	    int total = 0;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    total = rs.getInt(1);
		}
		return total;
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private ArticleRec[] getAllArticles(Connection con) throws SQLException {
	PreparedStatement pstmt = null;
        ArticleRec[] result = new ArticleRec[0];
	int id = 0;
	int pmid = 0;
	int ordering = 0;
	int total = 0;
	try {
	    pstmt = con.prepareStatement(CT_ARTICLES_SQL);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    total = rs.getInt(1);
		} else {
		    return result;
		}
		result = new ArticleRec[total];
		closeResultSet(rs);
		closePreparedStatement(pstmt);
		
		pstmt = con.prepareStatement(GET_ALL_ARTICLES_SQL);
		rs = pstmt.executeQuery();
		int ct = 0;
		while (rs.next()) {
		    id = rs.getInt("id");
		    pmid = rs.getInt("pubmed_id");
		    ordering = rs.getInt("ordering");
		    result[ct] = new ArticleRec(id,pmid,ordering);
		    ct++;
		}
		// should check that ct == total
		return result;
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private EntityRec[] getAllEntities(Connection con) throws SQLException {
	PreparedStatement pstmt = null;
        EntityRec[] result = new EntityRec[0];
	int id = 0;
	String source = null;
	String entity = null;
	int total = 0;
	try {
	    pstmt = con.prepareStatement(CT_ENTITIES_SQL);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    total = rs.getInt(1);
		} else {
		    return result;
		}
		result = new EntityRec[total];
		closeResultSet(rs);
		closePreparedStatement(pstmt);
		
		pstmt = con.prepareStatement(GET_ALL_ENTITIES_SQL);
		rs = pstmt.executeQuery();
		int ct = 0;
		while (rs.next()) {
		    id = rs.getInt("id");
		    source = rs.getString("source_id");
		    entity = rs.getString("entity_id");
		    result[ct] = new EntityRec(id,source,entity);
		    ct++;
		}
		// should check that ct == total
		return result;
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private UserRec[] getAllUsers(Connection con) throws SQLException {
	PreparedStatement pstmt = null;
        UserRec[] result = new UserRec[0];
	int id = 0;
	String name = null;
	int total = 0;
	try {
	    pstmt = con.prepareStatement(CT_USERS_SQL);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    total = rs.getInt(1);
		} else {
		    return result;
		}
		result = new UserRec[total];
		closeResultSet(rs);
		closePreparedStatement(pstmt);
		
		pstmt = con.prepareStatement(GET_ALL_USERS_SQL);
		rs = pstmt.executeQuery();
		int ct = 0;
		while (rs.next()) {
		    id = rs.getInt("id");
		    name = rs.getString("name");
		    result[ct] = new UserRec(id,name);
		    ct++;
		}
		// should check that ct == total
		return result;
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private ArticleRec getArticleById(int id, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_ARTICLE_BY_ID_SQL);
	    pstmt.setInt(1,id);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    int pmid = rs.getInt("pubmed_id");
		    int ordering = rs.getInt("ordering");
		    return new ArticleRec(id,pmid,ordering);
		} else {
		    String msg = "DB lookup failed, id="+id;
		    throw new SQLException(msg);
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private ArticleRec getArticleByPmid(int pmid, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_ARTICLE_BY_PMID_SQL);
	    pstmt.setInt(1,pmid);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    int id = rs.getInt("id");
		    int ordering = rs.getInt("ordering");
		    return new ArticleRec(id,pmid,ordering);
		} else {
		    String msg = "DB lookup failed, pmid="+pmid;
		    throw new SQLException(msg);
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private ArticleRec getNextArticle(int articleId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_NEXT_ARTICLE_SQL);
	    pstmt.setInt(1,articleId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    int id = rs.getInt("id");
		    int pmid = rs.getInt("pubmed_id");
		    int ordering = rs.getInt("ordering");
		    return new ArticleRec(id,pmid,ordering);
		} else {
		    return null;
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private ArticleRec getFirstArticle(Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_FIRST_ARTICLE_SQL);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    int id = rs.getInt("id");
		    int pmid = rs.getInt("pubmed_id");
		    int ordering = rs.getInt("ordering");
		    return new ArticleRec(id,pmid,ordering);
		} else {
		    return null;
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private ArticleRec getLastArticleForUser(int userId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_LAST_ARTICLE_FOR_USER_SQL);
	    pstmt.setInt(1,userId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    int id = rs.getInt("id");
		    int pmid = rs.getInt("pubmed_id");
		    int ordering = rs.getInt("ordering");
		    return new ArticleRec(id,pmid,ordering);
		} else {
		    return null;
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private ArticleRec[]getArticlesByUser(int userId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	HashSet<ArticleRec> articles = new HashSet<ArticleRec>();
	try {
	    pstmt = con.prepareStatement(GET_ARTICLES_BY_USER_SQL);
	    pstmt.setInt(1,userId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    int id = rs.getInt("id");
		    int pmid = rs.getInt("pubmed_id");
		    int ordering = rs.getInt("ordering");
		    articles.add(new ArticleRec(id,pmid,ordering));
		} 
		ArticleRec[] arr = new ArticleRec[articles.size()];
		return articles.toArray(arr);
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private ArticleRec[]getArticlesForEntity(int entityId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	HashSet<ArticleRec> articles = new HashSet<ArticleRec>();
	try {
	    pstmt = con.prepareStatement(GET_ARTICLES_FOR_ENTITY_SQL);
	    pstmt.setInt(1,entityId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    int id = rs.getInt("article_id");
		    int pmid = rs.getInt("pubmed_id");
		    int ordering = rs.getInt("ordering");
		    articles.add(new ArticleRec(id,pmid,ordering));
		} 
		ArticleRec[] arr = new ArticleRec[articles.size()];
		return articles.toArray(arr);
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private ArticleRec[]getArticlesWithEntity(Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	HashSet<ArticleRec> articles = new HashSet<ArticleRec>();
	try {
	    pstmt = con.prepareStatement(GET_ARTICLES_WITH_ENTITY_SQL);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    int id = rs.getInt("id");
		    int pmid = rs.getInt("pubmed_id");
		    int ordering = rs.getInt("ordering");
		    articles.add(new ArticleRec(id,pmid,ordering));
		} 
		ArticleRec[] arr = new ArticleRec[articles.size()];
		return articles.toArray(arr);
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private HashMap<ArticleRec, HashMap> getAnnotationDetail(Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	HashMap<ArticleRec, HashMap> articleMap = new HashMap<ArticleRec, HashMap>();
	try {
	    pstmt = con.prepareStatement(GET_ANNOTATION_DETAIL_SQL);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    int articleId = rs.getInt("article_id");
		    int pmid = rs.getInt("pubmed_id");
		    int ordering = rs.getInt("ordering");
		    int entityId = rs.getInt("id");
		    String source = rs.getString("source_id");
		    String entity = rs.getString("entity_id");
		    int userId = rs.getInt("user_id");
		    String userName = rs.getString("name");
		    ArticleRec curArt = new ArticleRec(articleId,pmid,ordering);
		    EntityRec curEnt = new EntityRec(entityId,source,entity);
		    UserRec curUser = new UserRec(userId,userName);
		    HashMap<UserRec,ArrayList<EntityRec>> userMap = null;
		    ArrayList<EntityRec> entities = null;
		    if (articleMap.containsKey(curArt)) {
			userMap = (HashMap<UserRec,ArrayList<EntityRec>>)articleMap.get(curArt);
			if (userMap.containsKey(curUser)) {
			    entities = (ArrayList<EntityRec>)userMap.get(curUser);
			    entities.add(curEnt);
			    System.out.println("add'l entity: "+curEnt+"\tknown user: "+curUser+"\tknown article: "+curArt);
			}
			else {
			    entities = new ArrayList<EntityRec>();
			    entities.add(curEnt);
			    userMap.put(curUser,entities);
			    System.out.println("add'l user, entity: "+curEnt+"\tnew curUser: "+userId+"\tknown article: "+curArt);
			}

		    } else {
			userMap = new HashMap<UserRec,ArrayList<EntityRec>>();
			entities = new ArrayList<EntityRec>();
			entities.add(curEnt);
			userMap.put(curUser,entities);
			articleMap.put(curArt,userMap);
			System.out.println("found article, user, entity: "+curEnt+"\tnew user: "+curUser+"\tnew article: "+curArt);
		    }
		}
		return articleMap;
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private AnnotationRec[] getAnnotationsForArticle(int articleId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	HashSet<AnnotationRec> annotations = new HashSet<AnnotationRec>();
	try {
	    pstmt = con.prepareStatement(GET_ANNOTATIONS_FOR_ARTICLE_SQL);
	    pstmt.setInt(1,articleId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    int entityId = rs.getInt("entity_id");
		    int userId = rs.getInt("user_id");
		    annotations.add(new AnnotationRec(articleId,entityId,userId));
		}
		AnnotationRec[] arr = new AnnotationRec[annotations.size()];
		return annotations.toArray(arr);
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    


    private AnnotationRec[] getAnnotationsForArticleUser(int articleId, int userId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	HashSet<AnnotationRec> annotations = new HashSet<AnnotationRec>();
	try {
	    pstmt = con.prepareStatement(GET_ANNOTATIONS_FOR_ARTICLE_USER_SQL);
	    pstmt.setInt(1,articleId);
	    pstmt.setInt(2,userId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    rs.getInt("entity_id");
		    int entityId = 0;
		    if (!rs.wasNull()) entityId = rs.getInt("entity_id");
		    annotations.add(new AnnotationRec(articleId,entityId,userId));
		}
		AnnotationRec[] arr = new AnnotationRec[annotations.size()];
		return annotations.toArray(arr);
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private AnnotationRec[] getAnnotationsForEntity(int entityId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	HashSet<AnnotationRec> annotations = new HashSet<AnnotationRec>();
	try {
	    pstmt = con.prepareStatement(GET_ANNOTATIONS_FOR_ENTITY_SQL);
	    pstmt.setInt(1,entityId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    int articleId = rs.getInt("article_id");
		    int userId = rs.getInt("user_id");
		    annotations.add(new AnnotationRec(articleId,entityId,userId));
		}
		AnnotationRec[] arr = new AnnotationRec[annotations.size()];
		return annotations.toArray(arr);
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private AnnotationRec[] getAnnotationsForUser(int userId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	HashSet<AnnotationRec> annotations = new HashSet<AnnotationRec>();
	try {
	    pstmt = con.prepareStatement(GET_ANNOTATIONS_FOR_USER_SQL);
	    pstmt.setInt(1,userId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    int articleId = rs.getInt("article_id");
		    rs.getInt("entity_id");
		    int entityId = 0;
		    if (!rs.wasNull()) entityId = rs.getInt("entity_id");
		    annotations.add(new AnnotationRec(articleId,entityId,userId));
		}
		AnnotationRec[] arr = new AnnotationRec[annotations.size()];
		return annotations.toArray(arr);
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private EntityRec getEntityById(int id, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_ENTITY_BY_ID_SQL);
	    pstmt.setInt(1,id);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    String source = rs.getString("source_id");
		    String entity = rs.getString("entity_id");
		    return new EntityRec(id,source,entity);
		} else {
		    String msg = "DB lookup failed, entity id="+id;
		    throw new SQLException(msg);
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private EntityRec getEntityByOtherIds(String sourceId, String entityId, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_ENTITY_BY_OTHER_IDS_SQL);
	    pstmt.setString(1,sourceId);
	    pstmt.setString(2,entityId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    int id = rs.getInt("id");
		    return new EntityRec(id,sourceId,entityId);
		} else {
		    String msg = "DB lookup failed, source="+sourceId+" entity= "+entityId;
		    throw new SQLException(msg);
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private int getUserArticleCtEntities(int articleId,int userId,Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_USER_ARTICLE_COUNT_SQL);
	    pstmt.setInt(1,articleId);
	    pstmt.setInt(2,userId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    return rs.getInt("ct_entities");
		} else {
		    return 0;
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    


    private UserRec getUserByName(String name, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_USER_BY_NAME_SQL);
	    pstmt.setString(1,name);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    return new UserRec(rs.getInt("id"),name);
		} else {
		    return null;
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private UserRec getUserById(int id, Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    pstmt = con.prepareStatement(GET_USER_BY_ID_SQL);
	    pstmt.setInt(1,id);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		if (rs.next()) {
		    return new UserRec(id,rs.getString("name"));
		} else {
		    String msg = "DB lookup failed, user id="+id;
		    throw new SQLException(msg);
		}
	    } finally {
		closeResultSet(rs);
	    }
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    

    private void putArticleAnnotations(int userId, 
				       int articleId, 
				       EntityRec[] entities,
				       Connection con) throws SQLException {
	PreparedStatement pstmt = null;
	con.setAutoCommit(false);
	try {
	    pstmt = con.prepareStatement(DELETE_ANNOTATIONS_FOR_ARTICLE_USER_SQL);
	    pstmt.setInt(1,articleId);
	    pstmt.setInt(2,userId);
	    pstmt.executeUpdate();
	    for (EntityRec entity : entities) {
		pstmt = con.prepareStatement(INSERT_ANNOTATION_SQL);
		pstmt.setInt(1,articleId);
		pstmt.setInt(2,entity.id());
		pstmt.setInt(3,userId);
		pstmt.executeUpdate();
	    }
	    pstmt = con.prepareStatement(DELETE_USER_ARTICLE_COUNT_SQL);
	    pstmt.setInt(1,articleId);
	    pstmt.setInt(2,userId);
	    pstmt.executeUpdate();
	    pstmt = con.prepareStatement(INSERT_USER_ARTICLE_COUNT_SQL);
	    pstmt.setInt(1,articleId);
	    pstmt.setInt(2,userId);
	    pstmt.setInt(3,entities.length);
	    pstmt.executeUpdate();
	    con.commit();
	} finally {
	    closePreparedStatement(pstmt);
	}
    }	    


    // //////////////////////////////////////////////
    // GENERAL ROUTINES

    private Connection getConnection() throws SQLException {
	try {
	    if (!isStandalone) {
		return sDs.getConnection();
	    }
	    return sDs.getConnection(sUserName,sPassword);
	} catch (SQLException e) {
	    String msg = "Could not connect to DB.";
	    System.out.println(e.getMessage());
	    e.printStackTrace();
	    throw new SQLException(msg);
	}
    }

    private void closeConnection(Connection con) {
	if (con == null) return;
	try {
	    con.close();
	} catch (SQLException e) {
	    String msg = "Could not close connection.";
	    System.out.println(e.getMessage());
	    e.printStackTrace();
	    //	    mLogger.error(msg,e);
	    // eat exception
	}
    }

    private void closePreparedStatement(PreparedStatement pstmt) {
	if (pstmt == null) return;
	try {
	    pstmt.close();
	} catch (SQLException e) {
	    String msg = "Could not close prepared statement.";
	    //	    mLogger.error(msg,e);
	    // eat exception
	}
    }

    private void closeResultSet(ResultSet rs) {
	if (rs == null) return;
	try {
	    rs.close();
	} catch (SQLException e) {
	    String msg = "Could not close result set.";
	    //	    mLogger.error(msg,e);
	    // eat exception
	}
    }


    private static final String[] CONFUSION_MATRIX_CATEGORIES = new String[] { "Mentioned", "Unmentioned" };
    private ConfusionMatrix getConfusionMatrix(int tp, int fp, int fn) {
	int[][] cm = new int[][] 
	    {  { tp, fp },
	       { fn, 0 } 
	    };
	return new ConfusionMatrix(CONFUSION_MATRIX_CATEGORIES,
				   cm);
    }
}
