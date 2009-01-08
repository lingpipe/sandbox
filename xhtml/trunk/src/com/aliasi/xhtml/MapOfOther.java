package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class MapOfOther extends Map {

    public MapOfOther(String id) {
        super(id,true);
    }

    List contentsInternal() {
        List contents = super.contents();
        return contents.isEmpty()
            ? Collections.singletonList(new Div())
            : contents;
    }

    public void add(LittleBlockElement block) {
        addContent(block);
    }

    public void add(Form form) {
        addContent(form);
    }

    public void add(MiscElement element) {
        addContent(element);
    }

}
