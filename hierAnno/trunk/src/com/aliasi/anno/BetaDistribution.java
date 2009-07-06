package com.aliasi.anno;

import com.aliasi.stats.OnlineNormalEstimator;

import java.util.Random;

public class BetaDistribution {

    final double mAlpha;
    final double mBeta;

    public BetaDistribution(double alpha, double beta) {
        mAlpha = alpha;
        mBeta = beta;
    }

    public BetaDistribution(double[] examples) {
        OnlineNormalEstimator estimator = new OnlineNormalEstimator();
        for (int i = 0; i < examples.length; ++i)
            estimator.handle(examples[i]);
        double mean = estimator.mean();
        double variance = estimator.variance();
        mAlpha = alpha(mean,variance);
        mBeta = beta(mean,variance);
    }


    public double mean() {
        return mean(mAlpha,mBeta);
    }

    public double variance() {
        return variance(mAlpha,mBeta);
    }

    public static double mean(double alpha, double beta) {
        assertValidAlphaBeta(alpha,beta);
        return alpha/(alpha + beta);
    }

    public static double variance(double alpha, double beta) {
        assertValidAlphaBeta(alpha,beta);
        return alpha * beta
            / (square(alpha + beta) * (alpha + beta + 1.0));
    }

    public static double alpha(double mean, double variance) {
        return square(mean) * (1.0 - mean) / variance - mean;
    }

    public static double beta(double mean, double variance) {
        double alpha = alpha(mean,variance);
        return alpha / mean - alpha;
    }


    static double square(double x) {
        return x * x;
    }

    static void assertValidAlphaBeta(double alpha, double beta) {
        assertValidConcentration("alpha",alpha);
        assertValidConcentration("beta",beta);
    }

    static void assertValidConcentration(String name, double gamma) {
        if (Double.isNaN(gamma)
            || Double.isInfinite(gamma)
            || gamma <= 0.0) {
            String msg = "Concentration parameter " + name
                + " must be finite and greater than 0."
                + " Found " + name + "=" + gamma;
            throw new IllegalArgumentException(msg);
        }
    }

    public static void main(String[] args) {
        Random random = new Random();
        for (int i = 0; i < 100; ++i) {
            double alpha = random.nextDouble() * 1000000;
            double beta = random.nextDouble() * 1000000;
            double mean = mean(alpha,beta);
            double variance = variance(alpha,beta);
            double alphaCalc = alpha(mean,variance);
            double betaCalc = beta(mean,variance);
            double meanCalc = mean(alphaCalc,betaCalc);
            double varianceCalc = variance(alphaCalc,betaCalc);
            System.out.printf("%14.4f %14.4f %14.4f %14.4f %14.4f %14.4f %14.4f %14.4f\n",
                              alpha,beta,mean,variance,
                              alphaCalc,betaCalc,meanCalc,varianceCalc);
        }
    }

}