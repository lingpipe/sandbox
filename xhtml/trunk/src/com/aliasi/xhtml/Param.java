package com.aliasi.xhtml;

public class Param extends AbstractIdElement {

    public Param() {
        super(PARAM,true);
    }

    public void setName(String name) {
        set(NAME,name);
    }

    public void setValue(String value) {
        set(VALUE,value);
    }

    public void setValueTypeData() {
        set(VALUETYPE,"data");
    }

    public void setValueTypeRef() {
        set(VALUETYPE,"ref");
    }

    public void setValueTypeObject() {
        set(VALUETYPE,"object");
    }

    public void setType(String contentType) {
        set(TYPE,contentType);
    }
}
