package com.aliasi.xhtml;

public class Small extends AbstractInlineContainerElement implements FontStyleElement {

    public Small() {
        super(SMALL,true);
    }

    boolean prohibitedAncestor(int mask) {
        return (PRE_MASK & mask) != 0;
    }

}
