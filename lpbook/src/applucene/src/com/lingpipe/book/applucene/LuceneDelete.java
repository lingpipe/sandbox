package com.lingpipe.book.applucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.LimitTokenCountAnalyzer;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class LuceneDelete {

    private LuceneDelete() { /* no instances */ }

    public static void main(String[] args) 
        throws CorruptIndexException, IOException {

        File indexDir = new File(args[0]);
        String fieldName = args[1];
        String token = args[2];

        Directory fsDir = FSDirectory.open(indexDir);

        Analyzer stdAn 
            = new StandardAnalyzer(Version.LUCENE_36);
        Analyzer ltcAn 
            = new LimitTokenCountAnalyzer(stdAn,Integer.MAX_VALUE);

        /*x LuceneDelete.1 */
        IndexWriterConfig iwConf 
            = new IndexWriterConfig(Version.LUCENE_36,ltcAn);
        iwConf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);

        IndexWriter indexWriter
            = new IndexWriter(fsDir,iwConf);
        /*x*/

        /*x LuceneDelete.2 */
        int numDocsBefore = indexWriter.numDocs();

        Term term = new Term(fieldName,token);
        indexWriter.deleteDocuments(term);
        boolean hasDeletedDocs = indexWriter.hasDeletions();
        int numDocsAfterDeleteBeforeCommit = indexWriter.numDocs();

        indexWriter.commit();
        int numDocsAfter = indexWriter.numDocs();

        indexWriter.close();
        /*x*/

        System.out.println("index.dir=" + indexDir.getCanonicalPath());
        System.out.println("field.name=" + fieldName);
        System.out.println("token=" + token);
        System.out.println("Num docs before delete=" + numDocsBefore);
        System.out.println("Has deleted docs=" + hasDeletedDocs);
        System.out.println("Num docs after delete before commit=" + numDocsAfterDeleteBeforeCommit);
        System.out.println("Num docs after commit=" + numDocsAfter);
    }

}