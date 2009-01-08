package com.aliasi.xhtml;

class AbstractInsertDeleteElement 
    extends AbstractFlowContainerElement 
    implements MiscInlineElement {

    AbstractInsertDeleteElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    public void setCite(String uri) {
        set(CITE_ATT,uri);
    }

    public void setDateTime(String dateTime) {
        set(DATETIME,dateTime);
    }
    
}
