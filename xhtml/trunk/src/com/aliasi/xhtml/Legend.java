package com.aliasi.xhtml;

public class Legend extends AbstractInlineContainerElement {

    public Legend() {
        super(LEGEND,true);
    }

    public void setAccessKey(String character) {
        set(ACCESSKEY,character);
    }

}
