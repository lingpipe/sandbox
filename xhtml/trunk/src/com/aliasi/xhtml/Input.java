package com.aliasi.xhtml;

public class Input 
    extends AbstractFocusElement 
    implements InlineFormElement {

    public Input() {
        super(INPUT,true);
    }

    public void setTypeText() {
        set(TYPE,"text");
    }
    public void setTypePassword() {
        set(TYPE,"password");
    }
    public void setTypeCheckbox() {
        set(TYPE,"checkbox");
    }
    public void setTypeRadio() {
        set(TYPE,"radio");
    }
    public void setTypeSubmit() {
        set(TYPE,"submit");
    }
    public void setTypeReset() {
        set(TYPE,"reset");
    }
    public void setTypeFile() {
        set(TYPE,"file");
    }
    public void setTypeHidden() {
        set(TYPE,"hidden");
    }
    public void setTypeImage() {
        set(TYPE,"image");
    }
    public void setTypeButton() {
        set(TYPE,"button");
    }

    public void setName(String name) {
        set(NAME,name);
    }

    public void setValue(String value) {
        set(VALUE,value);
    }

    public void setChecked() {
        set(CHECKED,"checked");
    }

    public void setDisabled() {
        set(DISABLED,"disabled");
    }

    public void setReadOnly() {
        set(READONLY,"readonly");
    }

    public void setSize(String size) {
        set(SIZE,size);
    }

    public void setMaxLength(String number) {
        set(MAXLENGTH,number);
    }

    public void setSrc(String uri) {
        set(SRC,uri);
    }

    public void setAlt(String alt) {
        set(ALT,alt);
    }

    public void setUseMap(String uri) {
        set(USEMAP,uri);
    }

    public void setOnSelect(String script) {
        set(ONSELECT,script);
    }

    public void setOnChange(String script) {
        set(ONCHANGE,script);
    }

    public void setAccept(String contentTypes) {
        set(ACCEPT,contentTypes);
    }
          

    boolean prohibitedAncestor(int mask) {
        return (BUTTON_MASK & mask) != 0;
    }

}
