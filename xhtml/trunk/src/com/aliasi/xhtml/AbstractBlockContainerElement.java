package com.aliasi.xhtml;

class AbstractBlockContainerElement extends AbstractAttrsElement {

    AbstractBlockContainerElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    public void add(BlockElement block) {
        addContent(block);
    }

}
