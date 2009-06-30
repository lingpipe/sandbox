package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class EntryCombination {

    // <!ELEMENT EntryCombinationList (EntryCombination+) >
    // <!ELEMENT EntryCombination     (ECIN,ECOUT)>
    // <!ELEMENT ECIN (DescriptorReferredTo,QualifierReferredTo) >
    // <!ELEMENT ECOUT (DescriptorReferredTo,QualifierReferredTo? ) >

    // <!ELEMENT DescriptorReferredTo (DescriptorUI, DescriptorName) >
    // <!ELEMENT DescriptorUI (#PCDATA) >
    // <!ELEMENT DescriptorName (String) >

    // <!ELEMENT QualifierReferredTo (QualifierUI, QualifierName) >
    // <!ELEMENT QualifierUI (#PCDATA) >
    // <!ELEMENT QualifierName (String) >



    private final String mInDescriptor;
    private final String mInQualifier;
    private final String mOutDescriptor;
    private final String mOutQualifier;
    public EntryCombination(String inDescriptor,
                            String inQualifier,
                            String outDescriptor,
                            String outQualifier) {
        mInDescriptor = inDescriptor;
        mInQualifier = inQualifier;
        mOutDescriptor = outDescriptor;
        mOutQualifier = outQualifier.length() == 0
            ? null
            : outQualifier;
    }

    public String inDescriptor() {
        return mInDescriptor;
    }

    public String inQualifier() {
        return mInQualifier;
    }

    public String outDescriptor() {
        return mOutDescriptor;
    }

    // may be null
    public String outQualifier() {
        return mOutQualifier;
    }

    @Override
    public String toString() {
        return "InDesc=" + inDescriptor()
            + "; InQual=" + inQualifier()
            + "; OutDesc=" + outDescriptor()
            + "; OutQual=" + outQualifier();
    }

    static class Handler extends DelegateHandler {
        private final TextAccumulatorHandler mDescriptorUiAccumulator;
        private final Mesh.StringHandler mDescriptorNameHandler;
        private final TextAccumulatorHandler mQualifierUiAccumulator;
        private final Mesh.StringHandler mQualifierNameHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mDescriptorUiAccumulator = new TextAccumulatorHandler();
            setDelegate(MeshParser.DESCRIPTOR_UI_ELEMENT,
                        mDescriptorUiAccumulator);
            mDescriptorNameHandler = new Mesh.StringHandler(parent);
            setDelegate(MeshParser.DESCRIPTOR_NAME_ELEMENT,
                        mDescriptorNameHandler);
            mQualifierUiAccumulator = new TextAccumulatorHandler();
            setDelegate(MeshParser.QUALIFIER_UI_ELEMENT,
                        mQualifierUiAccumulator);
            mQualifierNameHandler = new Mesh.StringHandler(parent);
            setDelegate(MeshParser.QUALIFIER_NAME_ELEMENT,
                        mQualifierNameHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mDescriptorUiAccumulator.reset();
            mDescriptorNameHandler.reset();
            mQualifierUiAccumulator.reset();
            mQualifierNameHandler.reset();
        }
        public EntryCombination getEntryCombination() {
            return new EntryCombination(mDescriptorUiAccumulator.getText(),
                                        mDescriptorNameHandler.getText(),
                                        mQualifierUiAccumulator.getText(),
                                        mQualifierNameHandler.getText());
        }
    }

    static class ListHandler extends DelegateHandler {
        private final List<EntryCombination> mEntryCombinationList
            = new ArrayList<EntryCombination>();
        private final Handler mEntryCombinationHandler;
        public ListHandler(DelegatingHandler parent) {
            super(parent);
            mEntryCombinationHandler = new Handler(parent);
            setDelegate(MeshParser.ENTRY_COMBINATION_ELEMENT,
                        mEntryCombinationHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        @Override
        public void finishDelegate(String qName, DefaultHandler handler) {
            mEntryCombinationList.add(mEntryCombinationHandler.getEntryCombination());
        }
        public void reset() {
            mEntryCombinationList.clear();
            mEntryCombinationHandler.reset();
        }
        public List<EntryCombination> getEntryCombinationList() {
            return mEntryCombinationList;
        }
    }

}