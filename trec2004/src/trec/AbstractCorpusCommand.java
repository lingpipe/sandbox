package trec;

import com.aliasi.util.Streams;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;


public abstract class AbstractCorpusCommand extends AbstractTrecCommand {

    protected int mInputDocCount = 0;

    public AbstractCorpusCommand(String[] args) throws IOException {
	super(args);
    }


    abstract public void processDocument(MedlineDoc doc);

    public void init() { }

    public void finish() { }

    public void run() {
	init();
	mLastTime = mStartTime = System.currentTimeMillis(); 
	for (int i = 0; i < numBareArguments(); ++i)
	    readMultiFile(new File(getBareArgument(i)));
	finish();

    }

    private void readMultiFile(File file) {
	System.out.println("Reading file=" + file);
	FileInputStream fileIn = null;
	BufferedInputStream bufIn = null;
	InputStreamReader inReader = null;
	BufferedReader bufReader = null;
	try {
	    fileIn = new FileInputStream(file);
	    bufIn = new BufferedInputStream(fileIn);
	    inReader = new InputStreamReader(bufIn,"ASCII");
	    bufReader = new BufferedReader(inReader);
	    while (true) {
		String line = bufReader.readLine();
		if (line == null) break; // finished reading
		MedlineDoc doc = new MedlineDoc(line,bufReader);
		++mInputDocCount;
		processDocument(doc);
	    }
	} catch (IOException e) {
	    errorMsg("IOException reading.",e);
	} finally {
	    Streams.closeReader(bufReader);
	    Streams.closeReader(inReader);
	    Streams.closeInputStream(bufIn);
	    Streams.closeInputStream(fileIn);
	}
    }
}


