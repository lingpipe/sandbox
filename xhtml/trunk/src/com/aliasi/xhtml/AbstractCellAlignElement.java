package com.aliasi.xhtml;

class AbstractCellAlignElement extends AbstractAttrsElement {

    AbstractCellAlignElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    public void setAlignLeft() {
        set(ALIGN,"left");
    }
    public void setAlignCenter() {
        set(ALIGN,"center");
    }
    public void setAlignRight() {
        set(ALIGN,"right");
    }
    public void setAlignJustify() {
        set(ALIGN,"justify");
    }
    public void setAlignChar() {
        set(ALIGN,"char");
    }
    
    public void setChar(String character) {
        set(CHAR,character);
    }

    public void setCharOff(String length) {
        set(CHAROFF,length);
    }

    public void setVAlignTop() {
        set(VALIGN,"top");
    }
    public void setVAlignMiddle() {
        set(VALIGN,"middle");
    }
    public void setVAlignBottom() {
        set(VALIGN,"bottom");
    }
    public void setVAlignBaseline() {
        set(VALIGN,"baseline");
    }


}
