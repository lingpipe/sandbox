package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshDescriptor {

    // <!ENTITY  % DescriptorReference "(DescriptorUI, DescriptorName)">
    // <!ELEMENT DescriptorUI (#PCDATA) >
    // <!ELEMENT DescriptorName (String) >

    private final String mUi;
    private final String mName;
    
    MeshDescriptor(String ui, String name) {
        mUi = ui;
        mName = name;
    }

    public String ui() {
        return mUi;
    }

    public String name() {
        return mName;
    }

    @Override
    public String toString() {
        return "UI=" + ui() + "; name=" + name();
    }
    
    static class Handler extends DelegateHandler {
        final TextAccumulatorHandler mUiHandler;
        final Mesh.StringHandler mNameHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.DESCRIPTOR_UI_ELEMENT,
                        mUiHandler);
            mNameHandler = new Mesh.StringHandler(parent);
            setDelegate(MeshParser.DESCRIPTOR_NAME_ELEMENT,
                        mNameHandler);
        }
        public MeshDescriptor getDescriptor() {
            return new MeshDescriptor(mUiHandler.getText().trim(),
                                      mNameHandler.getText().trim());
        }
        public void reset() {
            mUiHandler.reset();
            mNameHandler.reset();
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
    }

    static class ListHandler extends DelegateHandler {
        final List<MeshDescriptor> mDescriptorList
            = new ArrayList<MeshDescriptor>();
        final Handler mDescriptorHandler;
        public ListHandler(DelegatingHandler parent, String tagQName) {
            super(parent);
            mDescriptorHandler = new Handler(parent);
            setDelegate(tagQName,mDescriptorHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mDescriptorList.clear();
            mDescriptorHandler.reset();
        }
        public void finishDelegate(String qName, DefaultHandler hanlder) {
            mDescriptorList.add(mDescriptorHandler.getDescriptor());
        }
        public List<MeshDescriptor> getDescriptorList() {
            return new ArrayList<MeshDescriptor>(mDescriptorList); 
        }
    }

}