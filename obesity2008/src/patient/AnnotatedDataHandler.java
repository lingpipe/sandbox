package patient;

import com.aliasi.xml.SimpleElementHandler;

import com.aliasi.util.Pair;

import java.util.Map;
import java.util.HashMap;

import org.xml.sax.Attributes;

public class AnnotatedDataHandler extends SimpleElementHandler {

    final Map<Pair<String,String>,Map<String,String>> mSourceXDiseaseToIdToJudgement
	= new HashMap<Pair<String,String>,Map<String,String>>();

    private String mSource;
    private String mDisease;
    
    public void startElement(String url, String localName, String qName,
			     Attributes atts) {
	if ("diseases".equals(qName))
	    mSource = atts.getValue("source");
	else if ("disease".equals(qName)) 
	    mDisease = atts.getValue("name");
	else if ("doc".equals(qName)) {
	    Pair<String,String> sourceXDisease = new Pair<String,String>(mSource,mDisease);
	    Map<String,String> idToJudgement = mSourceXDiseaseToIdToJudgement.get(sourceXDisease);
	    if (idToJudgement == null) {
		idToJudgement = new HashMap<String,String>();
		mSourceXDiseaseToIdToJudgement.put(sourceXDisease,idToJudgement);
	    }
	    idToJudgement.put(atts.getValue("id"),
			      atts.getValue("judgment"));
	}
    }

}


