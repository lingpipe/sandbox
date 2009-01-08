package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class OptGroup extends AbstractAttrsElement {

    public OptGroup() {
        this("");
    }

    public OptGroup(String label) {
        super(OPTGROUP,true);
        set(LABEL,label);
    }

    List contentsInternal() {
        List contents = super.contentsInternal();
        return contents.isEmpty()
            ? Collections.singletonList(new Option())
            : contents;
    }
    

    public void setLabel(String label) {
        set(LABEL,label);
    }

    public void setDisabled() {
        set(DISABLED,"disabled");
    }

    public void add(Option option) {
        addContent(option);
    }

}
