package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MeshConcept {

    // <!ELEMENT ConceptList (Concept+)  >
    // <!ELEMENT Concept (ConceptUI,
    //      ConceptName,
    //      ConceptUMLSUI?,
    //      CASN1Name?,
    //      RegistryNumber?,
    //      ScopeNote?,
    //      SemanticTypeList?,
    //      RelatedRegistryNumberList?,
    //      ConceptRelationList?,
    //      TermList)>
    // <!ELEMENT ConceptUI (#PCDATA)>
    // <!ELEMENT ConceptName (String)>
    // <!ELEMENT ConceptUMLSUI (#PCDATA)>
    // <!ELEMENT CASN1Name (#PCDATA)>
    // <!ELEMENT RegistryNumber (#PCDATA)>
    // <!ELEMENT ScopeNote (#PCDATA)>
    // <!ELEMENT SemanticTypeList (SemanticType+)>
    // <!ELEMENT SemanticType (SemanticTypeUI, SemanticTypeName) >
    // <!ELEMENT SemanticTypeUI (#PCDATA)>
    // <!ELEMENT SemanticTypeName (#PCDATA)>
    // <!ELEMENT RelatedRegistryNumberList (RelatedRegistryNumber+)>
    // <!ELEMENT RelatedRegistryNumber (#PCDATA)>
    // <!ELEMENT ConceptRelationList (ConceptRelation+) >
    // <!ELEMENT ConceptRelation (Concept1UI,Concept2UI,RelationAttribute?)>
    // <!ATTLIST ConceptRelation RelationName (NRW | BRD | REL) #IMPLIED >
    // <!ELEMENT Concept1UI (#PCDATA)>
    // <!ELEMENT Concept2UI (#PCDATA)>
    // <!ELEMENT ConceptUMLSUI (#PCDATA)>
    // <!ELEMENT RelationAttribute (#PCDATA)>


    private final String mConceptUi;

    public MeshConcept(String conceptUi) {
        mConceptUi = conceptUi;
    }

    public String conceptUi() {
        return mConceptUi;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Concept UI=" + conceptUi());
        return sb.toString();
    }

    static class Handler extends DelegateHandler {
        TextAccumulatorHandler mConceptUiHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mConceptUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONCEPT_UI_ELEMENT,mConceptUiHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mConceptUiHandler.reset();
        }
        public MeshConcept getConcept() {
            return new MeshConcept(mConceptUiHandler.getText());
        }
    }


    static class ListHandler extends DelegateHandler {
        final List<MeshConcept> mConceptList = new ArrayList<MeshConcept>();
        final Handler mConceptHandler;
        public ListHandler(DelegatingHandler parent) {
            super(parent);
            mConceptHandler = new Handler(parent);
            setDelegate(MeshParser.CONCEPT_ELEMENT,mConceptHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mConceptHandler.reset();
            mConceptList.clear();
        }
        @Override
        public void finishDelegate(String qName, DefaultHandler h) {
            mConceptList.add(mConceptHandler.getConcept());
        }
        public List<MeshConcept> getConceptList() {
            return new ArrayList<MeshConcept>(mConceptList);
        }
    }


}