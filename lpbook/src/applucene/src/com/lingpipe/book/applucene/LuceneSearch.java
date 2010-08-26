package com.lingpipe.book.applucene;


import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class LuceneSearch {

    /*x LuceneSearch.1 */
    public static void main(String[] args) 
        throws ParseException, CorruptIndexException,
               IOException {

        File indexDir = new File(args[0]);
        String query = args[1];
        int maxHits = Integer.parseInt(args[2]);
    /*x*/        

        System.out.println("Index Dir=" + indexDir.getCanonicalPath());
        System.out.println("query=" + query);
        System.out.println("max hits=" + maxHits);
        System.out.println("Hits (rank,score,file name)");
        
    /*x LuceneSearch.2 */
        Directory dir = FSDirectory.open(indexDir);
        IndexReader reader = IndexReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        Version v = Version.LUCENE_30;
        String defaultField = "text";
        Analyzer analyzer = new StandardAnalyzer(v);
        QueryParser parser 
            = new QueryParser(v,defaultField,analyzer);
    /*x*/

    /*x LuceneSearch.3 */
        Query q = parser.parse(query);
        
        TopDocs hits = searcher.search(q,maxHits);
        ScoreDoc[] scoreDocs = hits.scoreDocs;

        for (int n = 0; n < scoreDocs.length; ++n) {
            ScoreDoc sd = scoreDocs[n];
            float score = sd.score;
            int docId = sd.doc;
            Document d = searcher.doc(docId);
            String fileName = d.get("file");
    /*x*/
            System.out.printf("%3d %4.2f  %s\n",
                              n, score, fileName);
        }
    }

}