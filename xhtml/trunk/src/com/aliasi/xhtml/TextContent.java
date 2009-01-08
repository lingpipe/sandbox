package com.aliasi.xhtml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Set;

public class TextContent extends AbstractContent {

    private char[] mCharacters;

    public TextContent() {
        mCharacters = EMPTY_CHARS;
    }
    
    public TextContent(CharSequence cSeq) {
        set(cSeq);
    }

    public String text() {
        return new String(mCharacters);
    }

    public void set(CharSequence cSeq) {
        int len = cSeq.length();
        mCharacters = new char[len];
        for (int i = 0; i < len; ++i)
            mCharacters[i] = cSeq.charAt(i);
    }

    public final void writeTo(ContentHandler handler) throws SAXException {
        handler.characters(mCharacters,0,mCharacters.length);
    }

    void propagateMask(int mask) {
        /* do not need to check with text content */
    }

    boolean isCyclic(Set visitedSet) {
        return false; // never get cycle through text
    }

    static final char[] EMPTY_CHARS = new char[0];

}
