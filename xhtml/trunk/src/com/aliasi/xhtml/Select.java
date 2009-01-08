package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class Select extends AbstractAttrsElement implements InlineFormElement {

    public Select() {
        super(SELECT,true);
    }

    List contentsInternal() {
        List contents = super.contentsInternal();
        return contents.isEmpty()
            ? Collections.singletonList(new Option())
            : contents;
    }


    public void setName(String name) {
        set(NAME,name);
    }

    public void setSize(String number) {
        set(SIZE,number);
    }

    public void setMultiple() {
        set(MULTIPLE,"multiple");
    }

    public void setDisabled() {
        set(DISABLED,"disabled");
    }
    
    public void setTabIndex(String number) {
        set(TABINDEX,number);
    }

    public void setOnFocus(String script) {
        set(ONFOCUS,script);
    }

    public void setOnBlur(String script) {
        set(ONBLUR,script);
    }

    public void setOnChange(String script) {
        set(ONCHANGE,script);
    }

    public void add(Option opt) {
        addContent(opt);
    }

    public void add(OptGroup optGroup) {
        addContent(optGroup);
    }

    boolean prohibitedAncestor(int mask) {
        return (BUTTON_MASK & mask) != 0;
    }

}
