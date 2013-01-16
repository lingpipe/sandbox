package com.lingpipe.book.xml;

import java.io.IOException;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class PrintXmlCharContent {

    public static void main(String[] args) 
        throws IOException, SAXException {

        System.out.println("Reading doc with URL=" + args[0]);
        
        InputSource in = new InputSource(args[0]);

        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(new PrintXmlCharContentHandler());
        reader.parse(in);
        
        System.out.println("Hello.");
    }

    static class PrintXmlCharContentHandler 
        extends DefaultHandler {

        public void characters(char[] cs, int start, int len) {
            System.out.println(new String(cs,start,len));
        }

    }

}