package com.lingpipe.book.naivebayes;

import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassifier;
import com.aliasi.classify.ConditionalClassifierEvaluator;
import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.PrecisionRecallEvaluation;
import com.aliasi.classify.TradNaiveBayesClassifier;

import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Nb20NewsCorpus {
    
    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

        File corpusTgzFile = new File(args[0]);
        
        System.out.println("READING CORPUS");

        /*x Nb20NewsCorpus.1 */
        Corpus<ObjectHandler<Classified<CharSequence>>> corpus
            = new TwentyNewsgroupsCorpus(corpusTgzFile);
        
        Set<String> catSet = getCatSet(corpus);
        String[] cats = catSet.toArray(Strings.EMPTY_STRING_ARRAY);
        Arrays.sort(cats);
        /*x*/


        
        System.out.println("TRAINING");

        /*x Nb20NewsCorpus.2 */
        TokenizerFactory tokFact
            = IndoEuropeanTokenizerFactory.INSTANCE;
        
        double catPrior = 1.0;
        double tokPrior = 0.1;
        double lengthNorm = 10.0;
        
        TradNaiveBayesClassifier classifier
            = new TradNaiveBayesClassifier(catSet,tokFact,catPrior,
                                          tokPrior,lengthNorm);

        corpus.visitTrain(classifier);
        /*x*/

        System.out.println("COMPILING");

        /*x Nb20NewsCorpus.3 */
        @SuppressWarnings("unchecked")
        ConditionalClassifier<CharSequence> cc
            = (ConditionalClassifier<CharSequence>)
            AbstractExternalizable.compile(classifier);
        /*x*/
        
        System.out.println("EVALUATING");

        /*x Nb20NewsCorpus.4 */
        boolean storeInputs = false;
        ConditionalClassifierEvaluator<CharSequence> evaluator
            = new ConditionalClassifierEvaluator<CharSequence>(cc,
                                                 cats, storeInputs);
        corpus.visitTest(evaluator);
        /*x*/

        /*x Nb20NewsCorpus.5 */
        ConfusionMatrix cm = evaluator.confusionMatrix();
        int totalCount = cm.totalCount();
        int totalCorrect = cm.totalCorrect();
        double accuracy = cm.totalAccuracy();
        double macroAvgPrec = cm.macroAvgPrecision();
        double macroAvgRec = cm.macroAvgRecall();
        double macroAvgF = cm.macroAvgFMeasure();
        double kappa = cm.kappa();
        /*x*/
        System.out.println("\nAll Versus All");
        System.out.println("  correct/total = " + totalCorrect + " / "  + totalCount);
        System.out.printf("  accuracy=%5.3f\n",accuracy);
        System.out.printf("  Macro Avg prec=%5.3f  rec=%5.3f   F=%5.3f\n",
                          macroAvgPrec, macroAvgRec, macroAvgF);
        System.out.printf("  Kappa=%5.3f\n",kappa);

        /*x Nb20NewsCorpus.6 */
        int[][] matrix = cm.matrix();
        for (int k = 0; k < cats.length; ++k) {
            PrecisionRecallEvaluation pr = cm.oneVsAll(k);

            long tp = pr.truePositive();
            long tn = pr.trueNegative();
            long fp = pr.falsePositive();
            long fn = pr.falseNegative();

            double acc = pr.accuracy();

            double prec = pr.precision();
            double recall = pr.recall();
            double specificity = pr.rejectionRecall();
            double f = pr.fMeasure();
        /*x*/
            
            System.out.println("\nCategory[" + k + "]=" + cats[k] + " versus All");
            System.out.println("  TP=" + tp + " TN=" + tn + " FP=" + fp + " FN=" + fn);
            System.out.printf("  Accuracy=%5.3f\n", acc);
            System.out.printf("  Prec=%5.3f  Rec(Sens)=%5.3f  Spec=%5.3f  F=%5.3f\n",
                              prec,recall,specificity,f);

        /*x Nb20NewsCorpus.7 */
            for (int k2 = 0; k2 < cats.length; ++k2)
                if (matrix[k][k2] > 0)
                    System.out.println("    * => " + cats[k2] 
                                       + " : " + matrix[k][k2]);
        /*x*/
            for (int k2 = 0; k2 < cats.length; ++k2)
                if (k != k2 && matrix[k2][k] > 0)
                    System.out.println("    " + cats[k2] + " => " + "*" + " : " + matrix[k2][k]);



        }

    }

    /*x Nb20NewsCorpus.0 */
    static <T> Set<String> 
        getCatSet(Corpus<ObjectHandler<Classified<T>>> corpus) 
        throws IOException {

        final Set<String> cSet = new HashSet<String>();
        corpus.visitCorpus(new ObjectHandler<Classified<T>>() {
                public void handle(Classified<T> c) {
                    cSet.add(c.getClassification().bestCategory());
                }
            });
        return catSet;
    }
    /*x*/

}

