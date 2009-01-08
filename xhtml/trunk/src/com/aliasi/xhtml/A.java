package com.aliasi.xhtml;

public class A 
    extends AbstractFocusElement 
    implements LittleInlineElement {

    public A() {
        super(A,true);
        mParentMask = A_MASK;
    }

    public void setCharset(String charset) {
        set(CHARSET,charset);
    }

    public void setType(String contentType) {
        set(TYPE,contentType);
    }

    public void setName(String nmToken) {
        set(NAME,nmToken);
    }

    public void setHref(String uri) {
        set(HREF,uri);
    }

    public void setHrefLang(String languageCode) {
        set(HREFLANG,languageCode);
    }

    public void setRel(String linkTypes) {
        set(REL,linkTypes);
    }

    public void setRev(String linkTypes) {
        set(REV,linkTypes);
    }
    
    public void setShape() {
        set(SHAPE,"rect");
    }

    public void setCoords(String coords) {
        set(COORDS,coords);
    }

    public void add(String textContent) {
        addContent(textContent);
    }

    public void add(SpecialElement elt) {
        addContent(elt);
    }

    public void add(FontStyleElement elt) {
        addContent(elt);
    }

    public void add(PhraseElement elt) {
        addContent(elt);
    }

    public void add(InlineFormElement elt) {
        addContent(elt);
    }

    public void add(MiscInlineElement elt) {
        addContent(elt);
    }


    boolean prohibitedAncestor(int mask) {
        //        System.out.println("A: Testing mask=" + mask);
        return (A_MASK & mask) != 0;
    }

}
