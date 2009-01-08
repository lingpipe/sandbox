package com.aliasi.xhtml;

public class Q extends AbstractInlineContainerElement implements PhraseElement {

    public Q() {
        super(Q,true);
    }

    public void setCite(String uri) {
        set(CITE,uri);
    }

}
