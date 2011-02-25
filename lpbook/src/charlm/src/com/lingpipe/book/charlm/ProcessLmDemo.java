package com.lingpipe.book.charlm;

import com.aliasi.lm.NGramProcessLM;

public class ProcessLmDemo {

    public static void main(String[] args) {
        int ngram = Integer.valueOf(args[0]);
        String textTrain = args[1];
        String textTest = args[2];

        /*x ProcessLmDemo.1 */
        NGramProcessLM lm = new NGramProcessLM(ngram);
        lm.handle(textTrain);
        double log2Prob = lm.log2Estimate(textTest);
        /*x*/

        System.out.println("ngram=" + ngram);
        System.out.println("train=|" + textTrain + "|");
        System.out.println("test=|" + textTest + "|");
        
        System.out.printf("log2 p(test|train)=%.3f\n",log2Prob);
    }

}