package com.aliasi.xhtml;

public class Th extends AbstractCellAlignElement {

    public Th() {
        super(TH,true);
    }

    public void add(FlowElement elt) {
        addContent(elt);
    }

}
