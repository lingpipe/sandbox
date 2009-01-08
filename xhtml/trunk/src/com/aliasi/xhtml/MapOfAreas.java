package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class MapOfAreas extends Map {

    public MapOfAreas(String id) {
        super(id,true);
    }

    List contentsInternal() {
        List contents = super.contentsInternal();
        return contents.isEmpty()
            ? Collections.singletonList(new Area())
            : contents;
    }

    public void add(Area area) {
        addContent(area);
    }

    

}
