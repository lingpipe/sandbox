package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class Tr extends AbstractCellAlignElement {

    public Tr() {
        super(TR,true);
    }

    public List contentsInternal() {
        List contents = super.contentsInternal();
        return contents.isEmpty()
            ? Collections.singletonList(new Td())
            : contents;
    }

    public void add(Th head) {
        addContent(head);
    }

    public void add(Td data) {
        addContent(data);
    }

}
