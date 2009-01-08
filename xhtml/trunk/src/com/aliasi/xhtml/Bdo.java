package com.aliasi.xhtml;

public class Bdo extends AbstractInlineContainerElement implements SpecialPreElement {

    public Bdo(boolean isLtr) {
        super(BDO,true);
        set(DIR, isLtr ? "ltr" : "rtl");
    }

}
