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
public class MeshEntryCombination {

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



    private final MeshNameUi mInDescriptor;
    private final MeshNameUi mInQualifier;
    private final MeshNameUi mOutDescriptor;
    private final MeshNameUi mOutQualifier;

    MeshEntryCombination(MeshNameUi inDescriptor,
                         MeshNameUi inQualifier,
                         MeshNameUi outDescriptor,
                         MeshNameUi outQualifier) {
        mInDescriptor = inDescriptor;
        mInQualifier = inQualifier;
        mOutDescriptor = outDescriptor;
        mOutQualifier = outQualifier;
    }

    public MeshNameUi inDescriptor() {
        return mInDescriptor;
    }

    public MeshNameUi inQualifier() {
        return mInQualifier;
    }

    public MeshNameUi outDescriptor() {
        return mOutDescriptor;
    }

    public MeshNameUi outQualifier() {
        return mOutQualifier;
    }


    /**
     * Return a string-based representation of this entry combination.
     * All of the information in this string is available programatically
     * through the other methods in this class.
     *
     * @return String-based representation of this entry combination.
     */
    @Override
    public String toString() {
        return "  In Descriptor=" + inDescriptor()
            + "\n  In Qualifier=" + inQualifier()
            + "\n  Out Descriptor=" + outDescriptor()
            + "\n  Out Qualifier=" + outQualifier();
    }

    static class DescriptorQualifierHandler extends DelegateHandler {
        private final MeshNameUi.Handler mDescriptorHandler;
        private final MeshNameUi.Handler mQualifierHandler;
        public DescriptorQualifierHandler(DelegatingHandler parent) {
            super(parent);
            mDescriptorHandler 
                = new MeshNameUi.Handler(parent,
                                         MeshParser.DESCRIPTOR_NAME_ELEMENT,
                                         MeshParser.DESCRIPTOR_UI_ELEMENT);
            setDelegate(MeshParser.DESCRIPTOR_REFERRED_TO_ELEMENT,
                        mDescriptorHandler);
            mQualifierHandler
                = new MeshNameUi.Handler(parent,
                                         MeshParser.QUALIFIER_NAME_ELEMENT,
                                         MeshParser.QUALIFIER_UI_ELEMENT);
            setDelegate(MeshParser.QUALIFIER_REFERRED_TO_ELEMENT,
                        mQualifierHandler);
        }
        public void reset() {
            mDescriptorHandler.reset();
            mQualifierHandler.reset();
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public MeshNameUi getDescriptor() {
            return mDescriptorHandler.getObject();
        }
        public MeshNameUi getQualifier() {
            return mQualifierHandler.getObject();
        }
    }

    static class Handler extends BaseHandler<MeshEntryCombination> {
        private final DescriptorQualifierHandler mInHandler;
        private final DescriptorQualifierHandler mOutHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mInHandler = new DescriptorQualifierHandler(parent);
            setDelegate(MeshParser.ECIN_ELEMENT,mInHandler);
            mOutHandler = new DescriptorQualifierHandler(parent);
            setDelegate(MeshParser.ECOUT_ELEMENT,mOutHandler);
        }
        public void reset() {
            mInHandler.reset();
            mOutHandler.reset();
        }
        public MeshEntryCombination getObject() {
            return new MeshEntryCombination(mInHandler.getDescriptor(),
                                            mInHandler.getQualifier(),
                                            mOutHandler.getDescriptor(),
                                            mOutHandler.getQualifier());
        }
    }

    static class ListHandler extends com.aliasi.lingmed.mesh.ListHandler<MeshEntryCombination> {
        public ListHandler(DelegatingHandler parent) {
            super(parent,new Handler(parent),MeshParser.ENTRY_COMBINATION_ELEMENT);
        }
    }


}