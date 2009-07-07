package com.aliasi.anno.cmd;

import com.aliasi.io.FileLineReader;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Exceptions;
import com.aliasi.util.Strings;

import com.aliasi.anno.BinomialAnnotationCollapsedGibbs;

import java.io.File;
import java.io.IOException;

import java.util.Properties;

/**
 * The {@code ByAnnoCmd} class provides a command-line executable
 * point estimator for a hierarchical annotation model. 
 *
 * <p>Full model documentation is available in the class documentation
 * for the class doing the estimation, {@link
 * BinomialAnnotationCollapsedGibbs}.
 *
 * <h4>Command-Line Arguments</h4>
 *
 * <table border="1" cellpadding="5">
 * <tr><th>Argument</th><th>Default</th><th>Description</th></tr>
 * <tr><td><code>annoTsv</code></td>
 *     <td><code>null</code></td>
 *     <td>annotation input data file</td></tr>
 * <tr><td><code>initialPi</code></td>
 *     <td><code>0.5</code></td>
 *     <td>initial estimate of prevalence</td></tr>
 * <tr><td><code>initialSpecificity</code></td>
 *     <td><code>0.8</code></td>
 *     <td>initial estimate of annotator specificity</td></tr>
 * <tr><td><code>initialSensitivity</code></td>
 *     <td><code>0.8</code></td>
 *     <td>initial estimate of annotator sensitivity</td></tr>
 * <tr><td><code>numSamples</code></td>
 *     <td><code>500</code></td>
 *     <td>number of Gibbs samples to generate</td></tr>
 * <tr><td><code>goldTsv</code></td>
 *     <td><code>null</code></td>
 *     <td>gold-standard input file (optional)</td></tr>
 * <tr><td><code>fixedAlpha0</code></td>
 *     <td><code></code></td>
 *     <td>fixed value for alpha prior parameter for specificity (optional<sup>*</sup>)</td></tr>
 * <tr><td><code>fixedBeta0</code></td>
 *     <td><code></code></td>
 *     <td>fixed value for beta prior parameter for specificity (optional<sup>*</sup>)</td></tr>
 * <tr><td><code>fixedAlpha1</code></td>
 *     <td><code></code></td>
 *     <td>fixed value for alpha prior parameter for sensitivity (optional<sup>*</sup>)</td></tr>
 * <tr><td><code>fixedBeta1</code></td>
 *     <td><code></code></td>
 *     <td>fixed value for beta prior parameter for sensitivity (optional<sup>*</sup>)</td></tr>
 * <tr><td><code>logLevel</code></td>
 *     <td><code>DEBUG</code></td>
 *     <td>log level for program output</td></tr>
 * <tr><td><code>logFile</code></td>
 *     <td><code>null</code></td>
 *     <td>file to which output is written (optional)</td></tr>
 * </table>
 * <sup>*</sup>&nbsp;Either none or all of the prior parameters must be fixed.
 *
 * <h4>Annotation File Format</h4>
 *
 * Input data is line-oriented, with each line representing the
 * annotation of a single item by a single annotator.  Each line is
 * of the form:
 *
 * <blockquote>
 * {@code <item>\t<annotator>\t<annotation>}
 * </blockquote>
 *
 * where the items should be numbered from 1:I, the annotators should
 * be numbered 1:J, and the annotations should be in {0,1}.   There is
 * no final newline on the last line.
 *  
 * <h4>Gold Standard File Format</h4>
 *
 * The gold-standard data file consists of one line per item (for
 * a total of I lines), where the i-th line provides the gold-standard
 * value of the annotation for the i-th item (in {0,1}).  
 * 
 * <h4>Log Levels</h4>
 *
 * The log level determines the verbosity of outputs.   Output will
 * always be written to standard out, though can be suppressed with
 * log level {@code NONE}, which suppresses all messages.  
 *
 * @author Bob Carpenter
 */
public class ByAnnoCmd extends AbstractCommand {

    /**
     * Main method to run the command from the command-line.  The
     * arguments are detailed in the class documentation above.
     *
     * @param args Command-line arguments.
     * @throws IOException If there is an I/O error reading any of the
     * input files.
     */
    public static void main(String[] args) throws IOException {
        new ByAnnoCmd(args).run();
    }

    final File mAnnoTsvFile;
    final double mInitialPi;
    final double mInitialSpecificity;
    final double mInitialSensitivity;
    final int mNumSamples;
    final File mGoldTsvFile;
    final boolean mHasFixedBetaPriors;
    final double mFixedAlpha0;
    final double mFixedBeta0;
    final double mFixedAlpha1;
    final double mFixedBeta1;
    final Reporter mReporter;
    final LogLevel mLogLevel;
    final File mLogFile;

