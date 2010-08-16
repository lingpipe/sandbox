package com.lingpipe.book.lda;

import com.aliasi.cluster.LatentDirichletAllocation;
import com.aliasi.cluster.LatentDirichletAllocation.GibbsSample;

import com.aliasi.symbol.MapSymbolTable;
import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;

import static com.aliasi.cluster.LatentDirichletAllocation.tokenizeDocuments;
import static com.aliasi.cluster.LatentDirichletAllocation.gibbsSampler;

import java.util.Random;

// example from: Steyvers and Griffiths. 2007. Probabilistic topic models.
// In Landauer, McNamara, Dennis and Kintsch (eds.) Handbook of Latent Semantic Analysis.
// Laurence Erlbaum.  
// see figure 7 for the data

public class SyntheticLdaExample {

    /*x SyntheticLdaExample.1a */
    static final CharSequence[] TEXTS = new String[] {
        "bank loan money loan loan money bank loan bank loan loan money bank money money money",
        "loan money money loan bank bank money bank money loan money money bank loan bank money",
    /*x*/
        "loan bank money money bank bank loan bank money bank bank loan bank money money loan",
        "loan bank money bank bank money bank money bank money bank loan money loan bank money",
        "bank money bank bank loan bank loan bank loan loan loan bank loan money loan bank",
        "bank bank loan money bank bank bank money loan loan money bank loan bank bank bank",
        "money bank stream stream bank money bank loan bank loan bank money loan river money bank",
        "money bank money loan stream bank loan bank bank stream money bank bank stream river money",
        "stream bank bank loan bank stream loan bank river loan money stream bank river bank loan",
        "money bank bank bank loan stream river river bank bank bank money stream stream money bank",
        "stream stream bank stream bank stream bank bank stream money river river river bank bank stream",
        "bank stream bank loan river stream bank bank river stream bank river river river river bank",
        "bank bank stream bank stream bank stream stream stream stream bank stream river bank river stream",
    /*x SyntheticLdaExample.1b */
        "river stream stream stream river stream stream bank bank bank bank river river stream bank stream",
        "stream river river bank stream stream stream stream bank river river stream bank river stream bank"
    };
    /*x*/

    public static void main(String[] args) {
        TokenizerFactory tokFact = new RegExTokenizerFactory("\\p{L}+");
        SymbolTable symTab = new MapSymbolTable();
        int minCount = 1;
        int[][] docWords = tokenizeDocuments(TEXTS,tokFact,symTab,minCount);

        for (int n = 0; n < docWords.length; ++n) {
            System.out.printf("docWords[%3d] = { ",n);
            for (int i = 0; i < docWords.length; ++i)
                System.out.printf("%s, ",docWords[n][i]);
            System.out.println(" }");
        }
        System.out.println();

        short numTopics = 2;
        double docTopicPrior = 0.1;
        double topicWordPrior = 0.01;
        
        int burninEpochs = 0;
        int sampleLag = 1;
        int numSamples = 256;

        Random random = new Random(43);

        int reportPeriod = 2;
        ReportingLdaSampleHandler handler 
            = new ReportingLdaSampleHandler(symTab,reportPeriod);

        GibbsSample sample
            = gibbsSampler(docWords, 
                           numTopics, docTopicPrior, topicWordPrior,
                           burninEpochs, sampleLag, numSamples,
                           random, handler);
        
        int wordsPerTopic = 5;
        int topicsPerDoc = 2;
        boolean reportTokens = true;
        handler.fullReport(sample,wordsPerTopic,topicsPerDoc,reportTokens);
    }

}