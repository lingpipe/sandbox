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

package com.aliasi.lingmed.medline;

import com.aliasi.lingmed.dao.Codec;
import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.dao.DaoSearcher;
import com.aliasi.lingmed.dao.DaoSearcherImpl;
import com.aliasi.lingmed.dao.SearchResults;

import com.aliasi.medline.MedlineCitation;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import java.io.IOException;

/**
 * A <code>MedlineSearcher</code> provides search methods 
 * over a data store of {@link MedlineCitation} objects.
 * 
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class MedlineSearcherImpl extends DaoSearcherImpl<MedlineCitation> 
    implements MedlineSearcher {

    /** 
     * Instantiate the <code>MedlineSearcher</code>
     * @param searcher searcher
     * 
     * @throws IOException
     */
    public MedlineSearcherImpl(Codec<MedlineCitation> codec, 
                               Searcher searcher) 
        throws IOException {
        super(codec, searcher);
    }

    /**      
     * Return count for MedlineCitations which have an exact match for phrase
     * in either the title or abstract field.
     */
    public int numExactPhraseMatches(String phrase) throws DaoException {
        String query = "abstractX:(+\"" + phrase + "\") titleX:(+\"" + phrase + "\")";
        return numHits(query);
    }

    /**      
     * Find all MedlineCitations published in the range fromYear, toYear, inclusive.
     */
    public SearchResults<MedlineCitation> getCitationsInYearRange(String fromYear, String toYear) 
        throws DaoException {

        if (!isYear(fromYear) || !isYear(toYear)
            || fromYear.compareTo(toYear) > 0) {
            throw new DaoException("bad year search range: "+fromYear+", "+toYear);
        }
        ConstantScoreRangeQuery rangeQuery = 
            new ConstantScoreRangeQuery(SearchableMedlineCodec.DATE_YEAR_FIELD,
                                        fromYear,
                                        toYear,
                                        true,
                                        true);
        return search(rangeQuery);
    }

    // check that year is string of 4 digits
    private boolean isYear(String s) {
        if (s.length() != 4) {
            return false;
        }
        for (int i=0; i<s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}