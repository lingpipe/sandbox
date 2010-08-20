package com.lingpipe.book.lda;

import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.cluster.LatentDirichletAllocation.GibbsSample;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.util.List;

/*x ReportingLdaSampleHandler.1 */
public class ReportingLdaSampleHandler 
    implements ObjectHandler<GibbsSample> {

    private final SymbolTable mSymbolTable;
    private final long mStartTime;
    private final int mReportPeriod;

    public ReportingLdaSampleHandler(SymbolTable symbolTable,
                                     int reportPeriod) {
        mSymbolTable = symbolTable;
        mStartTime = System.currentTimeMillis();
        mReportPeriod = reportPeriod;
    }
/*x*/

    /*x ReportingLdaSampleHandler.2 */
    public void handle(GibbsSample sample) {
        int epoch = sample.epoch();
        if ((epoch % mReportPeriod) != 0) return;
        long t = System.currentTimeMillis() - mStartTime;
        double xEntropyRate 
            = -sample.corpusLog2Probability() / sample.numTokens();
    /*x*/
        System.out.printf("n=%6d t=%8s x-entropy-rate=%8.4f\n",
                          epoch, Strings.msToString(t),
                          xEntropyRate);
    }

    public void reportParameters(GibbsSample sample) {
        System.out.println("sample=" + sample.epoch());
        System.out.println("numDocuments=" + sample.numDocuments());
        System.out.println("numTokens=" + sample.numTokens());
        System.out.println("numWords=" + sample.numWords());
        System.out.println("numTopics=" + sample.numTopics());
    }

    /*x ReportingLdaSampleHandler.3 */
    public void reportTopics(GibbsSample sample, int maxWords) {
        for (int topic = 0; topic < sample.numTopics(); ++topic) {
            int topicCount = sample.topicCount(topic);
            ObjectToCounterMap<Integer> counter 
                = new ObjectToCounterMap<Integer>();
            for (int word = 0; word < sample.numWords(); ++word)
                counter.set(word, 
                            sample.topicWordCount(topic,word));
            List<Integer> topWords 
                = counter.keysOrderedByCountList();
    /*x*/

            System.out.println("\nTOPIC " + topic  + "  (total count=" + topicCount + ")");
            System.out.println("SYMBOL             WORD    COUNT   PROB          Z");
            System.out.println("--------------------------------------------------");

   /*x ReportingLdaSampleHandler.4 */
            for (int rank = 0; 
                 rank < maxWords && rank < topWords.size(); 
                 ++rank) {

                int wordId = topWords.get(rank);
                String word = mSymbolTable.idToSymbol(wordId);
                int wordCount = sample.wordCount(wordId);
                int topicWordCount 
                    = sample.topicWordCount(topic,wordId);
                double topicWordProb 
                    = sample.topicWordProb(topic,wordId);
                double z = binomialZ(topicWordCount,
                                     topicCount,
                                     wordCount,
                                     sample.numTokens());
    /*x*/
                System.out.printf("%6d  %15s  %7d   %4.3f  %8.1f\n",
                                  wordId,
                                  word,
                                  topicWordCount,
                                  topicWordProb,
                                  z);
            }
        }
    }

    /*x ReportingLdaSampleHandler.6 */
    public void reportDocs(GibbsSample sample, int maxTopics, 
                           boolean reportTokens) {
        for (int doc = 0; doc < sample.numDocuments(); ++doc) {
            ObjectToCounterMap<Integer> counter 
                = new ObjectToCounterMap<Integer>();
            for (int topic = 0; 
                 topic < sample.numTopics(); 
                 ++topic)
                counter.set(topic,
                            sample.documentTopicCount(doc,topic));
            List<Integer> topTopics 
                = counter.keysOrderedByCountList();
    /*x*/          


            System.out.println("\nDOC " + doc);
            System.out.println("TOPIC    COUNT    PROB");
            System.out.println("----------------------");
    /*x ReportingLdaSampleHandler.7 */
            for (int rank = 0; 
                 rank < topTopics.size() && rank < maxTopics; 
                 ++rank) {

                int topic = topTopics.get(rank);
                int docTopicCount 
                    = sample.documentTopicCount(doc,topic);
                double docTopicProb 
                    = sample.documentTopicProb(doc,topic);
    /*x*/

                System.out.printf("%5d  %7d   %4.3f\n",
                                  topic,
                                  docTopicCount,
                                  docTopicProb);
            }

            System.out.println();

    /*x ReportingLdaSampleHandler.8 */
            if (!reportTokens) continue;
            int numDocTokens = sample.documentLength(doc);
            for (int tok = 0; tok < numDocTokens; ++tok) {
                int symbol = sample.word(doc,tok);
                short topic = sample.topicSample(doc,tok);
                String word = mSymbolTable.idToSymbol(symbol);
    /*x*/
                System.out.print(word + "(" + topic + ") ");
            }
            System.out.println();
        }
    }


    /*x ReportingLdaSampleHandler.5 */
    static double binomialZ(double wordCountInDoc, 
                            double wordsInDoc,
                            double wordCountinCorpus, 
                            double wordsInCorpus) {
        double pCorpus = wordCountinCorpus / wordsInCorpus;
        double var = wordsInCorpus * pCorpus * (1 - pCorpus);
        double dev = Math.sqrt(var);
        double expected = wordsInDoc * pCorpus;
        return (wordCountInDoc - expected) / dev;
    }
    /*x*/


}