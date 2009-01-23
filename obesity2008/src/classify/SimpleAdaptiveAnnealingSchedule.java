import com.aliasi.stats.AnnealingSchedule;

public  class  SimpleAdaptiveAnnealingSchedule extends AnnealingSchedule {
    double mRate = 0.001;
    double mBestError = Double.POSITIVE_INFINITY;
    public double learningRate(int epoch) {
	return mRate;
    }
    public boolean receivedError(int epoch, double rate, double error) {
	if (rate < mBestError) {
	    mBestError = rate;
	    mRate /= 0.99975; // .9999 OK
	} else {
	    mRate *= 0.9995;  // 0.999 OK
	}
	return true;
    }
}


