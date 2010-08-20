package com.lingpipe.book.lda;

import com.aliasi.cluster.LatentDirichletAllocation.GibbsSample;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.stats.OnlineNormalEstimator;

/*x CorpusLog2ProbAvgHandler.1 */
public class CorpusLog2ProbAvgHandler
    implements ObjectHandler<GibbsSample> {

    OnlineNormalEstimator mAvg = new OnlineNormalEstimator();

    public void handle(GibbsSample sample) {
        mAvg.handle(sample.corpusLog2Probability());
    }

    public OnlineNormalEstimator avg() {
        return mAvg;
    }
}
/*x*/