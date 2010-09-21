package com.lingpipe.book.classifiereval;

import com.aliasi.classify.PrecisionRecallEvaluation;

public class PrecisionRecallDemo {

    public static void main(String[] args) {
        long tp = Long.parseLong(args[0]);
        long fn = Long.parseLong(args[1]);
        long fp = Long.parseLong(args[2]);
        long tn = Long.parseLong(args[3]);
        
        /*x PrecisionRecallDemo.1 */
        PrecisionRecallEvaluation eval
            = new PrecisionRecallEvaluation(tp,fn,fp,tn);
        
        long tpOut = eval.truePositive();
        long fnOut = eval.falseNegative();
        long fpOut = eval.falsePositive();
        long tnOut = eval.trueNegative();

        long positiveRef = eval.positiveReference();
        long positiveResp = eval.positiveResponse();

        long total = eval.total();
        /*x*/

        /*x PrecisionRecallDemo.2 */
        double precision = eval.precision();
        double recall = eval.recall();
        double specificity = eval.rejectionRecall();
        double selectivity = eval.rejectionPrecision();

        double fMeasure = eval.fMeasure();

        double prevalence = eval.referenceLikelihood();
        /*x*/
        
        /*x PrecisionRecallDemo.3 */
        double accuracy = eval.accuracy();
        double accuracyStdDev = eval.accuracyDeviation();

        double kappa = eval.kappa();

        double chiSq = eval.chiSquared();
        double df = 1;
        /*x*/

        System.out.println("tpOut=" + tpOut);
        System.out.println("fnOut=" + fnOut);
        System.out.println("fpOut=" + fpOut);
        System.out.println("tnOut=" + tnOut);

        System.out.println("positiveRef=" + positiveRef);
        System.out.println("positiveResp=" + positiveResp);
        System.out.println("total=" + total);

        System.out.println("precision=" + precision);
        System.out.println("recall=" + recall);
        System.out.println("specificity=" + specificity);
        System.out.println("selectivity=" + selectivity);

        System.out.println("fMeasure=" + fMeasure);

        System.out.println("prevalence=" + prevalence);

        System.out.println("accuracy=" + accuracy);
        System.out.println("accuracyStdDev=" + accuracyStdDev);

        System.out.println("kappa=" + kappa);
        System.out.println("chisq=" + chiSq);
    }

}