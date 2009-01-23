package patient;

import com.aliasi.classify.Classification;
import com.aliasi.classify.XValidatingClassificationCorpus;

import com.aliasi.util.Counter;
import com.aliasi.util.Files;
import com.aliasi.util.Pair;
import com.aliasi.util.ObjectToCounterMap;

import java.io.File;
import java.io.IOException;

import java.util.TreeSet;
import java.util.Map;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Corpora {

    private final Map<String,String> mIdToTextRecord;
    private final Map<Pair<String,String>,Map<String,String>> mSourceXDiseaseToIdToJudgement;
    private final boolean mNormalize;

    public Corpora(File[] patientFiles,
		   File[] annotationFiles,
                   boolean normalize) throws SAXException, IOException {

        mNormalize = normalize;

        RecordSetHandler recordSetHandler = new RecordSetHandler();
	for (File patientFile : patientFiles)
	    handleXml(patientFile,recordSetHandler);
	mIdToTextRecord = recordSetHandler.mIdToTextRecord;
	
        AnnotatedDataHandler annotatedDataHandler = new AnnotatedDataHandler();
	for (File annotationFile : annotationFiles)
	    handleXml(annotationFile, annotatedDataHandler);
	mSourceXDiseaseToIdToJudgement = annotatedDataHandler.mSourceXDiseaseToIdToJudgement;
    }

    public XValidatingClassificationCorpus<CharSequence> get(String source, String disease, int numFolds) {
        XValidatingClassificationCorpus<CharSequence> corpus
            = new XValidatingClassificationCorpus<CharSequence>(numFolds);
	Map<String,String> idToJudgement = mSourceXDiseaseToIdToJudgement.get(new Pair(source,disease));
	for (String id : idToJudgement.keySet()) {
	    String judgement = idToJudgement.get(id);
            String text = mIdToTextRecord.get(id);
            boolean isIntuitive = "intuitive".equals(source);
            String normalizedJudgement 
                = mNormalize 
                ? (isIntuitive
                   ? normalizeIntuitive(judgement)
                   : normalizeTextual(judgement) )
                : judgement;
            // System.out.print(normalizedJudgement);
            Classification c = new Classification(normalizedJudgement);
            corpus.handle(text,c);
	}
        // System.out.println();
	return corpus;
    }

    public Set<String> sourceSet() {
        Set<String> sourceSet = new TreeSet<String>();
        for (Pair<String,String> sourceDisease : mSourceXDiseaseToIdToJudgement.keySet()) {
            sourceSet.add(sourceDisease.a());
        }
        return sourceSet;
    }

    public Set<String> diseaseSet() {
        Set<String> diseaseSet = new TreeSet<String>();
        for (Pair<String,String> sourceDisease : mSourceXDiseaseToIdToJudgement.keySet())
            diseaseSet.add(sourceDisease.b());
        return diseaseSet;
    }

    

    public void reportCategoryCount(String source, String disease) {
        Map<String,String> judgementMap 
            = mSourceXDiseaseToIdToJudgement.get(new Pair<String,String>(source,disease));
        ObjectToCounterMap<String> categoryCount = new ObjectToCounterMap<String>();
        for (String judgement : judgementMap.values())
            categoryCount.increment(judgement);
        
        double total = 0;
        for (Counter counter : categoryCount.values())
            total += counter.doubleValue();

        System.out.printf("  %10s  %9s %7s\n",
                          "Outcome", "Count", "Prob");
        for (String key : categoryCount.keySet()) {
            int count = categoryCount.getCount(key);
            double pKey = count / total;
            System.out.printf("  %10s  %9d %7.3f\n",
                              key,count,pKey);
        }
    }

    public void reportJudgements(String source, String disease) {
        Map<String,String> judgementMap 
            = mSourceXDiseaseToIdToJudgement.get(new Pair<String,String>(source,disease));
	for (String id : new java.util.TreeSet<String>(judgementMap.keySet())) {
	    String value = judgementMap.get(id);
	    System.out.println(id + "=" + value);
	}
    }

    // Y/U for textual
    static String normalizeTextual(String judgement) {
	return "Y".equals(judgement) ? "Y" : "U";
    }

    // Y/N for intuitive
    static String normalizeIntuitive(String judgement) {
	return "Y".equals(judgement) ? "Y" : "N";
    }


    public static void handleXml(File input, DefaultHandler handler) 
        throws SAXException, IOException {

        XMLReader saxParser = XMLReaderFactory.createXMLReader();
        saxParser.setContentHandler(handler);
        String url = Files.fileToURLName(input);
        InputSource in = new InputSource(url);
        saxParser.parse(in);
    }

}