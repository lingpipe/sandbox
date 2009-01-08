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

package com.aliasi.lingmed.homologene;

import com.aliasi.lingmed.dao.Codec;
import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.dao.DaoSearcher;
import com.aliasi.lingmed.dao.DaoSearcherImpl;
import com.aliasi.lingmed.dao.SearchResults;

import java.io.IOException;

import org.apache.lucene.index.Term;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;


/**
 * A <code>HomologeneSearcher</code> provides search methods 
 * over a data store of {@link HomologeneGroup} objects.
 * 
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class HomologeneSearcherImpl extends DaoSearcherImpl<HomologeneGroup> 
    implements HomologeneSearcher {

    /** 
     * Instantiate the <code>HomologeneSearcher</code>
     * 
     * @param codec Codec implementation for Homologene objects
     * @param searcher Searcher on Lucene index
     * 
     * @throws IOException
     */
    public HomologeneSearcherImpl(Codec<HomologeneGroup> codec, 
                                  Searcher searcher) throws IOException {
	super(codec, searcher);
    }
 
    /**      
     * Find HomologeneGroups given EntrezGene geneId,taxonId
     */
    public SearchResults<HomologeneGroup> getHomoloGroupsForGene(String geneId, String taxonId) 
	throws DaoException {
	String taxon = geneId+"_"+taxonId;
        Term term = new Term(HomologeneCodec.TAXON_FIELD,taxon);
	Query query = new TermQuery(term);
	return search(query);
    }
}
