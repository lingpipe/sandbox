package com.lingpipe.book.naivebayes;

import com.aliasi.classify.BaseClassifierEvaluator;
import com.aliasi.classify.Classified;
import com.aliasi.classify.BaseClassifier;
import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.TradNaiveBayesClassifier;

import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.util.Random;
import java.util.Set;

public class TwentyNewsXvalDemo {

    public static void main(String[] args) 
        throws IOException, ClassNotFoundException {

        /*x TwentyNewsXvalDemo.1 */
        File corpusFileTgz = new File(args[0]);
        double catPrior = Double.valueOf(args[1]);
        double tokPrior = Double.valueOf(args[2]);
        double lengthNorm = args[3].equalsIgnoreCase("NaN")
            ? Double.NaN ? Double.valueOf(args[3]);
        long randomSeed = Long.valueOf(args[4]);
        /*x*/

        /*x TwentyNewsXvalDemo.2 */
        TokenizerFactory tokFact
            = IndoEuropeanTokenizerFactory.INSTANCE;
        /*x*/

        System.out.println("PARAMETERS");
        System.out.println("  20-news-file=" 
                           + corpusFileTgz.getCanonicalPath());
        System.out.println("  cat-prior=" + catPrior);
        System.out.println("  tok-prior=" + tokPrior);
        System.out.println("  length-norm=" + lengthNorm);
        System.out.println("  random-seed=" + randomSeed);
        System.out.println("  tok-fact=" + tokFact);

        System.out.println("\nREADING AND PERMUTING CORPUS");

        /*x TwentyNewsXvalDemo.3 */
        int numFolds = 10;
        XValidatingObjectCorpus<Classified<CharSequence>> corpus
            = new TwentyNewsXvalCorpus(corpusFileTgz,numFolds);
        corpus.permuteCorpus(new Random(randomSeed));

        Set<String> catSet = Nb20NewsCorpus.getCatSet(corpus);
        String[] cats = catSet.toArray(Strings.EMPTY_STRING_ARRAY);
        /*x*/

        /*x TwentyNewsXvalDemo.4 */
        boolean store = false;
        BaseClassifierEvaluator<CharSequence> globalEvaluator
            = new BaseClassifierEvaluator<CharSequence>(null,cats,
                                                        store);
        /*x*/

        /*x TwentyNewsXvalDemo.5 */
        for (int fold = 0; fold < numFolds; ++fold) {
            corpus.setFold(fold);

            TradNaiveBayesClassifier classifier
                = new TradNaiveBayesClassifier(catSet,tokFact,catPrior,
                                               tokPrior,lengthNorm);
            corpus.visitTrain(classifier);
        /*x*/

        /*x TwentyNewsXvalDemo.6 */
            @SuppressWarnings("unchecked")
            BaseClassifier<CharSequence> cClas
                = (BaseClassifier<CharSequence>)
                AbstractExternalizable.compile(classifier);

            globalEvaluator.setClassifier(cClas);
            corpus.visitTest(globalEvaluator);
            
            BaseClassifierEvaluator<CharSequence> localEvaluator
                = new BaseClassifierEvaluator<CharSequence>(cClas,
                                                            cats,
                                                            store);
            corpus.visitTest(localEvaluator);
        /*x*/
            
            System.out.println("\nFOLD " + fold);
            printEval(localEvaluator);
        }

        System.out.println("\nGlobal Eval");
        printEval(globalEvaluator);
    }

    static void printEval(BaseClassifierEvaluator evaluator) {
        ConfusionMatrix cm = evaluator.confusionMatrix();
        System.out.printf("  totalCount=%6d   acc=%5.3f   conf95=%5.3f\n",
                          cm.totalCount(),
                          cm.totalAccuracy(), cm.confidence95());
        System.out.printf("  macro avg: prec=%5.3f  rec=%5.3f  F1=%5.3f\n",
                          cm.macroAvgPrecision(),
                          cm.macroAvgRecall(),
                          cm.macroAvgFMeasure());
    }

}