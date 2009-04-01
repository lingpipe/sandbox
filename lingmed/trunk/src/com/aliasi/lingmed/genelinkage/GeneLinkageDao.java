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
import com.aliasi.util.Pair;

import java.util.Map;
import java.util.Set;

import java.sql.SQLException;

/**
 * A <code>GeneLinkageDao</code> provides search functionality 
 * over a MySQL database containing records of 
 * gene mentions in pubmed citations.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public interface GeneLinkageDao {


    /** Given geneId, find all articles which mention it.
     * Returns a Map from article ids to a pair consisting
     * of the  article genomics_score, and a set of gene mentions.
     */
    public Map<String,Pair<Double,Set<Chunk>>> getArticleMentionsForGeneId(int geneId) throws SQLException;

    /** Given geneId, find N best articles which mention it.
     * Returns a Map from article ids to a pair consisting
     * of the  article genomics_score, and a set of gene mentions,
     * ordered by total score.
     */
    public Map<String,Pair<Double,Set<Chunk>>> getNArticleMentionsForGeneId(int geneId, int limit) throws SQLException;

    /** Given pubmedId, find all gene mentions.
     * Returns the article genomics_score, and a set of gene mentions.
     */
    public Pair<Double,Set<Chunk>> getGeneMentionsForPubmedId(int pubmedId) throws SQLException;

    //    public Pair<String,double>[] getArticleScoresForGeneId(String geneId) throws SQLException;
    //    public Pair<String,double>[] getGeneScoresForArticle(String articleId) throws SQLException;

    //    /** Given geneId, get count of articles which mention it.
    //     */
    //    public int ctArticlesForGeneId(String geneId) throws SQLException;

    //    /** Given geneId, get count of articles which mention it 
    //     * where per-gene language model score is below threshold.
    //     */
    //    public int ctArticlesForGeneIdThreshold(String geneId, double threshold) throws SQLException;

}