    static final String ANNO_TSV_FIELD = "annoTsv";
    static final String INITIAL_PI_FIELD = "initialPi";
    static final String INITIAL_SPECIFICITY_FIELD = "initialSpecificity";
    static final String INITIAL_SENSITIVITY_FIELD = "initialSensitivity";
    static final String NUM_SAMPLES_FIELD = "numSamples";
    static final String GOLD_TSV_FIELD = "goldTsv";
    static final String FIXED_ALPHA_0_FIELD = "fixedAlpha0";
    static final String FIXED_BETA_0_FIELD = "fixedBeta0";
    static final String FIXED_ALPHA_1_FIELD = "fixedAlpha1";
    static final String FIXED_BETA_1_FIELD = "fixedBeta1";
    static final String LOG_LEVEL_FIELD = "logLevel";
    static final String LOG_FILE_FIELD = "logFile";

    static final Properties DEFAULT_PROPERTIES = new Properties();
    static {
        DEFAULT_PROPERTIES.setProperty(INITIAL_PI_FIELD, "0.5");
        DEFAULT_PROPERTIES.setProperty(INITIAL_SPECIFICITY_FIELD,"0.8");
        DEFAULT_PROPERTIES.setProperty(INITIAL_SENSITIVITY_FIELD,"0.8");
        DEFAULT_PROPERTIES.setProperty(NUM_SAMPLES_FIELD, "500");
        DEFAULT_PROPERTIES.setProperty(FIXED_ALPHA_0_FIELD,"-1");
        DEFAULT_PROPERTIES.setProperty(FIXED_BETA_0_FIELD,"-1");
        DEFAULT_PROPERTIES.setProperty(FIXED_ALPHA_1_FIELD,"-1");
        DEFAULT_PROPERTIES.setProperty(FIXED_BETA_1_FIELD,"-1");
        DEFAULT_PROPERTIES.setProperty(LOG_LEVEL_FIELD,"ERROR");
    }

    ByAnnoCmd(String[] args) throws IOException {
        super(args,DEFAULT_PROPERTIES);

        try {
            mLogLevel = LogLevel.valueOf(getExistingArgument(LOG_LEVEL_FIELD));
        } catch (IllegalArgumentException e) {
            String msg = "Unknown log level. " 
                + LOG_LEVEL_FIELD + "=" + getExistingArgument(LOG_LEVEL_FIELD);
            throw new IllegalArgumentException(msg);
        }
        System.out.println("Log Level=" + mLogLevel);

        mLogFile = hasArgument(LOG_FILE_FIELD)
            ? getArgumentCreatableFile(LOG_FILE_FIELD)
            : null;
        System.out.println("Log file=" + mLogFile);

        mReporter
            = mLogFile == null
            ? Reporters.stdOut()
            : Reporters.tee(Reporters.stdOut(),
                            Reporters.file(mLogFile,Strings.UTF8));

        mAnnoTsvFile = getArgumentExistingNormalFile(ANNO_TSV_FIELD);
        System.out.println(ANNO_TSV_FIELD + "=" + mAnnoTsvFile);

        mInitialPi = getArgumentDouble(INITIAL_PI_FIELD);
        assertNonExtremeProbability(INITIAL_PI_FIELD,mInitialPi);
        System.out.println("Initial Pi=" + mInitialPi);

        mInitialSpecificity = getArgumentDouble(INITIAL_SPECIFICITY_FIELD);
        assertNonExtremeProbability(INITIAL_SPECIFICITY_FIELD,mInitialSpecificity);
        System.out.println("Initial Specificity=" + mInitialSpecificity);

        mInitialSensitivity = getArgumentDouble(INITIAL_SENSITIVITY_FIELD);
        assertNonExtremeProbability(INITIAL_SENSITIVITY_FIELD,mInitialSensitivity);
        System.out.println("Initial Sensitivity=" + mInitialSensitivity);

        mNumSamples = getArgumentInt(NUM_SAMPLES_FIELD);
        if (mNumSamples < 1) {
            String msg = "Require positive number of samples."
                + " Found " + NUM_SAMPLES_FIELD + "=" + mNumSamples;
            throw new IllegalArgumentException(msg);
        }
        System.out.println("# of Samples=" + mNumSamples);

        mGoldTsvFile = hasArgument(GOLD_TSV_FIELD) 
            ? getArgumentExistingNormalFile(GOLD_TSV_FIELD)
            : null;
        System.out.println("Gold TSV File=" + mGoldTsvFile);

        mFixedAlpha0 = getArgumentDouble(FIXED_ALPHA_0_FIELD);
        mFixedBeta0 = getArgumentDouble(FIXED_BETA_0_FIELD);
        mFixedAlpha1 = getArgumentDouble(FIXED_ALPHA_1_FIELD);
        mFixedBeta1 = getArgumentDouble(FIXED_BETA_1_FIELD);
        mHasFixedBetaPriors = (mFixedAlpha0 != -1);
        if (mHasFixedBetaPriors) {
            Exceptions.finiteNonNegative(FIXED_ALPHA_0_FIELD,mFixedAlpha0);
            Exceptions.finiteNonNegative(FIXED_BETA_0_FIELD,mFixedBeta0);
            Exceptions.finiteNonNegative(FIXED_ALPHA_1_FIELD,mFixedAlpha1);
            Exceptions.finiteNonNegative(FIXED_BETA_1_FIELD,mFixedBeta1);
            System.out.println("Fixing Beta Priors.");
            System.out.println("Fixed alpha.0=" + mFixedAlpha0);
            System.out.println("Fixed beta.0=" + mFixedBeta0);
            System.out.println("Fixed alpha.1=" + mFixedAlpha1);
            System.out.println("Fixed beta.1=" + mFixedBeta1);
        } else {
            System.out.println("Estimating Beta Priors.");
        }
        
    }
     
