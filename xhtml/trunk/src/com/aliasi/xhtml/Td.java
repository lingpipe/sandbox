package com.aliasi.xhtml;

public class Td extends AbstractCellAlignElement {

    public Td() {
        super(TD,true);
    }

    public void add(FlowElement elt) {
        addContent(elt);
    }

    public void setAbbr(String text) {
        set(ABBR_ATT,text);
    }

    public void setAxis(String axis) {
        set(AXIS,axis);
    }

    public void setHeaders(String idRefs) {
        set(HEADERS,idRefs);
    }

    public void setScopeRow() {
        set(SCOPE,"row");
    }
    public void setScopeCol() {
        set(SCOPE,"col");
    }
    public void setScopeRowGroup() {
        set(SCOPE,"rowgroup");
    }
    public void setScopeColGroup() {
        set(SCOPE,"colgroup");
    }

    public void setRowSpan(String number) {
        set(ROWSPAN,number);
    }

    public void setColSpan(String number) {
        set(COLSPAN,number);
    }


    
}
