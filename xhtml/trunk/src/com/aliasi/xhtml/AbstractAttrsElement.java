package com.aliasi.xhtml;

import java.util.List;

class AbstractAttrsElement extends AbstractInternationalizedElement {

    AbstractAttrsElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    AbstractAttrsElement(String tag, List contentList, boolean mutable) {
        super(tag,contentList,mutable);
    }

    public void setClass(String className) {
        set(CLASS,className);
    }

    public void setStyle(String style) {
        set(STYLE,style);
    }

    public void setTitle(String title) {
        set(TITLE,title);
    }

    public void setOnClick(String script) {
        set(ONCLICK,script);
    }

    public void setOnDblClick(String script) {
        set(ONDBLCLICK,script);
    }

    public void setOnMouseDown(String script) {
        set(ONMOUSEDOWN,script);
    }

    public void setOnMouseUp(String script) {
        set(ONMOUSEUP,script);
    }

    public void setOnMouseOver(String script) {
        set(ONMOUSEOVER,script);
    }

    public void setOnMouseMove(String script) {
        set(ONMOUSEMOVE,script);
    }

    public void setOnMouseOut(String script) {
        set(ONMOUSEOUT,script);
    }

    public void setOnKeyPress(String script) {
        set(ONKEYPRESS,script);
    }

    public void setOnKeyDown(String script) {
        set(ONKEYDOWN,script);
    }

    public void setOnKeyUp(String script) {
        set(ONKEYUP,script);
    }


}
