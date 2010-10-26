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
            System.out.printf("prec=%4.2f rec=%4.2f\n", 
                              precision, recall);
        }


        System.out.println("\nUninterpolated ROC");
        /*x ScoredPrecisionRecallDemo.3 */
        interpolate = false;
        double[][] rocCurve = eval.rocCurve(interpolate);
        for (double[] ss : rocCurve) {
            double oneMinusSpecificity = ss[0];
            double sensitivity = ss[1];
        /*x*/
            System.out.printf("1-spec=%4.2f sens=%4.2f\n", 
                              oneMinusSpecificity,sensitivity);
        }

        // cut and paste again, this time for ROC interpolated
        System.out.println("\nInterpolated ROC");
        interpolate = true;
        rocCurve = eval.rocCurve(interpolate);
        for (double[] ss : rocCurve) {
            double oneMinusSpecificity = ss[0];
            double sensitivity = ss[1];
            System.out.printf("1-spec=%4.2f sens=%4.2f\n", 
                              oneMinusSpecificity,sensitivity);
        }

        System.out.println();
        /*x ScoredPrecisionRecallDemo.4 */
        for (int n = 1; n <= eval.numCases(); ++n) {
            double precisionAtN = eval.precisionAt(n);
        /*x*/
            System.out.printf("Precision at %2d=%4.2f\n",
                              n,precisionAtN);
        }
            
        /*x ScoredPrecisionRecallDemo.5 */
        double maximumFMeasure = eval.maximumFMeasure();
        double bep = eval.prBreakevenPoint();
        /*x*/

        System.out.printf("\nMaximum F Measure=%4.2f\n",
                          maximumFMeasure);
        System.out.printf("\nPrecision/Recall Break-Even Point (BEP)=%4.2f\n",
                          bep);

    }


}