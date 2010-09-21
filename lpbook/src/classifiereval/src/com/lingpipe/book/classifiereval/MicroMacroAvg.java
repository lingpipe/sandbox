package com.lingpipe.book.classifiereval;

import com.aliasi.classify.ConfusionMatrix;
import com.aliasi.classify.PrecisionRecallEvaluation;

public class MicroMacroAvg {

    public static void main(String[] args) {

        String[] cats = new String[] {
            "cabernet", "syrah", "pinot"
        };

        int[][] cells = new int[][] {
            { 9, 3, 0 },
            { 3, 5, 1 },
            { 1, 1, 4 }
        };

        /*x MicroMacroAvg.1 */
        ConfusionMatrix cm = new ConfusionMatrix(cats, cells);

        double macroPrec = cm.macroAvgPrecision();
        double macroRec = cm.macroAvgRecall();
        double macroF = cm.macroAvgFMeasure();
        
        PrecisionRecallEvaluation prMicro = cm.microAverage();
        double microPrec = prMicro.precision();
        double microRec = prMicro.recall();
        double microF = prMicro.fMeasure();
        /*x*/

        /*x MicroMacroAvg.2 */
        for (int i = 0; i < cats.length; ++i) {
            PrecisionRecallEvaluation pr = cm.oneVsAll(i);
            double prec = pr.precision();
            double rec = pr.recall();
            double f = pr.fMeasure();
        /*x*/
            System.out.printf("cat=%8s prec=%5.3f rec=%5.3f F=%5.3f\n",
                              cats[i], prec, rec, f);
        }

        System.out.println();
        System.out.printf("Macro prec=%5.3f rec=%5.3f F=%5.3f\n",
                           macroPrec, macroRec, macroF);
        System.out.printf("Micro prec=%5.3f rec=%5.3f F=%5.3f\n",
                          microPrec, microRec, microF);
        
            
    }

}

