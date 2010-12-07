package com.lingpipe.book.naivebayes;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.TradNaiveBayesClassifier;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CatSkew {

    // major cut-and-paste hack from TradNbDemo.java

    public static void main(String[] args) {

        String text = args[0];

        TokenizerFactory tf 
            = new RegExTokenizerFactory("\\P{Z}+");

        Set<String> cats
            = CollectionUtils.asSet("his","hers");

        TradNaiveBayesClassifier classifier 
            = new TradNaiveBayesClassifier(cats,tf);


        Classification hersCl = new Classification("hers");

        /*x TradNbSkewedTrain.1 */
        List<String> herTexts
            = Arrays.asList("haw hee", "hee hee hee haw haw");
        /*x*/

        for (String t : herTexts)
            classifier.handle(new Classified<CharSequence>(t,hersCl));


        Classification hisCl = new Classification("his");
        /*x TradNbSkewedTrain.2 */
        List<String> hisTexts 
            = Arrays.asList("haw", "haw hee haw", "haw haw");
        /*x*/
        for (String t : hisTexts)
            classifier.handle(new Classified<CharSequence>(t,hisCl));


        System.out.printf("Input=|%s|\n",text);
        JointClassification jc = classifier.classify(text);
        for (int rank = 0; rank < jc.size(); ++rank) {
            String cat = jc.category(rank);
            double condProb = jc.conditionalProbability(rank);
            double jointProb = jc.jointLog2Probability(rank);
            System.out.printf("Rank=%2d  cat=%4s  p(c|txt)=%4.2f  log2 p(c,txt)=%6.2f\n",
                              rank,cat,condProb,jointProb);
        }

            
    }

}