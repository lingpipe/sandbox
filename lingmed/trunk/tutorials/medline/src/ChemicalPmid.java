import com.aliasi.lingmed.medline.parser.*;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;

import java.util.zip.GZIPInputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ChemicalPmid {

    static StringBuilder mSb = new StringBuilder();
    static MedlineParser mParser;
    public static void main(String[] args) throws IOException, SAXException {
        boolean saveXML = false;
        mParser = new MedlineParser(saveXML);
        ChemPmidHandler handler = new ChemPmidHandler();
        mParser.setHandler(handler);
	File outFile= new File(args[1]);
	File input = new File(args[0]);
	process(input);
	Files.writeStringToFile(mSb.toString(),outFile,Strings.UTF8);
	handler.report();
    }    
    static void process(File input) throws FileNotFoundException, IOException {
	if (input.isFile()) {
	    processFile(input);
	}
	else {
	    for (File file : input.listFiles()) {
		process(file);
	    }
	}
    }

    static void processFile(File file) throws FileNotFoundException, IOException {

	if (file.getName().endsWith(".xml")) {
	    System.out.println("Processing " + file);
	    InputSource inputSource = new InputSource(new FileInputStream(file));
	    mParser.parse(inputSource);
	} else if (file.getName().endsWith(".gz")) {
	    System.out.println("Processing " + file);
	    FileInputStream fileIn = null;
	    GZIPInputStream gzipIn = null;
	    InputStreamReader inReader = null;
	    BufferedReader bufReader = null;
	    InputSource inSource = null;
	    try {
		fileIn = new FileInputStream(file);
		gzipIn = new GZIPInputStream(fileIn);
		inReader = new InputStreamReader(gzipIn,Strings.UTF8);
		bufReader = new BufferedReader(inReader);
		inSource = new InputSource(bufReader);
		inSource.setSystemId(file.toURI().toURL().toString());
		mParser.parse(inSource);
	    } finally {
		Streams.closeReader(bufReader);
		Streams.closeReader(inReader);
		Streams.closeInputStream(gzipIn);
		Streams.closeInputStream(fileIn);
	    }
	}
    }
    

    static class ChemPmidHandler implements MedlineHandler {
        long mCitationCount = 0L;
        public void handle(MedlineCitation citation) {
            ++mCitationCount;

            String id = citation.pmid();
            // System.out.println("processing pmid=" + id);

	    for (Chemical chem : citation.chemicals()) {
		mSb.append(id + ",\"" + chem.nameOfSubstance() + "\",\"" + chem.registryNumber() + "\",\n");
	    }
        }
        public void delete(String pmid) {
            throw new UnsupportedOperationException("not expecting deletes. found pmid=" + pmid);
        }
        public void addText(String text) {
            throw new UnsupportedOperationException("not expecting addText.");
        }
        public void report() {
	    System.out.println(mCitationCount + " citations");
	
        }
    }

}