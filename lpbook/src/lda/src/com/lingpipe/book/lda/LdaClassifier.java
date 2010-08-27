package com.lingpipe.book.lda;

import com.aliasi.cluster.LatentDirichletAllocation;

import com.aliasi.symbol.SymbolTable;

import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.ObjectToDoubleMap;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Random;

public class LdaClassifier {

    private LdaClassifier() { /* no instances */ }


    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

        File modelFile = new File(args[0]);
        File symbolTableFile = new File(args[1]);
        String text = args[2];
        long randomSeed = Long.parseLong(args[3]);

        /*x LdaClassifier.1 */
        @SuppressWarnings("unchecked")
        LatentDirichletAllocation lda
            = (LatentDirichletAllocation)
            AbstractExternalizable.readObject(modelFile);
        
        @SuppressWarnings("unchecked")
        SymbolTable symbolTable
            = (SymbolTable)
            AbstractExternalizable.readObject(symbolTableFile);

        TokenizerFactory tokFact = LdaWorm.wormbaseTokenizerFactory();
        /*x*/

        /*x LdaClassifier.2 */
        int[] tokenIds 
            = LatentDirichletAllocation
            .tokenizeDocument(text,tokFact,symbolTable);

        int numSamples = 100;
        int burnin = numSamples / 2;
        int sampleLag = 1;
        Random random = new Random(randomSeed);
        
        double[] topicDist 
            = lda.bayesTopicEstimate(tokenIds,numSamples,burnin,
                                     sampleLag,random);
        /*x*/

        ObjectToDoubleMap<Integer> topicSorter
            = new ObjectToDoubleMap<Integer>();
        for (int k = 0; k < topicDist.length; ++k) {
            topicSorter.set(k,topicDist[k]);
        }
        System.out.printf("%6s %5s\n", "TOPIC", "Pr");
        System.out.printf("%6s %5s\n", "------", "-----");
        List<Integer> orderedTopics = topicSorter.keysOrderedByValueList();
        for (int rank = 0; rank < orderedTopics.size() && rank < 10; ++rank) {
            int k = orderedTopics.get(rank);
            System.out.printf("%6d %5.3f\n", k, topicDist[k]);
        }
        System.out.println();

        
    }
}