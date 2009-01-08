package com.aliasi.xhtml;

class AbstractFocusElement extends AbstractAttrsElement {

    AbstractFocusElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    public void setAccessKey(String character) {
        set(ACCESSKEY,character);
    }

    public void setTabIndex(String number) {
        set(TABINDEX,number);
    }

    public void setOnFocus(String script) {
        set(ONFOCUS,script);
    }

    public void setOnBlur(String script) {
        set(ONBLUR,script);
    }

}


