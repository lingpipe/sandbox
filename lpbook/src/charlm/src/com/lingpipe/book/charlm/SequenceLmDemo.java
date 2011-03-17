package com.lingpipe.book.charlm;

import com.aliasi.lm.NGramBoundaryLM;

public class SequenceLmDemo {

    public static void main(String[] args) {
        int ngram = Integer.valueOf(args[0]);
        String csvTrain = args[1];
        String textTest = args[2];

        /*x SequenceLmDemo.1 */
        NGramBoundaryLM lm = new NGramBoundaryLM(ngram);
        for (String text : csvTrain.split(",")) 
            lm.handle(text);
        double log2Prob = lm.log2Estimate(textTest);
        /*x*/

        System.out.println("ngram=" + ngram);
        for (String text : csvTrain.split(","))
            System.out.println("train=|" + text + "|");
        System.out.println("test=|" + textTest + "|");
        
        System.out.printf("log2 p(test|train)=%.3f\n",log2Prob);
    }

}