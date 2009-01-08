package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

class AbstractLiContainerElement extends AbstractAttrsElement {

    AbstractLiContainerElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    public void add(Li item) {
        addContent(item);
    }

    public List contentsInternal() {
        List contentsInternal = super.contentsInternal();
        return contentsInternal.size() == 0
            ? Collections.singletonList(new Li())
            : contentsInternal;
    }

}
