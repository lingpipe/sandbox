import com.aliasi.stats.AnnealingSchedule;

public class AdaptiveAnnealingSchedule extends AnnealingSchedule {
	private final double mInitialRate;
	private final int mNumProbes;
	private final double mExponentBase;
	private final double mPercentProbesBelowCurrent;
	private double mCurrentRate;
	private double mBestRate;
	private double mBestDelta;
	private double mLastError;
	private double mBestError = Double.POSITIVE_INFINITY;

	AdaptiveAnnealingSchedule(double initialLearningRate,
				  int numProbes,
				  double exponentBase,
				  double percentProbesBelowCurrent) {
	    verifyFinitePositive("initial learning rate",initialLearningRate);
	    if (numProbes < 1) {
		String msg = "Number of probes must be >= 1."
		    + " Found numProbes=" + numProbes;
		throw new IllegalArgumentException(msg);
	    }
	    if (Double.isNaN(exponentBase)
		|| exponentBase <= 0.0
		|| exponentBase >= 1.0) {
		String msg = "Exponent base must be between 0 and 1 exclusive."
		    + " Found exponentBase=" + exponentBase;
		throw new IllegalArgumentException(msg);
	    }
	    if (Double.isNaN(percentProbesBelowCurrent)
		|| percentProbesBelowCurrent < 0.0
		|| percentProbesBelowCurrent > 1.0) {
		String msg = "Percentage of probes below split must be "
		    + " between 0 and 1 inclusive."
		    + " Found percentProbesBelowCurrent=" + percentProbesBelowCurrent;
		throw new IllegalArgumentException(msg);
	    }

	    mInitialRate = initialLearningRate;
	    mNumProbes = numProbes;
	    mExponentBase = exponentBase;
	    mPercentProbesBelowCurrent = percentProbesBelowCurrent;
	    mCurrentRate = mInitialRate;
	    mBestDelta = Double.NEGATIVE_INFINITY;
	    mBestRate = mInitialRate;
	    mLastError = Double.POSITIVE_INFINITY;
	}
	public double learningRate(int epoch) {
	    double exponent = 
                (epoch % mNumProbes) // 0 to mNumProbes-1
		-  (mNumProbes * (1.0 - mPercentProbesBelowCurrent));
	    
            
	    return mCurrentRate * Math.pow(mExponentBase,exponent);
	}
	public boolean receivedError(int epoch, double rate, double error) {
	    if (error < mBestError) {
		mBestError = error;
	    } else {
		mCurrentRate *= 0.99;
		mBestRate *= 0.99;
	    }
	    double delta = error - mLastError;
	    mLastError = error;
	    if (delta > mBestDelta) {
		mBestRate = rate;
		mBestDelta = delta;
	    }
	    if (epoch > 0 && epoch % mNumProbes == 0) {
		mCurrentRate = mBestRate;
		mBestDelta = Double.NEGATIVE_INFINITY;
	    }
	    return true;
	}
	public String toString() {
	    return "AdaptiveAnnealingSchedule(initialRate=" + mInitialRate
		+ ", numProbes=" + mNumProbes
		+ ", base=" + mExponentBase
		+ ", split=" + mPercentProbesBelowCurrent
		+ ")  learningRate(epoch) is adaptive.";
	}


    /**
     * Return the adaptive annealing schedule with the specified
     * parameters.  The initial learning rate is the initial base
     * learning rate.  For the specified number of probes, rates above
     * and below the current base rate are explored and the best one
     * chosen.  The base and the percentage division of probes
     * determine the way in which probes around the current base rate
     * are explored.
     *
     * <blockqote><pre>
     * </pre></blockquote>
     *
     * @param initialLearningRate Initial base learning rate.
     * @param numProbes Number of probes between adaptations.
     * @param base Base of the exponent determining width and spacing
     * of probes around the current probe.  
     * @param percentProbesBelowCurrent Percentage of probes which are
     * less than the current rate.
     * @throws IllegalArgumentException If the initial learning rate is
     * not finite and positive, the number of probes is not positive,
     * the exponential base is not between 0 and 1 exclusive, or the
     * percentage of probes is not between 0 and 1 inclusive.
     */
    public static AnnealingSchedule adaptive(double initialLearningRate,
					     int numProbes,
					     double base,
					     double percentProbesBelowCurrent) {
	return new AdaptiveAnnealingSchedule(initialLearningRate,
					     numProbes,
					     base,
					     percentProbesBelowCurrent);
    }

    static void verifyFinitePositive(String varName, double val) {
        if (Double.isNaN(val) 
            || Double.isInfinite(val) 
            || val <= 0.0) {
            String msg = varName + " must be finite and positive."
                + " Found " + varName + "=" + val;
            throw new IllegalArgumentException(msg);
        }
    }

}