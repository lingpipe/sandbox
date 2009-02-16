package stem;

import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenFilterTokenizer;

public class StemmingTokenizerFilter extends TokenFilterTokenizer {

    private final Stemmer mStemmer;

    public StemmingTokenizerFilter(Tokenizer tokenizer,
				   Stemmer stemmer) {
	super(tokenizer);
	mStemmer = stemmer;
    }

    public String filter(String token) {
	return mStemmer.stem(token);
    }

}