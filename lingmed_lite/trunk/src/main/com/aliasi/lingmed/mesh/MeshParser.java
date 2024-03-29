package com.aliasi.lingmed.mesh;

import com.aliasi.lingpipe.corpus.ObjectHandler;
import com.aliasi.lingpipe.corpus.XMLParser;

import com.aliasi.lingpipe.xml.DelegatingHandler;

import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


/**
 * A {@link MeshParser} is able to parse the XML for a complete
 * MeSH distribution, sending the {@link Mesh} objects it finds
 * to the contained object handler.
 *
 * <p>The MeSH XML may be parsed directly from either the GZip or Zip
 * distribution without first unpacking it.  Just construct an input
 * stream that unpacks and wrap it in an {@link InputSource}.
 *
 * <p>The jar for this package includes the DTD for the MeSH XML, so
 * it does not need to be included locally.  The package
 * documentation, {@link com.aliasi.lingmed.mesh}, contains more
 * information on downloading MeSH.
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshParser extends XMLParser<ObjectHandler<Mesh>> {

    /**
     * Construct a MeSH parser.
     */
    public MeshParser() {
    }

    /**
     * Returns the XML handler for this parser, which is used internally
     * by the parser and should not be needed by clients.
     *
     * @return The XML handler for this parser.
     */
    protected DefaultHandler getXMLHandler() {
        return new XMLHandler();
    }
    
    class XMLHandler extends DelegatingHandler {
        final Mesh.Handler mHandler;
        XMLHandler() {
            mHandler = new Mesh.Handler(this);
            setDelegate(DESCRIPTOR_RECORD_ELEMENT,
                        mHandler);
        }
        @Override
        public void finishDelegate(String element,
                                   DefaultHandler handler) {
            getHandler().handle(mHandler.getMesh());
        }
        @Override
        public InputSource resolveEntity(String publicId, String systemId) 
            throws SAXException {
            if (systemId.endsWith("desc2009.dtd")) {
                InputStream in = this.getClass().getResourceAsStream("/com/aliasi/lingmed/mesh/desc2009.dtd");
                return new InputSource(in);
            } 
            // return super.resolveEntity(publicId,systemId);
            throw new UnsupportedOperationException("bah");
        }
    }

    static final String ABBREVIATION_ELEMENT = "Abbreviation";
    static final String ACTIVE_MESH_YEAR_LIST_ELEMENT = "ActiveMeSHYearList";
    static final String ALLOWABLE_QUALIFIER_ELEMENT = "AllowableQualifier";
    static final String ALLOWABLE_QUALIFIERS_LIST_ELEMENT = "AllowableQualifiersList";
    static final String ANNOTATION_ELEMENT = "Annotation";
    static final String CASN1_NAME_ELEMENT = "CASN1Name";
    static final String CONCEPT_ELEMENT = "Concept";
    static final String CONCEPT_LIST_ELEMENT = "ConceptList";
    static final String CONCEPT_NAME_ELEMENT = "ConceptName";
    static final String CONCEPT_RELATION_ELEMENT = "ConceptRelation";
    static final String CONCEPT_RELATION_LIST_ELEMENT = "ConceptRelationList";
    static final String CONCEPT_UI_ELEMENT = "ConceptUI";
    static final String CONCEPT_UMLS_UI_ELEMENT = "ConceptUMLSUI";
    static final String CONCEPT_1_UI_ELEMENT = "Concept1UI";
    static final String CONCEPT_2_UI_ELEMENT = "Concept2UI";
    static final String CONSIDER_ALSO_ELEMENT = "ConsiderAlso";
    static final String DATE_CREATED_ELEMENT = "DateCreated";
    static final String DATE_REVISED_ELEMENT = "DateRevised";
    static final String DATE_ESTABLISHED_ELEMENT = "DateEstablished";
    static final String DAY_ELEMENT = "Day";
    static final String DESCRIPTOR_NAME_ELEMENT = "DescriptorName";
    static final String DESCRIPTOR_RECORD_ELEMENT = "DescriptorRecord";
    static final String DESCRIPTOR_REFERRED_TO_ELEMENT = "DescriptorReferredTo";
    static final String DESCRIPTOR_UI_ELEMENT = "DescriptorUI";
    static final String ECIN_ELEMENT = "ECIN";
    static final String ECOUT_ELEMENT = "ECOUT";
    static final String ENTRY_COMBINATION_ELEMENT = "EntryCombination";
    static final String ENTRY_COMBINATION_LIST_ELEMENT = "EntryCombinationList";
    static final String ENTRY_VERSION_ELEMENT = "EntryVersion";
    static final String HISTORY_NOTE_ELEMENT = "HistoryNote";
    static final String MONTH_ELEMENT = "Month";
    static final String ONLINE_NOTE_ELEMENT = "OnlineNote";
    static final String PHARMACOLOGICAL_ACTION_ELEMENT = "PharmacologicalAction";
    static final String PHARMACOLOGICAL_ACTION_LIST_ELEMENT = "PharmacologicalActionList";
    static final String PREVIOUS_INDEXING_ELEMENT = "PreviousIndexing";
    static final String PREVIOUS_INDEXING_LIST_ELEMENT = "PreviousIndexingList";
    static final String PUBLIC_MESH_NOTE_ELEMENT = "PublicMeSHNote";
    static final String QUALIFIER_UI_ELEMENT = "QualifierUI";
    static final String QUALIFIER_NAME_ELEMENT = "QualifierName";
    static final String QUALIFIER_REFERRED_TO_ELEMENT = "QualifierReferredTo";
    static final String RECORD_AUTHORIZER_ELEMENT = "RecordAuthorizer";
    static final String RECORD_MAINTAINER_ELEMENT = "RecordMaintainer";
    static final String RECORD_ORIGINATOR_ELEMENT = "RecordOriginator";
    static final String RECORD_ORIGINATORS_LIST_ELEMENT = "RecordOriginatorsList";
    static final String REGISTRY_NUMBER_ELEMENT = "RegistryNumber";
    static final String RELATED_REGISTRY_NUMBER_ELEMENT = "RelatedRegistryNumber";
    static final String RELATED_REGISTRY_NUMBER_LIST_ELEMENT = "RelatedRegistryNumberList";
    static final String RELATION_ATTRIBUTE_ELEMENT = "RelationAttribute";
    static final String RUNNING_HEAD_ELEMENT = "RunningHead";
    static final String SEMANTIC_TYPE_ELEMENT = "SemanticType";
    static final String SEMANTIC_TYPE_LIST_ELEMENT = "SemanticTypeList";
    static final String SEMANTIC_TYPE_NAME_ELEMENT = "SemanticTypeName";
    static final String SEMANTIC_TYPE_UI_ELEMENT = "SemanticTypeUI";
    static final String SCOPE_NOTE_ELEMENT = "ScopeNote";
    static final String SEE_RELATED_LIST_ELEMENT = "SeeRelatedList";
    static final String SORT_VERSION_ELEMENT = "SortVersion";
    static final String STRING_ELEMENT = "String";
    static final String TERM_ELEMENT = "Term";
    static final String TERM_LIST_ELEMENT = "TermList";
    static final String TERM_UI_ELEMENT = "TermUI";
    static final String THESAURUS_ID_ELEMENT = "ThesaurusID";
    static final String THESAURUS_ID_LIST_ELEMENT = "ThesaurusIDlist";
    static final String TREE_NUMBER_ELEMENT = "TreeNumber";
    static final String TREE_NUMBER_LIST_ELEMENT = "TreeNumberList";
    static final String YEAR_ELEMENT = "Year";
    

    static final String CONCEPT_PREFERRED_TERM_YN_ATT = "ConceptPreferredTermYN";
    static final String DESCRIPTOR_CLASS_ATT = "DescriptorClass";
    static final String IS_PERMUTED_TERM_YN_ATT = "IsPermutedTermYN";
    static final String LEXICAL_TAG_ATT = "LexicalTag";
    static final String PREFERRED_CONCEPT_YN_ATT = "PreferredConceptYN";
    static final String PRINT_FLAG_YN_ATT = "PrintFlagYN";
    static final String RECORD_PREFERRED_TERM_YN_ATT = "RecordPreferredTermYN";
    static final String RELATION_NAME_ATT = "RelationName";
    
}
