package stemmers.em;

public abstract class AbstractModel implements Model {

    public String stem(String word) {
        double max = Double.NEGATIVE_INFINITY;
        int index = -1;
        for (int i = 1; i <= word.length(); ++i) {
            double estimate = estimate(word,i);
            if (estimate > max) {
                max = estimate;
                index = i;
            }
        }
        return index > 0 
            ? word.substring(0,index)
            : null;
    }

}

