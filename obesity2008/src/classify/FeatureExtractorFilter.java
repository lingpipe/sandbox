package classify;

import com.aliasi.util.FeatureExtractor;
import com.aliasi.util.ObjectToDoubleMap;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FeatureExtractorFilter<E> implements FeatureExtractor<E>, Serializable {

    private Set<String> mLegalFeatures;
    private FeatureExtractor<E> mBaseExtractor;

    public FeatureExtractorFilter() { }

    public FeatureExtractorFilter(Set<String> legalFeatures,
                                  FeatureExtractor<E> baseExtractor) {
        mLegalFeatures = legalFeatures;
        mBaseExtractor = baseExtractor;
    }

    public Map<String,? extends Number> features(E in) {
        Map<String,? extends Number> features 
            = mBaseExtractor.features(in);
        ObjectToDoubleMap<String> filteredFeatures
            = new ObjectToDoubleMap<String>();
        for (String feature : features.keySet()) {
            String lowerCaseFeature = feature.toLowerCase();
            if (mLegalFeatures.contains(lowerCaseFeature))
                filteredFeatures.increment(lowerCaseFeature,features.get(feature).doubleValue());
        }
        return filteredFeatures;
    }


}