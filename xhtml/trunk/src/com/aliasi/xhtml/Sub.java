package com.aliasi.xhtml;

public class Sub 
    extends AbstractInlineContainerElement 
    implements PhraseElement {

    public Sub() {
        super(SUB,true);
    }

    boolean prohibitedAncestor(int mask) {
        return (PRE_MASK & mask) != 0;
    }

}
