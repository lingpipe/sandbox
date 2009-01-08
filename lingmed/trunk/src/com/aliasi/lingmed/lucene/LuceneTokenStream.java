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

package com.aliasi.lingmed.lucene;

import com.aliasi.tokenizer.Tokenizer;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;


/**
 * A <code>LuceneTokenStream</code> is an adapter class which enables
 * us to use a LingPipe {@link com.aliasi.tokenizer.Tokenizer} with a
 * Lucene analyzer .
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */
public class LuceneTokenStream extends TokenStream {

    private Tokenizer mTokenizer;

    public LuceneTokenStream(Tokenizer tokenizer) {
        mTokenizer = tokenizer;
    }

    public void close() { 
	mTokenizer = null;
    }

    public Token next() throws IOException {
	if (mTokenizer == null) return null;
        String nextToken = mTokenizer.nextToken();
        if (nextToken == null) return null;
        int start = mTokenizer.lastTokenStartPosition();
        int end = start + nextToken.length();  // adding length is a hack; won't work with stemmers
        return new Token(nextToken,start,end);
    }

}
