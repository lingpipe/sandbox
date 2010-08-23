package com.lingpipe.book.stats;

import com.aliasi.stats.Statistics;

public class KlDivergence {

    /*x KlDivergence.1 */
    public static void main(String[] args) {
        double[] pX = parseDoubles(args[0],"pX");
        double[] pY = parseDoubles(args[1],"pY");
        double kld = Statistics.klDivergence(pX,pY);
        double skld = Statistics.symmetrizedKlDivergence(pX,pY);
        double jsd = Statistics.jsDivergence(pX,pY);
    /*x*/
        System.out.println("kld=" + kld);
        System.out.println("skld=" + skld);
        System.out.println("jsd=" + jsd);
    }

    static double[] parseDoubles(String arg, String name) {
        String[] xs = arg.trim().split(" ");
        System.out.print(name + "=(");
        double[] p = new double[xs.length];
        for (int i = 0; i < xs.length; ++i) {
            p[i] = Double.parseDouble(xs[i]);
            if (i > 0) System.out.print(", ");
            System.out.print(p[i]);
        }
        System.out.println(")");
        return p;
    }

}