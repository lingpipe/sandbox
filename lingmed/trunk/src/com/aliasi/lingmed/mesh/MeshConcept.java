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
 * A {@link MeshConcept} is a structured object representing the
 * meaning of a MeSH concept.  
 *
 * <p>There may be more than one concept for
 * a record, but there is always a preferred concept for a record
 * (see {@link #isPreferred()}). 
 *
 * <p>Both the concept universal identifier and concept name
 * are unique (see {@link #nameUi()}).
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshConcept {

    private final boolean mPreferred;
    private final MeshNameUi mConceptNameUi;
    private final String mConceptUmlsUi;
    private final String mCasn1Name;
    private final String mRegistryNumber;
    private final String mScopeNote;
    private final List<MeshSemanticType> mSemanticTypeList;
    private final List<String> mRelatedRegistryNumberList;
    private final List<MeshConceptRelation> mConceptRelationList;
    private final List<MeshTerm> mTermList;

    MeshConcept(boolean preferred,
                MeshNameUi conceptNameUi,
                String conceptUmlsUi,
                String casn1Name,
                String registryNumber,
                String scopeNote,
                List<MeshSemanticType> semanticTypeList,
                List<String> relatedRegistryNumberList,
                List<MeshConceptRelation> conceptRelationList,
                List<MeshTerm> termList) {
        mPreferred = preferred;
        mConceptNameUi = conceptNameUi;
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
    public boolean isPreferred() {
        return mPreferred;
    }

    public MeshNameUi conceptNameUi() {
        return mConceptNameUi;
    }


    public String conceptUmlsUi() {
        return mConceptUmlsUi;
    }

    /**
     * Returns the Chemical Abstracts Service (CAS) Type N1 name for
     * this concept, or {@code null} if there is none.  The name is
     * unique and represents the chemical structure of the concept.
     *
     * @return The CAS type N1 name for this concept.
     */
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
        sb.append("  Concept Name/UI=" + conceptNameUi());
        sb.append("\n  Preferred=" + isPreferred());
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

    static class Handler extends BaseHandler<MeshConcept> {
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
        public void startElement(String url, String name, String qName,
                                 Attributes atts) throws SAXException {
            super.startElement(url,name,qName,atts);
            if (!MeshParser.CONCEPT_ELEMENT.equals(qName)) return;
            mPreferred = "Y".equals(atts.getValue(MeshParser.PREFERRED_CONCEPT_YN_ATT));
        }
        @Override
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
        public MeshConcept getObject() {
            return new MeshConcept(mPreferred,
                                   new MeshNameUi(mConceptNameHandler.getObject(),
                                                  mConceptUiHandler.getText().trim()),
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


    static class ListHandler extends BaseListHandler<MeshConcept> {
        public ListHandler(DelegatingHandler parent) {
            super(parent,
                  new Handler(parent),
                  MeshParser.CONCEPT_ELEMENT);
        }
    }


}