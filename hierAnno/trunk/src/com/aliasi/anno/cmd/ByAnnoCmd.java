package com.aliasi.anno.cmd;

import com.aliasi.io.FileLineReader;

import com.aliasi.util.Strings;

import com.aliasi.anno.CollapsedMultinomialByAnno;

import java.io.File;
import java.io.IOException;

public class ByAnnoCmd {

    static boolean VERBOSE = true;

    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        File annoTsvFile = new File(args[0]);
        double initialPi = Double.valueOf(args[1]);
        double initialSpecificity = Double.valueOf(args[2]);
        double initialSensitivity = Double.valueOf(args[3]);
        int numSamples = Integer.valueOf(args[4]);
        File goldTsvFile = args.length > 5 ? new File(args[5]) : null;
        
        System.out.println("TSV Data File=" + annoTsvFile.getCanonicalPath());
        System.out.println("initial pi=" + initialPi);
        System.out.println("initial specificity=" + initialSpecificity);
        System.out.println("initial sensitivity=" + initialSensitivity);
        System.out.println("initial sensitivity=" + initialSensitivity);
        System.out.println("Gold Standard File=" 
                           + (goldTsvFile == null ? "NONE GIVEN" : goldTsvFile.getCanonicalPath()));

        System.out.println("Reading lines from file");
        String[] lines = FileLineReader.readLineArray(annoTsvFile,"ASCII");
        System.out.println("     #lines=" + lines.length);

        System.out.println("Parsing raw data");
        boolean[] annotations = new boolean[lines.length];
        int[] annotators = new int[lines.length];
        int[] items = new int[lines.length];
        for (int k = 0; k < lines.length; ++k) {
            String[] fields = lines[k].split("\t");
            items[k] = Integer.valueOf(fields[0]);
            annotators[k] = Integer.valueOf(fields[1]);
            annotations[k] = "1".equals(fields[2]);
        }

        System.out.println("Constructing sampler");
        CollapsedMultinomialByAnno sampler
            = new CollapsedMultinomialByAnno(annotations,
                                             annotators,
                                             items,
                                             initialPi,
                                             initialSpecificity,
                                             initialSensitivity);
        int count = 0;
        startTime = System.currentTimeMillis();
        CollapsedMultinomialByAnno.SampleDistribution sampleDistribution
            = new CollapsedMultinomialByAnno
            .SampleDistribution(sampler.numItems(),
                                sampler.numAnnotators());
        for (CollapsedMultinomialByAnno.Sample sample : sampler) {
            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            System.out.printf("%10s %7d  pi=%4.3f   phi0=%4.3f gamma0=%4.3f   phi1=%4.3f gamma1=%4.3f\n",
                              Strings.msToString(elapsedTimeMs),
                              count++,
                              sample.pi(),
                              sample.specificityPriorMean(),
                              sample.specificityPriorScale(),
                              sample.sensitivityPriorMean(),
                              sample.sensitivityPriorScale());

            sampleDistribution.handle(sample);
            if (sampleDistribution.numSamples() == numSamples)
                break;
        }

        if (VERBOSE)
            System.out.println(sampleDistribution);

        if (goldTsvFile == null) return;

        String[] linesGold = FileLineReader.readLineArray(goldTsvFile,"ASCII");
        boolean[] goldCategories = new boolean[linesGold.length+1];
        for (int i = 0; i < linesGold.length; ++i)
            goldCategories[i+1] = "1".equals(linesGold[i]);
        
        int tp = 0;
        int tn = 0;
        int p = 0;
        int n = 0;
        for (int i = 0; i < linesGold.length; ++i) {
            double sampleEstimateP1 = sampleDistribution.categoryEstimator(i).mean();
            if (goldCategories[i]) {
                ++p;
                if (sampleEstimateP1 >= 0.5)
                    ++tp;
            } else {
                ++n;
                if (sampleEstimateP1 < 0.5)
                    ++tn;
            }
        }
        System.out.printf("Spec=%4.3f; neg count=%d errors=%d\n",
                          (tn / (double) n), n, n-tn);
        System.out.printf("Sens=%4.3f; pos count=%d errors=%d\n",
                          (tp / (double) p), p, p-tp);
    }

}