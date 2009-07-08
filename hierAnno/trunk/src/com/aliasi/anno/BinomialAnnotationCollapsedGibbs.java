package com.aliasi.anno;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.stats.OnlineNormalEstimator;

import com.aliasi.util.Exceptions;
import com.aliasi.util.Iterators;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * The {@code BinomialAnnotationCollapsedGibbs} class implements a collapsed
 * Gibbs sampler for the binomial model of annotation.  
 *
 * <h4>Hierarchical Model</h4>
 *
 * The full hierarchical model for binomial data annotation is described
 * in the following table, in which each row describes a variable, with
 * its allowable value range, the status as to whether it's an input or
 * it's estimated, the distribution governing it, and a description of
 * the variable:

 * <blockquote><table border="1" cellpadding="5">
 * <tr><th>Variable</th>
 *     <th>Range</th>
 *     <th>Status</th>
 *     <th>Distribution</th>
 *     <th>Description</th></tr>
 * <tr><td>I</td>
 *     <td>&gt; 0</td>
 *     <td>input</td>
 *     <td>fixed</td>
 *     <td>number of Items</td></tr>
 * <tr><td>J</td>
 *     <td>&gt; 0</td>
 *     <td>input</td>
 *     <td>fixed</td>
 *     <td>number of annotators</td></tr>
 * <tr><td>&pi;</td>
 *     <td>[0,1]</td>
 *     <td>estimated</td>
 *     <td>Beta(1,1)<sup>&Dagger;</sup></td>
 *     <td>prevalence of category 1</td></tr>
 * <tr><td>c[i]</td>
 *     <td>{0,1}</td>
 *     <td>estimated</td>
 *     <td>Bern(&pi;)</td>
 *     <td>category for item i</td></tr>
 * <tr><td>&theta;<sub>0</sub>[j]</td>
 *     <td>[0,1]</td>
 *     <td>estimated</td>
 *     <td>Beta(&alpha;<sub>0</sub>,&beta;<sub>0</sub>)</td>
 *     <td>specificity of annotator j</td></tr>
 * <tr><td>&theta;<sub>1</sub>[j]</td>
 *     <td>[0,1]</td>
 *     <td>estimated</td>
 *     <td>Beta(&alpha;<sub>1</sub>,&beta;<sub>1</sub>)</td>
 *     <td>sensitivity of annotator j</td></tr>
 * <tr><td>&alpha;<sub>0</sub>/(&alpha;<sub>0</sub>+&beta;<sub>0</sub>)</td>
 *     <td>[0,1]</td>
 *     <td>estimated</td>
 *     <td>Beta(1,1)<sup>&dagger;</sup><sup>&Dagger;</sup></td>
 *     <td>prior specificity mean</td></tr>
 * <tr><td>&alpha;<sub>0</sub> + &beta;<sub>0</sub></td>
 *     <td>(0,&#x221E;)</td>
 *     <td>estimated</td>
 *     <td>Uniform(0,&#x221E;)<sup>*</sup><sup>&dagger;</sup></td>
 *     <td>prior specificity scale</td></tr>
 * <tr><td>&alpha;<sub>1</sub>/(&alpha;<sub>1</sub>+&beta;<sub>1</sub>)</td>
 *     <td>[0,1]</td>
 *     <td>estimated</td>
 *     <td>Beta(1,1)<sup>&dagger;<sup>&Dagger;</sup></sup></td>
 *     <td>prior sensitivity mean</td></tr>
 * <tr><td>&alpha;<sub>1</sub> + &beta;<sub>1</sub></td>
 *     <td>(0,&#x221E;)</td>
 *     <td>estimated</td>
 *     <td>Uniform(0,&#x221E;)<sup>*</sup><sup>&dagger;</sup></td>
 *     <td>prior sensitivity scale</td></tr>
 * <tr><td>x[i,j]</td>
 *     <td>{0,1}</td>
 *     <td>input</td>
 *     <td>Bern(c[i,j]==1 
 * ? &theta;<sub>1</sub>[j] 
 * : 1-&theta;<sub>0</sub>[j])</td>
 *     <td>annotation of item i by annotator j</td></tr>
 * </table></blockquote>
 *
 * <p>
 * <sup>*</sup>&nbsp;This is an improper prior for the concentration or
 * scale parameter of the beta distribution, leading to a maximum likelihood
 * estimate in principle.  In practice, the implementation uses moment
 * matching.  
 *
 * <p>
 * <sup>&dagger;</sup>&nbsp;Alternatively, fixed values for the parameters
 * of the beta priors for sensitivity and specificity may be
 * given.  The parameters are &alpha;<sub>0</sub>,
 * and &beta;<sub>0</sub>, &alpha;<sub>1</sub>, and
 * and &beta;<sub>1</sub>.
 *
 * <p>
 * <sup>&Dagger;</sup>&nbsp;A Beta(1,1) distribution is the
 * same as Uniform(0,1).
 *
 * <h4>Collapsed Gibbs Sampler</h4>
 *
 * This class estimates a Gibbs sampler which collapses all of the
 * samples other than the true category c[i] of item i.  The sampling
 * distribution for c[i] is straightforwardly derived with Bayes'
 * rule:
 *
 * <blockquote>p(c[i] | x, &theta;<sub>0</sub>, &theta;<sub>1</sub>) 
 * &#x221D;
 * p(c[i]) 
 * * <big><big>&Pi;</big></big><sub>j in 1:J</sub> p(x[i,j] | c[i], &theta;<sub>0</sub>[j], &theta;<sub>1</sub>[j])</blockquote>
 *
 * where
 *
 * <blockquote>p(x[i,j] | c[i], &theta;<sub>0</sub>[j], &theta;<sub>1</sub>[j])
 * = c[i] ? &theta;<sub>1</sub>[j] : (1 - &theta;<sub>0</sub>[j])</blockquote>
 *
 * 
 * <h4>Missing Annotations</h4>
 *
 * The actual model is set up so that not every annotator needs to
 * annotate every item.  The math remains essentially the same, bu
 * the indexing gets more complex.  Assuming there are k annotations,
 * we let xx[k] be the annotation, ii[k] be the annotator for the k-th
 * item, and jj[k] be the annotator for the k-th annotation.  So we
 * need additional variables jj, ii, and xx, and no longer need x.  
 *
 * xx[k] is sampled according to the following Bernoulli:
 *
 * <pre>Bern(c[ii[k]]==1 ? &theta;<sub>1</sub>[jj[k]] : (1 - &theta;<sub>0</sub>[jj[k]])</pre>
 *
 * and the category sampling distribution is now:
 * 
 * <blockquote>p(c[i] | xx, &theta;<sub>0</sub>, &theta;<sub>1</sub>) 
 * &#x221D;
 * p(c[i]) 
 * * <big><big>&Pi;</big></big><sub>k: ii[k]==i</sub> p(xx[k] | c[i], &theta;<sub>0</sub>[jj[k]], &theta;<sub>1</sub>[jj[k]])</blockquote>
 * 
 * with no change in the inner probability definition.
 *
 * <h4>Initialization</h4>
 *
 * The first sample is constructed from the initial prevalence &pi;
 * and initial values for annotator sensitivity and specificity
 * (&theta;<sub>0</sub> and &theta;<sub>1</sub>).  First, the
 * categories for the items are sampled given the initial values.
 * Then everything else is estimated as described in the next section.

 *
 * <h4>Estimating Everything Else</h4>
 *
 * All other variables get point estimates based on the category
 * estimates.
 *
 * <p>The prevalence is estimated based on a uniform beta prior
 * Beta(1,1), so the point estimate for &pi; is just the proportion of
 * 1 outcomes.
 *
 * <p>The sensitivities and specificities are assigned maximum a
 * posteriori (MAP) estimates given their beta priors.
 *
 * <p>The beta priors may be fixed or may be estimated.  If they
 * are estimated, moment matching is used to derive beta parameters
 * whose mean and variance match that of the sensitivities (or
 * specificities) of the annotators.  
 *
 * <p><b>Warning:</b> Estimating the beta priors can cause
 * non-divergence in the case where annotator sensitivities and
 * specifities are tightly grouped.  The problem is that once you get
 * close estimates, the variance is estimated as lower, the next set
 * of estimates are even closer to each other, and so on until
 * variance approaches 0 and the likelihood blows up (as do the
 * &alpha; and &beta; parameters).
 *
 *
 * <h4>Retrieving Samples</h4>
 *
 * This class implements the {@code Iterable<BinomialAnnotationCollapsedGibbs.Sample>}
 * interface.   Thus calling {@link #iterator()} returns an iterator over
 * samples.   
 *
 * <h4>Collecting Whole Chain Statistics</h4>
 *
 * The static class {@link SampleStatistics} may be used to collect
 * up distributional statistics for the samples.  For each variable
 * in the model, the sample distribution computes means and variances
 * online.  
 *
 * The following code can be used for collecting chain statistics:
 *
 * <blockquote><pre>
 * BinomialAnnotationCollapsedGibbs sampler = ...;
 * BinomialAnnotationCollapsedGibbs.SampleStatistics sampleStats
 *     = new BinomialAnnotationCollapsedGibbs
 *           .SampleStatistics(sampler.numItems(),
 *                               sampler.numAnnotators());
 * for (BinomialAnnotationCollapsedGibbs.Sample sample : sampler)
 *     sampleDistribution.handle(sample);
 *
 * System.out.println(sampleStats);</pre></blockquote>
 *
 * Instead of printing, the means and variances for the sample
 * variables may be retrieved programatically from the sample
 * distribution.
 *
 *
 * <h4>Thread Safety and Reuse</h4>
 *
 * An instance of {@code BinomialAnnotationCollapsedGibbs} may
 * be used in multiple threads, each of which may call 
 * the {@link #iterator()} method.  The samplers across threads
 * will share the fixed data, but will preserve their own
 * sequence of samples.
 *
 *
 * <h4>References</h4>
 *
 * <ul>
 * <li>Dawid, A. P. and A. M. Skene. 1979. Maximum likelihood estimation of observer error-rates using the EM
 * algorithm. <i>Applied Statistics</i>, <b>28</b>(1):20--28.
* </li>
 * <li>Carpenter, Bob. 2008. <a href="http://lingpipe.files.wordpress.com/2008/11/carp-bayesian-multilevel-annotation.pdf">Multilevel Bayesian Models of Categorical Data Annotation</a>. Technical Report. Alias-i.
 * </ul>
 * 
 * @author Bob Carpenter
 */
public class BinomialAnnotationCollapsedGibbs
    implements Iterable<BinomialAnnotationCollapsedGibbs.Sample> {

    final boolean[] mAnnotations;
    final int[] mAnnotators;
    final int mNumAnnotators;
    final int[] mItems;
    final int mNumItems;

    final double mInitialPi;
    final double mInitialSpecificity;
    final double mInitialSensitivity;

    final boolean mHasFixedBetaPriors;
    final double mFixedAlpha0;
    final double mFixedBeta0;
    final double mFixedAlpha1;
    final double mFixedBeta1;

    /**
     * Construct a collapsed Gibbs sampler for the specified
     * annotations, annotators, items, and initial parameter values,
     * estimating the beta priors for sensitivity and specificity.
     *
     * @param annotations Array of annotations, with {@code true} for 1
     * and {@code false} for 0.
     * @param annotators Parallel array of annotators, numbered from
     * 1 to the number of annotators.
     * @param items Parallel array of items, numbered from 1 to the
     * number of items.
     * @param initialPi Initial value for prevalence (proportion of 1
     * outcomes).
     * @param initialSpecificity Initial value for each annotator's
     * specificity.
     * @param initialSensitivity Initial value for each annotator's
     * sensitivity.
     * @throws IllegalArgumentException If the three arrays are not
     * the same length, if any of the items or annotators are less
     * than or equal to zero or if the item and annotator lists do not
     * form a contiguous sequence, or if the initial values for pi,
     * sensitivity and specificity are not between 0 and 1 exclusive.
     */
    public BinomialAnnotationCollapsedGibbs(boolean[] annotations,
                                      int[] annotators,
                                      int[] items,
                                      double initialPi,
                                      double initialSpecificity,
                                      double initialSensitivity) {
        this(annotations,annotators,items,
             initialPi,initialSpecificity,initialSensitivity,
             false,Double.NaN,Double.NaN,Double.NaN,Double.NaN);

    }


    /**
     * Construct a collapsed Gibbs sampler for the specified
     * annotations, annotators, items, and initial parameter values,
     * estimating the beta priors for sensitivity and specificity.
     *
     * @param annotations Array of annotations, with {@code true} for 1
     * and {@code false} for 0.
     * @param annotators Parallel array of annotators, numbered from
     * 1 to the number of annotators.
     * @param items Parallel array of items, numbered from 1 to the
     * number of items.
     * @param initialPi Initial value for prevalence (proportion of 1
     * outcomes).
     * @param initialSpecificity Initial value for each annotator's
     * specificity.
     * @param initialSensitivity Initial value for each annotator's
     * sensitivity.
     * @param alpha0 Fixed value for parameter &alpha;<sub>0</sub>, the
     * prior true negative count.
     * @param beta0 Fixed value for parameter &alpha;<sub>0</sub>,
     * the prior false positive count.
     * @param alpha1 Fixed value for parameter &alpha;<sub>0</sub>,
     * the prior true positive count.
     * @param beta1 Fixed value for parameter &alpha;<sub>0</sub>,
     * the prior false positive count.
     * @throws IllegalArgumentException If the three arrays are not
     * the same length, if any of the items or annotators are less
     * than or equal to zero or if the item and annotator lists do not
     * form a contiguous sequence, or if the initial values for pi,
     * sensitivity and specificity are not between 0 and 1 exclusive,
     * or if the fixed alpha and beta values are not finite and
     * non-negative.
     */
    public BinomialAnnotationCollapsedGibbs(boolean[] annotations,
                                      int[] annotators,
                                      int[] items,
                                      double initialPi,
                                      double initialSpecificity,
                                      double initialSensitivity,
                                      double alpha0,
                                      double beta0,
                                      double alpha1,
                                      double beta1) {

        this(annotations,annotators,items,
             initialPi,initialSpecificity,initialSensitivity,
             true,alpha0,beta0,alpha1,beta1);

        Exceptions.finiteNonNegative("alpha0",alpha0);
        Exceptions.finiteNonNegative("beta0",beta0);
        Exceptions.finiteNonNegative("alph1",alpha1);
        Exceptions.finiteNonNegative("beta1",beta1);

    }

    private BinomialAnnotationCollapsedGibbs(boolean[] annotations,
                                      int[] annotators,
                                      int[] items,
                                      double initialPi,
                                      double initialSpecificity,
                                      double initialSensitivity,
                                      boolean hasFixedBetaPriors,
                                      double alpha0,
                                      double beta0,
                                      double alpha1,
                                      double beta1) {

        assertNonExtremeProbability("initialPi",initialPi);
        assertNonExtremeProbability("initialSpecificity",initialSpecificity);
        assertNonExtremeProbability("initialSensitivity",initialSensitivity);
        assertPositiveSequence("annotators",annotators);
        assertPositiveSequence("items",items);
        if (annotations.length != annotators.length
            || annotators.length != items.length) {
            String msg = "Annotations, annotators, and items must be same length."
                + " Found annotations.length=" + annotations.length
                + "; annotators.length=" + annotators.length
                + "; items.length=" + items.length;
            throw new IllegalArgumentException(msg);
        }

        mAnnotations = annotations;
        mAnnotators = annotators;
        mNumAnnotators = max(annotators)+1;
        mItems = items;
        mNumItems = max(items)+1;
        mInitialPi = initialPi;
        mInitialSpecificity = initialSpecificity;
        mInitialSensitivity = initialSensitivity;

        mHasFixedBetaPriors = hasFixedBetaPriors;
        mFixedAlpha0 = alpha0;
        mFixedBeta0 = beta0;
        mFixedAlpha1 = alpha1;
        mFixedBeta1 = beta1;


    }

    /**
     * Returns the number of items in this sampler.
     *
     * @return Number of items.
     */
    public int numItems() {
        return mNumItems;
    }

    /**
     * The number of annotators for this sampler.
     *
     * @return Number of annotators.
     */
    public int numAnnotators() {
        return mNumAnnotators;
    }

    /**
     * Returns a shallow copy of the annotations in this sampler.
     *
     * @return Annotations for this sampler.
     */ 
    public boolean[] annotations() {
        return mAnnotations.clone();
    }

    /**
     * Returns a shallow copy of the annotators in this sampler.
     *
     * @return Annotators for this sampler.
     */
    public int[] annotators() {
        return mAnnotators.clone();
    }

    /**
     * Returns a shallow copy of the items in this sampler.
     *
     * @return Items for this sampler.
     */
    public int[] items() {
        return mItems.clone();
    }

    /**
     * Returns the initial prevalence for chains produced
     * by this sampler.
     *
     * @return Initial prevalence.
     */
    public double initialPi() {
        return mInitialPi;
    }

    /**
     * Returns the initial specificity for chains produced
     * by this sampler.
     *
     * @return Initial specificity.
     */
    public double initialSpecificity() {
        return mInitialSpecificity;
    }

    /**
     * Returns the initial sensitivity for chains produced
     * by this sampler.
     *
     * @return Initial sensitivity.
     */
    public double initialSensitivity() {
        return mInitialSensitivity;
    }

    /**
     * Returns the fixed value of the &alpha;<sub>0</sub> parameter,
     * the prior count of true negatives, or {@code -1} if it is
     * estimated.
     *
     * @return Initial value of &alpha;<sub>0</sub>.
     */
    public double fixedAlpha0() {
        return mFixedAlpha0;
    }

    /**
     * Returns the fixed value of the &beta;<sub>0</sub> parameter,
     * the prior count of false positives, or {@code -1} if it is
     * estimated.
     *
     * @return Initial value of &beta;<sub>0</sub>.
     */
    public double fixedBeta0() {
        return mFixedBeta0;
    }

    /**
     * Returns the fixed value of the &alpha;<sub>1</sub> parameter,
     * the prior count of true negatives, or {@code -1} if it is
     * estimated.
     *
     * @return Initial value of &alpha;<sub>1</sub>.
     */
    public double fixedAlpha1() {
        return mFixedAlpha1;
    }

    /**
     * Returns the fixed value of the &beta;<sub>1</sub> parameter,
     * the prior count of false positives, or {@code -1} if it is
     * estimated.
     *
     * @return Initial value of &beta;<sub>1</sub>.
     */
    public double fixedBeta1() {
        return mFixedBeta1;
    }

    /**
     * Returns a fresh Markov chain from the data in this class
     * in the form of an iterator over samples.   This method is
     * thread safe and different threads may call it to produce
     * different Markov chains which may be sampled in parallel.
     *
     * @return An iterator over Gibbs samples.
     */
    public Iterator<Sample> iterator() {
        return new SampleIterator();
    }

    class SampleIterator extends Iterators.Buffered<Sample> {
        final boolean[] mCategories;
        double mPi;
        final double[] mSpecificities;
        final double[] mSensitivities;

        // start uniform
        double mAlphaSpecificity = mHasFixedBetaPriors ? mFixedAlpha0 : 1.0;
        double mBetaSpecificity = mHasFixedBetaPriors ? mFixedBeta0 : 1.0;
        double mAlphaSensitivity = mHasFixedBetaPriors ? mFixedAlpha1 : 1.0;
        double mBetaSensitivity = mHasFixedBetaPriors ? mFixedBeta1 : 1.0;

        double mSpecificityPriorScale = BetaDistribution.variance(1.0,1.0);
        double mSensitivityPriorMean;
        double mSensitivityPriorScale;

        final double[] mCategories0;
        final double[] mCategories1;

        final Random mRandom = new Random();

        SampleIterator() {
            mPi = mInitialPi;
            mCategories = new boolean[mNumItems];
            mSpecificities = new double[mNumAnnotators];
            Arrays.fill(mSpecificities,mInitialSpecificity);
            mSensitivities = new double[mNumAnnotators];
            Arrays.fill(mSensitivities,mInitialSensitivity);
            mCategories0 = new double[mNumItems];
            mCategories1 = new double[mNumItems];
        }
        public Sample bufferNext() {
            sampleCategories();
            recomputeNonCategories();
            return new Sample(mPi,
                              mCategories.clone(),
                              mAlphaSpecificity,
                              mBetaSpecificity,
                              mAlphaSensitivity,
                              mBetaSensitivity,
                              mSpecificities.clone(),
                              mSensitivities.clone());
        }
        void sampleCategories() {
            Arrays.fill(mCategories0,1.0-mPi);
            Arrays.fill(mCategories1,mPi);
            for (int k = 0; k < mAnnotations.length; ++k) {
                int i = mItems[k];
                int j = mAnnotators[k];

                if (mAnnotations[k]) {
                    // annotator says 1
                    mCategories0[i] *= (1.0 - mSpecificities[j]);
                    mCategories1[i] *= mSensitivities[j];
                } else {
                    mCategories0[i] *= mSpecificities[j];
                    mCategories1[i] *= (1.0 - mSensitivities[j]);
                }

                if (mCategories0[i] == 0.0) {
                    if (mCategories1[i] == 0.0) {
                        mCategories0[i] = 1.0;
                        mCategories1[i] = 1.0;
                    } else {
                        mCategories0[i] = MIN_VALUE;
                    }
                } else if (mCategories1[i] == 0.0) {
                    mCategories1[i] = MIN_VALUE;
                } else {
                    while (mCategories0[i] < 0.5
                           && mCategories1[i] < 0.5) {
                        mCategories0[i] *= 4.0;
                        mCategories1[i] *= 4.0;
                    }
                }
            }
            for (int i = 0; i < mNumItems; ++i) {
                double p1 = mCategories1[i] / (mCategories1[i] + mCategories0[i]);
                mCategories[i]
                    = mRandom.nextDouble() < p1;
                // System.out.printf("p1=%3.2f c[%d]=%b\n",p1,i,mCategories[i]);
            }
        }
        void recomputeNonCategories() {
            recomputeSensitivitySpecificity();
            recomputePriors();
            recomputePrevalence();
        }

        void recomputeSensitivitySpecificity() {
            double[] tp = new double[mNumAnnotators];
            double[] tn = new double[mNumAnnotators];
            double[] fp = new double[mNumAnnotators];
            double[] fn = new double[mNumAnnotators];

            // handled in math below
            // Arrays.fill(tp,mAlphaSensitivity);
            // Arrays.fill(fn,mBetaSensitivity);
            // Arrays.fill(tn,mAlphaSpecificity);
            // Arrays.fill(fp,mBetaSpecificity);

            for (int k = 0; k < mAnnotations.length; ++k) {
                if (mCategories[mItems[k]]) {
                    if (mAnnotations[k])
                        ++tp[mAnnotators[k]];
                    else
                        ++fn[mAnnotators[k]];
                } else {
                    if (mAnnotations[k])
                        ++fp[mAnnotators[k]];
                    else
                        ++tn[mAnnotators[k]];
                }
            }

            for (int j = 0; j < mNumAnnotators; ++j) {
                // System.out.printf("%d. tp=%8.2f fn=%8.2f tn=%8.2f fp=%8.2f spec=%5.3f sens=%5.3f\n",
                // j,tp[j],fn[j],tn[j],fp[j],
                // mSpecificities[j],mSensitivities[j]);
                mSensitivities[j]
                    = (mAlphaSensitivity + tp[j])
                    / (mAlphaSensitivity + tp[j]
                       + mBetaSensitivity + fn[j]);
                mSpecificities[j]
                    = (mAlphaSpecificity + tn[j])
                    / (mAlphaSpecificity + tn[j]
                       + mBetaSpecificity + fp[j]);
            }
        }

        void recomputePriors() {
            if (mHasFixedBetaPriors)
                return;
            OnlineNormalEstimator specificityPriorEstimator
                = new OnlineNormalEstimator();
            for (int j = 0; j < mNumAnnotators; ++j)
                specificityPriorEstimator.handle(mSpecificities[j]);
            double specificityMean = specificityPriorEstimator.mean();
            double specificityVariance = specificityPriorEstimator.variance();
            mAlphaSpecificity = BetaDistribution.alpha(specificityMean,
                                                       specificityVariance);
            mBetaSpecificity = BetaDistribution.beta(specificityMean,
                                                     specificityVariance);
            // ugly cut and paste
            OnlineNormalEstimator sensitivityPriorEstimator
                = new OnlineNormalEstimator();
            for (int j = 0; j < mNumAnnotators; ++j)
                sensitivityPriorEstimator.handle(mSensitivities[j]);
            double sensitivityMean = sensitivityPriorEstimator.mean();
            double sensitivityVariance = sensitivityPriorEstimator.variance();
            mAlphaSensitivity = BetaDistribution.alpha(sensitivityMean,
                                                       sensitivityVariance);
            mBetaSensitivity = BetaDistribution.beta(sensitivityMean,
                                                     sensitivityVariance);
            if (Double.isNaN(mBetaSensitivity)) {
                System.out.println("mean=" + sensitivityMean + " var=" + sensitivityVariance);
                for (int j = 0; j < mNumAnnotators; ++j)
                    System.out.printf("anno[%d].sens=%9.7f\n",j,mSensitivities[j]);
                System.exit(1);
            }

        }

        void recomputePrevalence() {
            int success = 0;
            for (int i = 0; i < mNumItems; ++i) {
                if (mCategories[i])
                    ++success;
            }
            mPi = success / (double) mNumItems;
        }

        static final double MIN_VALUE = Double.MIN_VALUE;
    }


    static int max(int[] xs) {
        int max = xs[0];
        for (int i = 1; i < xs.length; ++i)
            if (xs[i] > max)
                max = xs[i];
        return max;
    }

    static void assertPositiveSequence(String arrayName, int[] values) {
        Set<Integer> valSet = new TreeSet<Integer>();
        int max = 0;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] < 1) {
                String msg = "Values in array " + arrayName + " must be positive."
                    + " Found " + arrayName + "[" + i + "]=" + values[i];
                throw new IllegalArgumentException(msg);
            }
            if (values[i] > max)
                max = values[i];
            valSet.add(values[i]);
        }
        if (valSet.size() != max) {
            String msg = "Values in array " + arrayName + " must form a contiguous list."
                + " Found values=" + valSet;
            throw new IllegalArgumentException(msg);
        }
    }

    static void assertNonExtremeProbability(String name, double x) {
        if (Double.isNaN(x) || x <= 0.0 || x >= 1.0) {
            String msg = name + " must be between 0 and 1 exclusive."
                + " Found " + name + "=" + x;
            throw new IllegalArgumentException(msg);
        }
    }


    /**
     * A {@code SampleStatistics} collects sufficient statistics
     * online for calculating the sample mean and variance of the
     * variables being sampled.  
     *
     * @author Bob Carpenter
     */
    public static class SampleStatistics
        implements ObjectHandler<Sample> {
        final OnlineNormalEstimator mPiEstimator;
        final OnlineNormalEstimator[] mCategoryEstimators;
        final OnlineNormalEstimator mAlphaSpecificityEstimator;
        final OnlineNormalEstimator mBetaSpecificityEstimator;
        final OnlineNormalEstimator mAlphaSensitivityEstimator;
        final OnlineNormalEstimator mBetaSensitivityEstimator;
        final OnlineNormalEstimator[] mSpecificityEstimators;
        final OnlineNormalEstimator[] mSensitivityEstimators;
        /**
         * Construct a sample distribution for the specified number
         * of items and number of annotators.
         *
         * @param numItems Number of items.
         * @param numAnnotators Number of annotators.
         */
        public SampleStatistics(int numItems, int numAnnotators) {
            mPiEstimator = new OnlineNormalEstimator();
            mCategoryEstimators = onlineNormalEstimators(numItems);
            mAlphaSpecificityEstimator = new OnlineNormalEstimator();
            mBetaSpecificityEstimator = new OnlineNormalEstimator();
            mAlphaSensitivityEstimator = new OnlineNormalEstimator();
            mBetaSensitivityEstimator = new OnlineNormalEstimator();
            mSpecificityEstimators = onlineNormalEstimators(numAnnotators);
            mSensitivityEstimators = onlineNormalEstimators(numAnnotators);
        }
        /**
         * Add the specified sample's values to the sufficient
         * statistics.
         *
         * @param sample Sample to count.
         */
        public void handle(Sample sample) {
            mPiEstimator.handle(sample.pi());
            for (int k = 0; k < sample.categories().length; ++k) { // numItems too long
                double sampleCategory = sample.categories()[k] ? 1.0 : 0.0;
                mCategoryEstimators[k].handle(sampleCategory);
            }
            mAlphaSpecificityEstimator.handle(sample.alphaSpecificity());
            mBetaSpecificityEstimator.handle(sample.betaSpecificity());
            mAlphaSensitivityEstimator.handle(sample.alphaSensitivity());
            mBetaSpecificityEstimator.handle(sample.betaSensitivity());
            for (int j = 0; j < mSpecificityEstimators.length; ++j)
                mSpecificityEstimators[j].handle(sample.specificities()[j]);
            for (int j = 0; j < mSensitivityEstimators.length; ++j)
                mSensitivityEstimators[j].handle(sample.sensitivities()[j]);
        }
        /**
         * Return the number of samples added to this distribution.
         *
         */
        public long numSamples() {
            return mPiEstimator.numSamples();
        }
        public int numAnnotators() {
            return mSpecificityEstimators.length;
        }
        public int numItems() {
            return mCategoryEstimators.length;
        }
        public OnlineNormalEstimator piEstimator() {
            return mPiEstimator;
        }
        public int numCategories() {
            return mCategoryEstimators.length;
        }
        public OnlineNormalEstimator categoryEstimator(int i) {
            return mCategoryEstimators[i];
        }
        public OnlineNormalEstimator alphaSpecificityEstimator() {
            return mAlphaSpecificityEstimator;
        }
        public OnlineNormalEstimator betaSpecificityEstimator() {
            return mBetaSpecificityEstimator;
        }
        public OnlineNormalEstimator alphaSensitivityEstimator() {
            return mAlphaSensitivityEstimator;
        }
        public OnlineNormalEstimator betaSensitivityEstimator() {
            return mBetaSpecificityEstimator;
        }
        public OnlineNormalEstimator specificityEstimator(int j) {
            return mSpecificityEstimators[j];
        }
        public OnlineNormalEstimator sensitivityEstimator(int j) {
            return mSensitivityEstimators[j];
        }
        static OnlineNormalEstimator[] onlineNormalEstimators(int length) {
            OnlineNormalEstimator[] estimators = new OnlineNormalEstimator[length];
            for (int i = 0; i < length; ++i)
                estimators[i] = new OnlineNormalEstimator();
            return estimators;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Sample Estimates");
            sb.append("\n  num samples N=" + numSamples());
            sb.append("\n  num annotators J=" + numAnnotators());
            sb.append("\n  num items I=" + numItems());
            sb.append("\n  pi=" + piEstimator());
            sb.append("\n  alpha.0=" + alphaSpecificityEstimator());
            sb.append("\n  beta.0=" + betaSensitivityEstimator());
            sb.append("\n  mean.0="
                      + ( alphaSpecificityEstimator().mean()
                          / ( alphaSpecificityEstimator().mean()
                              + betaSpecificityEstimator().mean() ) ));
            sb.append("\n  alpha.1=" + alphaSensitivityEstimator());
            sb.append("\n  beta.1=" + betaSensitivityEstimator());
            sb.append("\n  mean.1="
                      + ( alphaSensitivityEstimator().mean()
                          / ( alphaSensitivityEstimator().mean()
                              + betaSensitivityEstimator().mean() ) ));
            for (int j = 0; j < numAnnotators(); ++j)
                sb.append("\n  specificity[" + j + "]="
                          + specificityEstimator(j));
            for (int j = 0; j < numAnnotators(); ++j)
                sb.append("\n  sensitivity[" + j + "]="
                          + sensitivityEstimator(j));
            for (int i = 0; i < numItems(); ++i)
                sb.append("\n  category[" + i + "]="
                          + categoryEstimator(i));
            return sb.toString();
        }
    }


    /**
     * A {@code Sample} instance represents a single Gibbs sample from
     * the collapsed sampler.  A sample determines values for all
     * model parameters, though only the ones that are not fixed as
     * data are included in the sample class.  
     *
     * <p>Samples are constructed by the sampler; there is no public
     * constructor.
     *
     * @author Bob Carpenter
     */
    public static class Sample {
        final double mPi;
        final boolean[] mCategories;
        final double mAlphaSpecificity;
        final double mBetaSpecificity;
        final double mAlphaSensitivity;
        final double mBetaSensitivity;
        final double[] mSpecificities;
        final double[] mSensitivities;
        Sample(double pi,
               boolean[] categories,
               double alphaSpecificity,
               double betaSpecificity,
               double alphaSensitivity,
               double betaSensitivity,
               double[] specificities,
               double[] sensitivities) {
            mPi = pi;
            mCategories = categories;
            mAlphaSpecificity = alphaSpecificity;
            mBetaSpecificity = betaSpecificity;
            mAlphaSensitivity = alphaSensitivity;
            mBetaSensitivity = betaSensitivity;
            mSpecificities = specificities;
            mSensitivities = sensitivities;
        }
        /**
         * Return the number of annotators in the model.
         *
         * @return The number of annotators.
         */
        public int numAnnotators() {
            return mSpecificities.length;
        }
        /**
         * Return the number of items being annotated.
         *
         * @return The number of items.
         */
        public int numItems() {
            return mCategories.length;
        }
        /**
         * Returns the value of the prevalence, &pi;.
         *
         * @return The prevalence value.
         */
        public double pi() {
            return mPi;
        }
        /**
         * Returns the category assignments in this sample, with
         * indexes corresponding to the item numbers.
         *
         * @return The categories in this sample.
         */
        public boolean[] categories() {
            return mCategories;
        }
        /**
         * Returns the value of the specificity parameter
         * &alpha;<sub>0</sub>.
         *
         * @return The prior specificity &alpha;<sub>0</sub>.
         */
        public double alphaSpecificity() {
            return mAlphaSpecificity;
        }
        /**
         * Returns the value of the specificity parameter
         * &beta;<sub>0</sub>.
         *
         * @return The prior specificity &beta;<sub>0</sub>.
         */
        public double betaSpecificity() {
            return mBetaSpecificity;
        }
        /**
         * Returns the value of the sensitivity parameter
         * &alpha;<sub>1</sub>.
         *
         * @return The prior sensitivity &alpha;<sub>1</sub>.
         */
        public double alphaSensitivity() {
            return mAlphaSensitivity;
        }
        /**
         * Returns the value of the sensitivity parameter
         * &beta;<sub>1</sub>.
         *
         * @return The prior sensitivity &beta;<sub>1</sub>.
         */
        public double betaSensitivity() {
            return mBetaSensitivity;
        }
        /**
         * Returns the specificity prior mean,
         * &alpha;<sub>0</sub>/(&alpha;<sub>0</sub>+&beta;<sub>0</sub>).
         *
         * @return The specificity prior mean.
         */
        public double specificityPriorMean() {
            return mAlphaSpecificity / (mAlphaSpecificity + mBetaSpecificity);
        }
        /**
         * Returns the specificity prior scale,
         * &alpha;<sub>0+&beta;<sub>0</sub>.
         *
         * @return The specificity prior scale.
         */
        public double specificityPriorScale() {
            return mAlphaSpecificity + mBetaSpecificity;
        }
        /**
         * Returns the sensitivity prior mean,
         * &alpha;<sub>1</sub>/(&alpha;<sub>1</sub>+&beta;<sub>0</sub>).
         *
         * @return The sensitivity prior mean.
         */
        public double sensitivityPriorMean() {
            return mAlphaSensitivity / (mAlphaSensitivity + mBetaSensitivity);
        }
        /**
         * Returns the sensitivity prior scale,
         * &alpha;<sub>1+&beta;<sub>1</sub>.
         *
         * @return The sensitivity prior scale.
         */
        public double sensitivityPriorScale() {
            return mAlphaSensitivity + mBetaSensitivity;
        }
        /**
         * Returns the array of specificities, &theta;<sub>0</sub>,
         * indexed by annotator.
         *
         * @return Annotator specificities.
         */
        public double[] specificities() {
            return mSpecificities;
        }
        /**
         * Returns the array of sensitivities, &theta;<sub>1</sub>,
         * indexed by annotator.
         *
         * @return Annotator sensitivities.
         */
        public double[] sensitivities() {
            return mSensitivities;
        }
    }






}