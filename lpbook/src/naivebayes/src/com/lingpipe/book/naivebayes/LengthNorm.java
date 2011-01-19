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

public class LengthNorm {


    public static void main(String[] args) {

        // CUT AND PASTE FROM TradNbDemo

        String text = args[0];
        double lengthNorm = args[0].equalsIgnoreCase("NaN")
            ? Double.NaN
            : Double.valueOf(args[1]);

        TokenizerFactory tf 
            = new RegExTokenizerFactory("\\P{Z}+");

        Set<String> cats
            = CollectionUtils.asSet("his","hers");

        TradNaiveBayesClassifier classifier 
            = new TradNaiveBayesClassifier(cats,tf);

        Classification hersCl = new Classification("hers");

        List<String> herTexts
            = Arrays.asList("haw hee", "hee hee hee haw", "haw");

        for (String t : herTexts)
            classifier.handle(new Classified<CharSequence>(t,hersCl));

        Classification hisCl = new Classification("his");
        List<String> hisTexts 
            = Arrays.asList("haw", "haw hee haw", "haw haw");
        for (String t : hisTexts)
            classifier.handle(new Classified<CharSequence>(t,hisCl));



        // ONLY CHANGE FOR THIS FILE
        System.out.printf("Input=|%s|   Length Norm=%7.2f\n",
                          text,lengthNorm);

        /*x LengthNorm.1 */
        classifier.setLengthNorm(lengthNorm);
        JointClassification jc = classifier.classify(text);
        /*x*/

        // MORE CUT AND PASTE

        for (int rank = 0; rank < jc.size(); ++rank) {
            String cat = jc.category(rank);
            double condProb = jc.conditionalProbability(rank);
            double jointProb = jc.jointLog2Probability(rank);
            System.out.printf("Rank=%2d  cat=%4s  p(c|txt)=%4.2f  log2 p(c,txt)=%6.2f\n",
                              rank,cat,condProb,jointProb);
        }

    }

}