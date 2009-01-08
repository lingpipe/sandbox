package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Title extends AbstractInternationalizedElement {

    final TextContent mTextContent = new TextContent();

    public Title() {
        this("");
    }

    public Title(CharSequence title) {
        super(TITLE,null,true);
        set(title);
    }


    public String title() {
        return mTextContent.text();
    }
    
    public List contents() {
        return contentsInternal();
    }
    
    List contentsInternal() {
        return Collections.singletonList(mTextContent);
    }


    public void set(CharSequence title) {
        mTextContent.set(title);
    }



    void propagateMask(int mask) { }

    boolean isCyclic(Set visitedSet) { 
        return false;
    }

}
