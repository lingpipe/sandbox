package com.aliasi.lingmed.mesh;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.Collections;
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
    // <!ELEMENT TermList (Term+)>
    // <!ELEMENT Term (%TermReference;,
    //     DateCreated?,
    //     Abbreviation?,
    //     SortVersion?,
    //     EntryVersion?,
    //     ThesaurusIDlist?)>
    // <!ATTLIST Term    ConceptPreferredTermYN (Y | N) #REQUIRED
    //     IsPermutedTermYN (Y | N) #REQUIRED
    //     LexicalTag (ABB|ABX|ACR|ACX|EPO|LAB|NAM|NON|TRD) #REQUIRED
    //     PrintFlagYN (Y | N) #REQUIRED
    //     RecordPreferredTermYN (Y | N)  #REQUIRED>
    // <!ELEMENT TermUI (#PCDATA)>


    private final String mConceptUi;
    private final String mConceptName;
    private final String mConceptUmlsUi;
    private final String mCasn1Name;
    private final String mRegistryNumber;
    private final String mScopeNote;
    private final List<MeshSemanticType> mSemanticTypeList;
    private final List<String> mRelatedRegistryNumberList;
    private final List<MeshConceptRelation> mConceptRelationList;

    public MeshConcept(String conceptUi,
                       String conceptName,
                       String conceptUmlsUi,
                       String casn1Name,
                       String registryNumber,
                       String scopeNote,
                       List<MeshSemanticType> semanticTypeList,
                       List<String> relatedRegistryNumberList,
                       List<MeshConceptRelation> conceptRelationList) {
        mConceptUi = conceptUi;
        mConceptName = conceptName;
        mConceptUmlsUi = conceptUmlsUi.length() == 0 ? null : conceptUmlsUi;
        mCasn1Name = casn1Name.length() == 0 ? null : casn1Name;
        mRegistryNumber = registryNumber.length() == 0 ? null : registryNumber;
        mScopeNote = scopeNote.length() == 0 ? null : scopeNote;
        mSemanticTypeList = semanticTypeList;
        mRelatedRegistryNumberList = relatedRegistryNumberList;
        mConceptRelationList = conceptRelationList;
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

    public String casn1Name() {
        return mCasn1Name;
    }

    public String registryNumber() {
        return mRegistryNumber;
    }

    public String scopeNote() {
        return mScopeNote;
    }

    public List<MeshSemanticType> semanticTypeList() {
        return Collections.unmodifiableList(mSemanticTypeList);
    }

    public List<String> relatedRegistryNumberList() {
        return Collections.unmodifiableList(mRelatedRegistryNumberList);
    }

    public List<MeshConceptRelation> conceptRelationList() {
        return Collections.unmodifiableList(mConceptRelationList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Concept UI=" + conceptUi() + "\n");
        sb.append("  Concept Name=" + conceptName() + "\n");
        sb.append("  Concept UMLS UI=" + conceptUmlsUi() + "\n");
        sb.append("  CASN1 Name=" + casn1Name() + "\n");
        sb.append("  Registry Number=" + registryNumber() + "\n");
        sb.append("  Scope Note=" + scopeNote() + "\n");
        List<MeshSemanticType> semanticTypeList = semanticTypeList();
        for (int i = 0; i < semanticTypeList.size(); ++i)
            sb.append("  Semantic Type=[" + i + "]="
                      + semanticTypeList.get(i) + "\n");
        List<String> relatedRegistryNumberList = relatedRegistryNumberList();
        for (int i = 0; i < relatedRegistryNumberList.size(); ++i)
            sb.append("  Related Registry Number[" + i + "]="
                      + relatedRegistryNumberList.get(i) + "\n");
        List<MeshConceptRelation> conceptRelationList = conceptRelationList();
        for (int i = 0; i < conceptRelationList.size(); ++i)
            sb.append("  Concept Relation[" + i + "]="
                      + conceptRelationList.get(i) + "\n");
        return sb.toString();
    }

    static class Handler extends DelegateHandler {
        TextAccumulatorHandler mConceptUiHandler;
        Mesh.StringHandler mConceptNameHandler;
        TextAccumulatorHandler mConceptUmlsUiHandler;
        TextAccumulatorHandler mCasn1NameHandler;
        TextAccumulatorHandler mRegistryNumberHandler;
        TextAccumulatorHandler mScopeNoteHandler;
        MeshSemanticType.ListHandler mSemanticTypeListHandler;
        Mesh.ListHandler mRelatedRegistryNumberListHandler;
        MeshConceptRelation.ListHandler mConceptRelationListHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mConceptUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONCEPT_UI_ELEMENT,mConceptUiHandler);
            mConceptNameHandler = new Mesh.StringHandler(parent);
            setDelegate(MeshParser.CONCEPT_NAME_ELEMENT,mConceptNameHandler);
            mConceptUmlsUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONCEPT_UMLS_UI_ELEMENT,mConceptUmlsUiHandler);
            mCasn1NameHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CASN1_NAME_ELEMENT,mCasn1NameHandler);
            mRegistryNumberHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.REGISTRY_NUMBER_ELEMENT,mRegistryNumberHandler);
            mScopeNoteHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.SCOPE_NOTE_ELEMENT,mScopeNoteHandler);
            mSemanticTypeListHandler = new MeshSemanticType.ListHandler(parent);
            setDelegate(MeshParser.SEMANTIC_TYPE_LIST_ELEMENT,
                        mSemanticTypeListHandler);
            mRelatedRegistryNumberListHandler 
                = new Mesh.ListHandler(parent, MeshParser.RELATED_REGISTRY_NUMBER_ELEMENT);
            setDelegate(MeshParser.RELATED_REGISTRY_NUMBER_LIST_ELEMENT,
                        mRelatedRegistryNumberListHandler);
            mConceptRelationListHandler = new MeshConceptRelation.ListHandler(parent);
            setDelegate(MeshParser.CONCEPT_RELATION_LIST_ELEMENT,
                        mConceptRelationListHandler);
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
            mCasn1NameHandler.reset();
            mRegistryNumberHandler.reset();
            mScopeNoteHandler.reset();
            mSemanticTypeListHandler.reset();
            mRelatedRegistryNumberListHandler.reset();
            mConceptRelationListHandler.reset();
        }
        public MeshConcept getConcept() {
            return new MeshConcept(mConceptUiHandler.getText(),
                                   mConceptNameHandler.getText(),
                                   mConceptUmlsUiHandler.getText(),
                                   mCasn1NameHandler.getText(),
                                   mRegistryNumberHandler.getText(),
                                   mScopeNoteHandler.getText(),
                                   mSemanticTypeListHandler.getList(),
                                   mRelatedRegistryNumberListHandler.getList(),
                                   mConceptRelationListHandler.getList());
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