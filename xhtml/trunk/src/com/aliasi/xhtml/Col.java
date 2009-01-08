package com.aliasi.xhtml;

public class Col extends AbstractCellAlignElement {

    public Col() {
        super(COL,true);
    }

    public void setSpan(String number) {
        set(SPAN_ATT,number);
    }

    public void setWidth(String multiLength) {
        set(WIDTH,multiLength);
    }
    
}