    /**
     * Top-level method to run the command; cannot be called by
     * users as there is no way to construct an instance of this
     * class other than through the {@link main(String[])} method.
     */
    public void run() {
        try {
            runWithExceptions();
        } catch (Throwable t) {
            System.out.println("Throwable=" + t);
            t.printStackTrace(System.out);
        }
    }

    void runWithExceptions() throws IOException {

        System.out.println("Reading lines from file");
        String[] lines = FileLineReader.readLineArray(mAnnoTsvFile,"ASCII");
        System.out.println("     #lines=" + lines.length);

        System.out.println("Parsing raw data");
        boolean[] annotations = new boolean[lines.length];
        int[] annotators = new int[lines.length];
        int[] items = new int[lines.length];
        for (int k = 0; k < lines.length; ++k) {
            String[] fields = lines[k].split("\t");
            items[k] = Integer.valueOf(fields[0]);
            annotators[k] = Integer.valueOf(fields[1]);
            annotations[k] = "1".equals(fields[2]);
        }

        System.out.println("Constructing sampler");
        BinomialAnnotationCollapsedGibbs sampler
            = mHasFixedBetaPriors
            ? new BinomialAnnotationCollapsedGibbs(annotations,
                                             annotators,
                                             items,
                                             mInitialPi,
                                             mInitialSpecificity,
                                             mInitialSensitivity,
                                             mFixedAlpha0,
                                             mFixedBeta0,
                                             mFixedAlpha1,
                                             mFixedBeta1)
            : new BinomialAnnotationCollapsedGibbs(annotations,
                                             annotators,
                                             items,
                                             mInitialPi,
                                             mInitialSpecificity,
                                             mInitialSensitivity);
            
        int count = 0;
        long startTime = System.currentTimeMillis();
        BinomialAnnotationCollapsedGibbs.SampleDistribution sampleDistribution
            = new BinomialAnnotationCollapsedGibbs
            .SampleDistribution(sampler.numItems(),
                                sampler.numAnnotators());
        for (BinomialAnnotationCollapsedGibbs.Sample sample : sampler) {
            long elapsedTimeMs = System.currentTimeMillis() - startTime;
            System.out.printf("%10s %7d  pi=%4.3f   phi0=%4.3f gamma0=%4.3f   phi1=%4.3f gamma1=%4.3f\n",
                              Strings.msToString(elapsedTimeMs),
                              count++,
                              sample.pi(),
                              sample.specificityPriorMean(),
                              sample.specificityPriorScale(),
                              sample.sensitivityPriorMean(),
                              sample.sensitivityPriorScale());

            sampleDistribution.handle(sample);
            if (sampleDistribution.numSamples() == mNumSamples)
                break;
        }

        if (mReporter.isDebugEnabled())
            System.out.println(sampleDistribution);

        if (mGoldTsvFile == null) return;

        String[] linesGold = FileLineReader.readLineArray(mGoldTsvFile,"ASCII");
        boolean[] goldCategories = new boolean[linesGold.length+1];
        for (int i = 0; i < linesGold.length; ++i)
            goldCategories[i+1] = "1".equals(linesGold[i]);
        
        int tp = 0;
        int tn = 0;
        int p = 0;
        int n = 0;
        for (int i = 0; i < linesGold.length; ++i) {
            double sampleEstimateP1 = sampleDistribution.categoryEstimator(i).mean();
            if (goldCategories[i]) {
                ++p;
                if (sampleEstimateP1 >= 0.5)
                    ++tp;
            } else {
                ++n;
                if (sampleEstimateP1 < 0.5)
                    ++tn;
            }
        }
        System.out.printf("Spec=%4.3f; neg count=%d errors=%d\n",
                          (tn / (double) n), n, n-tn);
        System.out.printf("Sens=%4.3f; pos count=%d errors=%d\n",
                          (tp / (double) p), p, p-tp);
    }


    // cut and paste from BinomialAnnotationCollapsedGibbs
    static void assertNonExtremeProbability(String name, double x) {
        if (Double.isNaN(x) || x <= 0.0 || x >= 1.0) {
            String msg = name + " must be between 0 and 1 exclusive."
                + " Found " + name + "=" + x;
            throw new IllegalArgumentException(msg);
        }
    }


}