import com.aliasi.classify.PrecisionRecallEvaluation;
import com.aliasi.classify.ScoredPrecisionRecallEvaluation;

import com.aliasi.util.Arrays;
import com.aliasi.util.Files;

import java.io.*;
import java.util.*;

public class Scorer {

    public static void main(String[] args) throws Exception {
        File dataFile = new File(args[0]);
        int startRow = Integer.parseInt(args[1]);
        int startReferenceColumn = Integer.parseInt(args[2]);
        int startResponseColumn = Integer.parseInt(args[3]);
        int numSyndromes = Integer.parseInt(args[4]);
        int numCases = Integer.parseInt(args[5]);

        System.out.println("Command-line Parameters");
        System.out.println("  File=" + dataFile);
        System.out.println("  startRow=" + startRow);
        System.out.println("  startReferenceColumn=" + startReferenceColumn);
        System.out.println("  startResponseColumn=" + startResponseColumn);
        System.out.println("  numSyndromes=" + numSyndromes);
        System.out.println("  numCases=" + numCases);
        
        String dataText = Files.readFromFile(dataFile,"ISO-8859-1");
        String[][] dataMatrix = Arrays.csvToArray2D(dataText);

        for (int i = 0; i < numSyndromes; ++i) {
            String syndrome = dataMatrix[startRow][startReferenceColumn+i];
            System.out.println("\nSYNDROME=" + syndrome);
            PrecisionRecallEvaluation prEval = new PrecisionRecallEvaluation();
            ScoredPrecisionRecallEvaluation scoredPrEval = new ScoredPrecisionRecallEvaluation();
            for (int j = 0; j < numCases; ++j) {
                try {
                    String reference = dataMatrix[j+1][i+startReferenceColumn];
                    String response = dataMatrix[j+1][i+startResponseColumn];
                    boolean correct = "1".equals(reference);
                    double score = Double.parseDouble(response);
                    scoredPrEval.addCase("1".equals(reference), Double.parseDouble(response));
                } catch (Exception e) {
                    System.out.println("j=" + j + " j+1=" + (j+1) + " startRespCol=" + startResponseColumn
                                       + " i+startResp=" + (i + startResponseColumn)
                                       + " startRefCol=" + startReferenceColumn
                                       + " i+startRefCol=" + (i + startReferenceColumn));
                    throw new RuntimeException(e);
                }
            }
            System.out.printf("     Max F(1)=%6.4f\n",scoredPrEval.maximumFMeasure());
            System.out.printf("     PR Breakeven=%6.4f\n",scoredPrEval.prBreakevenPoint());
            System.out.printf("     Avg Precision=%6.4f\n",scoredPrEval.averagePrecision());
            System.out.printf("     Area under PR Curve[interpolated]=%6.4f\n",scoredPrEval.areaUnderPrCurve(true));
            System.out.printf("     Precision At\n");
            for (Integer k : new Integer[] { 5, 10, 25, 50, 100 })
                if (k <= numCases)
                    System.out.printf("           %4d:  %6.4f\n", k, scoredPrEval.precisionAt(k));
                               
            System.out.printf("     Uninterpolated precision-recall curve\n");
            System.out.printf("          %5s %5s %10s\n","REC","PREC","CONVEX-HULL");
            double[][] prCurve = scoredPrEval.prCurve(false);
            for (int k = 0; k < prCurve.length; ++k) {
                System.out.printf("          %5.3f %5.3f %1s\n",prCurve[k][0],prCurve[k][1],
                                  isInterpolated(k,prCurve) ? "*" : "");
            }
        }
    }

    static boolean isInterpolated(int k, double[][] prCurve) {
        for (int j = k+1; j < prCurve.length; ++j)
            if (prCurve[j][1] > prCurve[k][1])
                return false;
        return true;
    }


}