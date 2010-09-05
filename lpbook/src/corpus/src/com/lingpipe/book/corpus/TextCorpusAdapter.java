package com.lingpipe.book.corpus;

import com.aliasi.classify.Classified;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

public class TextCorpusAdapter
    extends Corpus<ObjectHandler<CharSequence>> {
    
    final Corpus<Classified<CharSequence>> mCp;

    public TextCorpusAdapter(Corpus<Classified<CharSequence>> cp) {
                             corpus) {
        mCorpus=corpus;
    }

    @Override
    public void visitTrain(ObjectHandler<CharSequence> handler) {
        mCp.visitTrain(new Forgetter(handler));
    }

    @Override
    public void visitTest(final ObjectHandler<CharSequence> 
                          handler) {
        
        mCp.visitTest(new ObjectHandler<Classified<CharSequence>>() {
                public void handle(Classified<CharSequence> c) {
                    handler.handle(c.getObject());
                }
            });
    }

            
    static class Forgetter 
        implements ObjectHandler<Classified<CharSequence>> {

        final ObjectHandler<CharSequence> mTxtHandler;

        public Forgetter(ObjectHandler<CharSequence> textHandler) {
            mTextHandler = textHandler;
        }

        public void handle(Classified<CharSequence> c) {
            mTextHandler.handle(c.getObject());
        }

    }

}