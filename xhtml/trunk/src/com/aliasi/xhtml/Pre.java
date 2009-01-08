package com.aliasi.xhtml;

public class Pre extends AbstractAttrsElement implements BlockTextElement {

    public Pre() {
        super(PRE,true);
    }

    public void setXmlSpace() {
        set(XML_SPACE,"preserve");
    }

    public void add(String text) {
        addContent(text);
    }

    public void add(A a) {
        addContent(a);
    }

    public void add(FontStyleElement elt) {
        addContent(elt);
    }

    public void add(PhraseElement elt) {
        addContent(elt);
    }

    public void add(SpecialPreElement elt) {
        addContent(elt);
    }

    public void add(MiscInlineElement elt) {
        addContent(elt);
    }

    public void add(InlineFormElement elt) {
        addContent(elt);
    }

}
