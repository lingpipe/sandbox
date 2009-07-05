package com.aliasi.anno.cmd;

import com.aliasi.io.FileLineReader;

import com.aliasi.anno.CollapsedMultinomialByAnno;

import java.io.File;
import java.io.IOException;

public class ByAnnoCmd {

    public static void main(String[] args) throws IOException {
        File annoTsvFile = new File(args[0]);
        double initialPi = Double.valueOf(args[1]);
        double initialSpecificity = Double.valueOf(args[2]);
        double initialSensitivity = Double.valueOf(args[3]);

        System.out.println("TSV Data File=" + annoTsvFile.getCanonicalPath());
        System.out.println("initial pi=" + initialPi);
        System.out.println("initial specificity=" + initialSpecificity);
        System.out.println("initial sensitivity=" + initialSensitivity);

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
        for (CollapsedMultinomialByAnno.Sample sample : sampler) {
            System.out.printf("%7d  pi=%4.3f   phi0=%4.3f gamma0=%4.3f   phi1=%4.3f gamma1=%4.3f\n",
                              count++,
                              sample.pi(),
                              sample.specificityPriorMean(),
                              sample.specificityPriorScale(),
                              sample.sensitivityPriorMean(),
                              sample.sensitivityPriorScale());

            /*
            for (int j = 0; j < sample.numAnnotators(); ++j)
                System.out.printf("  theta0[%d]= %4.3f  theta1[%d]= %4.3f\n",
                                   j,sample.specificities()[j],
                                   j,sample.sensitivities()[j]);
            */
        }

    }

}