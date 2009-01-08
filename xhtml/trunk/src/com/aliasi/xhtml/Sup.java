package com.aliasi.xhtml;

public class Sup extends AbstractInlineContainerElement implements PhraseElement {

    public Sup() {
        super(SUP,true);
    }

    boolean prohibitedAncestor(int mask) {
        return (PRE_MASK & mask) != 0;
    }
}
