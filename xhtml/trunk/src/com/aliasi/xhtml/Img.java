package com.aliasi.xhtml;

public class Img extends AbstractAttrsElement implements SpecialElement {

    public Img(String srcUri, String altText) {
        super(IMG,true);
        set(SRC,srcUri);
        set(ALT,altText);
    }

    public void setLongDesc(String uri) {
        set(LONGDESC,uri);
    }

    public void setHeight(String length) {
        set(HEIGHT,length);
    }

    public void setWidth(String length) {
        set(WIDTH,length);
    }

    public void setUseMap(String uri) {
        set(USEMAP,uri);
    }

    public void setIsMap() {
        set(ISMAP,"ismap");
    }


    boolean prohibitedAncestor(int mask) {
        return (PRE_MASK & mask) != 0;
    }

}
