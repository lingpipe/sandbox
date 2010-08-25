package com.lingpipe.book.applucene;

import com.aliasi.util.Files;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.TermVector;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;

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
        Analyzer an = new StandardAnalyzer(Version.LUCENE_30);
        IndexWriter indexWriter
            = new IndexWriter(fsDir,an,MaxFieldLength.UNLIMITED);
    /*x*/

    /*x LuceneIndexing.2 */        
        long numChars = 0L;
        for (File f : docDir.listFiles()) {
            String fileName = f.getName();
            String text = Files.readFromFile(f,"ASCII");
            numChars += text.length();
            Document d = new Document();
            d.add(new Field("file",fileName,
                            Store.YES,Index.NOT_ANALYZED));
            d.add(new Field("text",text,
                            Store.YES,Index.ANALYZED));
            indexWriter.addDocument(d);
        }

        indexWriter.optimize();
        indexWriter.close();
        int numDocs = indexWriter.numDocs();
    /*x*/
        System.out.println("Index Directory=" + indexDir.getCanonicalPath());
        System.out.println("Doc Directory=" + docDir.getCanonicalPath());
        System.out.println("num docs=" + numDocs);
        System.out.println("num chars=" + numChars);
    }

}