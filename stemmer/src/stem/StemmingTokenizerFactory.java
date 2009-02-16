package stem;


import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;


public class StemmingTokenizerFactory implements TokenizerFactory {

    private final TokenizerFactory mFactory;
    private final Stemmer mStemmer;

    public StemmingTokenizerFactory(TokenizerFactory factory,
				    Stemmer stemmer) {
	mFactory = factory;
	mStemmer = stemmer;
    }


    public Tokenizer tokenizer(char[] cs, int start, int length) {
	Tokenizer tokenizer = mFactory.tokenizer(cs,start,length);
	return new StemmingTokenizerFilter(tokenizer,mStemmer);
    }
}