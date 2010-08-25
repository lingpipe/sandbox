package com.lingpipe.book.applucene;

import org.apache.lucene.index.Term;

import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanQuery;

import org.apache.lucene.search.BooleanClause.Occur;



class FragmentsLucene {

    private FragmentsLucene() { /* no instances */ }

    void frag1() {
        
        /*x FragmentsLucene.1 */
        BooleanQuery bq1 = new BooleanQuery();
        bq1.add(new TermQuery(new Term("text","biology")), Occur.MUST);
        bq1.add(new TermQuery(new Term("text","cell")), Occur.SHOULD);
        
        BooleanQuery bq2 = new BooleanQuery();
        bq2.add(new TermQuery(new Term("text","micro")), Occur.SHOULD);
        bq2.add(bq1,Occur.MUST);
        /*x*/
    }
    
}