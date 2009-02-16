package util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import java.io.Serializable;

public class StringToDouble extends HashMap implements Serializable {

    private double mTotal = 0.0;

    public void set(String in, double val) {
	validate(val);
	double currentVal = getDouble(in);
	mTotal += val - currentVal;
	doSet(in,val);
    }

    public void increment(String in, double val) {
	double currentVal = getDouble(in);
	double newVal = val+currentVal;
	validate(newVal);

	mTotal += val;
	doSet(in,val+currentVal);
    }

    public double entropy() {
	double entropy = 0.0;
	Iterator it = keySet().iterator();
	while (it.hasNext()) {
	    String s = it.next().toString();
	    double p = getDouble(s);
	    entropy += p * com.aliasi.util.Math.log2(p);
	}
	return entropy;
    }

    void validate(double val) {
	if (Double.isInfinite(val) 
	    || Double.isNaN(val)
	    || val < 0.0) {
	    String msg = "Value must be positive and finite."
		+ " Found val=" + val;
	    throw new IllegalArgumentException(msg);
	}
    }

    double totalCount() {
	return mTotal;
    }

    void doSet(String in, double val) {
	super.put(in,new Double(val));
    }

    public double maxLikelihood(String in) {
	if (mTotal == 0.0) {
	    String msg = "Need total count > 0 for max likelihood.";
	    throw new IllegalStateException(msg);
	}
	return getDouble(in) / mTotal;
    }

    public String[] stringsByFrequency() {
	String[] keys = (String[]) keySet().toArray(new String[0]);
	Arrays.sort(keys,KEY_COMPARATOR);
	for (int i = 1; i < keys.length; ++i)
	    if (getDouble(keys[i-1]) < getDouble(keys[i])) {
		String msg = "Error. getDouble(keys[" + (i-1) + "])=" 
		    + getDouble(keys[i-1])
		    + " < getDouble(keys[" + i + "])="
		    + getDouble(keys[i]);
		System.out.println(msg);
	    }
	return keys;
    }

    /*
    public String[] stringsByFrequency() {
	HashSet positiveEntrySet = new HashSet();
	Iterator it = entrySet().iterator();
	while (it.hasNext()) {
	    Map.Entry entry = (Map.Entry) it.next();
	    double val = ((Double)entry.getValue()).doubleValue();
	    if (val > 1.0) positiveEntrySet.add(entry);
	}
	Map.Entry[] entries 
	    = (Map.Entry[]) positiveEntrySet.toArray(new Map.Entry[0]);

	Arrays.sort(entries,SCORE_COMPARATOR);
	String[] result = new String[entries.length];
	for (int i = 0; i < entries.length; ++i) {
	    Map.Entry entry = (Map.Entry) entries[i];
	    String word = entry.getKey().toString();
	    double val = ((Double)entry.getValue()).doubleValue();
	    result[i] = word;
	}
	return result;
    }
    */

    public double getDouble(String in) {
	Double val = (Double) super.get(in);
	return val == null ? 0.0 : val.doubleValue();
    }

    Comparator KEY_COMPARATOR
	= new Comparator() {
		public int compare(Object o1, Object o2) {
		    String s1 = o1.toString();
		    String s2 = o2.toString();
		    double val1 = getDouble(s1);
		    double val2 = getDouble(s2);
		    if (val1 > val2) return -1;
		    if (val1 < val2) return 1;
		    return 0;
		};
	    };

    // sorts to increasing order
    static Comparator SCORE_COMPARATOR
	= new Comparator() {
		public int compare(Object o1, Object o2) {
		    Map.Entry entry1 = (Map.Entry) o1;
		    Map.Entry entry2 = (Map.Entry) o2;
		    double val1 = ((Double) entry1.getValue()).doubleValue();
		    double val2 = ((Double) entry2.getValue()).doubleValue();
		    if (val1 > val2) return -1;
		    if (val1 < val2) return 1;
		    return 0;
		    /*
		    String string1 = entry1.getKey().toString();
		    String string2 = entry2.getKey().toString();
		    return string1.compareTo(string2);
		    */
		}
	    };
    



}