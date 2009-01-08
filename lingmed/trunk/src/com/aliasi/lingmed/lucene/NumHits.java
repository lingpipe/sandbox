package com.aliasi.lingmed.lucene;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import java.io.IOException;

// imports only for main
import com.aliasi.lingmed.medline.MedlineCodec;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.analysis.Analyzer;


public class NumHits {

    public static int numHitsDirect(String phrase,
                                    Searcher searcher,
                                    QueryParser queryParser) 
        throws IOException, ParseException {

        Query query = queryParser.parse(phrase);
        Hits hits = searcher.search(query);
        return hits.length();
    }

    public static int numHits(String phrase,
                               Searcher searcher,
                               QueryParser queryParser)
        throws IOException, ParseException {

        Query query = queryParser.parse(phrase);
        CountCollector counter = new CountCollector();
        searcher.search(query,counter);
        return counter.count();
    }

    static class CountCollector extends HitCollector {
        private int mCount = 0;
        public void collect(int doc, float score) {
            ++mCount;
        }
        public int count() {
            return mCount;
        }
    }
        
    // NumHits <indexPath> <phrase1> ... <phraseN>
    public static void main(String[] args) throws Exception {
        Searcher searcher = new IndexSearcher(args[0]);
	MedlineCodec codec = new MedlineCodec();
	LuceneAnalyzer analyzer = codec.getAnalyzer();
        QueryParser queryParser = new QueryParser("foo",analyzer);
        for (int i = 1; i < args.length; ++i) {
            String phrase = args[i];
            String query = "abstractX:(+\"" + phrase + "\") titleX:(+\"" + phrase + "\")";
            int numHits = NumHits.numHits(query,searcher,queryParser);
            System.out.printf("%10d  ",numHits);
            System.out.println(query);
        }
    }



}