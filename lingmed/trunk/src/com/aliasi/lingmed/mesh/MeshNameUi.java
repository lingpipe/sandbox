package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A {@code MeshNameUi} combines a name with a universal identifier
 * (UI).  Such combinations are used in several places within MeSH to
 * identify objects by both name and identifier.
 * 
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshNameUi {

    private final String mName;
    private final String mUi;

    public MeshNameUi(String name,
                      String ui) {
        mName = name;
        mUi = ui;
    }

    public String name() {
        return mName;
    }

    public String ui() {
        return mUi;
    }

    @Override
    public String toString() {
        return name() + ":" + ui();
    }


    static class Handler extends DelegateHandler {
        final Mesh.StringHandler mNameHandler;
        final TextAccumulatorHandler mUiHandler;
        public Handler(DelegatingHandler parent, 
                       String nameTag, String uiTag) {
            super(parent);
            mNameHandler = new Mesh.StringHandler(parent);
            setDelegate(nameTag,mNameHandler);
            mUiHandler = new TextAccumulatorHandler();
            setDelegate(uiTag,mUiHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mNameHandler.reset();
            mUiHandler.reset();
        }
        public MeshNameUi getNameUi() {
            String name = mNameHandler.getText();
            String ui = mUiHandler.getText();
            return (name.length() == 0 && ui.length() == 0)
                ? null
                : new MeshNameUi(name,ui);
        }
    }
    
}