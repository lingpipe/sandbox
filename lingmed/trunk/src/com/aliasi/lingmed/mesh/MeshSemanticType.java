package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

class MeshSemanticType {

    static class Handler extends BaseHandler<MeshNameUi> {
        final TextAccumulatorHandler mUiHandler;
        final TextAccumulatorHandler mNameHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.SEMANTIC_TYPE_UI_ELEMENT,mUiHandler);
            mNameHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.SEMANTIC_TYPE_NAME_ELEMENT,mNameHandler);
        }
        @Override
        public void reset() {
            mUiHandler.reset();
            mNameHandler.reset();
        }
        public MeshNameUi getObject() {
            return new MeshNameUi(mNameHandler.getText().trim(),
                                  mUiHandler.getText().trim());
        }
    }

    static class ListHandler extends BaseListHandler<MeshNameUi> {
        public ListHandler(DelegatingHandler parent) {
            super(parent,
                  new Handler(parent),
                  MeshParser.SEMANTIC_TYPE_ELEMENT);
        }
    }

}