package stem;

import com.aliasi.tokenizer.TokenizerFactory;

/**
 * A <code>Stemmer</code> maps a word to its stem form.
 */
public interface Stemmer {

    /**
     * Return the stem of the specified word.
     *
     * @param word Input word.
     * @return Stem of the specified word.
     */
    public String stem(String word);
    
}