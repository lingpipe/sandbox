package stemmers.em;

import stem.Stemmer;

public interface Model extends Stemmer {

    public void increment(String word, int beginSuffixIndex, double delta);

    public String stem(String word);

    public double estimate(String word, int beginSuffixIndex);



}
