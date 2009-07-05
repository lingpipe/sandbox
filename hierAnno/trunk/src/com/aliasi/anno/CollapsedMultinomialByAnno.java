package com.aliasi.anno;

import com.aliasi.stats.OnlineNormalEstimator;

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

    public CollapsedMultinomialByAnno(boolean[] annotations,
                                      int[] annotators,
                                      int[] items,
                                      double initialPi,
                                      double initialSpecificity,
                                      double initialSensitivity) {
        mAnnotations = annotations;
        mAnnotators = annotators;
        mNumAnnotators = max(annotators)+1;
        mItems = items;
        mNumItems = max(items)+1;
        mInitialPi = initialPi;
        mInitialSpecificity = initialSpecificity;
        mInitialSensitivity = initialSensitivity;
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
        double mAlphaSpecificity = 1.0;
        double mBetaSpecificity = 1.0;
        double mAlphaSensitivity = 1.0;
        double mBetaSensitivity = 1.0;

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
            return new Sample(mNumAnnotators,
                              mNumItems,
                              mPi,
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
            for (int i = 0; i < mNumItems; ++i)
                mCategories[i]
                    = mRandom.nextDouble()
                    < (mCategories1[i] / (mCategories1[i] + mCategories0[i]));
        }
        void recomputeNonCategories() {
            recomputeSensitivitySpecificity();
            recomputePriors();
            recomputePrevalence();
        }

        void recomputeSensitivitySpecificity() {
            double[] mTP = new double[mNumAnnotators];
            double[] mTN = new double[mNumAnnotators];
            double[] mFP = new double[mNumAnnotators];
            double[] mFN = new double[mNumAnnotators];

            for (int k = 0; k < mAnnotations.length; ++k) {
                if (mCategories[mItems[k]]) {
                    if (mAnnotations[k])
                        ++mTP[mAnnotators[k]];
                    else
                        ++mFN[mAnnotators[k]];
                } else {
                    if (mAnnotations[k])
                        ++mFP[mAnnotators[k]];
                    else
                        ++mTN[mAnnotators[k]];
                }
            }

            for (int j = 0; j < mNumAnnotators; ++j) {
                mSensitivities[j]
                    = (mAlphaSensitivity + mTP[j])
                    / (mAlphaSensitivity + mTP[j]
                       + mBetaSensitivity + mFN[j]);
                mSpecificities[j]
                    = (mAlphaSpecificity + mTN[j])
                    / (mAlphaSpecificity + mTN[j]
                       + mBetaSpecificity + mFP[j]);
            }
        }

        void recomputePriors() {
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


    public static class Sample {
        final int mNumAnnotators;
        final int mNumItems;
        final double mPi;
        final boolean[] mCategories;
        final double mAlphaSpecificity;
        final double mBetaSpecificity;
        final double mAlphaSensitivity;
        final double mBetaSensitivity;
        final double[] mSpecificities;
        final double[] mSensitivities;
        Sample(int numAnnotators,
               int numItems,
               double pi,
               boolean[] categories,
               double alphaSpecificity,
               double betaSpecificity,
               double alphaSensitivity,
               double betaSensitivity,
               double[] specificities,
               double[] sensitivities) {
            mNumAnnotators = numAnnotators;
            mNumItems = numItems;
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
            return mNumAnnotators;
        }
        public int numItems() {
            return mNumItems;
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