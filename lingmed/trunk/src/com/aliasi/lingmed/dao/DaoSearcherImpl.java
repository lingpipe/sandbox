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

import com.aliasi.util.Iterators;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.document.Document;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import org.apache.log4j.Logger;

/**
 * Base implementation for {@link DaoSearcher}.
 * A {@link Codec}&lt;E&gt; is used to convert Lucene documents to 
 * objects of the generic type.
 * A {@link org.apache.lucene.search.Searcher} used to search the index. 
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class DaoSearcherImpl<E> implements DaoSearcher<E> {
    private final Logger mLogger
        = Logger.getLogger(DaoSearcherImpl.class);

    private final Analyzer mQueryAnalyzer;
    private final Codec<E> mCodec;
    private final QueryParser mQueryParser;
    private final Searcher mSearcher;

    private static final int MAX_HITS = 10000;

    /**
     * Instantiate the <code>DaoSearcher</code> for an index.
     *
     * @param codec Codec implementation for generic object of type <E>
     * @param searcher Searcher on Lucene index
     * 
     * @throws IOException
     */
    public DaoSearcherImpl(Codec<E> codec,
                           Searcher searcher) throws IOException {
        mQueryAnalyzer = codec.getAnalyzer();
        mCodec = codec;
        mQueryParser = new QueryParser(Fields.DEFAULT_FIELD,mQueryAnalyzer);
        mSearcher = searcher;
    }

    /**
     * Returns an {@link java.util.Iterator} over all documents in the index.
     * Iterator returns objects of the generic type.
     * Objects are instantiated when <code>next()</code> is called.
     */
    public Iterator<E> iterator() {
        try {
            // check index version??
            return new LuceneDocIterator(mCodec);
        } catch (Exception e) {
            String msg = "Index corrupted."
                + " Exception=" + e;
            IllegalStateException e2 
                = new IllegalStateException(msg);
            e2.setStackTrace(e.getStackTrace());
            throw e2;
        }
    }

    /**
     * Executes a search against the {@link Fields#ID_FIELD}.
     * Returns an object of the generic type which matches that ID, or
     * <code>null</code> if no object with that ID is found in the index.
     *
     * @param id search string for ID field
     * @throws DaoException
     */
    public E getById(String id) throws DaoException {
        SearchResults<E> results = search(Fields.ID_FIELD + ":" + id);
        // ??
        if (results.size() == 0) 
            return null; // ?? or return diff exception?
        if (results.size() > 1) {
            String msg = "Multiple entries with id=" + id;
            throw new DaoException(msg);
        }
        return results.getResult(0);
    }



    /**
     * Executes a query against the Lucene index.
     * Returns a {@link SearchResults} containing objects of the generic type
     * ranked by Lucene score.
     *
     * @param query Lucene Query object
     * @throws DaoException
     */
    public SearchResults<E> search(Query query)
        throws DaoException {
        try {
            TopDocs results = mSearcher.search(query,MAX_HITS);
            return new LuceneSearchResults(mCodec,results);
        } catch (Exception e) {
            String message = "search exception: "+e.getMessage();
            throw new DaoException(message, e);
        }
    }

    /**
     * Executes a search against the Lucene index 
     * Returns a {@link SearchResults} containing objects of the generic type
     * ranked by Lucene score.
     * 
     * <P>Note that the query is passed through to Lucene without any error checking.
     *
     * @param queryString Lucene query as string
     * @throws DaoException
     */
    public SearchResults<E> search(String queryString) 
        throws DaoException {
        try {
            Query query = mQueryParser.parse(queryString);
            return search(query);
        } catch (Exception e) {
            String message = "search("+queryString+") exception: "+e.getMessage();
            throw new DaoException(message, e);
        }
    }

    /**      
     * Executes a query against the Lucene index.
     * Returns the number of documents which match.
     *
     * @param queryString Lucene query as string
     * @throws DaoException
     */
    public int numHits(String queryString) throws DaoException {
        try {
            Query query = mQueryParser.parse(queryString);
            TopDocs results = mSearcher.search(query,MAX_HITS);
            return results.totalHits;
        } catch (Exception e) {
            String message = "search exception: "+e.getMessage();
            throw new DaoException(message, e);
        }
    }

    class LuceneDocIterator extends Iterators.Buffered<E> {
        private final Codec<E> mCodec;
        int mCurrentDoc = 0;
        final int mMaxDoc;
        public LuceneDocIterator(Codec<E> codec) throws IOException {
            mCodec = codec;
            mMaxDoc = mSearcher.maxDoc();
            Logger.getLogger(LuceneDocIterator.class).debug("maxDoc: " + mMaxDoc);
        }
        public E bufferNext() {
            try {
                while (mCurrentDoc < mMaxDoc) {
                    Document doc = mSearcher.doc(mCurrentDoc);
                    mCurrentDoc++;
                    if (doc != null) {
                        E obj = mCodec.toObject(doc);
                        if (obj != null)
                            return obj;
                    }
                }
                return null;
            } catch (IOException e) {
                String msg = "Index corrupted."
                    + " IOException=" + e;
                NoSuchElementException e2 
                    = new NoSuchElementException(msg);
                e2.setStackTrace(e.getStackTrace());
                throw e2;
            }
        }
    }
 
    private class LuceneSearchResults implements SearchResults {
        private final Codec<E> mCodec;
        private final TopDocs mResults;
        LuceneSearchResults(Codec<E> codec, TopDocs results) {
            mCodec = codec;
            mResults = results;
        }
        public int size() {
            return mResults.totalHits;
        }
        public double getScore(int rank) {
            return mResults.scoreDocs[rank].score;
        }
        public E getResult(int rank) throws DaoException {
            try {
                Document d = mSearcher.doc(mResults.scoreDocs[rank].doc);
                return mCodec.toObject(d);
            } catch (IOException ioe) {
                String message = "getResult("+rank+") exception: "+ioe.getMessage();
                throw new DaoException(message, ioe);
            }
        }
        public Iterator<E> iterator() {
            return new ResultsIterator();
        }
        class ResultsIterator implements Iterator<E> {
            int mNextHit = 0;
            public boolean hasNext() {
                return mNextHit < mResults.totalHits;
            }
            public E next() {
                if (!hasNext()) {
                    String msg = "No more results.";
                    throw new NoSuchElementException(msg);
                }
                try {
                    return getResult(mNextHit++);
                } catch (DaoException dao) {
                    String message = "getResult("+(mNextHit-1)+") exception: "+dao.getMessage();
                    throw new NoSuchElementException(message);
                }
            }
            public void remove() {
                String msg = "Cannot remove documents through iterators.";
                throw new UnsupportedOperationException(msg);
            }
        }
    }

}
