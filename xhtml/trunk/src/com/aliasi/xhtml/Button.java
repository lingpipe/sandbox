package com.aliasi.xhtml;

public class Button 
    extends AbstractFocusElement 
    implements InlineFormElement {

    public Button() {
        super(BUTTON,true);
    }

    public void setName(String name) {
        set(NAME,name);
    }

    public void setValue(String value) {
        set(VALUE,value);
    }

    public void setTypeButton() {
        set(TYPE,"button");
    }

    public void setTypeSubmit() {
        set(TYPE,"submit");
    }

    public void setTypeReset() {
        set(TYPE,"reset");
    }

    public void setDisabled() {
        set(DISABLED,"disabled");
    }

    public void add(String text) {
        addContent(text);
    }

    public void add(P p) {
        addContent(p);
    }

    public void add(Div div) {
        addContent(div);
    }

    public void add(Table table) {
        addContent(table);
    }

    public void add(HeadingElement elt) {
        addContent(elt);
    }

    public void add(ListElement elt) {
        addContent(elt);
    }

    public void add(BlockTextElement elt) {
        addContent(elt);
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

    public void add(MiscElement elt) {
        addContent(elt);
    }

    boolean prohibitedAncestor(int mask) {
        return (BUTTON_MASK & mask) != 0;
    }

}
