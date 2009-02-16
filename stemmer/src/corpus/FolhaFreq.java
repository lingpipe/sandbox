package corpus;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.XML;

import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public class FolhaFreq {

    static final IndoEuropeanTokenizerFactory TOKENIZER_FACTORY
	= new IndoEuropeanTokenizerFactory();

    public static void main(String[] args) throws Exception {
	XMLReader xmlReader = XMLReaderFactory.createXMLReader();
	xmlReader.setFeature(XML.VALIDATION_FEATURE,false);
	TextVisitor visitor = new TextVisitor();
	xmlReader.setContentHandler(visitor);
	xmlReader.setDTDHandler(visitor);
	xmlReader.setEntityResolver(visitor);

	File outFile = new File(args[0]);

	for (int i = 1; i < args.length; ++i) {
	    File file = new File(args[i]);
	    String systemId = Files.fileToURLName(file);
	    System.out.println("Parsing input url=" + systemId);
	    InputSource inSource = new InputSource(systemId);
	    xmlReader.parse(inSource);
	}

	LeipzigFreq.writeOutput(visitor.mCounter,outFile);
    }

    public static class TextVisitor extends DelegatingHandler {
	ObjectToCounterMap mCounter = new ObjectToCounterMap();
	TextAccumulatorHandler mTextAccumulator 
	    = new TextAccumulatorHandler();
	public TextVisitor() {
	    setDelegate("TEXT",mTextAccumulator);
	}
	public void finishDelegate(String qName, DefaultHandler handler) {
	    if (!qName.equals("TEXT")) return;
	    String text = mTextAccumulator.getText();
	    LeipzigFreq.addTokens(text,mCounter);
	}
    }
	
}