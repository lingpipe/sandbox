package com.aliasi.xhtml;

public class ColGroup extends AbstractCellAlignElement {

    public ColGroup() {
        super(COLGROUP,true);
    }

    public void setSpan(String number) {
        set(SPAN_ATT,number);
    }

    public void setWidth(String multiLength) {
        set(WIDTH,multiLength);
    }

    public void add(Col col) {
        addContent(col);
    }

}
