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
    private final String mConceptName;
    private final String mConceptUmlsUi;

    public MeshConcept(String conceptUi,
                       String conceptName,
                       String conceptUmlsUi) {
        mConceptUi = conceptUi;
        mConceptName = conceptName;
        mConceptUmlsUi = conceptUmlsUi.length() == 0 ? null : conceptUmlsUi;
    }

    public String conceptUi() {
        return mConceptUi;
    }

    public String conceptName() {
        return mConceptName;
    }

    public String conceptUmlsUi() {
        return mConceptUmlsUi;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Concept UI=" + conceptUi() + "\n");
        sb.append("  Concept Name=" + conceptName() + "\n");
        sb.append("  Concept UMLS UI=" + conceptUmlsUi());
        return sb.toString();
    }

    static class Handler extends DelegateHandler {
        TextAccumulatorHandler mConceptUiHandler;
        Mesh.StringHandler mConceptNameHandler;
        TextAccumulatorHandler mConceptUmlsUiHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mConceptUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONCEPT_UI_ELEMENT,mConceptUiHandler);
            mConceptNameHandler = new Mesh.StringHandler(parent);
            setDelegate(MeshParser.CONCEPT_NAME_ELEMENT,mConceptNameHandler);
            mConceptUmlsUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONCEPT_UMLS_UI_ELEMENT,mConceptUmlsUiHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mConceptUiHandler.reset();
            mConceptNameHandler.reset();
            mConceptUmlsUiHandler.reset();
        }
        public MeshConcept getConcept() {
            return new MeshConcept(mConceptUiHandler.getText(),
                                   mConceptNameHandler.getText(),
                                   mConceptUmlsUiHandler.getText());
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