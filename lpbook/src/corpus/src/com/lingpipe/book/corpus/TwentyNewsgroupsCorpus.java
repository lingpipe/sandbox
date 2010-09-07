package com.lingpipe.book.corpus;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ListCorpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.util.Streams;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.Set;
import java.util.TreeSet;

import java.util.zip.GZIPInputStream;

public class TwentyNewsgroupsCorpus
    extends Corpus<ObjectHandler<Classified<CharSequence>>> {

    private final File mCorpusFileTgz;

    public TwentyNewsgroupsCorpus(File corpusFileTgz) {
        mCorpusFileTgz = corpusFileTgz;
    }

    @Override
    public void visitTest(ObjectHandler<Classified<CharSequence>> handler) 
        throws IOException {

        visitFile("20news-bydate-test",handler);
    }

    @Override
    public void visitTrain(ObjectHandler<Classified<CharSequence>> handler)
        throws IOException {

        visitFile("20news-bydate-train",handler);
    }
    
    private void visitFile(String trainOrTest,
                           ObjectHandler<Classified<CharSequence>> handler) 
        throws IOException {

        InputStream in = new FileInputStream(mCorpusFileTgz);
        GZIPInputStream gzipIn = new GZIPInputStream(in);
        TarInputStream tarIn = new TarInputStream(gzipIn);
        while (true) {
            TarEntry entry = tarIn.getNextEntry();
            if (entry == null) break;
            if (entry.isDirectory()) continue;
            String name = entry.getName();
            int n = name.lastIndexOf('/');
            int m = name.lastIndexOf('/',n-1);
            String trainTest = name.substring(0,m);
            if (!trainOrTest.equals(trainTest)) continue;
            String newsgroup = name.substring(m+1,n);
            byte[] bs = Streams.toByteArray(tarIn);
            CharSequence text = new String(bs,"ASCII");
            Classification c = new Classification(newsgroup);
            Classified<CharSequence> classified
                = new Classified<CharSequence>(text,c);
            handler.handle(classified);
        }
        tarIn.close();
    }


    public static void main(String[] args) throws IOException {
        File tngFileTgz = new File(args[0]);
        Corpus<ObjectHandler<Classified<CharSequence>>> corpus
            = new TwentyNewsgroupsCorpus(tngFileTgz);
        
        final Set<String> catSet = new TreeSet<String>();
        ObjectHandler<Classified<CharSequence>> handler
            = new ObjectHandler<Classified<CharSequence>>() {
            public void handle(Classified<CharSequence> c) {
                catSet.add(c.getClassification().bestCategory());
            }
        };

        corpus.visitTrain(handler);
        corpus.visitTest(handler);
        System.out.println("Cats=" + catSet);
    }

}