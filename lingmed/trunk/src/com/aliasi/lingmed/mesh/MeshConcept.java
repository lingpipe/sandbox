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
public class MeshConcept {

    private final boolean mPreferred;
    private final String mConceptUi;
    private final String mConceptName;
    private final String mConceptUmlsUi;
    private final String mCasn1Name;
    private final String mRegistryNumber;
    private final String mScopeNote;
    private final List<MeshSemanticType> mSemanticTypeList;
    private final List<String> mRelatedRegistryNumberList;
    private final List<MeshConceptRelation> mConceptRelationList;
    private final List<MeshTerm> mTermList;

    MeshConcept(boolean preferred,
                String conceptUi,
                String conceptName,
                String conceptUmlsUi,
                String casn1Name,
                String registryNumber,
                String scopeNote,
                List<MeshSemanticType> semanticTypeList,
                List<String> relatedRegistryNumberList,
                List<MeshConceptRelation> conceptRelationList,
                List<MeshTerm> termList) {
        mPreferred = preferred;
        mConceptUi = conceptUi;
        mConceptName = conceptName;
        mConceptUmlsUi = conceptUmlsUi.length() == 0 ? null : conceptUmlsUi;
        mCasn1Name = casn1Name.length() == 0 ? null : casn1Name;
        mRegistryNumber = registryNumber.length() == 0 ? null : registryNumber;
        mScopeNote = scopeNote.length() == 0 ? null : scopeNote;
        mSemanticTypeList = semanticTypeList;
        mRelatedRegistryNumberList = relatedRegistryNumberList;
        mConceptRelationList = conceptRelationList;
        mTermList = termList;
    }

    /**
     * Returns {@code true} if this is the preferred concept in
     * a list of concepts for a record.
     *
     * @return Preferential status of this concept.
     */
    public boolean preferred() {
        return mPreferred;
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

    public List<MeshTerm> termList() {
        return Collections.unmodifiableList(mTermList);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Concept UI=" + conceptUi());
        sb.append("\n  Concept Name=" + conceptName());
        sb.append("\n  Concept UMLS UI=" + conceptUmlsUi());
        sb.append("\n  CASN1 Name=" + casn1Name());
        sb.append("\n  Registry Number=" + registryNumber());
        sb.append("\n  Scope Note=" + scopeNote());
        List<MeshSemanticType> semanticTypeList = semanticTypeList();
        for (int i = 0; i < semanticTypeList.size(); ++i)
            sb.append("\n  Semantic Type=[" + i + "]="
                      + semanticTypeList.get(i));
        List<String> relatedRegistryNumberList = relatedRegistryNumberList();
        for (int i = 0; i < relatedRegistryNumberList.size(); ++i)
            sb.append("\n  Related Registry Number[" + i + "]="
                      + relatedRegistryNumberList.get(i));
        List<MeshConceptRelation> conceptRelationList = conceptRelationList();
        for (int i = 0; i < conceptRelationList.size(); ++i)
            sb.append("\n  Concept Relation[" + i + "]="
                      + conceptRelationList.get(i));
        List<MeshTerm> termList = termList();
        for (int i = 0; i < termList.size(); ++i)
            sb.append("\n  Term[" + i + "]=\n" 
                      + termList.get(i));
        return sb.toString();
    }

    static class Handler extends DelegateHandler {
        boolean mPreferred;
        TextAccumulatorHandler mConceptUiHandler;
        Mesh.StringHandler mConceptNameHandler;
        TextAccumulatorHandler mConceptUmlsUiHandler;
        TextAccumulatorHandler mCasn1NameHandler;
        TextAccumulatorHandler mRegistryNumberHandler;
        TextAccumulatorHandler mScopeNoteHandler;
        MeshSemanticType.ListHandler mSemanticTypeListHandler;
        Mesh.ListHandler mRelatedRegistryNumberListHandler;
        MeshConceptRelation.ListHandler mConceptRelationListHandler;
        MeshTerm.ListHandler mTermListHandler;
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
            mTermListHandler = new MeshTerm.ListHandler(parent);
            setDelegate(MeshParser.TERM_LIST_ELEMENT,
                        mTermListHandler);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        @Override
        public void startElement(String url, String name, String qName,
                                 Attributes atts) throws SAXException {
            super.startElement(url,name,qName,atts);
            if (!MeshParser.CONCEPT_ELEMENT.equals(qName)) return;
            mPreferred = "Y".equals(atts.getValue(MeshParser.PREFERRED_CONCEPT_YN_ATT));
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
            mTermListHandler.reset();
            mPreferred = false;
        }
        public MeshConcept getConcept() {
            return new MeshConcept(mPreferred,
                                   mConceptUiHandler.getText().trim(),
                                   mConceptNameHandler.getObject(),
                                   mConceptUmlsUiHandler.getText().trim(),
                                   mCasn1NameHandler.getText().trim(),
                                   mRegistryNumberHandler.getText().trim(),
                                   mScopeNoteHandler.getText().trim(),
                                   mSemanticTypeListHandler.getList(),
                                   mRelatedRegistryNumberListHandler.getList(),
                                   mConceptRelationListHandler.getList(),
                                   mTermListHandler.getTermList());
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