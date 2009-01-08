package com.aliasi.xhtml;

public class Object extends AbstractAttrsElement implements SpecialElement {

    public Object() {
        super(OBJECT,true);
    }

    public void setDeclare() {
        set(DECLARE,"declare");
    }

    public void setClassId(String uri) {
        set(CLASSID,uri);
    }

    public void setCodeBase(String uri) {
        set(CODEBASE,uri);
    }

    public void setData(String uri) {
        set(DATA,uri);
    }

    public void setType(String contentType) {
        set(TYPE,contentType);
    }

    public void setCodeType(String contentType) {
        set(CODETYPE,contentType);
    }

    public void setArchive(String uriList) {
        set(ARCHIVE,uriList);
    }

    public void setStandby(String text) {
        set(STANDBY,text);
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

    public void setName(String nmToken) {
        set(NAME,nmToken);
    }

    public void setTabIndex(String number) {
        set(TABINDEX,number);
    }

    public void add(String text) {
        addContent(text);
    }

    public void add(Param param) {
        addContent(param);
    }

    public void add(Form form) {
        addContent(form);
    }

    public void add(LittleBlockElement elt) {
        addContent(elt);
    }

    public void add(LittleInlineElement elt) {
        addContent(elt);
    }

    public void add(MiscElement elt) {
        addContent(elt);
    }
    
    boolean prohibitedAncestor(int mask) {
        return (PRE_MASK & mask) != 0;
    }


}
