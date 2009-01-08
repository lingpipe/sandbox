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

package com.aliasi.lingmed.dao;

import com.aliasi.lingmed.lucene.Fields;
import com.aliasi.lingmed.lucene.LuceneAnalyzer;
import com.aliasi.lingmed.lucene.LuceneTokenStream;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.document.Document;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

/**
 * A <code>DaoSearcher</code> provides search functionality over a Lucene Index. 
 * Search queries return a {@link SearchResults}&lt;E&gt;,
 * which is a set of ranked objects.
 *
 * The <code>DaoSearcher</code> interface extends the Iterable interface,
 * and so can be used to walk over the entire index.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public interface DaoSearcher<E> extends Iterable<E>  {

    /**
     * Executes a search against the {@link Fields#ID_FIELD}.
     * Returns an object of the generic type which matches that ID, or
     * <code>null</code> if no object with that ID is found in the index.
     *
     * @param id search string for ID field
     * @throws DaoException
     */
    public E getById(String id) throws DaoException;

    /**
     * Executes a query against the Lucene index.
     * Returns a {@link SearchResults} containing objects of the generic type
     * ranked by Lucene score.
     *
     * @param query Lucene Query object
     * @throws DaoException
     */
    public SearchResults<E> search(Query query)	throws DaoException;

    /**
     * Executes a search against the Lucene index.
     * Returns a {@link SearchResults} containing objects of the generic type
     * ranked by Lucene score.
     * 
     * <P>Note that the query is passed through to Lucene without any error checking.
     *
     * @param queryString Lucene query
     * @throws DaoException
     */
    public SearchResults<E> search(String queryString) throws DaoException;

    /**      
     * Executes a query against the Lucene index.
     * Returns the number of documents which match.
     *
     * @param queryString Lucene query as string
     * @throws DaoException
     */
    public int numHits(String queryString) throws DaoException;
}
