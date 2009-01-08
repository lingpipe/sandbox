package com.aliasi.xhtml;

public class Br extends AbstractIdElement implements SpecialPreElement {

    public Br() {
        super(BR,true);
    }

    public void setClass(String className) {
        set(CLASS,className);
    }

    public void setStyle(String style) {
        set(STYLE,style);
    }

    public void setTitle(String title) {
        set(TITLE,title);
    }


}
