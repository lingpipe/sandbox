package stemmers.em2;

import stem.Stemmer;

import java.util.Iterator;

public interface Model extends Stemmer {

    public void train(String word, String stem, double weight);

    // inherited from Stemmer
    public String stem(String word);

    public StemScore firstBest(String word);
    
    // iterates n-best as scored object
    public StemScore[] nBest(String word);

    // returns compiled version of model
    public Model compile();

}
