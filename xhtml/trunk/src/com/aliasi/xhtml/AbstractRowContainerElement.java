package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

class AbstractRowContainerElement extends AbstractCellAlignElement {

    AbstractRowContainerElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    List contentsInternal() {
        List contents = super.contentsInternal();
        return contents.isEmpty()
            ? Collections.singletonList(new Tr())
            : contents;
    }

    public void add(Tr row) {
        addContent(row);
    }

}
