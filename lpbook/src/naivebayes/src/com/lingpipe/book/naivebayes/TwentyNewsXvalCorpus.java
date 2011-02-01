package com.lingpipe.book.naivebayes;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XValidatingObjectCorpus;

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

/*x TwentyNewsXvalCorpus.1 */
public class TwentyNewsXvalCorpus
    extends XValidatingObjectCorpus<Classified<CharSequence>> {

    public TwentyNewsXvalCorpus(File corpusFileTgz, 
                                int numFolds) 
        throws IOException {

        super(numFolds);
/*x*/
        InputStream in = new FileInputStream(corpusFileTgz);
        GZIPInputStream gzipIn = new GZIPInputStream(in);
/*x TwentyNewsXvalCorpus.2 */
        TarInputStream tarIn = new TarInputStream(gzipIn);
        while (true) {
            TarEntry entry = tarIn.getNextEntry();
/*x*/
            if (entry == null) break;
            if (entry.isDirectory()) continue;
            String name = entry.getName();
            int n = name.lastIndexOf('/');
            int m = name.lastIndexOf('/',n-1);
            String newsgroup = name.substring(m+1,n);
            byte[] bs = Streams.toByteArray(tarIn);
            CharSequence text = new String(bs,"ASCII");
            Classification c = new Classification(newsgroup);
/*x TwentyNewsXvalCorpus.3 */
            Classified<CharSequence> classified
                = new Classified<CharSequence>(text,c);
            handle(classified);
        }
        tarIn.close();
    }
/*x*/


}