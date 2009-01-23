package classify;

import com.aliasi.stats.AnnealingSchedule;



import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.*;
import javax.swing.border.*;


import com.aliasi.util.Strings;

public class GuiRate extends AnnealingSchedule {

    private final JFrame mTopFrame;
    private final JPanel mContentPane;
    private final JLabel mErrorLabel;
    private final JLabel mTimeLabel;
    private final JLabel mVelocityLabel;
    private final JTextField mRateField;

    private double mRate;

    private final long mStartTime=System.currentTimeMillis();


    private double mLastError = Double.MAX_VALUE;

    public GuiRate(double initialRate) {
	mRate = initialRate;
	mRateField = new JTextField(Double.toString(initialRate));

	mTimeLabel = new JLabel(Strings.msToString(System.currentTimeMillis() - mStartTime));

	mContentPane = new JPanel(new GridBagLayout());

	mContentPane.add(mTimeLabel,
			 new GridBagConstraints(0,0, 1,1, 0.0,0.0,
						GridBagConstraints.EAST,
						GridBagConstraints.NONE,
						new Insets(5,5,5,5),
						0,0));

	mContentPane.add(new JLabel("Rate"),
			 new GridBagConstraints(0,1, 1,1, 0.0,0.0,
						GridBagConstraints.WEST,
						GridBagConstraints.NONE,
						new Insets(5,5,5,5),
						0,0));
	mContentPane.add(mRateField,
			 new GridBagConstraints(1,1, 1,1, 1.0,1.0,
						GridBagConstraints.WEST,
						GridBagConstraints.BOTH,
						new Insets(5,5,5,5),
						0,0));


	mContentPane.add(new JLabel("Error"),
			 new GridBagConstraints(0,2, 1,1, 0.0,0.0,
						GridBagConstraints.WEST,
						GridBagConstraints.NONE,
						new Insets(5,5,5,5),
						0,0));

	mErrorLabel = new JLabel("infinity");
	mContentPane.add(mErrorLabel,
			 new GridBagConstraints(1,2, 1,1, 1.0,1.0,
						GridBagConstraints.WEST,
						GridBagConstraints.BOTH,
						new Insets(5,5,5,5),
						0,0));

	mContentPane.add(new JLabel("Velocity"),
			 new GridBagConstraints(0,3, 1,1, 0.0,0.0,
						GridBagConstraints.WEST,
						GridBagConstraints.NONE,
						new Insets(5,5,5,5),
						0,0));
	mVelocityLabel = new JLabel("0.0");
	mContentPane.add(mVelocityLabel,
			 new GridBagConstraints(1,3, 1,1, 1.0,1.0,
						GridBagConstraints.WEST,
						GridBagConstraints.BOTH,
						new Insets(5,5,5,5),
						0,0));
	


	mTopFrame = new JFrame("Manual Annealing");
	mTopFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	mTopFrame.setContentPane(mContentPane);
	mTopFrame.pack();
	mTopFrame.setResizable(true);
	mTopFrame.setVisible(true);
	
	
    }



    public synchronized double learningRate(int epoch) {
	try {
	    double newRate = Double.parseDouble(mRateField.getText());
	    mRate = newRate;
	} catch (Exception e) {
	    // eat it
	}
	return mRate;
    }

    public boolean receivedError(int epoch, double rate, double error) {
	mErrorLabel.setText(Double.toString(error));
	mTimeLabel.setText(Strings.msToString(System.currentTimeMillis()-mStartTime));
	mVelocityLabel.setText(Double.toString(-(error-mLastError)));
	mLastError = error;
	return true;
    }

}