package trec;

import com.aliasi.util.Files;

import EDU.oswego.cs.dl.util.concurrent.BoundedLinkedQueue;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;

import org.apache.lucene.document.Document;

import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;


/**
 *
 * @author Bob Carpenter
 * @version 1.0
 * @since Trec 1.0
 */
public final class SimpleIndex extends AbstractCorpusCommand {

    private RAMDirectory mRAMDirectory;
    private IndexWriter mRAMIndexWriter;

    private IndexWriter mFinalIndexWriter;

    private final FSDirectory[] mFSDirectories; 
    private final IndexWriter[] mFSIndexWriters;

    /**
     * Construct an instance of an index building command
     * using the specified command line arguments.
     *
     * @param args Command-line arguments.  See class documentation
     * for a specification.
     */
    public SimpleIndex(String[] args) throws IOException {
	super(args);

	mFSDirectories = new FSDirectory[NUM_BINS];
	Arrays.fill(mFSDirectories,null);
	mFSIndexWriters = new IndexWriter[NUM_BINS];
	Arrays.fill(mFSIndexWriters,null);
	
	freshIndexWriter();

	FSDirectory finalDirectory = FSDirectory.getDirectory(mIndexDirectory,true); 
	mFinalIndexWriter = new IndexWriter(finalDirectory,mAnalyzer,true);

    }

    public void processDocument(MedlineDoc doc) {
	if (doc == null) return;
	try {
	    Document luceneDocument = doc.toLuceneDocument();
	    mRAMIndexWriter.addDocument(luceneDocument);
	    if (mRAMIndexWriter.docCount() >= MAX_COUNT) {
		merge();
		report();
	    }
	} catch (IOException e) {
	    errorMsg("Process Document=" + doc, e);
	}
    }

    public void finish() {
	try {
	    finalMerge();
	} catch (Exception e) {
	    errorMsg("Finish.",e);
	}
	report();
    }



    private void freshIndexWriter() throws IOException {
	if (mRAMIndexWriter != null)
	    mRAMIndexWriter.close();
	mRAMDirectory = new RAMDirectory();
	mRAMIndexWriter = new IndexWriter(mRAMDirectory,mAnalyzer,true);
    }

    private void merge() throws IOException {
	if (mFSIndexWriters[0] == null) {
	    mFSDirectories[0] = FSDirectory.getDirectory(genTempFile(),true); // temp
	    mFSIndexWriters[0] = new IndexWriter(mFSDirectories[0],mAnalyzer,true);
	    mRAMIndexWriter.optimize();
	    mFSIndexWriters[0].addIndexes(new Directory[] { mRAMDirectory });
	} else {
	    ArrayList mergeDirectoryList = new ArrayList();
	    mergeDirectoryList.add(mRAMDirectory);
	    // start with mFSIndexWriters[0] != null, mFSIndexWriters[1] != null
	    for (int i = 0; i+1 < NUM_BINS; ++i) {
		if (mFSIndexWriters[i+1] == null) {
		    Directory[] directories = new Directory[mergeDirectoryList.size()];
		    mergeDirectoryList.toArray(directories);
		    mFSIndexWriters[i].addIndexes(directories);
		    mFSIndexWriters[i+1] = mFSIndexWriters[i];
		    mFSIndexWriters[i] = null;
		    mFSDirectories[i+1] = mFSDirectories[i];
		    mFSDirectories[i] = null;
		    break;
		}
		mergeDirectoryList.add(mFSDirectories[i]);
		mFSDirectories[i] = null;
		mFSIndexWriters[i] = null;
	    }
	}
	freshIndexWriter();
    }
    

    // initialize top directory to initial index, and merge all else
    // into it throughout

    private void finalMerge() throws IOException {
	ArrayList mergeDirectoryList = new ArrayList();
	mRAMIndexWriter.optimize();
	mergeDirectoryList.add(mRAMDirectory);
	mRAMDirectory = null;
	for (int i = 0; i < NUM_BINS; ++i) {
	    if (mFSDirectories[i] == null) continue;
	    mFSIndexWriters[i].optimize();
	    mergeDirectoryList.add(mFSDirectories[i]);
	    mFSDirectories[i] = null;
	}
	Directory[] directoriesToMerge = new Directory[mergeDirectoryList.size()];
	mergeDirectoryList.toArray(directoriesToMerge);
	mFinalIndexWriter.addIndexes(directoriesToMerge);
	mRAMIndexWriter.close();
	for (int i = 0; i < NUM_BINS; ++i) {
	    if (mFSIndexWriters[i] == null) continue;
	    mFSIndexWriters[i].close();
	    mFSIndexWriters[i] = null;
	}
	System.out.println("\nFINAL REPORT");
	System.out.println("TOTAL: "
			   + mFinalIndexWriter.docCount() 
			   + " docs");
	mFinalIndexWriter.close();
	report();
    }

    private void report() {
	long nowTime = System.currentTimeMillis();
	// long diff = (nowTime - mLastTime) / 1000l;
	long total = (nowTime - mStartTime)/ 1000l;
	mLastTime = nowTime;
	System.out.println("#Docs=" + mInputDocCount
			   + " elapsed=" + toClockTime(total) 
			   + " rate=" + (mInputDocCount/total) + "doc/s");
	for (int i = 0; i < NUM_BINS; ++i)
	    if (mFSIndexWriters[i] != null)
		System.out.println("  writer " + i + ": " 
				   + mFSIndexWriters[i].docCount()
				   + " docs.");
    }

    private File genTempFile() {
	File result = Files.createTempFile("luceneTemp" + (sNextTempFileID++));
	System.out.println("Temp file=" + result);
	return result; 
    }



    private static int sNextTempFileID = 0;

    private static final int MAX_COUNT = 10000;
    private static final int NUM_BINS = 16;

    /**
     * Execute the command with the specified argument.  This just
     * constructs an instance of <code>BuildIndex</code> with the
     * specified arguments and runs it once.
     *
     * @param args Command-line arguments.  See class documentation
     * for a specification.
     */
    public static void main(String[] args) throws IOException {
	new SimpleIndex(args).run();
    }
    

    
}
