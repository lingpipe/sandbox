package classify;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classifier;

import com.aliasi.corpus.ClassificationHandler;
import com.aliasi.util.ObjectToCounterMap;

import java.util.List;

public class PickMostLikelyCategoryClassifier 
    implements Classifier<CharSequence,Classification>,
               ClassificationHandler<CharSequence,Classification>  {

    ObjectToCounterMap<String> mCategories = new ObjectToCounterMap();
    
    public Classification classify(CharSequence object) {
        List<String> categoryList = mCategories.keysOrderedByCountList();
        return new Classification(categoryList.get(0));
    }
    
    public void handle(CharSequence str, Classification classification ) {
        mCategories.increment(classification.bestCategory());
    }


}
