package biocreative;

import com.aliasi.xml.SimpleElementHandler;
import com.aliasi.xml.SAXFilterHandler;
import com.aliasi.xml.SAXWriter;
import com.aliasi.xml.XMLFileVisitor;

import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class SwissProtCommand extends SAXFilterHandler {

    StringBuffer mBuf = null;
    String mProtType = null;
    boolean mInProtein = false;
    boolean mInProteinName = false;
    HashSet mNames = new HashSet();

    public static void main(String[] args) throws Exception {
        File inFile = new File(args[0]);
        File outFile = new File(args[1]);


        FileOutputStream fileOut = new FileOutputStream(outFile);
        BufferedOutputStream bufOut = new BufferedOutputStream(fileOut);
        SAXWriter saxWriter = new SAXWriter(bufOut,Strings.UTF8);

        SwissProtCommand handler = new SwissProtCommand();
        handler.setHandler(saxWriter);

        XMLFileVisitor.handlePath(inFile,handler);

        bufOut.flush();
        fileOut.flush();
        Streams.closeOutputStream(bufOut);
        Streams.closeOutputStream(fileOut);

    }

    public void startDocument() throws SAXException {
        mHandler.startDocument();
    }

    public void endDocument() throws SAXException {
        try {
        SimpleElementHandler.startSimpleElement(mHandler,"dictionary");
        String[] names = new String[mNames.size()];
        mNames.toArray(names);
        Arrays.sort(names);
        System.out.println("Number of names from Swiss-Prot =" + names.length);
        for (int i = 0; i < names.length; ++i) {
            SimpleElementHandler.startSimpleElement(mHandler,"entry","type","GENE");
            mHandler.characters(names[i].toCharArray(),0,names[i].length());
            SimpleElementHandler.endSimpleElement(mHandler,"entry");
            SimpleElementHandler.startSimpleElement(mHandler,"entry","type","GENE");
            mHandler.characters(names[i].toLowerCase().toCharArray(),0,names[i].length());
            SimpleElementHandler.endSimpleElement(mHandler,"entry");
        }
        SimpleElementHandler.endSimpleElement(mHandler,"dictionary");
        mHandler.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
        throws SAXException {

        if (qName.equals("protein")) {
            mInProtein = true;
            mProtType = atts.getValue("type");
        }
        if (mInProtein && qName.equals("name")) {
            mInProteinName = true;
            mBuf = new StringBuffer();
        }
    }

    public void endElement(String namespaceURI, String localName,
                           String qName)
        throws SAXException {

        if (qName.equals("name") && mInProteinName) {
            mNames.add(mBuf.toString());

            /*
            char[] cs = mBuf.toString().toCharArray();
            if (mProtType != null)
                SimpleElementHandler.startSimpleElement(mHandler,"name","type",mProtType);
            else
                SimpleElementHandler.startSimpleElement(mHandler,"name");
            mHandler.characters(cs,0,cs.length);
            SimpleElementHandler.endSimpleElement(mHandler,"name");
            */

            mInProteinName = false;
        } else if (qName.equals("protein")) {
            mInProtein = false;
        }
    }

    public void characters(char[] ch, int start, int length) {
        if (mInProteinName)
            mBuf.append(ch,start,length);
    }

}
