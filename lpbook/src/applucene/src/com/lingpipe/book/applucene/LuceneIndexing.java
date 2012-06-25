package com.lingpipe.book.applucene;

import com.aliasi.util.Files;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.LimitTokenCountAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.TermVector;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

import org.apache.lucene.util.Version;
import java.io.File;
import java.io.IOException;

public class LuceneIndexing {

    /*x LuceneIndexing.1 */
    public static void main(String[] args) 
        throws CorruptIndexException, LockObtainFailedException,
               IOException {

        File docDir = new File(args[0]);
        File indexDir = new File(args[1]);
        
        Directory fsDir = FSDirectory.open(indexDir);

        Analyzer stdAn 
            = new StandardAnalyzer(Version.LUCENE_36);
        Analyzer ltcAn 
            = new LimitTokenCountAnalyzer(stdAn,Integer.MAX_VALUE);

        IndexWriterConfig iwConf 
            = new IndexWriterConfig(Version.LUCENE_36,ltcAn);
        iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter indexWriter
            = new IndexWriter(fsDir,iwConf);
        /*x*/

        /*x LuceneIndexing.2 */        
        for (File f : docDir.listFiles()) {
            String fileName = f.getName();
            String text = Files.readFromFile(f,"ASCII");
            Document d = new Document();
            d.add(new Field("file",fileName,
                            Store.YES,Index.NOT_ANALYZED));
            d.add(new Field("text",text,
                            Store.YES,Index.ANALYZED));
            indexWriter.addDocument(d);
        }
        int numDocs = indexWriter.numDocs();

        indexWriter.forceMerge(1);
        indexWriter.commit();
        indexWriter.close();
        /*x*/
        System.out.println("Index Directory=" + indexDir.getCanonicalPath());
        System.out.println("Doc Directory=" + docDir.getCanonicalPath());
        System.out.println("num docs=" + numDocs);
    }

}