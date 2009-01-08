package com.aliasi.xhtml;

public class BlockQuote
    extends AbstractBlockContainerElement 
    implements BlockTextElement {

    public BlockQuote() {
        super(BLOCKQUOTE,true);
    }

    public void setCite(String uri) {
        set(CITE_ATT,uri);
    }

}
