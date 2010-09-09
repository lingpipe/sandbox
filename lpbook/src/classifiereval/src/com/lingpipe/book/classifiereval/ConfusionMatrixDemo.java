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

    }

}