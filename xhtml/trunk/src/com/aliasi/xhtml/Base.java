package com.aliasi.xhtml;

import java.util.Collections;
import java.util.Set;

public class Base extends AbstractIdElement {

    public Base() {
        this("");
    }

    public Base(String hrefUri) {
        super(BASE,Collections.EMPTY_LIST,true);
        set(HREF,hrefUri);
    }

    public void setHref(String uri) {
        set(HREF,uri);
    }

    void propagateMask(int mask) { }

    boolean isCyclic(Set visitedSet) { 
        return false;
    }
}
