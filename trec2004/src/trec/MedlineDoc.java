package trec;

import com.aliasi.util.ObjectToSet;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class MedlineDoc {
    private static String SEPARATOR = " " + '\uFFFF' + " ";
    private ObjectToSet mFields = new ObjectToSet();

    public MedlineDoc(String line, BufferedReader reader) 
	throws IOException {

	while (line != null  && line.length() > 0) {
	    if (line.length() < 6) {
		System.out.println("Illegal line=|" + line + "|");
		line = reader.readLine();
		continue;
	    }
	    String field = line.substring(0,4).trim();
	    StringBuffer sb = new StringBuffer(1024);
	    sb.append(line.substring(5));
	    while ((line = reader.readLine()) != null
		   && line.length() > 0
		   && line.charAt(0) == ' ') {
		sb.append(' ');
		if (line.length() < 7) continue; // only space
		sb.append(line.substring(6));
	    }
	    if (!FIELDS_TO_KEEP.contains(field)) continue;
	    String value = sb.toString().trim();
	    mFields.addMember(field,value);
	}
    }


    public MedlineDoc(Document document) {
	Enumeration fieldEnum = document.fields();
	while (fieldEnum.hasMoreElements()) {
	    Field field = (Field) fieldEnum.nextElement();
	    String fieldName = field.name();
	    if (fieldName.charAt(0) == '3') continue;
	    String value = field.stringValue();
	    if (value == null) {
		System.out.println("Unexpected null value for fieldName=" 
				   + fieldName);
		continue;
	    }
	    String[] values = value.split(SEPARATOR);
	    for (int i = 0; i < values.length; ++i)
		mFields.addMember(fieldName,value);
	}
    }

    public Document toLuceneDocument() {
	Document luceneDocument = new Document();
	Iterator it = mFields.keySet().iterator();
	StringBuffer txt = new StringBuffer();
	while (it.hasNext()) {
	    String field = it.next().toString();
	    Set valueSet = mFields.getSet(field);
	    if (valueSet == null) {
		System.out.println("Unexpected null value set.");
	    }
	    String value = concatenateValues(valueSet);

	    // Field(String name, String val, store?, index?, tokenize?)


	    // store and index identifier
	    if (field.equals(ORIG_PMID_FIELD)) {
		luceneDocument.add(new Field(ID_FIELD,value,true,true,false));
		continue;
	    } 

	    // store contents
	    if (field.equals(ORIG_TITLE_FIELD)) {
		luceneDocument.add(new Field(TITLE_FIELD,value,true,false,false));
	    } 
	    if (field.equals(ORIG_ABSTRACT_FIELD)) {
		luceneDocument.add(new Field(ABSTRACT_FIELD,value,true,false,false));
	    }
	    if (field.equals(ORIG_MESH_HEADING_FIELD)) {
		luceneDocument.add(new Field(MESH_FIELD,value,true,false,false));
	    }

	    // weight
	    if (field.equals(ORIG_MESH_HEADING_FIELD)) {
		// separator removed
		value.replaceAll("/"," ");
		if (value.indexOf('*') >= 0) {
		    // import headings quadrupled
		    // remove importance marker
		    value = value.replace('*',' ');
		    value = value 
			+ SEPARATOR + value
			+ SEPARATOR + value
			+ SEPARATOR + value;
		} else {
		    // other headings doubled
		    value = value 
			+ SEPARATOR + value; 
		}
	    }
	    if (field.equals(ORIG_TITLE_FIELD)) {
		value = value
		    + SEPARATOR + value
		    + SEPARATOR + value
		    + SEPARATOR + value;
	    }
	    txt.append(value);
	}
	luceneDocument.add(new Field(WEIGHTED_TEXT_FIELD,txt.toString(),
				     false,true,true));
	
	return luceneDocument;
    }
    private static String concatenateValues(Set x) {
	if (x == null) {
	    System.out.println("Unexpected null concat");
	}
	if (x.size() == 0) return "";
	if (x.size() == 1) return x.iterator().next().toString();
	StringBuffer sb = new StringBuffer(2048);
	Iterator it = x.iterator();
	sb.append(it.next());
	while (it.hasNext()) {
	    sb.append(SEPARATOR);
	    sb.append(it.next());
	}
	return sb.toString();
    }
    public String toString() {
	return mFields.toString();
    }
    private static final String ORIG_PMID_FIELD = "PMID";
    private static final String ORIG_TITLE_FIELD = "TI";
    private static final String ORIG_ABSTRACT_FIELD = "AB";
    private static final String ORIG_MESH_HEADING_FIELD = "MH";
    private static final String ORIG_CHEMICAL_FIELD = "RN";
    private static Set FIELDS_TO_KEEP = new HashSet();
    static {
	FIELDS_TO_KEEP.add(ORIG_PMID_FIELD); 
	FIELDS_TO_KEEP.add(ORIG_TITLE_FIELD);
	FIELDS_TO_KEEP.add(ORIG_ABSTRACT_FIELD);  
	FIELDS_TO_KEEP.add(ORIG_MESH_HEADING_FIELD);
	FIELDS_TO_KEEP.add(ORIG_CHEMICAL_FIELD);

	// FIELDS_TO_KEEP.add("LA");   // language

	// FIELDS_TO_KEEP.add("FAU");  // full author names/initials
	// FIELDS_TO_KEEP.add("AD");   // affilliation/address of authors
	// FIELDS_TO_KEEP.add("DP");   // publication date
	// FIELDS_TO_KEEP.add("TA");   // journal title abbreviation
	// FIELDS_TO_KEEP.add("PT");   // publication type
	// FIELDS_TO_KEEP.add("PL");   // publication location
	// FIELDS_TO_KEEP.add("SB");   // journal/citation subset value??
    }

    public static final String WEIGHTED_TEXT_FIELD = "TEXT";

    public static final String TITLE_FIELD = "TITLE";
    public static final String ID_FIELD = "ID";
    public static final String MESH_FIELD = "MESH";
    public static final String ABSTRACT_FIELD = "ABSTRACT";
}

