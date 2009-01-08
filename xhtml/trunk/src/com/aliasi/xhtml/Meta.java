package com.aliasi.xhtml;

import java.util.Collections;
import java.util.Set;

public class Meta extends AbstractInternationalizedElement {

    public Meta() {
        this("");
    }

    public Meta(String content) {
        super(META,Collections.EMPTY_LIST,true);
        set(CONTENT,content);
    }

    public void setContent(String content) {
        set(CONTENT,content);
    }

    public void setHttpEquiv(String httpEquiv) {
        set(HTTP_EQUIV,httpEquiv);
    }

    public void setName(String name) {
        set(NAME,name);
    }

    public void setScheme(String scheme) {
        set(SCHEME,scheme);
    }

    void propagateMask(int mask) { }

    boolean isCyclic(Set visitedSet) { 
        return false;
    }
}
