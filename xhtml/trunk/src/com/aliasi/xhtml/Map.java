package com.aliasi.xhtml;

public class Map extends AbstractAttrsElement implements SpecialPreElement {

    Map(String id, boolean mutable) {
        super(MAP,mutable);
        set(ID,id);
    }

    public void setName(String nmToken) {
        set(NAME,nmToken);
    }

}
