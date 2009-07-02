package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * An {@code MeshAllowableQualifier} provides information about a
 * qualification that may be used with a term.  Qualifiers contain
 * names, universal identifiers, and abbreviations.
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshAllowableQualifier {

    // <!ELEMENT AllowableQualifiersList (AllowableQualifier+) >
    // <!ELEMENT AllowableQualifier (QualifierReferredTo,Abbreviation )>
    // <!ELEMENT Abbreviation (#PCDATA)>
    // <!ELEMENT QualifierReferredTo (%QualifierReference;) >
    // <!ENTITY  % QualifierReference "(QualifierUI, QualifierName)">
    // <!ELEMENT QualifierUI (#PCDATA) >
    // <!ELEMENT QualifierName (String) >
    
    // AllowableQualifier(QualifierUI,QualifierName,Abbreviation)

    final MeshNameUi mNameUi;
    final String mAbbreviation;

    MeshAllowableQualifier(MeshNameUi nameUi,
                           String abbreviation) {
        mNameUi = nameUi;
        mAbbreviation = abbreviation;
    }

    /**
     * Returns the name and universal identifier pair
     * for this qualifier.
     *
     * @return Name and identifier for this qualifier.
     */
    public MeshNameUi nameUi() {
        return mNameUi;
    }


    /**
     * Returns the two-letter abbreviation for this qualifier.
     *
     * @return Abbreviation for this qualifier.
     */
    public String abbreviation() {
        return mAbbreviation;
    }

    /**
     * Returns a string-based representation of this qualifier.  All
     * of the information returned by this method is available
     * programatically from the other methods in this class.
     *
     * @return A string-based representation of this qualifier.
     */
    @Override
    public String toString() {
        return nameUi() + " (" + abbreviation() + ")";
    }
    
    static class Handler extends DelegateHandler {
        private final MeshNameUi.Handler mNameUiHandler;
        private final TextAccumulatorHandler mAbbreviationHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mNameUiHandler = new MeshNameUi.Handler(parent,
                                                    MeshParser.QUALIFIER_NAME_ELEMENT,
                                                    MeshParser.QUALIFIER_UI_ELEMENT);
            setDelegate(MeshParser.QUALIFIER_REFERRED_TO_ELEMENT,mNameUiHandler);
            mAbbreviationHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.ABBREVIATION_ELEMENT,mAbbreviationHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mNameUiHandler.reset();
            mAbbreviationHandler.reset();
        }
        public MeshAllowableQualifier getQualifier() {
            return new MeshAllowableQualifier(mNameUiHandler.getObject(),
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