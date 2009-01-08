package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class Option extends AbstractAttrsElement {
    
    final TextContent mTextContent = new TextContent();


    public Option() {
        super(OPTION,true);
    }


    public String option() {
        return mTextContent.text();
    }

    public List contents() {
        return contentsInternal();
    }
    
    List contentsInternal() {
        return Collections.singletonList(mTextContent);
    }


    public void setSelected() {
        set(SELECTED,"selected");
    }

    public void setDisabled() {
        set(DISABLED,"disabled");
    }

    public void setLabel(String text) {
        set(LABEL,text);
    }

    public void setValue(String value) {
        set(VALUE,value);
    }

    public void set(CharSequence option) {
        mTextContent.set(option);
    }

}
