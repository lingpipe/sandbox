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

public class TradNbDemo {


    public static void main(String[] args) {

        /*x TradNbDemo.1 */
        String text = args[0];
        /*x*/

        /*x TradNbDemo.2 */
        TokenizerFactory tf 
            = new RegExTokenizerFactory("\\P{Z}+");

        Set<String> cats
            = CollectionUtils.asSet("his","hers");

        TradNaiveBayesClassifier classifier 
            = new TradNaiveBayesClassifier(cats,tf);
        /*x*/

        /*x TradNbDemo.3 */
        Classification hersCl = new Classification("hers");

        List<String> herTexts
            = Arrays.asList("haw hee", "hee hee hee haw", "haw");

        for (String t : herTexts)
            classifier.handle(new Classified<CharSequence>(t,hersCl));
        /*x*/


        Classification hisCl = new Classification("his");
        List<String> hisTexts 
            = Arrays.asList("haw", "haw hee haw", "haw haw");
        for (String t : hisTexts)
            classifier.handle(new Classified<CharSequence>(t,hisCl));


        System.out.printf("Input=|%s|\n",text);
        /*x TradNbDemo.4 */
        JointClassification jc = classifier.classify(text);
        for (int rank = 0; rank < jc.size(); ++rank) {
            String cat = jc.category(rank);
            double condProb = jc.conditionalProbability(rank);
            double jointProb = jc.jointLog2Probability(rank);
        /*x*/
            System.out.printf("Rank=%2d  cat=%4s  p(c|txt)=%4.2f  log2 p(c,txt)=%6.2f\n",
                              rank,cat,condProb,jointProb);
        }

            
    }

}