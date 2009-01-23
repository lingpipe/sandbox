package trec;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Reflection;

import java.io.File;
import java.io.IOException;

import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;

public abstract class AbstractTrecCommand extends AbstractCommand {

    protected long mStartTime;
    protected long mLastTime; 

    protected final Analyzer mAnalyzer;
    protected final File mIndexDirectory;


    public AbstractTrecCommand(String[] args) throws IOException {
	super(args,DEFAULT_PROPERTIES);
	mAnalyzer = getAnalyzer();
	mIndexDirectory = getIndexDirectory();
    }

    protected Analyzer getAnalyzer() {
	//System.out.println("Creating analyzer, class="
	// + getArgument(ANALYZER_PARAM));
	String className = getArgument(ANALYZER_PARAM);
	Analyzer analyzer = (Analyzer) Reflection.newInstance(className);
	if (analyzer == null) 
	    illegalPropertyArgument("Could not create analyzer of specified class.",
				    ANALYZER_PARAM);
	return analyzer;
    }

    protected File getIndexDirectory() throws IOException {
	File file = getArgumentFile(INDEX_DIR_PARAM);
	// System.out.println("Index Directory=" + file);
	if (file.isFile()) {
	    String msg = "Index dir cannot be ordinary file. Found=" + file;
	    throw new IllegalArgumentException(msg);
	}
	if (!file.exists())
	    file.mkdirs();
	return file;
    }



    private static final String ANALYZER_PARAM = "analyzer";
    private static final String INDEX_DIR_PARAM = "indexDir";
    private static final Properties DEFAULT_PROPERTIES
	= new Properties();
    static {
	DEFAULT_PROPERTIES
	    .setProperty(ANALYZER_PARAM,
			 "org.apache.lucene.analysis.standard.StandardAnalyzer");
	DEFAULT_PROPERTIES
	    .setProperty(INDEX_DIR_PARAM,
			 "index");
    }

    protected static void errorMsg(String msg, Exception e) {
	System.out.println("Error: " + msg + " Stack trace follows:");
	e.printStackTrace(System.out);
    }

    public static String toClockTime(long totalTime) {
	StringBuffer timeBuf = new StringBuffer(16);
	if (totalTime > 3600l) {
	    timeBuf.append((totalTime/3600l) + "h ");
	    totalTime = totalTime % 3600l;
	}
	if (totalTime > 60l) {
	    timeBuf.append((totalTime/60l) + "m ");
	    totalTime = totalTime % 60l;
	}
	if (totalTime > 0l || timeBuf.length() < 1l) {
	    timeBuf.append(totalTime + "s");
	}
	return timeBuf.toString().trim();
    }


}
