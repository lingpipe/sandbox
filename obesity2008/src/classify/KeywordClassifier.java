package classify;

import com.aliasi.classify.Classifier;
import com.aliasi.classify.Classification;

public class KeywordClassifier implements Classifier<CharSequence,Classification> {

    private final String[] mKeywords;
    private final String[] mCategories;
    private final Classification mDefaultClassification;

    public KeywordClassifier(String[] keywords, String[] categories, String defaultCategory) {
	if (keywords.length != categories.length) {
	    String msg = "Keywords and categories must be same length."
		+ " Found keywords.length=" + keywords.length
		+ " categories.length=" + categories.length;
	}
	mKeywords = keywords;
	mCategories = categories;
	mDefaultClassification = new Classification(defaultCategory);
    }

    public Classification classify(CharSequence in) {
	String s = in.toString();
	for (int i = 0; i < mKeywords.length; ++i)
	    if (s.indexOf(mKeywords[i]) > 0)
		return new Classification(mCategories[i]);
	return mDefaultClassification;
    }

}