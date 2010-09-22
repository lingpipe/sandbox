package com.lingpipe.book.classifiereval;

import com.aliasi.classify.ScoredPrecisionRecallEvaluation;

public class ScoredPrecisionRecallDemo {

    public static void main(String[] args) {
        /*x ScoredPrecisionRecallDemo.1 */
        ScoredPrecisionRecallEvaluation eval
            = new ScoredPrecisionRecallEvaluation();

        eval.addCase(false,-1.21);      eval.addCase(false,-1.80);
        eval.addCase(true,-1.60);       eval.addCase(false,-1.65);
        eval.addCase(false,-1.39);      eval.addCase(true,-1.47);
        eval.addCase(true,-2.01);       eval.addCase(false,-3.70);
        eval.addCase(true,-1.27);       eval.addCase(false,-1.79);
        
        eval.addMisses(1);
        /*x*/

        System.out.println("\nUninterpolated Precision/Recall");

        /*x ScoredPrecisionRecallDemo.2 */
        boolean interpolate = false;
        double[][] prCurve = eval.prCurve(interpolate);
        for (double[] pr : prCurve) {
            double recall = pr[0];
            double precision = pr[1];
        /*x*/
            System.out.printf("prec=%4.2f rec=%4.2f\n", 
                              precision, recall);
        }

        // ugly cut-and-paste for simplicity of extracted code above
        System.out.println("\nInterpolated Precision/Recall");
        interpolate = true;
        prCurve = eval.prCurve(interpolate);
        for (double[] pr : prCurve) {
            double recall = pr[0];
            double precision = pr[1];
        /*x*/
            System.out.printf("prec=%4.2f rec=%4.2f\n", 
                              precision, recall);
        }

        double precisionAt0 = eval.precisionAt(1);
        double precisionAt1 = eval.precisionAt(2);
        double precisionAt2 = eval.precisionAt(3);

        System.out.printf("precision @1=%4.2f  @2=%4.2f  @3=%4.2f\n",
                          precisionAt0, precisionAt1, precisionAt2);

        System.out.println("\nUninterpolated Specificity/Sensitivity");
        /*x ScoredPrecisionRecallDemo.3 */
        interpolate = true;
        double[][] rocCurve = eval.rocCurve(interpolate);
        for (double[] pr : prCurve) {
            double sensitivity = pr[0];
            double specificity = pr[1];
        /*x*/
            System.out.printf("spec=%4.2f sens=%4.2f\n", 
                              specificity, sensitivity);
        }
        
    }


}