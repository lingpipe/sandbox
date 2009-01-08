package com.aliasi.xhtml;

class AbstractFlowContainerElement extends AbstractAttrsElement {

    AbstractFlowContainerElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    public void add(String text) {
        addContent(text);
    }

    public void add(FlowElement elt) {
        addContent(elt);
    }

}
