package com.aliasi.xhtml;

import java.util.Set;

public class Body extends AbstractBlockContainerElement {

    public Body() {
        super(BODY,true);
    }

    public void setOnLoad(String script) {
        set(ONLOAD,script);
    }

    public void setOnUnload(String script) {
        set(ONUNLOAD,script);
    }

    void propagateMask(int mask) { }

    boolean isCyclic(Set visitedSet) { 
        return false;
    }

}
