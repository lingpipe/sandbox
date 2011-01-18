package com.lingpipe.book.naivebayes;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.TradNaiveBayesClassifier;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class SerializeCompile {


    /*x SerializeCompile.1 */
    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {
    /*x*/

        // CUT AND PASTE FROM TradNbDemo

        String text = args[0];
        File file = new File(args[1]);

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

        System.out.println("text=" + text);

        /*x SerializeCompile.2 */
        AbstractExternalizable.compileTo(classifier,file);

        JointClassifier<CharSequence> compiledClassifier
            = (JointClassifier<CharSequence>)
            AbstractExternalizable.readObject(file);

        file.delete();
        /*x*/


        /*x SerializeCompile.3 */
        AbstractExternalizable.serializeTo(classifier,file);

        JointClassifier<CharSequence> deserializedClassifier
            = (TradNaiveBayesClassifier)
            AbstractExternalizable.readObject(file);

        file.delete();

        String s = "hardy har har";
        deserializedClassifier.handle(new Classified<CharSequence>(t,s));
        /*x*/


        printResults("Original",classifier,text);
        printResults("Compiled",compiledClassifier,text);
        printResults("Serialized, Additionally Trained",
                     deserializedClassifier,text);
    }

    static void print(String msg,
                      JointClassifier<CharSequence> classifier,
                      String text) {
        System.out.println("Results for: " + msg);
        JointClassification jc
            = classifier.classify(text);
        for (int rank = 0; rank < jc.size(); ++rank) {
            String cat = jc.category(rank);
            double condProb = jc.conditionalProbability(rank);
            double jointProb = jc.jointLog2Probability(rank);
            System.out.printf("Rank=%2d  cat=%4s  p(c|txt)=%4.2f  log2 p(c,txt)=%6.2f\n",
                              rank,cat,condProb,jointProb);
        }
    }

}