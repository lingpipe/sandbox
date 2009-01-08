package com.aliasi.xhtml;

class AbstractInlineContainerElement extends AbstractAttrsElement {

    AbstractInlineContainerElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    public void add(String text) {
        addContent(text);
    }

    public void add(InlineElement elt) {
        addContent(elt);
    }

}
