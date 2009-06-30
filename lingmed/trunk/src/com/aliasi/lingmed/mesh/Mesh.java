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

    private final DescriptorClass mDescriptorClass;
    private final String mDescriptorUI;
    private final String mDescriptorName;
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
    
    public Mesh(DescriptorClass descriptorClass,
                String descriptorUI,
                String descriptorName,
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
                List<EntryCombination> entryCombinationList) {
        mDescriptorClass = descriptorClass;
        mDescriptorUI = descriptorUI;
        mDescriptorName = descriptorName;
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
    }

    public DescriptorClass descriptorClass() {
        return mDescriptorClass;
    }

    public String descriptorUI() {
        return mDescriptorUI;
    }

    public String descriptorName() {
        return mDescriptorName;
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Descriptor UI=" + descriptorUI() + "\n");
        sb.append("Descriptor Class=" + descriptorClass() + "\n");
        sb.append("Descriptor Name=" + descriptorName() + "\n");
        sb.append("Date Created=" + dateCreated() + "\n");
        sb.append("Date Revised=" + dateRevised() + "\n");
        sb.append("Date Established=" + dateEstablished() + "\n");
        sb.append("Active Year List=" + activeYearList() + "\n");
        List<MeshAllowableQualifier> allowableQualifierList = allowableQualifierList();
        for (int i = 0; i < allowableQualifierList.size(); ++i)
            sb.append("Allowable Qualifiers[" + i + "]=" 
                      + allowableQualifierList.get(i) + "\n");
        sb.append("Annotation=" + annotation() + "\n");
        sb.append("History Note=" + historyNote() + "\n");
        sb.append("Online Note=" + onlineNote() + "\n");
        sb.append("Public Mesh Note=" + publicMeshNote() + "\n");
        sb.append("Previous Indexing List=" + previousIndexingList());
        List<EntryCombination> entryCombinationList = entryCombinationList();
        for (int i = 0; i < entryCombinationList.size(); ++i)
            sb.append("Entry Combination["  + i + "]="
                      + entryCombinationList.get(i) + "\n");
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
                            mDescriptorUIHandler.getText(),
                            mDescriptorNameHandler.getText(),
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
                            mEntryCombinationListHandler.getEntryCombinationList());
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