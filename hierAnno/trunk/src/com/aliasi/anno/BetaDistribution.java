package com.aliasi.anno;

import com.aliasi.stats.OnlineNormalEstimator;

import com.aliasi.util.Exceptions;

import java.util.Random;

/**
 * The class {@code BetaDistribution} represents a beta distribution,
 * and the class contains extra static methods relating to beta
 * distributions.  The beta distribution is a Dirichlet distribution
 * with number of outcomes equal to 2.
 *
 * <p>The beta distribution is a probability density with support on
 * the interval [0,1].  Thus it is a natural model for Bernoulli
 * probabilities.  The beta distribution has two parameters, &alpha;
 * and &beta;, with &alpha; acting as the prior count of outcome 1
 * (aka success) and &beta; as the prior count of outcome 0 (aka
 * failure).  The density is defined for &theta; in [0,1] by:
 *
 * <blockquote>
 * Beta(&theta;|&alpha;, &beta;) = [1 / B(&alpha;,&beta)] * &theta;<sup>&alpha;-1</sup> * (1-&theta;)<sup>&beta;-1</sup>
 * </blockquote>
 *
 * where the normaliziner is the beta function B defined by:
 *
 * <blockquote>
 * B(&alpha;,&beta;) = &Gamma;(&alpha;) * &Gamma;(&beta;) / &Gamma;(&alpha; + &beta;)
 * </blockquote>
 *
 * The expected value, or mean, of the beta distribution is:
 *
 * <blockquote>
 * E[Beta(&alpha;,&beta;)] = &alpha; / (&alpha; + &beta;)
 * </blockquote>
 *
 * the mode, which exists for &alpha;, &beta; &gt; 1, is:
 *
 * <blockquote>
 * mode[Beta(&alpha;,&beta;)] = &alpha;-1 / (&alpha;+&beta;-2)
 * </blockquote>
 *
 * and the variance is:
 *
 * <blockquote>
 * var[Beta(&alpha;,&beta;)] = &alpha; * &beta; / [ (&alpha; + &beta;)<sup>2</sup> * (&alpha; + &beta; + 1) ]
 * </blockquote>
 *
 * <b>Moment Matching</b>
 * 
 * <p>The beta distribution Beta(&alpha;,&beta;) with mean &mu; in (0,1)
 * and positive variance &sigma;<sup>2</sup>, is given by:
 *
 * <blockquote>
 * &alpha; = [ &mu;<sup>2</sup> * (1.0 - &mu;) / &sigma;<sup>2</sup> ] - &mu;
 * </blockquote>
 *
 * <blockquote>
 * &beta; = ( &alpha; / &mu; ) - &alpha;
 * </blockquote>
 *
 * 
 * @author Bob Carpenter
 */
public class BetaDistribution {

    final double mAlpha;
    final double mBeta;

    /**
     * Construct a beta distribution with the specified parameters.
     *
     * @param alpha Alpha parameter indicating prior count of positive
     * outcomes.
     * @param beta Beta parameter representing the prior count of
     * negative outcomes.
     * @throws IllegalArgumentException If alpha and beta are not
     * both finite and non-negative.
     */
    public BetaDistribution(double alpha, double beta) {
        validateAlphaBeta(alpha,beta);
        mAlpha = alpha;
        mBeta = beta;
    }


    /**
     * Returns the alpha parameter of this beta distribution,
     * representing the prior count of successes.
     *
     * @return The prior success count.
     */
    public double alpha() {
        return mAlpha;
    }

    /**
     * Returns the alpha parameter of this beta distribution,
     * representing the prior count of failures.
     *
     * @return The prior failure count.
     */
    public double beta() {
        return mBeta;
    }

    /**
     * Returns the mean of this beta distribution.
     *
     * @return The mean of the beta distribution.
     */
    public double mean() {
        return mean(mAlpha,mBeta);
    }

    /**
     * Return the variance of this beta distribution.
     *
     * @return The variance of the beta distribution.
     */
    public double variance() {
        return variance(mAlpha,mBeta);
    }

    /**
     * Returns a string-based representation of this distribution
     * and its parameters.
     *
     * @return A string-based representation of this distribution.
     */
    public String toString() {
        return "Beta(" + alpha() + "," + beta() + ")";
    }

