package com.lingpipe.book.applucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.TermVector;

public class DocDemo {
    
    public static void main(String[] args) {
        /*x DocDemo.1 */
        Document doc = new Document();
        doc.add(new Field("title", "Fast and Accurate Read Alignment",
                          Store.YES,Index.ANALYZED, TermVector.NO));
        doc.add(new Field("author", "Heng Li",
                          Store.YES,Index.ANALYZED));
        doc.add(new Field("author", "Richard Durbin",
                          Store.YES,Index.ANALYZED));
        doc.add(new Field("journal","Bioinformatics",
                          Store.YES,Index.ANALYZED));
        doc.add(new Field("mesh","algorithms",
                          Store.YES,Index.ANALYZED));
        doc.add(new Field("mesh","genomics/methods",
                          Store.YES,Index.ANALYZED));
        doc.add(new Field("mesh","sequence alignment/methods",
                          Store.YES,Index.ANALYZED));
        doc.add(new Field("pmid","20080505",
                          Store.YES,Index.NOT_ANALYZED));
        /*x*/

        /*x DocDemo.2 */
        for (Fieldable f : doc.getFields()) {
            String name = f.name();
            String value = f.stringValue();
            boolean isIndexed = f.isIndexed();
            boolean isStored = f.isStored();
            boolean isTokenized = f.isTokenized();
            boolean isTermVectorStored = f.isTermVectorStored();
        /*x*/
            System.out.println("name=" + name 
                               + " value=" + value
                               + "\n     indexed=" + isIndexed
                               + " store=" + isStored
                               + " tok=" + isTokenized
                               + " termVecs=" + isTermVectorStored);
        }
    }

}