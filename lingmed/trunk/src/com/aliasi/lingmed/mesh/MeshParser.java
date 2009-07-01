package com.aliasi.lingmed.mesh;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.XMLParser;

import com.aliasi.xml.DelegatingHandler;

import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;


public class MeshParser extends XMLParser<ObjectHandler<Mesh>> {


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
    static final String RUNNING_HEAD_ELEMENT = "RunningHead";
    static final String SEE_RELATED_LIST_ELEMENT = "SeeRelatedList";
    static final String STRING_ELEMENT = "String";
    static final String TREE_NUMBER_ELEMENT = "TreeNumber";
    static final String TREE_NUMBER_LIST_ELEMENT = "TreeNumberList";
    static final String YEAR_ELEMENT = "Year";
    

    static final String DESCRIPTOR_CLASS_ATT = "DescriptorClass";

    
}