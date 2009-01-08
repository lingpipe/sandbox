package com.aliasi.xhtml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class Document {

    private final Html mHtml;

    public Document(Html html) {
        mHtml = html;
    }

    public Html html() {
        return mHtml;
    }

    public void writeTo(ContentHandler handler) throws SAXException {
        handler.startDocument();
        mHtml.writeTo(handler);
        handler.endDocument();
    }

    public static final String DOCTYPE
        = "<!DOCTYPE html PUBLIC"
        + " \"-//W3C//DTD XHTML 1.0 Strict//EN\""
        + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
    

}
