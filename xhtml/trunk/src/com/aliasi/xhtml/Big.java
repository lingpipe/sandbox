package com.aliasi.xhtml;

public class Big extends AbstractInlineContainerElement implements FontStyleElement {

    public Big() {
        super(BIG,true);
    }

    boolean prohibitedAncestor(int mask) {
        return (PRE_MASK & mask) != 0;
    }

}
