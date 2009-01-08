package com.aliasi.xhtml;

import java.util.List;

class AbstractInternationalizedElement extends AbstractIdElement {

    AbstractInternationalizedElement(String tag, boolean mutable) {
        super(tag,mutable);
    }

    AbstractInternationalizedElement(String tag, List contentList,
                                     boolean mutable) {
        super(tag,contentList,mutable);
    }

    public void setLanguage(String languageCode) {
        // validNmToken(languageCode);
        set(LANG,languageCode,"NMTOKEN");
    }

    public void setXmlLanguage(String languageCode) {
        // validNmToken(languageCode);
        set(XML_LANG,languageCode);
    }

    public void setDirectionLTR() {
        set(DIR_ATT,"ltr");
    }

    public void setDirectionRTL() {
        set(DIR_ATT,"rtl");
    }


    

}
