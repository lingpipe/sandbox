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
 * An instance of {@code Mesh} represents a single record for a
 * subject heading from the NLM's Medical Subject Headings (MeSH).
 * The {@code Mesh} object is the top-level object produced by the
 * {@link MeshParser}.  The data contained in a record is described in
 * the accessor methods.
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class Mesh {

    private final MeshDescriptorClass mDescriptorClass;
    private final MeshNameUi mDescriptor;
    private final MeshDate mDateCreated;
    private final MeshDate mDateRevised;
    private final MeshDate mDateEstablished;
    private final List<String> mActiveMeshYearList;
    private final List<MeshAllowableQualifier> mAllowableQualifierList;
    private final String mAnnotation;
    private final String mHistoryNote;
    private final String mOnlineNote;
    private final String mPublicMeshNote;
    private final List<String> mPreviousIndexingList;
    private final List<MeshEntryCombination> mEntryCombinationList;
    private final List<MeshNameUi> mSeeRelatedList;
    private final String mConsiderAlso;
    private final List<MeshNameUi> mPharmacologicalActionList;
    private final String mRunningHead;
    private final List<String> mTreeNumberList;
    private final MeshRecordOriginatorsList mRecordOriginatorsList;
    private final List<MeshConcept> mConceptList;

    Mesh(MeshDescriptorClass descriptorClass,
         MeshNameUi descriptor,
         MeshDate dateCreated,
         MeshDate dateRevised,
         MeshDate dateEstablished,
         List<String> activeMeshYearList,
         List<MeshAllowableQualifier> allowableQualifierList,
         String annotation,
         String historyNote,
         String onlineNote,
         String publicMeshNote,
         List<String> previousIndexingList,
         List<MeshEntryCombination> entryCombinationList,
         List<MeshNameUi> seeRelatedList,
         String considerAlso,
         List<MeshNameUi> pharmacologicalActionList,
         String runningHead,
         List<String> treeNumberList,
         MeshRecordOriginatorsList recordOriginatorsList,
         List<MeshConcept> conceptList) {

        mDescriptorClass = descriptorClass;
        mDescriptor = descriptor;
        mDateCreated = dateCreated;
        mDateRevised = dateRevised;
        mDateEstablished = dateEstablished;
        mActiveMeshYearList = activeMeshYearList;
        mAllowableQualifierList = allowableQualifierList;
        mAnnotation = annotation.length() == 0 ? null : annotation;
        mHistoryNote = historyNote.length() == 0 ? null : historyNote;
        mOnlineNote = onlineNote.length() == 0 ? null : onlineNote;
        mPublicMeshNote = publicMeshNote.length() == 0 ? null : publicMeshNote;
        mPreviousIndexingList = previousIndexingList;
        mEntryCombinationList = entryCombinationList;
        mSeeRelatedList = seeRelatedList;
        mConsiderAlso = considerAlso.length() == 0 ? null : considerAlso;
        mPharmacologicalActionList = pharmacologicalActionList;
        mRunningHead = runningHead.length() == 0 ? null : runningHead;
        mTreeNumberList = treeNumberList;
        mRecordOriginatorsList = recordOriginatorsList;
        mConceptList = conceptList;
    }

    /**
     * Returns the descriptor class value for this MeSH term.
     *
     * @return The descriptor class.
     */
    public MeshDescriptorClass descriptorClass() {
        return mDescriptorClass;
    }

    /**
     * Returns the top-level descriptor associated with this term,
     * which includes both its name and its unique identifier (UI).
     *
     * @return The top-level descriptor.
     */
    public MeshNameUi descriptor() {
        return mDescriptor;
    }
    
    /**
     * Returns the date when a term or record was first entered into
     * the MeSH data entry system.  All terms created before January 1,
     * 1999, have that date as their creation date.
     *
     * <p><b>Warning:</b> The DTD doesn't match the description
     * for this element; it may not be {@code null} according to the DTD,
     * but is optional according to the descriptions.
     *
     * @return The date this record was created.
     */
    public MeshDate dateCreated() {
        return mDateCreated;
    }

    /**
     * Returns the date when this record was last changed, or
     * {@code null} if it has never been changed.
     *
     * @return The date when this record was last changed.
     */
    public MeshDate dateRevised() {
        return mDateRevised;
    }

    /**
     * Returns the first full month in which this record
     * was available for searching, or {@code null} if
     * it has not been established.
     *
     * <p><b>Warning:</b> The DTD doesn't match the description
     * for this element; it may be {@code null} according to the DTD,
     * but not according to the descriptions.
     */
    public MeshDate dateEstablished() {
        return mDateEstablished;
    }
    
    /**
     * Returns a list of dates represented as strings for the active
     * years for this record since its last modification.  The list
     * elements must be strings because of &quot;years&quot; values
     * like &quot;2006A&quot;.  There will be an entry for each year
     * in which this record has been active since its last
     * modification.
     *
     * @return The active years since the last modification of this
     * record.
     */
    public List<String> activeYearList() {
        return Collections.unmodifiableList(mActiveMeshYearList);
    }

    /**
     * Returns the list of qualifiers which may be used with the
     * descriptor for indexing.
     *
     * @return The list of allowable qualifiers for this record's
     * descriptor.
     */
    public List<MeshAllowableQualifier> allowableQualifierList() {
        return Collections.unmodifiableList(mAllowableQualifierList);
    }

    /**
     * Returns free text intended for indexers and catalogers concerning
     * the descriptor or qualifiers for this record or {@code null} if
     * there is no annotation.  
     *
     * @return The annotation for this record.
     */
    public String annotation() {
        return mAnnotation;
    }

    /**
     * Returns free text describing the history of this record or
     * {@code null} if there is no note.  The initial characters are
     * dates of the year in which the record appeared in its current
     * form.  Dates in parentheses indicate the oldest creation dates
     * of terms, provisional headings or minor headings.  Entries with
     * no dates are records from 1963-1966.
     *
     * @return Free text historical note for this record.
     */
    public String historyNote() {
        return mHistoryNote;
    }

    /**
     * Returns free text to help online search or {@code null} if
     * there is no note.  This field has been deprecated and replaced
     * with the history note {@link #historyNote()}.
     *
     * @return Onlilne search help notes. 
     */
    public String onlineNote() {
        return mOnlineNote;
    }

    /**
     * Returns free text that may be helpful to users of <i>Index
     * Medicus</i>.  Note that the index was discontinued in 2005.
     * The note includes the creation date, changes in preferred
     * terms, etc.  MeSH terms in the note are in upper case.
     */
    public String publicMeshNote() {
        return mPublicMeshNote;
    }

    /**
     * Returns free text referring to descriptors (with qualifiers) that
     * were used before the the descriptors were created.  Not currently
     * maintained for changes.
     *
     * @return The list of previous indexing notes.
     */
    public List<String> previousIndexingList() {
        return Collections.unmodifiableList(mPreviousIndexingList);
    }

    /**
     * Returns the list of entry combinations that are not allowed
     * with preferred substitutions.
     *
     * @return List of entry combination substitutions.
     */
    public List<MeshEntryCombination> entryCombinationList() {
        return Collections.unmodifiableList(mEntryCombinationList);
    }

    /**
     * Returns a list of related descriptors consisting of names
     * and unique identifiers.
     *
     * @return List of related descriptors.
     */
    public List<MeshNameUi> seeRelatedList() {
        return Collections.unmodifiableList(mSeeRelatedList);
    }

    /**
     * Returns free-text describing cross-references for terms with
     * realted roots, or {@code null} if there is none.
     *
     * @return Free text cross-referencing information.
     */
    public String considerAlso() {
        return mConsiderAlso;
    }

    /**
     * Returns a list of descriptors describing observed biological activity
     * of an exogenously administered chemical.
     *
     * @return List of descriptors for pharamacological actions for
     * this record.
     */
    public List<MeshNameUi> pharmacologicalActionList() {
        return Collections.unmodifiableList(mPharmacologicalActionList);
    }

    /**
     * Returns the header printed at the top of each page of the
     * printed MeSH, or {@code null} if there is none.  There is a
     * separate header for the 114 subcategories.
     *
     * @return The header printed above this entry in the printed
     * form of MeSH.
     */
    public String runningHead() {
        return mRunningHead;
    }

    /**
     * Returns a list of tree numbers referring to a location within the
     * descriptor or qualifier hierarchy.  Note that the numbers are
     * structured with periods.
     *
     * @return The list of tree numbers for this record.
     */
    public List<String> treeNumberList() {
        return Collections.unmodifiableList(mTreeNumberList);
    }

    /**
     * Returns a structured object naming the NLM editor(s) who
     * created, maintained or authorized this record.  
     * 
     * @return Structured record of originators, maintainers and
     * authorizes of this record.
     */
    public MeshRecordOriginatorsList recordOriginatorList() {
        return mRecordOriginatorsList;
    }

    /**
     * Returns one or more structured concepts for this record.  One
     * of these concepts will be the preferred concept for this record
     * (indicated by a flag in the returned object).  These concepts
     * contain most of the linguistic meaning of the record terms.
     *
     * @return List of concepts for this record.  
     */
    public List<MeshConcept> conceptList() {
        return Collections.unmodifiableList(mConceptList);
    }

    /**
     * Returns a complete string-based representation of this MeSH
     * record.  All of the information returned here is derived
     * by applying the relevant {@code toString()} methods to
     * objects returned by methods.  There is no information returned
     * here not available through methods.
     *
     * @return String-based representation of this record.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Descriptor Class=" + descriptorClass());
        sb.append("\nDescriptor=" + descriptor());
        sb.append("\nDate Created=" + dateCreated());
        sb.append("\nDate Revised=" + dateRevised());
        sb.append("\nDate Established=" + dateEstablished());
        sb.append("\nActive Year List=" + activeYearList());
        List<MeshAllowableQualifier> allowableQualifierList 
            = allowableQualifierList();
        for (int i = 0; i < allowableQualifierList.size(); ++i)
            sb.append("\nAllowable Qualifiers[" + i + "]=" 
                      + allowableQualifierList.get(i));
        sb.append("\nAnnotation=" + annotation());
        sb.append("\nHistory Note=" + historyNote());
        sb.append("\nOnline Note=" + onlineNote());
        sb.append("\nPublic Mesh Note=" + publicMeshNote());
        sb.append("\nPrevious Indexing List=" + previousIndexingList());
        List<MeshEntryCombination> entryCombinationList 
            = entryCombinationList();
        for (int i = 0; i < entryCombinationList.size(); ++i)
            sb.append("\nEntry Combination["  + i + "]=\n"
                      + entryCombinationList.get(i));
        List<MeshNameUi> seeRelatedList = seeRelatedList();
        for (int i = 0; i < seeRelatedList.size(); ++i)
            sb.append("\nSee Related[" + i + "]=" 
                      + seeRelatedList.get(i));
        sb.append("\nConsider Also=" + mConsiderAlso);
        List<MeshNameUi> pharmacologicalActionList
            = pharmacologicalActionList();
        for (int i = 0; i < pharmacologicalActionList.size(); ++i)
            sb.append("\nPharmacological Action[" + i + "]=" 
                      + pharmacologicalActionList.get(i));
        sb.append("\nRunning Head=" + mRunningHead);
        List<String> treeNumberList = treeNumberList();
        for (int i = 0; i < treeNumberList.size(); ++i)
            sb.append("\nTree Number[" + i + "]=" 
                      + treeNumberList.get(i));
        sb.append("\nRecord Originator List=" + recordOriginatorList());
        List<MeshConcept> conceptList = conceptList();
        for (int i = 0; i < conceptList.size(); ++i)
            sb.append("\nConcept[" + i + "]=\n" 
                      + conceptList.get(i));
        return sb.toString();
    }

    static class Handler extends DelegateHandler {
        private MeshDescriptorClass mDescriptorClass;
        private final StringHandler mDescriptorNameHandler;
        private final TextAccumulatorHandler mDescriptorUIHandler;
        private final MeshDate.Handler mDateCreatedHandler;
        private final MeshDate.Handler mDateRevisedHandler;
        private final MeshDate.Handler mDateEstablishedHandler;
        private final ListHandler mActiveMeshYearListHandler;
        private final MeshAllowableQualifier.ListHandler mAllowableQualifierListHandler;
        private final TextAccumulatorHandler mAnnotationHandler;
        private final TextAccumulatorHandler mHistoryNoteHandler;
        private final TextAccumulatorHandler mOnlineNoteHandler;
        private final TextAccumulatorHandler mPublicMeshNoteHandler;
        private final ListHandler mPreviousIndexingListHandler;
        private final MeshEntryCombination.ListHandler mEntryCombinationListHandler;
        private final MeshNameUi.ListHandler mSeeRelatedListHandler;
        private final TextAccumulatorHandler mConsiderAlsoHandler;
        private final MeshNameUi.ListHandler mPharmacologicalActionListHandler;
        private final TextAccumulatorHandler mRunningHeadHandler;
        private final ListHandler mTreeNumberListHandler;
        private final MeshRecordOriginatorsList.Handler mRecordOriginatorsListHandler;
        private final MeshConcept.ListHandler mConceptListHandler;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mDescriptorNameHandler = new StringHandler(parent);
            setDelegate(MeshParser.DESCRIPTOR_NAME_ELEMENT,
                        mDescriptorNameHandler);
            mDescriptorUIHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.DESCRIPTOR_UI_ELEMENT,
                        mDescriptorUIHandler);
            mDateCreatedHandler = new MeshDate.Handler(parent);
            setDelegate(MeshParser.DATE_CREATED_ELEMENT,
                        mDateCreatedHandler);
            mDateRevisedHandler = new MeshDate.Handler(parent);
            setDelegate(MeshParser.DATE_REVISED_ELEMENT,
                        mDateRevisedHandler);
            mDateEstablishedHandler = new MeshDate.Handler(parent);
            setDelegate(MeshParser.DATE_ESTABLISHED_ELEMENT,
                        mDateEstablishedHandler);
            mActiveMeshYearListHandler 
                = new ListHandler(parent,MeshParser.YEAR_ELEMENT);
            setDelegate(MeshParser.ACTIVE_MESH_YEAR_LIST_ELEMENT,
                        mActiveMeshYearListHandler);
            mAllowableQualifierListHandler 
                = new MeshAllowableQualifier.ListHandler(parent);
            setDelegate(MeshParser.ALLOWABLE_QUALIFIERS_LIST_ELEMENT,
                        mAllowableQualifierListHandler);
            mAnnotationHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.ANNOTATION_ELEMENT,
                        mAnnotationHandler);
            mHistoryNoteHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.HISTORY_NOTE_ELEMENT,
                        mHistoryNoteHandler);
            mOnlineNoteHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.ONLINE_NOTE_ELEMENT,
                        mOnlineNoteHandler);
            mPublicMeshNoteHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.PUBLIC_MESH_NOTE_ELEMENT,
                        mPublicMeshNoteHandler);
            mPreviousIndexingListHandler 
                = new ListHandler(parent,MeshParser.PREVIOUS_INDEXING_ELEMENT);
            setDelegate(MeshParser.PREVIOUS_INDEXING_LIST_ELEMENT,
                        mPreviousIndexingListHandler);
            mEntryCombinationListHandler
                = new MeshEntryCombination.ListHandler(parent);
            setDelegate(MeshParser.ENTRY_COMBINATION_LIST_ELEMENT,
                        mEntryCombinationListHandler);
            mSeeRelatedListHandler
                = new MeshNameUi.ListHandler(parent,MeshParser.DESCRIPTOR_REFERRED_TO_ELEMENT);
            setDelegate(MeshParser.SEE_RELATED_LIST_ELEMENT,
                        mSeeRelatedListHandler);
            mConsiderAlsoHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONSIDER_ALSO_ELEMENT,
                        mConsiderAlsoHandler);
            mPharmacologicalActionListHandler
                = new MeshNameUi.ListHandler(parent,MeshParser.PHARMACOLOGICAL_ACTION_ELEMENT);
            setDelegate(MeshParser.PHARMACOLOGICAL_ACTION_LIST_ELEMENT,
                        mPharmacologicalActionListHandler);
            mRunningHeadHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.RUNNING_HEAD_ELEMENT,
                        mRunningHeadHandler);
            mTreeNumberListHandler = new ListHandler(parent,MeshParser.TREE_NUMBER_ELEMENT);
            setDelegate(MeshParser.TREE_NUMBER_LIST_ELEMENT,
                        mTreeNumberListHandler);
            mRecordOriginatorsListHandler = new MeshRecordOriginatorsList.Handler(parent);
            setDelegate(MeshParser.RECORD_ORIGINATORS_LIST_ELEMENT,
                        mRecordOriginatorsListHandler);
            mConceptListHandler = new MeshConcept.ListHandler(parent);
            setDelegate(MeshParser.CONCEPT_LIST_ELEMENT,
                        mConceptListHandler);
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            mDescriptorClass = null;
            mDescriptorUIHandler.reset();
            mDescriptorNameHandler.reset();
            mDateCreatedHandler.reset();
            mDateRevisedHandler.reset();
            mDateEstablishedHandler.reset();
            mActiveMeshYearListHandler.reset();
            mAllowableQualifierListHandler.reset();
            mAnnotationHandler.reset();
            mHistoryNoteHandler.reset();
            mOnlineNoteHandler.reset();
            mPublicMeshNoteHandler.reset();
            mPreviousIndexingListHandler.reset();
            mEntryCombinationListHandler.reset();
            mSeeRelatedListHandler.reset();
            mConsiderAlsoHandler.reset();
            mPharmacologicalActionListHandler.reset();
            mRunningHeadHandler.reset();
            mTreeNumberListHandler.reset();
            mRecordOriginatorsListHandler.reset();
            mConceptListHandler.reset();
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) 
            throws SAXException {

            super.startElement(namespaceURI,localName,qName,atts);
            if (MeshParser.DESCRIPTOR_RECORD_ELEMENT.equals(qName)) {
                String descriptorString = atts.getValue(MeshParser.DESCRIPTOR_CLASS_ATT);
                if ("2".equals(descriptorString))
                    mDescriptorClass = MeshDescriptorClass.TWO;
                else if ("3".equals(descriptorString))
                    mDescriptorClass = MeshDescriptorClass.THREE;
                else if ("4".equals(descriptorString))
                    mDescriptorClass = MeshDescriptorClass.FOUR;
                else
                    mDescriptorClass = MeshDescriptorClass.ONE;
            }                    
            
        }
        public Mesh getMesh() {
            return new Mesh(mDescriptorClass,
                            new MeshNameUi(mDescriptorUIHandler.getText().trim(),
                                           mDescriptorNameHandler.getObject()),
                            mDateCreatedHandler.getObject(),
                            mDateRevisedHandler.getObject(),
                            mDateEstablishedHandler.getObject(),
                            mActiveMeshYearListHandler.getList(),
                            mAllowableQualifierListHandler.getObject(),
                            mAnnotationHandler.getText().trim(),
                            mHistoryNoteHandler.getText().trim(),
                            mOnlineNoteHandler.getText().trim(),
                            mPublicMeshNoteHandler.getText().trim(),
                            mPreviousIndexingListHandler.getList(),
                            mEntryCombinationListHandler.getObject(),
                            mSeeRelatedListHandler.getList(),
                            mConsiderAlsoHandler.getText().trim(),
                            mPharmacologicalActionListHandler.getList(),
                            mRunningHeadHandler.getText().trim(),
                            mTreeNumberListHandler.getList(),
                            mRecordOriginatorsListHandler.getObject(),
                            mConceptListHandler.getObject());
        }
    }

    static class StringHandler extends BaseHandler<String> {
        private final TextAccumulatorHandler mTextAccumulator = new TextAccumulatorHandler();
        public StringHandler(DelegatingHandler parent, String element) {
            super(parent);
            setDelegate(element,mTextAccumulator);
        }
        public StringHandler(DelegatingHandler parent) {
            this(parent,MeshParser.STRING_ELEMENT);
        }
        public String getObject() {
            return mTextAccumulator.getText().trim();
        }
        @Override
        public void reset() {
            mTextAccumulator.reset();
        }
    }

    // can't genericize because TextAccumulatorHandler doesn't return an object
    static class ListHandler extends DelegateHandler {
        private List<String> mList = new ArrayList<String>();
        private TextAccumulatorHandler mAccumulator;
        public ListHandler(DelegatingHandler parent, String element) {
            super(parent);
            mAccumulator = new TextAccumulatorHandler();
            setDelegate(element,mAccumulator);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mList.clear();
        }
        public List<String> getList() {
            return new ArrayList<String>(mList);
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            mList.add(mAccumulator.getText().trim());
        }
    }

    
}