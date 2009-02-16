package corpus;


import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.ObjectToSet;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import com.aliasi.xml.DelegatingHandler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.Set;

import java.util.zip.GZIPInputStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class Senseval3Counter {

    ObjectToCounterMap mWordCounter = new ObjectToCounterMap();
    ObjectToSet mWordToLemmaSet = new ObjectToSet();
    ObjectToCounterMap mOverallCounter = new ObjectToCounterMap();
    
    public Senseval3Counter() {
    }

    public void incrementCounts(File file) 
	throws IOException, SAXException {

	InputStream in = new FileInputStream(file);
	in = new BufferedInputStream(in);
	if (file.getName().endsWith(".gz"))
	    in = new GZIPInputStream(in);
	InputSource inSource = new InputSource(in);
	inSource.setEncoding("ISO-8859-1");
	XMLReader reader = XMLReaderFactory.createXMLReader();
	reader.setContentHandler(new CountHandler());
	reader.parse(inSource);
    }

    public ObjectToCounterMap wordCounter() {
	report();
	return mWordCounter;
    }

    public void addWord(String word, String pos, String lemma) {
	if (word == null) return;
	// System.out.println("word=" + word + " lemma=" + lemma + " POS=" + pos);
	mWordCounter.increment(word);
	mWordToLemmaSet.addMember(word,lemma);
	mOverallCounter.increment(word + "^" + lemma);
    }

    public void report() {
	System.out.println("Unique Words=" + mWordCounter.size());
	System.out.println("Ambiguous Words");
	Iterator it = mWordToLemmaSet.keySet().iterator();
	int numAmbiguities = 0;
	while (it.hasNext()) {
	    String word = it.next().toString();
	    Set lemmaSet = mWordToLemmaSet.getSet(word);
	    if (lemmaSet.size() > 1) {
		System.out.println(word);
		System.out.print("     ");
		Iterator lemmaIt = lemmaSet.iterator();
		while (lemmaIt.hasNext()) {
		    String lemma = lemmaIt.next().toString();
		    int count = mOverallCounter.getCount(word + "^" + lemma);
		    System.out.print(" " + lemma + ":" + count);
		}
		System.out.println();
		++numAmbiguities;
	    }
	}
	System.out.println("# Ambiguous Words=" + numAmbiguities
			   + "/" + mWordCounter.size());
    }

    class CountHandler extends DelegatingHandler {
	CountHandler() {
	    setDelegate("w",new WordHandler());
	}
    }

    class WordHandler extends DefaultHandler {
	public void startElement(String uri, String name, String qName,
				 Attributes atts) {
	    String word = atts.getValue("frm").toLowerCase();
	    String lemma = atts.getValue("lem").toLowerCase();
	    String pos = atts.getValue("pos");
	    if (word == null)  System.out.println("qName=" + qName);
	    addWord(word,pos,lemma);
	}
    }
}