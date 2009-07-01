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
 * An instance of {@code Mesh} represents a single concept
 * from the Medical Subject Headings (MeSH).
 */
public class Mesh {

    // <!ENTITY  % DescriptorReference "

    // SeeRelatedList?,
    // <!ELEMENT SeeRelatedList (SeeRelatedDescriptor+)>
    // <!ELEMENT SeeRelatedDescriptor (DescriptorReferredTo)>
    // <!ELEMENT DescriptorReferredTo (DescriptorUI, DescriptorName) >
    // <!ELEMENT DescriptorUI (#PCDATA) >
    // <!ELEMENT DescriptorName (String) >


    private final DescriptorClass mDescriptorClass;
    private final MeshDescriptor mDescriptor;
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
    private final List<EntryCombination> mEntryCombinationList;
    private final List<MeshDescriptor> mSeeRelatedList;
    private final String mConsiderAlso;
    private final List<MeshDescriptor> mPharmacologicalActionList;
    private final String mRunningHead;
    private final List<String> mTreeNumberList;
    private final MeshRecordOriginatorList mRecordOriginatorList;
    private final List<MeshConcept> mConceptList;

    public Mesh(DescriptorClass descriptorClass,
                MeshDescriptor descriptor,
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
                List<EntryCombination> entryCombinationList,
                List<MeshDescriptor> seeRelatedList,
                String considerAlso,
                List<MeshDescriptor> pharmacologicalActionList,
                String runningHead,
                List<String> treeNumberList,
                MeshRecordOriginatorList recordOriginatorList,
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
        mRecordOriginatorList = recordOriginatorList;
        mConceptList = conceptList;
    }


    public DescriptorClass descriptorClass() {
        return mDescriptorClass;
    }

    public MeshDescriptor descriptor() {
        return mDescriptor;
    }
    
    public MeshDate dateCreated() {
        return mDateCreated;
    }

    public MeshDate dateRevised() {
        return mDateRevised;
    }

    public MeshDate dateEstablished() {
        return mDateEstablished;
    }
    
    // string because of years like 2006A.  Doh!
    public List<String> activeYearList() {
        return Collections.unmodifiableList(mActiveMeshYearList);
    }

    public List<MeshAllowableQualifier> allowableQualifierList() {
        return Collections.unmodifiableList(mAllowableQualifierList);
    }

    public String annotation() {
        return mAnnotation;
    }

    public String historyNote() {
        return mHistoryNote;
    }

    public String onlineNote() {
        return mOnlineNote;
    }

    public String publicMeshNote() {
        return mPublicMeshNote;
    }

    public List<String> previousIndexingList() {
        return Collections.unmodifiableList(mPreviousIndexingList);
    }

    public List<EntryCombination> entryCombinationList() {
        return Collections.unmodifiableList(mEntryCombinationList);
    }

    public List<MeshDescriptor> seeRelatedList() {
        return Collections.unmodifiableList(mSeeRelatedList);
    }

    public String considerAlso() {
        return mConsiderAlso;
    }

    public List<MeshDescriptor> pharmacologicalActionList() {
        return Collections.unmodifiableList(mPharmacologicalActionList);
    }

    public String runningHead() {
        return mRunningHead;
    }

    public List<String> treeNumberList() {
        return Collections.unmodifiableList(mTreeNumberList);
    }

    public MeshRecordOriginatorList recordOriginatorList() {
        return mRecordOriginatorList;
    }

    public List<MeshConcept> conceptList() {
        return Collections.unmodifiableList(mConceptList);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Descriptor Class=" + descriptorClass() + "\n");
        sb.append("Descriptor=" + descriptor() + "\n");
        sb.append("Date Created=" + dateCreated() + "\n");
        sb.append("Date Revised=" + dateRevised() + "\n");
        sb.append("Date Established=" + dateEstablished() + "\n");
        sb.append("Active Year List=" + activeYearList() + "\n");
        List<MeshAllowableQualifier> allowableQualifierList 
            = allowableQualifierList();
        for (int i = 0; i < allowableQualifierList.size(); ++i)
            sb.append("Allowable Qualifiers[" + i + "]=" 
                      + allowableQualifierList.get(i) + "\n");
        sb.append("Annotation=" + annotation() + "\n");
        sb.append("History Note=" + historyNote() + "\n");
        sb.append("Online Note=" + onlineNote() + "\n");
        sb.append("Public Mesh Note=" + publicMeshNote() + "\n");
        sb.append("Previous Indexing List=" + previousIndexingList() + "\n");
        List<EntryCombination> entryCombinationList 
            = entryCombinationList();
        for (int i = 0; i < entryCombinationList.size(); ++i)
            sb.append("Entry Combination["  + i + "]="
                      + entryCombinationList.get(i) + "\n");
        List<MeshDescriptor> seeRelatedList = seeRelatedList();
        for (int i = 0; i < seeRelatedList.size(); ++i)
            sb.append("See Related[" + i + "]=" 
                      + seeRelatedList.get(i) + "\n");
        sb.append("Consider Also=" + mConsiderAlso + "\n");
        List<MeshDescriptor> pharmacologicalActionList
            = pharmacologicalActionList();
        for (int i = 0; i < pharmacologicalActionList.size(); ++i)
            sb.append("Pharmacological Action[" + i + "]=" 
                      + pharmacologicalActionList.get(i)
                      + "\n");
        sb.append("Running Head=" + mRunningHead + "\n");
        List<String> treeNumberList = treeNumberList();
        for (int i = 0; i < treeNumberList.size(); ++i)
            sb.append("Tree Number[" + i + "]=" 
                      + treeNumberList.get(i) + "\n");
        sb.append("Record Originator List=" + recordOriginatorList() + "\n");
        List<MeshConcept> conceptList = conceptList();
        for (int i = 0; i < conceptList.size(); ++i)
            sb.append("Concept[" + i + "]=\n" 
                      + conceptList.get(i) + "\n");
        return sb.toString();
    }

