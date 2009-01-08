package com.aliasi.xhtml;

public class FieldSet extends AbstractAttrsElement implements LittleBlockElement {

    public FieldSet() {
        super(FIELDSET,true);
    }

    public void add(String text) {
        addContent(text);
    }

    public void add(Legend legend) {
        addContent(legend);
    }

    public void add(Form form) {
        addContent(form);
    }
    
    public void add(LittleBlockElement elt) {
        addContent(elt);
    }

    public void add(LittleInlineElement elt) {
        addContent(elt);
    }

    public void add(MiscElement elt) {
        addContent(elt);
    }

    boolean prohibitedAncestor(int mask) {
        return (BUTTON_MASK & mask) != 0;
    }

}
