package com.lingpipe.book.naivebayes;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.TradNaiveBayesClassifier;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.CollectionUtils;

import java.util.Arrays;
import java.util.Set;

public class AdditiveSmooth {

    public static void main(String[] args) {

        /*x AdditiveSmooth.1 */
        Set<String> cats = CollectionUtils.asSet("hot","cold");
        TokenizerFactory tf = new RegExTokenizerFactory("\\S+"); 
        double catPrior = 1.0;
        double tokenPrior = 0.5;
        double lengthNorm = Double.NaN;
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(cats,tf,catPrior,
                                           tokenPrior,lengthNorm);
        /*x*/

        /*x AdditiveSmooth.2 */
        Classification hot = new Classification("hot");
        for (String s : Arrays.asList("super steamy out",
                                      "boiling",
                                      "steamy today"))
            classifier.handle(new Classified<CharSequence>(s,hot));

        Classification cold = new Classification("cold");
        for (String s : Arrays.asList("freezing out",
                                      "icy")) 
            classifier.handle(new Classified<CharSequence>(s,cold));
        /*x*/

        /*x AdditiveSmooth.3 */
        for (String cat : classifier.categorySet()) {
            double probCat = classifier.probCat(cat);
        /*x*/
            System.out.printf("    p(%s)=%5.3f",cat,probCat);
        }
        System.out.println("\n");
        
        /*x AdditiveSmooth.4 */
        for (String tok : classifier.knownTokenSet()) {
            for (String cat : classifier.categorySet()) {
                double pTok = classifier.probToken(tok,cat);
        /*x*/        
                System.out.printf("    p(%8s|%4s)=%5.3f",tok,cat,pTok);
            }
            System.out.println();
        }

    }


}