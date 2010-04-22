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

package com.aliasi.lingmed.test.functional.medline;

import com.aliasi.lingmed.dao.*;
import com.aliasi.lingmed.entrezgene.*;
import com.aliasi.lingmed.medline.*;
import com.aliasi.lingmed.server.*;

import com.aliasi.lingmed.medline.parser.*;

import java.io.*;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;

import org.apache.log4j.Logger;

public class VerifyDates {

    public static void main(String[] args) throws Exception {
        File luceneDir = new File( args[0] );
        Searcher medlineLocalSearcher = new IndexSearcher(FSDirectory.getDirectory(luceneDir));
        MedlineSearcher medlineSearcher = new MedlineSearcherImpl(new MedlineCodec(),medlineLocalSearcher);

        int ct=0;
        for (MedlineCitation citation : medlineSearcher) {
            if (citation.article() == null 
                || citation.article().journal() == null
                || citation.article().journal().journalIssue() == null
                || citation.article().journal().journalIssue().pubDate() == null) {
                continue;
            }
            PubDate pubDate = citation.article().journal().journalIssue().pubDate();
            if (pubDate.isStructured()) {
                System.out.println("citation: "+citation.pmid()
                                   +"\tpubdate "+pubDate.toString());
            } else {
                System.out.println("citation: "+citation.pmid()
                                   +"\tUNSTRUCTURED "+pubDate.medlineDate());
            }           

        }
        System.exit(0);
    }

}
