package com.aliasi.xhtml;

import java.util.Collections;
import java.util.List;

public class Script extends AbstractIdElement implements MiscInlineElement {

    final TextContent mTextContent = new TextContent();

    public Script(String contentType) {
        super(SCRIPT,true);
        set(TYPE,contentType);
    }
    

    public String script() {
        return mTextContent.text();
    }

    public List contents() {
        return contentsInternal();
    }
    
    List contentsInternal() {
        return Collections.singletonList(mTextContent);
    }


    public void setCharset(String charset) {
        set(CHARSET,charset);
    }

    public void setSrc(String uri) {
        set(SRC,uri);
    }

    public void setDefer() {
        set(DEFER,"defer");
    }

    public void setXmlSpace() {
        set(XML_SPACE,"preserver");
    }

    public void set(CharSequence script) {
        mTextContent.set(script);
    }
}
