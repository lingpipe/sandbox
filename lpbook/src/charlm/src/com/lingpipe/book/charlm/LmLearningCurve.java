package com.lingpipe.book.charlm;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.util.Files;

import com.aliasi.stats.OnlineNormalEstimator;

import java.io.File;
import java.io.IOException;


import java.util.ArrayList;

public class LmLearningCurve {

    public static void main(String[] args) throws IOException {
        int ngram = Integer.valueOf(args[0]);
        int numChars = Integer.valueOf(args[1]);
        double lambdaFactor = Double.valueOf(args[2]);
        File dataDir = new File(args[3]);
        
        System.out.println("PROCESS LM PARAMETERS");
        System.out.println("    ngram=" + ngram);
        System.out.println("    numchars=" + numChars);
        System.out.println("    lambdaFactor=" + lambdaFactor);
        System.out.println("    data.dir=" + dataDir.getCanonicalPath());

        /*x LmLearningCurve.1 */
        for (File file : dataDir.listFiles()) {

            char[] cs = Files.readCharsFromFile(file,"ISO-8859-1");

            NGramProcessLM lm 
                = new NGramProcessLM(ngram,numChars,lambdaFactor);

            OnlineNormalEstimator counter 
                = new OnlineNormalEstimator();
        /*x*/

            System.out.println("\nProcessing File=" + file);
        
        /*x LmLearningCurve.2 */
            for (int n = 0; n < cs.length; ++n) {
                double log2Prob 
                    = lm.log2ConditionalEstimate(cs,0,n + 1);

                lm.trainConditional(cs, Math.max(0,n - ngram),
                                    n, Math.max(0,n - 1));
        /*x*/

        /*x LmLearningCurve.3 */
                counter.handle(log2Prob);
                double avg = counter.mean();
                double sd = counter.standardDeviationUnbiased();
        /*x*/
                if ((n % 5000) == 0 || n == (cs.length - 1))
                    System.out.printf("   log2Prob[c[%d]]=%6.3f   sample mean=%6.3f   sample sd=%6.3f\n",
                                      n,log2Prob,avg,sd);
            }
        }
    }

}