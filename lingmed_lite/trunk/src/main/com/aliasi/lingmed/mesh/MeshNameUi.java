package com.aliasi.lingmed.mesh;

import com.aliasi.lingpipe.xml.DelegateHandler;
import com.aliasi.lingpipe.xml.DelegatingHandler;
import com.aliasi.lingpipe.xml.TextAccumulatorHandler;

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

    MeshNameUi(String name,
               String ui) {
        mName = name;
        mUi = ui;
    }

    /**
     * Returns the name for this object.
     *
     * @return Name for object.
     */
    public String name() {
        return mName;
    }

    /**
     * Returns the unique identifier (UI) for this object.
     *
     * @return Unique identifier for this object.
     */
    public String ui() {
        return mUi;
    }

    /**
     * Returns a string-based representation of this name and unique
     * identifier.
     *
     * @return String-based representation of this name and unique
     * identifier.
     */
    @Override
    public String toString() {
        return name() + ":" + ui();
    }


    static class Handler extends BaseHandler<MeshNameUi> {
        final Mesh.StringHandler mNameHandler;
        final TextAccumulatorHandler mUiHandler;
        public Handler(DelegatingHandler parent) {
            this(parent,
                 MeshParser.DESCRIPTOR_NAME_ELEMENT,
                 MeshParser.DESCRIPTOR_UI_ELEMENT);
        }
        public Handler(DelegatingHandler parent, 
                       String nameTag, String uiTag) {
            super(parent);
            mNameHandler = new Mesh.StringHandler(parent);
            setDelegate(nameTag,mNameHandler);
            mUiHandler = new TextAccumulatorHandler();
            setDelegate(uiTag,mUiHandler);
        }
        public void reset() {
            mNameHandler.reset();
            mUiHandler.reset();
        }
        public MeshNameUi getObject() {
            String name = mNameHandler.getObject();
            String ui = mUiHandler.getText();
            return (name.length() == 0 && ui.length() == 0)
                ? null
                : new MeshNameUi(name,ui);
        }
    }


    static class ListHandler extends DelegateHandler {
        final List<MeshNameUi> mList
            = new ArrayList<MeshNameUi>();
        final Handler mHandler;
        public ListHandler(DelegatingHandler parent, String containingTag) {
            this(parent,
                 containingTag,
                 MeshParser.DESCRIPTOR_NAME_ELEMENT,
                 MeshParser.DESCRIPTOR_UI_ELEMENT);
        }                           
        public ListHandler(DelegatingHandler parent, 
                           String containingTag,
                           String nameTag, 
                           String uiTag) {
            super(parent);
            mHandler = new Handler(parent,nameTag,uiTag);
            setDelegate(containingTag,mHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mList.clear();
            mHandler.reset();
        }
        public void finishDelegate(String qName, DefaultHandler hanlder) {
            mList.add(mHandler.getObject());
        }
        public List<MeshNameUi> getList() {
            return new ArrayList<MeshNameUi>(mList); 
        }
    }
    
}
