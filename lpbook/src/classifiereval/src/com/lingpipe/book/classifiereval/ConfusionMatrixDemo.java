package com.lingpipe.book.classifiereval;

import com.aliasi.classify.ConfusionMatrix;

public class ConfusionMatrixDemo {

    private ConfusionMatrixDemo() { /* no instances */ }

    public static void main(String[] args) {

        /*x ConfusionMatrixDemo.1 */
        String[] cats = new String[] {
            "cabernet", "syrah", "pinot"
        };

        int[][] cells = new int[][] {
            { 9, 3, 0 },
            { 3, 5, 1 },
            { 1, 1, 4 }
        };

        ConfusionMatrix cm = new ConfusionMatrix(cats, cells);
        /*x*/

        /*x ConfusionMatrixDemo.2 */
        String[] categories = cm.categories();

        int totalCount = cm.totalCount();
        int totalCorrect= cm.totalCorrect();
        double totalAccuracy = cm.totalAccuracy();
        double confidence95 = cm.confidence95();
        double confidence99 = cm.confidence99();
        /*x*/

        /*x ConfusionMatrixDemo.3 */
        double randomAccuracy = cm.randomAccuracy();
        double randomAccuracyUnbiased = cm.randomAccuracyUnbiased();
        double kappa = cm.kappa();
        double kappaUnbiased = cm.kappaUnbiased();
        double kappaNoPrevalence = cm.kappaNoPrevalence();
        /*x*/

        /*x ConfusionMatrixDemo.4 */
        double referenceEntropy = cm.referenceEntropy();
        double responseEntropy = cm.responseEntropy();
        double crossEntropy = cm.crossEntropy();
        double jointEntropy = cm.jointEntropy();
        double conditionalEntropy = cm.conditionalEntropy();
        double mutualInformation = cm.mutualInformation();
        double klDivergence = cm.klDivergence();

        double conditionalEntropyCab = cm.conditionalEntropy(0);
        double conditionalEntropySyr = cm.conditionalEntropy(1);
        double conditionalEntropyPin = cm.conditionalEntropy(2);
        /*x*/

        /*x ConfusionMatrixDemo.5 */
        double chiSquared = cm.chiSquared();
        double chiSquaredDegreesOfFreedom
            = cm.chiSquaredDegreesOfFreedom();
        double phiSquared = cm.phiSquared();
        double cramersV = cm.cramersV();
        
        double lambdaA = cm.lambdaA();
        double lambdaB = cm.lambdaB();
        /*x*/
        
        
        System.out.println("categories[0]=" + categories[0]);
        System.out.println("categories[1]=" + categories[1]);
        System.out.println("categories[2]=" + categories[2]);
        System.out.println("totalCount=" + totalCount);
        System.out.println("totalCorrect=" + totalCorrect);
        System.out.println("totalAccuracy=" + totalAccuracy);
        System.out.println("confidence95=" + confidence95);
        System.out.println("confidence99=" + confidence99);

        System.out.println("randomAccuracy=" + randomAccuracy);
        System.out.println("randomAccuracyUnbiased=" + randomAccuracyUnbiased);
        System.out.println("kappa=" + kappa);
        System.out.println("kappaUnbiased=" + kappaUnbiased);
        System.out.println("kappaNoPrevalence=" + kappaNoPrevalence);

        System.out.println("referenceEntropy=" + referenceEntropy);
        System.out.println("responseEntropy=" + responseEntropy);
        System.out.println("crossEntropy=" + crossEntropy);
        System.out.println("jointEntropy=" + jointEntropy);
        System.out.println("conditionalEntropy=" + conditionalEntropy);
        System.out.println("mutualInformation=" + mutualInformation);
        System.out.println("klDivergence=" + klDivergence);
        System.out.println("conditionalEntropyCab=" + conditionalEntropyCab);
        System.out.println("conditionalEntropySyr=" + conditionalEntropySyr);
        System.out.println("conditionalEntropyPin=" + conditionalEntropyPin);

        System.out.println("chiSquared=" + chiSquared);
        System.out.println("chiSquaredDegreesOfFreedom=" + chiSquaredDegreesOfFreedom);
        System.out.println("phiSquared=" + phiSquared);
        System.out.println("cramersV=" + cramersV);

        System.out.println("lambdaA=" + lambdaA);
        System.out.println("lambdaB=" + lambdaB);
        
    }

}