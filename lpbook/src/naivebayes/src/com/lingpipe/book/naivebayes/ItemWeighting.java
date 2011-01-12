package com.lingpipe.book.naivebayes;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.TradNaiveBayesClassifier;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.CollectionUtils;

import java.util.Arrays;
import java.util.Set;

public class ItemWeighting {

    public static void main(String[] args) {

        // first block cut and paste from AdditiveSmooth for consistency of exposition
        Set<String> cats = CollectionUtils.asSet("hot","cold");
        TokenizerFactory tf = new RegExTokenizerFactory("\\S+"); 
        double catPrior = 1.0;
        double tokenPrior = 0.5;
        double lengthNorm = Double.NaN;
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(cats,tf,catPrior,
                                           tokenPrior,lengthNorm);

        /*x ItemWeighting.1 */
        Classification hot = new Classification("hot");
        Classification cold = new Classification("cold");
        classifier.train("dress warmly",cold,0.8);
        classifier.train("mild out",cold,0.5);
        classifier.train("boiling out",hot,0.99);
        /*x*/
        
        // also cut and paste from AdditiveSmooth for consistency
        for (String cat : classifier.categorySet()) {
            double probCat = classifier.probCat(cat);
            System.out.printf("    p(%s)=%5.3f",cat,probCat);
        }
        System.out.println("\n");
        
        for (String tok : classifier.knownTokenSet()) {
            for (String cat : classifier.categorySet()) {
                double pTok = classifier.probToken(tok,cat);
                System.out.printf("    p(%8s|%4s)=%5.3f",tok,cat,pTok);
            }
            System.out.println();
        }


        
        
    }

}