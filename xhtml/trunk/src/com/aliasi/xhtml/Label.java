package com.aliasi.xhtml;

public class Label 
    extends AbstractInlineContainerElement 
    implements InlineFormElement {

    public Label() {
        super(LABEL,true);
    }

    public void setFor(String idRef) {
        set(FOR,idRef);
    }

    public void setAccessKey(String character) {
        set(ACCESSKEY,character);
    }

    public void setOnFocus(String script) {
        set(ONFOCUS,script);
    }

    public void setOnBlur(String script) {
        set(ONBLUR,script);
    }


    boolean prohibitedAncestor(int mask) {
        return (LABEL_ANCESTOR_MASK & mask) != 0;
    }

    private final static int LABEL_ANCESTOR_MASK = BUTTON_MASK | LABEL_MASK;
    
}
