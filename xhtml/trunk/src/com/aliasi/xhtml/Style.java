package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class Style extends AbstractInternationalizedElement {

    final TextContent mTextContent = new TextContent();

    public Style() {
        this("","");
    }

    public Style(String type, String style) {
        super(STYLE,null,true);
        set(TYPE,type);
        mTextContent.set(style);
    }


    public String style() {
        return mTextContent.text();
    }

    public List contents() {
        return contentsInternal();
    }

    List contentsInternal() {
        return Collections.singletonList(mTextContent);
    }


    public void set(CharSequence style) {
        mTextContent.set(style);
    }

    public void setType(String type) {
        set(TYPE,type);
    }

    public void setMedia(String mediaDesc) {
        set(MEDIA,mediaDesc);
    }

    public void setTitle(String title) {
        set(TITLE,title);
    }

    public void setSrc(String uri) {
        set(SRC,uri);
    }

    public void setXmlSpace() {
        set(XML_SPACE,"preserve");
    }

    public void setDefer() {
        set(DEFER,"defer");
    }


}