    static class Handler extends DelegateHandler {
        private DescriptorClass mDescriptorClass;
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
        private final EntryCombination.ListHandler mEntryCombinationListHandler;
        private final MeshDescriptor.ListHandler mSeeRelatedListHandler;
        private final TextAccumulatorHandler mConsiderAlsoHandler;
        private final MeshDescriptor.ListHandler mPharmacologicalActionListHandler;
        private final TextAccumulatorHandler mRunningHeadHandler;
        private final ListHandler mTreeNumberListHandler;
        private final MeshRecordOriginatorList.Handler mRecordOriginatorListHandler;
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
                = new EntryCombination.ListHandler(parent);
            setDelegate(MeshParser.ENTRY_COMBINATION_LIST_ELEMENT,
                        mEntryCombinationListHandler);
            mSeeRelatedListHandler
                = new MeshDescriptor.ListHandler(parent,MeshParser.DESCRIPTOR_REFERRED_TO_ELEMENT);
            setDelegate(MeshParser.SEE_RELATED_LIST_ELEMENT,
                        mSeeRelatedListHandler);
            mConsiderAlsoHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.CONSIDER_ALSO_ELEMENT,
                        mConsiderAlsoHandler);
            mPharmacologicalActionListHandler
                = new MeshDescriptor.ListHandler(parent,MeshParser.PHARMACOLOGICAL_ACTION_ELEMENT);
            setDelegate(MeshParser.PHARMACOLOGICAL_ACTION_LIST_ELEMENT,
                        mPharmacologicalActionListHandler);
            mRunningHeadHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.RUNNING_HEAD_ELEMENT,
                        mRunningHeadHandler);
            mTreeNumberListHandler = new ListHandler(parent,MeshParser.TREE_NUMBER_ELEMENT);
            setDelegate(MeshParser.TREE_NUMBER_LIST_ELEMENT,
                        mTreeNumberListHandler);
            mRecordOriginatorListHandler = new MeshRecordOriginatorList.Handler(parent);
            setDelegate(MeshParser.RECORD_ORIGINATORS_LIST_ELEMENT,
                        mRecordOriginatorListHandler);
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
            mRecordOriginatorListHandler.reset();
            mConceptListHandler.reset();
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes atts) 
            throws SAXException {

            super.startElement(namespaceURI,localName,qName,atts);
            if (MeshParser.DESCRIPTOR_RECORD_ELEMENT.equals(qName)) {
                String descriptorString = atts.getValue(MeshParser.DESCRIPTOR_CLASS_ATT);
                if ("2".equals(descriptorString))
                    mDescriptorClass = DescriptorClass.TWO;
                else if ("3".equals(descriptorString))
                    mDescriptorClass = DescriptorClass.THREE;
                else if ("4".equals(descriptorString))
                    mDescriptorClass = DescriptorClass.FOUR;
                else
                    mDescriptorClass = DescriptorClass.ONE;
            }                    
            
        }
        public Mesh getMesh() {
            return new Mesh(mDescriptorClass,
                            new MeshDescriptor(mDescriptorUIHandler.getText(),
                                               mDescriptorNameHandler.getText()),
                            mDateCreatedHandler.getDate(),
                            mDateRevisedHandler.getDate(),
                            mDateEstablishedHandler.getDate(),
                            mActiveMeshYearListHandler.getList(),
                            mAllowableQualifierListHandler.getAllowableQualifierList(),
                            mAnnotationHandler.getText(),
                            mHistoryNoteHandler.getText(),
                            mOnlineNoteHandler.getText(),
                            mPublicMeshNoteHandler.getText(),
                            mPreviousIndexingListHandler.getList(),
                            mEntryCombinationListHandler.getEntryCombinationList(),
                            mSeeRelatedListHandler.getDescriptorList(),
                            mConsiderAlsoHandler.getText(),
                            mPharmacologicalActionListHandler.getDescriptorList(),
                            mRunningHeadHandler.getText(),
                            mTreeNumberListHandler.getList(),
                            mRecordOriginatorListHandler.getRecordOriginatorList(),
                            mConceptListHandler.getConceptList());
        }
    }

    static class StringHandler extends DelegateHandler {
        private final TextAccumulatorHandler mTextAccumulator = new TextAccumulatorHandler();
        public StringHandler(DelegatingHandler parent, String element) {
            super(parent);
            setDelegate(element,mTextAccumulator);
        }
        public StringHandler(DelegatingHandler parent) {
            this(parent,MeshParser.STRING_ELEMENT);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public String getText() {
            return mTextAccumulator.getText();
        }
        public void reset() {
            mTextAccumulator.reset();
        }
    }

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
            mList.add(mAccumulator.getText());
        }
    }

    
}