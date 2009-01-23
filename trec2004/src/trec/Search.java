package trec;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

public class Search extends AbstractTrecCommand {

    private static final int MAXIMUM_HITS = 10;

    private final Searcher mSearcher;

    public Search(String[] args) throws IOException {
	super(args);
	ArrayList searcherList = new ArrayList();
	// System.out.println("Creating Searcher Indexes");
	for (int i = 0; i < numBareArguments(); ++i) {
	    String indexFileName = getBareArgument(i);
	    // System.out.println("     " + (i+1) + ": " + indexFileName);
	    searcherList.add(new IndexSearcher(indexFileName));
	}
	IndexSearcher[] searchers = new IndexSearcher[searcherList.size()];
	searcherList.toArray(searchers);
	mSearcher = new MultiSearcher(searchers);
	// System.out.println("Max doc=" + mSearcher.maxDoc());
	// System.out.println("Searcher constructed.");
    }

    public Hits search(Query query) throws IOException {
	return mSearcher.search(query);
    }

    public void run() {
	try {
	    for (int i = 0; i < numBareArguments(); ++i) {
		String queryString = getBareArgument(i).toString();
		// System.out.println("--------------------------------------");
		// System.out.println("Query=" + queryString);
		// System.out.println("Rank PubMedID Score");
		Query query = QueryParser.parse(queryString,MedlineDoc.WEIGHTED_TEXT_FIELD,mAnalyzer);
		Hits hits = search(query);
		int numResults= Math.min(MAXIMUM_HITS,hits.length());
		System.out.println("NUMRESULTS=" + numResults);
		for (int n = 0; n < numResults; ++n) {
		    Document doc = hits.doc(n);
		    String id = doc.get(MedlineDoc.ID_FIELD);
		    String text = doc.get(MedlineDoc.ABSTRACT_FIELD);
		    String mhs = doc.get(MedlineDoc.MESH_FIELD);
		    System.out.println((n < 10 ? "  " : " ")
				       + (n+1) + ". "
				       + ((id.trim().length() < 8) ? " " : "")
				       + id.trim()
				       + " "
				       + hits.score(n)
				       + " "
				       + doc.get(MedlineDoc.TITLE_FIELD) 
				       + ((text != null) ? ("\nABSTRACT: " + text) : "")
				       + ((mhs != null) ? ("\nMESH TERMS: " + mhs) : ""));
		}
	    }
	} catch (ParseException e) {
	    errorMsg("Query parse exception in search", e);
	} catch (IOException e) {
	    errorMsg("IOException in search.",e);
	}
    }


    public static void main(String[] args) throws IOException {
	Search cmd = new Search(args);
	cmd.run();
    }

    
}
