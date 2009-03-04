package com.aliasi.webnotes;

import com.aliasi.classify.ConfusionMatrix;
import java.sql.SQLException;
import java.util.HashMap;

public interface WikiDao {

    public ConfusionMatrix interAnnotatorAgreement(int user1Id, int user2Id) throws SQLException;

    /** count true positive annotations for pair of users */
    public int ctTruePositiveAnnotations(int user1Id, int user2Id) throws SQLException;

    /** count false positive annotations for pair of users */
    public int ctFalsePositiveAnnotations(int user1Id, int user2Id) throws SQLException;

    /** count false negative annotations for pair of users */
    public int ctFalseNegativeAnnotations(int user1Id, int user2Id) throws SQLException;

    /** count number of entities annotated for article, user */
    public int ctAnnotationsForArticleUser(int articleId, int userId) throws SQLException;

    /** get all articles in db */
    public ArticleRec[] getAllArticles() throws SQLException;

    /** get all entities in db */
    public EntityRec[] getAllEntities() throws SQLException;

    /** get all users in db */
    public UserRec[] getAllUsers() throws SQLException;

    /** get annotations for article */
    public AnnotationRec[] getAnnotationsForArticle(int articleId) throws SQLException;

    /** get annotations for article */
    public AnnotationRec[] getAnnotationsForArticleUser(int articleId, int userId) throws SQLException;

    /** get annotations for entity */
    public AnnotationRec[] getAnnotationsForEntity(int entityId) throws SQLException;

    /** get annotations for user */
    public AnnotationRec[] getAnnotationsForUser(int userId) throws SQLException;

    public HashMap<ArticleRec, HashMap> getAnnotationDetail() throws SQLException;


    /** get article record for id */
    public ArticleRec getArticleById(int id) throws SQLException;

    /** get article record for id */
    public ArticleRec getArticleByPmid(int pmid) throws SQLException;

    /** get articles which have annotations for entity */
    public ArticleRec[] getArticlesForEntity(int entityId) throws SQLException;

    /** get articles which have annotations by user */
    public ArticleRec[] getArticlesByUser(int userId) throws SQLException;

    /** get articles which have annotations */
    public ArticleRec[] getArticlesWithEntity() throws SQLException;

    /** get entity record for id */
    public EntityRec getEntityById(int id) throws SQLException;

    /** get entity record by source_id, entity_id */
    public EntityRec getEntityByOtherIds(String sourceId, String entityId) throws SQLException;

    /** get first article for annotation */
    public ArticleRec getFirstArticle() throws SQLException;

    /** get last article annotation for user */
    public ArticleRec getLastArticleForUser(int userId) throws SQLException;

    /** get next article according to article ordering */
    public ArticleRec getNextArticle(int prevArticleId) throws SQLException;

    /** get count of entities in article annotated by user */
    public int getUserArticleCtEntities(int articleId, int userId) throws SQLException;

    /** get user record for name */
    public UserRec getUserByName(String name) throws SQLException;

    /** get user record for id */
    public UserRec getUserById(int id) throws SQLException;

    /** annotate article */
    public void putArticleAnnotations(int userId, int articleId, EntityRec[] entities) throws SQLException;

}
