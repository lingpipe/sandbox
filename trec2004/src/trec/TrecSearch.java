package trec;

import com.aliasi.util.Streams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;

import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class TrecSearch extends Search {

    private static int MAX_RESULTS = 1000;

    public TrecSearch(String[] args) throws IOException {
	super(args);
    }
    
    public static void main(String[] args) throws IOException {
	TrecSearch search = new TrecSearch(args);
	search.run();
    }

    public void run() {

	String searchName = getExistingArgument("name");
	File file = new File(searchName + ".txt");
	FileOutputStream out = null; 
	PrintStream printer = null; 

	try {
	    out = new FileOutputStream(file);
	    printer = new PrintStream(out);
	    boolean useTerms = hasFlag("useLingPipeTerms");
	    boolean useStopList = hasFlag("useStopList");
	    TrecQueryHandler queryHandler = new TrecQueryHandler();

	    XMLReader xmlReader = XMLReaderFactory.createXMLReader();
	    xmlReader.setContentHandler(queryHandler);

	    File queryFile = getArgumentFile("queryFile");
	    // System.out.println("Query file=" + queryFile);
	    xmlReader.parse(queryFile.getCanonicalPath());


	    Iterator queryIterator = queryHandler.queryIterator();
	    for (int j = 1; queryIterator.hasNext(); ++j) {
		TrecQuery tQuery = (TrecQuery) queryIterator.next();
		tQuery.useLingPipeTerms(useTerms);
		tQuery.useStopList(useStopList);
		Query query = tQuery.toLuceneQuery();
		Hits hits = search(query);
		// System.err.println("QUERY=" + j + " " + query);
		// System.err.println("#HITS=" + hits.length());
		int numResults = Math.min(MAX_RESULTS,hits.length());
		if (numResults < 1) {
		    System.err.println("****** NO results for query=" + tQuery.mId + " ****");
		}
		for (int i = 0; i < numResults; ++i) {
		    Document doc = hits.doc(i);
		    String id = doc.get(MedlineDoc.ID_FIELD);
		    printer.println(tQuery.mId + "\t"
				    + "Q0" + "\t"
				    + id + "\t"
				    + (i+1) + "\t"
				    + hits.score(i) + "\t"
				    + searchName);
		    /*
		    System.err.println("\n      RANK: " + i);
		    System.err.println("     TITLE: " + doc.get(MedlineDoc.TITLE_FIELD));
		    System.err.println("  ABSTRACT: " + doc.get(MedlineDoc.ABSTRACT_FIELD));
		    System.err.println("      MESH: " + doc.get(MedlineDoc.MESH_FIELD));
		    */
		}
	    }
	} catch (IOException e) {
	    errorMsg("run",e);
	} catch (SAXException e) {
	    errorMsg("run",e);
	} finally {
	    Streams.closeOutputStream(printer);
	    Streams.closeOutputStream(out);
	}
    }

}
