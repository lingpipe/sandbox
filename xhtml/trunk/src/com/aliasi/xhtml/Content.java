package com.aliasi.xhtml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public interface Content {

    public void writeTo(ContentHandler handler) throws SAXException;

}