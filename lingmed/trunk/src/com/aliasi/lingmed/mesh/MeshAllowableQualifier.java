package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MeshAllowableQualifier {

    // <!ELEMENT AllowableQualifiersList (AllowableQualifier+) >
    // <!ELEMENT AllowableQualifier (QualifierReferredTo,Abbreviation )>
    // <!ELEMENT Abbreviation (#PCDATA)>
    // <!ELEMENT QualifierReferredTo (%QualifierReference;) >
    // <!ENTITY  % QualifierReference "(QualifierUI, QualifierName)">
    // <!ELEMENT QualifierUI (#PCDATA) >
    // <!ELEMENT QualifierName (String) >
    
    // AllowableQualifier(QualifierUI,QualifierName,Abbreviation)

    final String mQualifierUi;
    final String mQualifierName;
    final String mAbbreviation;

    public MeshAllowableQualifier(String qualifierUi,
                                  String qualifierName,
                                  String abbreviation) {
        mQualifierUi = qualifierUi;
        mQualifierName = qualifierName;
        mAbbreviation = abbreviation;
    }

    public String qualifierUi() {
        return mQualifierUi;
    }

    public String qualifierName() {
        return mQualifierName;
    }

    public String abbreviation() {
        return mAbbreviation;
    }

    @Override
    public String toString() {
        return "UI=" + qualifierUi() + "; Name=" + qualifierName() + "; Abbrev=" + abbreviation();
    }
    
    static class Handler extends DelegateHandler {
        private final TextAccumulatorHandler mQualifierUiHandler;
        private final Mesh.StringHandler mQualifierNameHandler;
        private final TextAccumulatorHandler mAbbreviationHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mQualifierUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.QUALIFIER_UI_ELEMENT,mQualifierUiHandler);
            mQualifierNameHandler = new Mesh.StringHandler(parent);
            setDelegate(MeshParser.QUALIFIER_NAME_ELEMENT,mQualifierNameHandler);
            mAbbreviationHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.ABBREVIATION_ELEMENT,mAbbreviationHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mQualifierUiHandler.reset();
            mQualifierNameHandler.reset();
            mAbbreviationHandler.reset();
        }
        public MeshAllowableQualifier getQualifier() {
            return new MeshAllowableQualifier(mQualifierUiHandler.getText().trim(),
                                              mQualifierNameHandler.getText().trim(),
                                              mAbbreviationHandler.getText().trim());
        }
    }

    static class ListHandler extends DelegateHandler {
        final List<MeshAllowableQualifier> mQualifierList
            = new ArrayList<MeshAllowableQualifier>();
        final Handler mAllowableQualifierHandler;
        public ListHandler(DelegatingHandler parent) {
            super(parent);
            mAllowableQualifierHandler = new Handler(parent);
            setDelegate(MeshParser.ALLOWABLE_QUALIFIER_ELEMENT,
                        mAllowableQualifierHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mQualifierList.clear();
            mAllowableQualifierHandler.reset(); // not really nec.
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (!MeshParser.ALLOWABLE_QUALIFIER_ELEMENT.equals(qName)) return;
            mQualifierList.add(mAllowableQualifierHandler.getQualifier());
        }
        public List<MeshAllowableQualifier> getAllowableQualifierList() {
            return new ArrayList<MeshAllowableQualifier>(mQualifierList);
        }
    }

}