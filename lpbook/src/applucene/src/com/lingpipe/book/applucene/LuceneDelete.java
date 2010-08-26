package com.lingpipe.book.applucene;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public class LuceneDelete {

    private LuceneDelete() { /* no instances */ }

    public static void main(String[] args) 
        throws CorruptIndexException, IOException {

        File indexDir = new File(args[0]);
        String fieldName = args[1];
        String token = args[2];

        /*x LuceneDelete.1 */
        Directory dir = FSDirectory.open(indexDir);
        boolean readOnly = false;
        IndexReader reader = IndexReader.open(dir,readOnly);

        int numDocsBefore = reader.numDocs();

        Term term = new Term(fieldName,token);
        reader.deleteDocuments(term);

        int numDocsAfter = reader.numDocs();
        int numDeletedDocs = reader.numDeletedDocs();

        reader.close();
        /*x*/

        System.out.println("index.dir=" + indexDir.getCanonicalPath());
        System.out.println("field.name=" + fieldName);
        System.out.println("token=" + token);
        System.out.println("Num docs before delete=" + numDocsBefore);
        System.out.println("Num docs after delete=" + numDocsAfter);
        System.out.println("Num deleted docs=" + numDeletedDocs);
    }

}