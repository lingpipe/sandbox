package com.lingpipe.book.corpus;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.util.Streams;

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import java.util.zip.GZIPInputStream;

public class TwentyNewsgroupsCorpus
    extends Corpus<ObjectHandler<Classified<CharSequence>>> {

    final List<Classified<CharSequence>> mTrainInstances
        = new ArrayList<Classified<CharSequence>>();
    final List<Classified<CharSequence>> mTestInstances
        = new ArrayList<Classified<CharSequence>>();
    final Set<String> mCategorySet = new TreeSet<String>();

    public TwentyNewsgroupsCorpus(File corpusFileTgz) 
        throws IOException {
        
        InputStream in = new FileInputStream(corpusFileTgz);
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
            String newsgroup = name.substring(m+1,n);
            mCategorySet.add(newsgroup);
            String fileNum = name.substring(n+1);
            byte[] bs = Streams.toByteArray(tarIn);
            CharSequence text = new String(bs,"ASCII");
            Classification c = new Classification(newsgroup);
            Classified<CharSequence> e
                = new Classified<CharSequence>(text,c);
            if ("20news-bydate-test".equals(trainTest))
                mTestInstances.add(e);
            else
                mTrainInstances.add(e);
        }
            
    }

    public int testSize() {
        return mTestInstances.size();
    }

    public int trainSize() {
        return mTrainInstances.size();
    }

    public Set<String> newsgroups() { 
        return Collections.unmodifiableSet(mCategorySet);
    }

    @Override
    public void visitTest(ObjectHandler<Classified<CharSequence>> handler) {
        for (Classified<CharSequence> c : mTestInstances)
            handler.handle(c);
    }

    @Override
    public void visitTrain(ObjectHandler<Classified<CharSequence>> handler) {
        for (Classified<CharSequence> c : mTrainInstances)
            handler.handle(c);
    }

    public static void main(String[] args) throws IOException {
        File tngFileTgz = new File(args[0]);
        TwentyNewsgroupsCorpus corpus
            = new TwentyNewsgroupsCorpus(tngFileTgz);
        System.out.println("# train=" + corpus.trainSize() + " #test=" + corpus.testSize());
        System.out.println("#cats=" + corpus.newsgroups().size());
        System.out.println("Cats=");
        for (String cat : corpus.newsgroups()) 
            System.out.println("  " + cat);
    }

}