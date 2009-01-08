package com.aliasi.xhtml;

import java.util.List;

class AbstractIdElement extends AbstractElement {
    
    AbstractIdElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    AbstractIdElement(String tag, List contentList, boolean mutable) {
        super(tag,contentList,mutable);
    }

    public void setId(String id) {
        // validId(id);
        set(ID,id,"ID");
    }

}
