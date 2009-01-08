package com.aliasi.xhtml;

public class Form 
    extends AbstractAttrsElement 
    implements BlockElement, FlowElement {

    public Form(String actionUri) {
        super(FORM,true);
        set(ACTION,actionUri);
    }

    public void setMethodGet() {
        set(METHOD,"get");
    }

    public void setMethodPost() {
        set(METHOD,"post");
    }

    public void setEncType(String contentType) {
        set(ENCTYPE,contentType);
    }

    public void setOnSubmit(String script) {
        set(ONSUBMIT,script);
    }

    public void setOnReset(String script) {
        set(ONRESET,script);
    }

    public void setAccept(String contentTypes) {
        set(ACCEPT,contentTypes);
    }

    public void setAcceptCharset(String charsets) {
        set(ACCEPT_CHARSET,charsets);
    }

    public void add(LittleBlockElement elt) {
        addContent(elt);
    }

    public void add(MiscElement elt) {
        addContent(elt);
    }

    boolean prohibitedAncestor(int mask) {
        return (FORM_ANCESTOR_MASK & mask) != 0;
    }

    final static int FORM_ANCESTOR_MASK = BUTTON_MASK | FORM_MASK;

}
