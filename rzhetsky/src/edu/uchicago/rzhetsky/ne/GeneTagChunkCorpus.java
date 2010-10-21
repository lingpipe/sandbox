package edu.uchicago.rzhetsky.ne;

import com.aliasi.chunk.Chunking;

import com.aliasi.corpus.XValidatingObjectCorpus;

import java.io.File;
import java.io.IOException;

public class GeneTagChunkCorpus extends XValidatingObjectCorpus<Chunking> {
    
    static final long serialVersionUID = 884099984173841141L;

    public GeneTagChunkCorpus(File goldFile,
                              File corpusFile,
                              int numFolds) 
        throws IOException {

        super(numFolds);
        GeneTagChunkParser parser = new GeneTagChunkParser(goldFile);
        parser.setHandler(this);

        parser.parse(corpusFile);
    }


    

}