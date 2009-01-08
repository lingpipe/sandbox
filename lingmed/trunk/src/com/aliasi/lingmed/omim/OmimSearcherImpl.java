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

package com.aliasi.lingmed.omim;

import com.aliasi.corpus.Handler;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.dao.Codec;
import com.aliasi.lingmed.dao.DaoSearcher;
import com.aliasi.lingmed.dao.DaoSearcherImpl;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Searcher;

/**
 * A <code>OmimSearcher</code> provides search methods 
 * over a data store of {@link OmimRecord} objects.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class OmimSearcherImpl 
    extends DaoSearcherImpl<OmimRecord> 
    implements OmimSearcher {


    /** 
     * Instantiate the <code>OmimSearcher</code>
     *
     * @param codec Codec implementation for OmimRecord objects
     * @param searcher Searcher on Lucene index
     * 
     * @throws IOException
     */
    public OmimSearcherImpl(Codec<OmimRecord> codec, 
                            Searcher searcher) throws IOException {
	super(codec, searcher);
    }


}