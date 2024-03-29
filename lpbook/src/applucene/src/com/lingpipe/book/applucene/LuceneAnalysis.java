package com.lingpipe.book.applucene;

import com.aliasi.util.Strings;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class LuceneAnalysis {

    private LuceneAnalysis() { }
    
    public static void main(String[] args) throws IOException {
        String fieldName = args[0];
        String text = args[1];
        
        /*x LuceneAnalysis.1 */
        StandardAnalyzer analyzer 
            = new StandardAnalyzer(Version.LUCENE_36);
        /*x*/
        
        /*x LuceneAnalysis.2 */
        Reader textReader = new StringReader(text);

        TokenStream tokenStream 
            = analyzer.tokenStream(fieldName,textReader);

        CharTermAttribute terms = 
            tokenStream.addAttribute(CharTermAttribute.class);

        OffsetAttribute offsets 
            = tokenStream.addAttribute(OffsetAttribute.class);

        PositionIncrementAttribute positions
            = tokenStream
            .addAttribute(PositionIncrementAttribute.class);
        /*x*/

        System.out.println(Strings.textPositions(text));
        System.out.printf("\n%5s (%5s, %5s) %s\n","INCR","START","END","TERM");
        /*x LuceneAnalysis.3 */
        while (tokenStream.incrementToken()) {
            int increment = positions.getPositionIncrement();
            int start = offsets.startOffset();
            int end = offsets.endOffset();
            String term = terms.toString();
        /*x*/
            System.out.printf("%5d (%5d, %5d) %s\n",increment,start,end,term);
        }
        
    }

}