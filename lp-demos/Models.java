import com.aliasi.util.Files;

import com.aliasi.classify.Classification;
import com.aliasi.classify.ClassifierEvaluator;
import com.aliasi.classify.Classifier;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramProcessLM;

import com.aliasi.util.AbstractExternalizable;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.HashSet;
import java.util.Iterator;

public class Models {

    static int NGRAM = 8;

    static String[] SUBJECTIVE_CATS = new String[] { "plot", "quote" };
    static String[] POLARITY_CATS = new String[] { "pos", "neg" };

    public static void main(String[] args) throws Exception {
        File subjectivityDir = new File(args[0]);
        File polarityDir = new File(args[1]);
        File subjectivityModelFile = new File(args[2]);
        File polarityModelFile = new File(args[3]);

        // subjectivity
        System.out.println("Training Subjectivity Model from dir=" + subjectivityDir);
        DynamicLMClassifier<NGramProcessLM> classifier 
            = DynamicLMClassifier.createNGramProcess(SUBJECTIVE_CATS,NGRAM);
        for (String cat : SUBJECTIVE_CATS) {
            System.out.println("     Training category=" + cat);
            File file = new File(subjectivityDir,cat + ".tok.gt9.5000");
            String data = Files.readFromFile(file,"ISO-8859-1");
            String[] sentences = data.split("\n");
            System.out.println("          #sentences=" + sentences.length);
            for (String sentence : sentences)
                classifier.train(cat,sentence);
        }
        System.out.println("     Compiling Subjectivity Model to File=" + subjectivityModelFile);
        AbstractExternalizable.compileTo(classifier,subjectivityModelFile);


        // polarity
        System.out.println("\nTraining Polarity Model from dir=" + polarityDir);
        DynamicLMClassifier<NGramProcessLM> classifier2
            = DynamicLMClassifier.createNGramProcess(POLARITY_CATS,NGRAM);
        for (String cat : POLARITY_CATS) {
            System.out.println("     Training category=" + cat);
            File trainDir = new File(polarityDir,cat);
            File[] trainingFiles = trainDir.listFiles();
            System.out.println("          # training files=" + trainingFiles.length);
            for (File trainingFile : trainingFiles) {
                String text = Files.readFromFile(trainingFile,"ISO-8859-1");
                classifier2.train(cat,text);
            }
        }
        System.out.println("     Compiling Polarity Model to File=" + polarityModelFile);
        AbstractExternalizable.compileTo(classifier2,polarityModelFile);
        
        System.out.println("\nDONE.");
    }

}