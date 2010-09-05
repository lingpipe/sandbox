package com.lingpipe.book.corpus;

import com.aliasi.classify.Classified;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import java.io.IOException;

public class TextCorpusAdapter
    extends Corpus<ObjectHandler<CharSequence>> {
    
    final Corpus<ObjectHandler<Classified<CharSequence>>> mCorpus;

    public TextCorpusAdapter(Corpus<ObjectHandler<Classified<CharSequence>>> corpus) {
        mCorpus=corpus;
    }

    @Override
    public void visitTrain(ObjectHandler<CharSequence> handler) 
        throws IOException {

        mCorpus.visitTrain(new Forgetter(handler));
    }

    @Override
    public void visitTest(final ObjectHandler<CharSequence> 
                          handler) 
        throws IOException {
        
        mCorpus.visitTest(new ObjectHandler<Classified<CharSequence>>() {
                public void handle(Classified<CharSequence> c) {
                    handler.handle(c.getObject());
                }
            });
    }

            
    static class Forgetter 
        implements ObjectHandler<Classified<CharSequence>> {

        final ObjectHandler<CharSequence> mTextHandler;

        public Forgetter(ObjectHandler<CharSequence> textHandler) {
            mTextHandler = textHandler;
        }

        public void handle(Classified<CharSequence> c) {
            mTextHandler.handle(c.getObject());
        }

    }

}