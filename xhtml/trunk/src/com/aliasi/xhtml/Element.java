package com.aliasi.xhtml;

import java.util.List;

import org.xml.sax.Attributes;

public interface Element extends Content {

    public String tag();
    
    public Attributes attributes();
    
    public List contents();


}
