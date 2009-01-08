package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class Dl extends AbstractAttrsElement implements ListElement {

    public Dl() {
        super(DL,true);
    }

    List contentsInternal() {
        List contents = super.contentsInternal();
        return contents.isEmpty()
            ? Collections.singletonList(new Dd())
            : contents;
    }

    public final void add(Dd definition) {
        addContent(definition);
    }

    public final void add(Dt term) {
        addContent(term);
    }


}
