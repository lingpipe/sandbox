package com.aliasi.anno;

import com.aliasi.corpus.ObjectHandler;

import com.aliasi.stats.OnlineNormalEstimator;

import com.aliasi.util.Exceptions;
import com.aliasi.util.Iterators;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

public class CollapsedMultinomialByAnno
    implements Iterable<CollapsedMultinomialByAnno.Sample> {

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

    public CollapsedMultinomialByAnno(boolean[] annotations,
                                      int[] annotators,
                                      int[] items,
                                      double initialPi,
                                      double initialSpecificity,
                                      double initialSensitivity) {
        this(annotations,annotators,items,
             initialPi,initialSpecificity,initialSensitivity,
             false,Double.NaN,Double.NaN,Double.NaN,Double.NaN);

    }


    public CollapsedMultinomialByAnno(boolean[] annotations,
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

    private CollapsedMultinomialByAnno(boolean[] annotations,
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

    public int numItems() {
        return mNumItems;
    }

    public int numAnnotators() {
        return mNumAnnotators;
    }

    // returns a fresh Markov Chain; thread safe
    public synchronized Iterator<Sample> iterator() {
        return new SampleIterator();
    }

    static int max(int[] xs) {
        int max = xs[0];
        for (int i = 1; i < xs.length; ++i)
            if (xs[i] > max)
                max = xs[i];
        return max;
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


    static void assertNonExtremeProbability(String name, double x) {
        if (Double.isNaN(x) || x <= 0.0 || x >= 1.0) {
            String msg = name + " must be between 0 and 1 exclusive."
                + " Found " + name + "=" + x;
            throw new IllegalArgumentException(msg);
        }
    }


    public static class SampleDistribution
        implements ObjectHandler<Sample> {
        final OnlineNormalEstimator mPiEstimator;
        final OnlineNormalEstimator[] mCategoryEstimators;
        final OnlineNormalEstimator mAlphaSpecificityEstimator;
        final OnlineNormalEstimator mBetaSpecificityEstimator;
        final OnlineNormalEstimator mAlphaSensitivityEstimator;
        final OnlineNormalEstimator mBetaSensitivityEstimator;
        final OnlineNormalEstimator[] mSpecificityEstimators;
        final OnlineNormalEstimator[] mSensitivityEstimators;
        public SampleDistribution(int numItems, int numAnnotators) {
            mPiEstimator = new OnlineNormalEstimator();
            mCategoryEstimators = onlineNormalEstimators(numItems);
            mAlphaSpecificityEstimator = new OnlineNormalEstimator();
            mBetaSpecificityEstimator = new OnlineNormalEstimator();
            mAlphaSensitivityEstimator = new OnlineNormalEstimator();
            mBetaSensitivityEstimator = new OnlineNormalEstimator();
            mSpecificityEstimators = onlineNormalEstimators(numAnnotators);
            mSensitivityEstimators = onlineNormalEstimators(numAnnotators);
        }
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
        public int numAnnotators() {
            return mSpecificities.length;
        }
        public int numItems() {
            return mCategories.length;
        }
        public double pi() {
            return mPi;
        }
        public boolean[] categories() {
            return mCategories;
        }
        public double alphaSpecificity() {
            return mAlphaSpecificity;
        }
        public double betaSpecificity() {
            return mBetaSpecificity;
        }
        public double alphaSensitivity() {
            return mAlphaSensitivity;
        }
        public double betaSensitivity() {
            return mBetaSensitivity;
        }
        public double specificityPriorMean() {
            return mAlphaSpecificity / (mAlphaSpecificity + mBetaSpecificity);
        }
        public double specificityPriorScale() {
            return mAlphaSpecificity + mBetaSpecificity;
        }
        public double sensitivityPriorMean() {
            return mAlphaSensitivity / (mAlphaSensitivity + mBetaSensitivity);
        }
        public double sensitivityPriorScale() {
            return mAlphaSensitivity + mBetaSensitivity;
        }
        public double[] sensitivities() {
            return mSensitivities;
        }
        public double[] specificities() {
            return mSpecificities;
        }
    }






}