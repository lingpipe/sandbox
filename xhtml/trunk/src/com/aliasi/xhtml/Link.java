package com.aliasi.xhtml;

import java.util.Collections;
import java.util.Set;

public class Link extends AbstractAttrsElement {

    public Link() {
        super(LINK,Collections.EMPTY_LIST,true);
    }

    public void setCharset(String charset) {
        set(CHARSET,charset);
    }

    public void setHref(String uri) {
        set(HREF,uri);
    }
        
    public void setHrefLang(String languageCode) {
        set(HREFLANG,languageCode);
    }

    public void setType(String contentType) {
        set(TYPE,contentType);
    }
    
    public void setRel(String linkTypes) {
        set(REL,linkTypes);
    }

    public void setRev(String linkTypes) {
        set(REV,linkTypes);
    }
    
    public void setMedia(String mediaDesc) {
        set(MEDIA,mediaDesc);
    }

    void propagateMask(int mask) { }

    boolean isCyclic(Set visitedSet) { 
        return false;
    }

}