    /**
     * Returns the beta distribution with the specified mean and
     * variance.
     *
     * @param mean Mean of beta distribution.
     * @param variance Variance of beta distribution.
     * @throws IllegalArgumentException If the mean is not between 0 and 1
     * inclusive or if the variance is not finite and non-negative.
     */
    public static BetaDistribution betaWithMeanVariance(double mean, double variance) {
        return new BetaDistribution(alpha(mean,variance),
                                    beta(mean,variance));
    }

    /**
     * Returns the beta distribution estimated using the method of
     * moments from the specified array of samples.
     *
     * @param samples Array of sample probabilities drawn from the
     * beta distribution.
     * @throws An illegal argument exception if there are not at least
     * two samples or if any of the samples is not between 0 and 1
     * inclusive.
     */
    public static BetaDistribution betaByMoments(double[] samples) {
        if (samples.length < 2) {
            String msg = "Require at least 2 samples."
                + " Found samples.length=" + samples.length;
            throw new IllegalArgumentException(msg);
        }
        for (int i = 0; i < samples.length; ++i) {
            if (Double.isNaN(samples[i]) || samples[i] < 0.0 || samples[i] > 0.0) {
                String msg = "All samples must be between 0 and 1 inclusive."
                    + " Found samples[" + i + "]=" + samples[i];
                throw new IllegalArgumentException(msg);
            }
        }
        OnlineNormalEstimator estimator = new OnlineNormalEstimator();
        for (int i = 0; i < samples.length; ++i)
            estimator.handle(samples[i]);
        double mean = estimator.mean();
        double variance = estimator.variance();
        return betaWithMeanVariance(mean,variance);
    }
    

    /**
     * Return the mean of the beta distribution with the specified
     * alpha and beta parameters.
     *
     * @param alpha The prior success count.
     * @param beta The prior failure count.
     * @return The mean of the beta distribution with the specified parameters.
     * @throws IllegalArgumentException If alpha and beta are not
     * both finite and non-negative.
     */
    public static double mean(double alpha, double beta) {
        validateAlphaBeta(alpha,beta);
        return alpha/(alpha + beta);
    }

    
    /**
     * Return the variance of the beta distribution with the specified
     * alpha and beta parameters.
     *
     * @param alpha The prior success count.
     * @param beta The prior failure count.
     * @return The variance of the beta distribution with the specified parameters.
     * @throws IllegalArgumentException If alpha and beta are not
     * both finite and non-negative.
     */
    public static double variance(double alpha, double beta) {
        validateAlphaBeta(alpha,beta);
        return alpha * beta
            / (square(alpha + beta) * (alpha + beta + 1.0));
    }


    /**
     * Returns the alpha parameter representing prior success counts for
     * the beta distribution with the specified mean and variance.
     *
     * @param mean Beta distribution mean.
     * @param variance Beta distribution variance.
     * @return Prior success counts for the specified beta
     * distribution.
     */
    public static double alpha(double mean, double variance) {
        validateMeanVariance(mean,variance);
        return square(mean) * (1.0 - mean) / variance - mean;
    }

    /**
     * Returns the beta parameter representing prior failure counts for
     * the beta distribution with the specified mean and variance.
     *
     * @param mean Beta distribution mean.
     * @param variance Beta distribution variance.
     * @return Prior failure counts for the specified beta
     * distribution.
     */
    public static double beta(double mean, double variance) {
        validateMeanVariance(mean,variance);
        double alpha = alpha(mean,variance);
        return alpha / mean - alpha;
    }


    static double square(double x) {
        return x * x;
    }

    static void validateMeanVariance(double mean, double variance) {
        if (Double.isNaN(mean) || mean <= 0.0 || mean >= 1.0) {
            String msg = "Mean must be between 0.0 and 1.0 exclusive."
                + " Found mean=" + mean;
            throw new IllegalArgumentException(msg);
        }
        Exceptions.finiteNonNegative("variance",variance);
    }

    
    static void validateAlphaBeta(double alpha, double beta) {
        Exceptions.finiteNonNegative("alpha",alpha);
        Exceptions.finiteNonNegative("beta",beta);
    }


    /**
     * A simple demo of how it works.
     */
    public static void main(String[] args) {
        Random random = new Random();
        for (int i = 0; i < 100; ++i) {
            double alpha = random.nextDouble() * 100;
            double beta = random.nextDouble() * 1000;
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