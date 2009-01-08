package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class TextArea 
    extends AbstractFocusElement 
    implements InlineFormElement {

    final TextContent mTextContent = new TextContent();

    public TextArea(String rows, String cols) {
        super(TEXTAREA,true);
        set(ROWS,rows);
        set(COLS,cols);
    }


    public String text() {
        return mTextContent.text();
    }

    public List contents() {
        return contentsInternal();
    }
    
    List contentsInternal() {
        return Collections.singletonList(mTextContent);
    }


    public void setName(String name) {
        set(NAME,name);
    }

    public void setDisabled() {
        set(DISABLED,"disabled");
    }

    public void setReadOnly() {
        set(READONLY,"readonly");
    }

    public void setOnSelect(String script) {
        set(ONSELECT,script);
    }

    public void setOnChange(String script) {
        set(ONCHANGE,script);
    }

    public void set(CharSequence text) {
        mTextContent.set(text);
    }

    boolean prohibitedAncestor(int mask) {
        return (BUTTON_MASK & mask) != 0;
    }

}
