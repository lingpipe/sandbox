package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MeshSemanticType {


    // <!ELEMENT SemanticTypeList (SemanticType+)>
    // <!ELEMENT SemanticType (SemanticTypeUI, SemanticTypeName) >
    // <!ELEMENT SemanticTypeUI (#PCDATA)>
    // <!ELEMENT SemanticTypeName (#PCDATA)>

    private final String mUi;
    private final String mName;

    public MeshSemanticType(String ui, 
                            String name) {
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
        return "UI=" + ui() + "; Name=" + name();
    }

    static class Handler extends DelegateHandler {
        final TextAccumulatorHandler mUiHandler;
        final TextAccumulatorHandler mNameHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.SEMANTIC_TYPE_UI_ELEMENT,mUiHandler);
            mNameHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.SEMANTIC_TYPE_NAME_ELEMENT,mNameHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mUiHandler.reset();
            mNameHandler.reset();
        }
        public MeshSemanticType getSemanticType() {
            return new MeshSemanticType(mUiHandler.getText(),
                                        mNameHandler.getText());
        }
    }

    static class ListHandler extends DelegateHandler {
        final Handler mHandler;
        final List<MeshSemanticType> mSemanticTypeList
            = new ArrayList<MeshSemanticType>();
        public ListHandler(DelegatingHandler parent) {
            super(parent);
            mHandler = new Handler(parent);
            setDelegate(MeshParser.SEMANTIC_TYPE_ELEMENT,mHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            mSemanticTypeList.add(mHandler.getSemanticType());
        }
        public void reset() {
            mSemanticTypeList.clear();
            mHandler.reset();
        }
        public List<MeshSemanticType> getList() {
            return new ArrayList<MeshSemanticType>(mSemanticTypeList);
        }
    }

}