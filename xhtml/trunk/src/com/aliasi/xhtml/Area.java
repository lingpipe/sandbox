package com.aliasi.xhtml;

public class Area extends AbstractFocusElement {

    public Area() {
        this("");
    }

    public Area(String altText) {
        super(AREA,true);
        set(ALT,altText);
    }


    public void setAlt(String text) {
        set(ALT,text);
    }

    public void setShape() {
        set(SHAPE,"rect");
    }

    public void setCoords(String coords) {
        set(COORDS,coords);
    }

    public void setHref(String uri) {
        set(HREF,uri);
    }

    public void setNoHref() {
        set(NOHREF,"nohref");
    }

}
