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

    public Mesh(DescriptorClass descriptorClass,
                String descriptorUI,
                String descriptorName,
                MeshDate dateCreated,
                MeshDate dateRevised,
                MeshDate dateEstablished,
                List<String> activeMeshYearList) {
        mDescriptorClass = descriptorClass;
        mDescriptorUI = descriptorUI;
        mDescriptorName = descriptorName;
        mDateCreated = dateCreated;
        mDateRevised = dateRevised;
        mDateEstablished = dateEstablished;
        mActiveMeshYearList = activeMeshYearList;
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Descriptor UI=" + descriptorUI() + "\n");
        sb.append("Descriptor Class=" + descriptorClass() + "\n");
        sb.append("Descriptor Name=" + descriptorName() + "\n");
        sb.append("Date Created=" + dateCreated() + "\n");
        sb.append("Date Revised=" + dateRevised() + "\n");
        sb.append("Date Established=" + dateEstablished() + "\n");
        sb.append("Active Year List=" + activeYearList() + "\n");
        return sb.toString();
    }

    static class Handler extends DelegateHandler {
        private DescriptorClass mDescriptorClass;
        private final StringHandler mDescriptorNameHandler;
        private final TextAccumulatorHandler mDescriptorUIHandler;
        private final MeshDate.Handler mDateCreatedHandler;
        private final MeshDate.Handler mDateRevisedHandler;
        private final MeshDate.Handler mDateEstablishedHandler;
        private final ActiveMeshYearListHandler mActiveMeshYearListHandler;
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
            mActiveMeshYearListHandler = new ActiveMeshYearListHandler(parent);
            setDelegate(MeshParser.ACTIVE_MESH_YEAR_LIST_ELEMENT,
                        mActiveMeshYearListHandler);
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
                            mActiveMeshYearListHandler.getYearList());
        }
    }

    static class StringHandler extends DelegateHandler {
        private final TextAccumulatorHandler mTextAccumulator = new TextAccumulatorHandler();
        public StringHandler(DelegatingHandler parent) {
            super(parent);
            setDelegate(MeshParser.STRING_ELEMENT,mTextAccumulator);
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

    static class ActiveMeshYearListHandler extends DelegateHandler {
        private List<String> mActiveYearList = new ArrayList<String>();
        private TextAccumulatorHandler mYearAccumulator;
        public ActiveMeshYearListHandler(DelegatingHandler parent) {
            super(parent);
            mYearAccumulator = new TextAccumulatorHandler();
            setDelegate(MeshParser.YEAR_ELEMENT,mYearAccumulator);
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void reset() {
            mActiveYearList.clear();
        }
        public List<String> getYearList() {
            return new ArrayList<String>(mActiveYearList);
        }
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (!MeshParser.YEAR_ELEMENT.equals(qName)) return;
            mActiveYearList.add(mYearAccumulator.getText());
        }
    }

    
}