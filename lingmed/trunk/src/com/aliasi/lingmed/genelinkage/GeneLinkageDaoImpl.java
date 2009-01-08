/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.lingmed.genelinkage;

import  com.aliasi.chunk.Chunk;
import  com.aliasi.chunk.ChunkFactory;
import  com.aliasi.lingmed.dao.*;
import com.aliasi.util.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;



import org.apache.log4j.Logger;

/**
 * A <code>GeneLinkageDao</code> provides search functionality 
 * over a MySQL database containing records of 
 * gene mentions in pubmed citations.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class GeneLinkageDaoImpl implements GeneLinkageDao {
    private final Logger mLogger
	= Logger.getLogger(GeneLinkageDaoImpl.class);

    private static final String CT_ARTICLES_FOR_GENE_SQL = 
	"SELECT COUNT(*) FROM gene_article_score " 
	+ " WHERE entrezgene_id = ? ";

    private static final String CT_ARTICLES_FOR_GENE_THRESHOLD_SQL = 
	"SELECT COUNT(*) FROM gene_article_score " 
	+ " WHERE entrezgene_id = ? "
	+ " AND total_score < ? ";

    private static final String GET_MENTIONS_FOR_GENE_SQL = 
	"SELECT g.pubmed_id, g.per_gene_score, g.total_score, "
	+ " m.text_matched, m.start_offset "
	+ " FROM gene_article_score g, gene_article_mention m "
	+ " WHERE g.entrezgene_id = ? "
	+ " AND g.entrezgene_id = m.entrezgene_id "
	+ " AND g.pubmed_id = m.pubmed_id "
	;

    private static final String GET_N_MENTIONS_FOR_GENE_SQL = 
	"SELECT g.pubmed_id, g.entrezgene_id, g.per_gene_score, g.total_score, "
	+ " m.text_matched, m.start_offset "
	+ " FROM gene_article_score g, gene_article_mention m "
	+ " WHERE g.entrezgene_id = ? "
	+ " AND g.entrezgene_id = m.entrezgene_id "
	+ " AND g.pubmed_id = m.pubmed_id "
	+ " ORDER BY g.total_score asc "
	+ " limit ? "
	;

    private static final String GET_MENTIONS_FOR_PUBMED_SQL = 
	"SELECT g.entrezgene_id, g.per_gene_score, g.total_score, "
	+ " m.text_matched, m.start_offset "
	+ " FROM gene_article_score g, gene_article_mention m "
	+ " WHERE g.pubmed_id = ? "
	+ " AND g.entrezgene_id = m.entrezgene_id "
	+ " AND g.pubmed_id = m.pubmed_id "
	;


    private static MysqlDaoImpl sDao;
    private static GeneLinkageDao sInstance;

    public static synchronized GeneLinkageDao getInstance(Context context,String name) throws NamingException {
	if (sInstance == null) {
	    sDao = (MysqlDaoImpl)MysqlDaoImpl.getInstance(context,name);
	    sInstance = new GeneLinkageDaoImpl();
	}
	return sInstance;
    }

    public static synchronized GeneLinkageDao getInstance(Context context,String name, 
						   String userName, String password) throws NamingException {
	if (sInstance == null) {
	    sDao = (MysqlDaoImpl)MysqlDaoImpl.getInstance(context,name,userName,password);
	    sInstance = new GeneLinkageDaoImpl();
	}
	return sInstance;
    }

    /** Given geneId, find all articles which mention it.
     * Returns a Map from article ids to a pair consisting
     * of the  article genomics_score, and a set of gene mentions.
     */
    public Map<String,Pair<Double,Set<Chunk>>> getArticleMentionsForGeneId(int geneId) 
	throws SQLException {
	Connection con = null;
	try {
	    con = sDao.getConnection();
	    return getArticleMentionsForGeneId(geneId,con);
	} finally {
	    sDao.closeConnection(con);
	}
    }

    /** Given geneId, find N best articles which mention it.
     * Returns a Map from article ids to a pair consisting
     * of the  article genomics_score, and a set of gene mentions,
     * ordered by total score.
     */
    public Map<String,Pair<Double,Set<Chunk>>> getNArticleMentionsForGeneId(int geneId, int limit) 
	throws SQLException {
	Connection con = null;
	try {
	    con = sDao.getConnection();
	    return getNArticleMentionsForGeneId(geneId,limit,con);
	} finally {
	    sDao.closeConnection(con);
	}
    }

    /** Given pubmedId, find all gene mentions.
     * Returns the article genomics_score, and a set of gene mentions.
     */
    public Pair<Double,Set<Chunk>> getGeneMentionsForPubmedId(int pubmedId) throws SQLException {
	Connection con = null;
	try {
	    con = sDao.getConnection();
	    return getGeneMentionsForPubmedId(pubmedId,con);
	} finally {
	    sDao.closeConnection(con);
	}
    }


    private Map<String,Pair<Double,Set<Chunk>>> getArticleMentionsForGeneId(int geneId, Connection con) 
	throws SQLException {
	PreparedStatement pstmt = null;
	Map<String,Pair<Double,Set<Chunk>>> articlesMap = new HashMap<String,Pair<Double,Set<Chunk>>>();
	try {
	    pstmt = con.prepareStatement(GET_MENTIONS_FOR_GENE_SQL);
	    pstmt.setInt(1,geneId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    int pubmedId = rs.getInt("pubmed_id");
		    double perGeneScore = rs.getDouble("per_gene_score");
		    double totalScore = rs.getDouble("total_score");
		    double genomicsScore = totalScore - perGeneScore;
		    String phrase = rs.getString("text_matched");
		    int start = rs.getInt("start_offset");
		    addArticleMention(articlesMap,pubmedId,geneId,perGeneScore,genomicsScore,phrase,start);
		}
		return articlesMap;
	    } finally {
		sDao.closeResultSet(rs);
	    }
	} finally {
	    sDao.closePreparedStatement(pstmt);
	}
    }	    

    private Map<String,Pair<Double,Set<Chunk>>> getNArticleMentionsForGeneId(int geneId, int limit, Connection con) 
	throws SQLException {
	PreparedStatement pstmt = null;
	Map<String,Pair<Double,Set<Chunk>>> articlesMap = new HashMap<String,Pair<Double,Set<Chunk>>>();
	try {
	    pstmt = con.prepareStatement(GET_N_MENTIONS_FOR_GENE_SQL);
	    pstmt.setInt(1,geneId);
	    pstmt.setInt(2,limit);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		while (rs.next()) {
		    int pubmedId = rs.getInt("pubmed_id");
		    double perGeneScore = rs.getDouble("per_gene_score");
		    double totalScore = rs.getDouble("total_score");
		    double genomicsScore = totalScore - perGeneScore;
		    String phrase = rs.getString("text_matched");
		    int start = rs.getInt("start_offset");
		    addArticleMention(articlesMap,pubmedId,geneId,perGeneScore,genomicsScore,phrase,start);
		}
		return articlesMap;
	    } finally {
		sDao.closeResultSet(rs);
	    }
	} finally {
	    sDao.closePreparedStatement(pstmt);
	}
    }	    

    private void addArticleMention(Map<String,Pair<Double,Set<Chunk>>> articlesMap,
			   int pubmedId, 
			   int geneId,
			   double perGeneScore,
			   double genomicsScore,
			   String phrase,
			   int start) {
	if (pubmedId == 0) return;
	String sPubmedId = Integer.toString(pubmedId);
	String sGeneId = Integer.toString(geneId);
	int end = start + phrase.length();
	Chunk chunk = ChunkFactory.createChunk(start,end,sGeneId,perGeneScore);
	if (articlesMap.containsKey(sPubmedId)) {
	    Pair<Double,Set<Chunk>> article = articlesMap.get(sPubmedId);
	    article.b().add(chunk);
	} else {
	    Set<Chunk> chunkSet = new HashSet<Chunk>();
	    chunkSet.add(chunk);
	    Pair<Double,Set<Chunk>> article = new Pair<Double,Set<Chunk>>(genomicsScore,chunkSet);
	    articlesMap.put(sPubmedId,article);
	}
    }

    private Pair<Double,Set<Chunk>> getGeneMentionsForPubmedId(int pubmedId, Connection con) 
	throws SQLException {
	PreparedStatement pstmt = null;
	try {
	    Set<Chunk> chunkSet = new HashSet<Chunk>();
	    pstmt = con.prepareStatement(GET_MENTIONS_FOR_PUBMED_SQL);
	    pstmt.setInt(1,pubmedId);
	    ResultSet rs = null;
	    try {
		rs = pstmt.executeQuery();
		double genomicsScore = 0.0;
		while (rs.next()) {
		    int geneId = rs.getInt("entrezgene_id");
		    double perGeneScore = rs.getDouble("per_gene_score");
		    double totalScore = rs.getDouble("total_score");
		    genomicsScore = totalScore - perGeneScore;
		    String phrase = rs.getString("text_matched");
		    int start = rs.getInt("start_offset");
		    addGeneMention(chunkSet,geneId,perGeneScore,phrase,start);
		}
		Pair<Double,Set<Chunk>> result = new Pair<Double,Set<Chunk>>(genomicsScore,chunkSet);
		return result;
	    } finally {
		sDao.closeResultSet(rs);
	    }
	} finally {
	    sDao.closePreparedStatement(pstmt);
	}
    }	    

    private void addGeneMention(Set<Chunk> chunkSet,
			   int geneId,
			   double perGeneScore,
			   String phrase,
			   int start) {
	if (geneId == 0) return;
	String sGeneId = Integer.toString(geneId);
	int end = start + phrase.length();
	Chunk chunk = ChunkFactory.createChunk(start,end,sGeneId,perGeneScore);
	chunkSet.add(chunk);
    }
}