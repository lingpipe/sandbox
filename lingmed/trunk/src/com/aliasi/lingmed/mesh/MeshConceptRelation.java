package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


/**
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshConceptRelation {

    private final String mRelationName;
    private final String mConcept1Ui;
    private final String mConcept2Ui;
    private final String mRelationAttribute;

    MeshConceptRelation(String relationName,
                        String concept1Ui,
                        String concept2Ui,
                        String relationAttribute) {
        mRelationName = relationName;
        mConcept1Ui = concept1Ui;
        mConcept2Ui = concept2Ui;
        mRelationAttribute = relationAttribute.length() == 0 ? null : relationAttribute;
        if (mRelationAttribute != null)
            System.out.println("RA123=" + mRelationAttribute);
    }

    public String relationName() {
        return mRelationName;
    }

    public String concept1Ui() {
        return mConcept1Ui;
    }

    public String concept2Ui() {
        return mConcept2Ui;
    }

    public String relationAttribute() {
        return mRelationAttribute;
    }

    @Override
    public String toString() {
        return "Relation Name=" + mRelationName
            + "; Concept 1 UI=" + mConcept1Ui
            + "; Concept 2 UI=" + mConcept2Ui
            + "; Relational Attribute=" + mRelationAttribute;
    }

    static class Handler extends DelegateHandler {
        final TextAccumulatorHandler mConcept1UiHandler;
        final TextAccumulatorHandler mConcept2UiHandler;
        final TextAccumulatorHandler mRelationAttributeHandler;
        String mRelationName;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mConcept1UiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONCEPT_1_UI_ELEMENT,
                        mConcept1UiHandler);
            mConcept2UiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONCEPT_2_UI_ELEMENT,
                        mConcept2UiHandler);
            mRelationAttributeHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.RELATION_ATTRIBUTE_ELEMENT,
                        mRelationAttributeHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        @Override
        public void startElement(String url, String relName, String qName,
                                     Attributes atts) throws SAXException {
            super.startElement(url,relName,qName,atts);
            if (MeshParser.CONCEPT_RELATION_ELEMENT.equals(qName))
                mRelationName = atts.getValue(MeshParser.RELATION_NAME_ATT);
        }
        public void reset() {
            mConcept1UiHandler.reset();
            mConcept2UiHandler.reset();
            mRelationAttributeHandler.reset();
            mRelationName = null;
        }
        public MeshConceptRelation getConcept() {
            return new MeshConceptRelation(mRelationName,
                                           mConcept1UiHandler.getText().trim(),
                                           mConcept2UiHandler.getText().trim(),
                                           mRelationAttributeHandler.getText().trim());
                                   
        }
    }

    static class ListHandler extends DelegateHandler {
        final List<MeshConceptRelation> mConceptRelationList
            = new ArrayList<MeshConceptRelation>();
        final Handler mHandler;
        ListHandler(DelegatingHandler parent) {
            super(parent);
            mHandler = new Handler(parent);
            setDelegate(MeshParser.CONCEPT_RELATION_ELEMENT,mHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mConceptRelationList.clear();
            mHandler.reset();
        }
        @Override
        public void finishDelegate(String qName, DefaultHandler h) {
            mConceptRelationList.add(mHandler.getConcept());
        }
        public List<MeshConceptRelation> getList() {
            return new ArrayList<MeshConceptRelation>(mConceptRelationList);
        }
    }

}