package com.lingpipe.book.naivebayes;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.ConditionalClassifier;
import com.aliasi.classify.JointClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.TradNaiveBayesClassifier;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import java.io.File;
import java.io.IOException;

public class SerializeCompile {

    /*x SerializeCompile.1 */
    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {
    /*x*/

        // CUT AND PASTE FROM TradNbDemo

        String text = args[0];
        File file = new File(args[1]);

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

        @SuppressWarnings("unchecked")
        ConditionalClassifier<CharSequence> compiledClassifier
            = (ConditionalClassifier<CharSequence>)
            AbstractExternalizable.readObject(file);

        file.delete();
        /*x*/


        /*x SerializeCompile.3 */
        AbstractExternalizable.serializeTo(classifier,file);

        @SuppressWarnings("unchecked")
        TradNaiveBayesClassifier deserializedClassifier
            = (TradNaiveBayesClassifier)
            AbstractExternalizable.readObject(file);
        file.delete();
        /*x*/

        AbstractExternalizable.serializeTo(classifier,file);

        @SuppressWarnings("unchecked")
        TradNaiveBayesClassifier deserializedClassifierTrain
            = (TradNaiveBayesClassifier)
            AbstractExternalizable.readObject(file);

        file.delete();

        /*x SerializeCompile.4 */
        String s = "hardy har har";
        Classified<CharSequence> trainInstance
            = new Classified<CharSequence>(s,hisCl);
        deserializedClassifierTrain.handle(trainInstance);
        /*x*/


        print("Original",classifier,text);
        print("Compiled",compiledClassifier,text);
        print("Serialized",
              deserializedClassifier,text);
        print("Serialized, Additional Training",
              deserializedClassifierTrain,text);
    }

    static void print(String msg,
                      ConditionalClassifier<CharSequence> classifier,
                      String text) {
        System.out.println("\nResults for: " + msg);
        ConditionalClassification cc
            = classifier.classify(text);
        for (int rank = 0; rank < cc.size(); ++rank) {
            String cat = cc.category(rank);
            double condProb = cc.conditionalProbability(rank);
            System.out.printf("Rank=%2d  cat=%4s  p(c|txt)=%4.2f\n",
                              rank,cat,condProb);
        }
    }

}