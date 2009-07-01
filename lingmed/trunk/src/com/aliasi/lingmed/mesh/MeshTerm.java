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
 *
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshTerm {

    // <!ELEMENT Term (TermUI,String,
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
    // <!ELEMENT DateCreated (%normal.date;) >
    // <!ELEMENT Abbreviation (#PCDATA)>
    // <!ELEMENT EntryVersion (#PCDATA)>
    // <!ELEMENT ThesaurusIDlist (ThesaurusID+)>
    // <!ELEMENT ThesaurusID (#PCDATA)>
    // <!ELEMENT SortVersion (#PCDATA)>

    private final String mReferenceUi;
    private final String mReferenceString;
    private final MeshDate mDateCreated;
    private final String mAbbreviation;
    private final String mSortVersion;
    private final String mEntryVersion;
    private final List<String> mThesaurusIdList;
    private final boolean mConceptPreferred;
    private final boolean mIsPermuted;
    private final String mLexicalTag;
    private final boolean mPrintFlag;
    private final boolean mRecordPreferred;

    MeshTerm(String referenceUi,
             String referenceString,
             MeshDate dateCreated,
             String abbreviation,
             String sortVersion,
             String entryVersion,
             List<String> thesaurusIdList,
             boolean conceptPreferred,
             boolean isPermuted,
             String lexicalTag,
             boolean printFlag,
             boolean recordPreferred) {
        mReferenceUi = referenceUi;
        mReferenceString = referenceString;
        mDateCreated = dateCreated;
        mAbbreviation = abbreviation.length() == 0 ? null : abbreviation;
        mSortVersion = sortVersion.length() == 0 ? null : sortVersion;
        mEntryVersion = entryVersion.length() == 0 ? null : entryVersion;
        mThesaurusIdList = thesaurusIdList;
        mConceptPreferred = conceptPreferred;
        mIsPermuted = isPermuted;
        mLexicalTag = lexicalTag;
        mPrintFlag = printFlag;
        mRecordPreferred = recordPreferred;
    }

    public String referenceUi() {
        return mReferenceUi;
    }
    
    public String referenceString() {
        return mReferenceString;
    }

    public MeshDate dateCreated() {
        return mDateCreated;
    }

    public String abbreviation() {
        return mAbbreviation;
    }

    public String sortVersion() {
        return mSortVersion;
    }

    public String entryVersion() {
        return mEntryVersion;
    }

    public List<String> thesaurusIdList() {
        return Collections.unmodifiableList(mThesaurusIdList);
    }

    public boolean conceptPreferred() {
        return mConceptPreferred;
    }

    public boolean isPermuted() {
        return mIsPermuted;
    }

    public String lexicalTag() {
        return mLexicalTag;
    }

    public boolean printFlag() {
        return mPrintFlag;
    }

    public boolean recordPreferred() {
        return mRecordPreferred;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    Concept Preferred=" + conceptPreferred());
        sb.append("\n    Is Permuted=" + isPermuted());
        sb.append("\n    Lexical Tag=" + lexicalTag());
        sb.append("\n    Print Flag=" + printFlag());
        sb.append("\n    Record Preferred=" + recordPreferred());
        sb.append("\n    Reference UI=" + referenceUi());
        sb.append("\n    Reference String=" + referenceString());
        sb.append("\n    Date Created=" + dateCreated());
        sb.append("\n    Abbreviation=" + abbreviation());
        sb.append("\n    Sort Version=" + sortVersion());
        sb.append("\n    Entry Version=" + entryVersion());
        List<String> thesaurusIdList = thesaurusIdList();
        for (int i = 0; i < thesaurusIdList.size(); ++i)
            sb.append("\n    Thesaurus ID[" + i + "]="
                      + thesaurusIdList.get(i));
        return sb.toString();
    }
                

    static class Handler extends DelegateHandler {
        final TextAccumulatorHandler mUiHandler;
        final TextAccumulatorHandler mReferenceStringHandler;
        final MeshDate.Handler mDateHandler;
        final TextAccumulatorHandler mAbbreviationHandler;
        final TextAccumulatorHandler mSortVersionHandler;
        final TextAccumulatorHandler mEntryVersionHandler;
        final Mesh.ListHandler mThesaurusIdListHandler;
        boolean mConceptPreferred;
        boolean mIsPermuted;
        String mLexicalTag;
        boolean mPrintFlag;
        boolean mRecordPreferred;
        public Handler(DelegatingHandler parent) {
            super(parent);
            mUiHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.TERM_UI_ELEMENT,
                        mUiHandler);
            mReferenceStringHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.STRING_ELEMENT,
                        mReferenceStringHandler);
            mDateHandler = new MeshDate.Handler(parent);
            setDelegate(MeshParser.DATE_CREATED_ELEMENT,
                        mDateHandler);
            mAbbreviationHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.ABBREVIATION_ELEMENT,
                        mAbbreviationHandler);
            mSortVersionHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.SORT_VERSION_ELEMENT,
                        mSortVersionHandler);
            mEntryVersionHandler = new TextAccumulatorHandler();
            setDelegate(MeshParser.ENTRY_VERSION_ELEMENT,
                        mEntryVersionHandler);
            mThesaurusIdListHandler 
                = new Mesh.ListHandler(parent,
                                       MeshParser.THESAURUS_ID_ELEMENT);
            setDelegate(MeshParser.THESAURUS_ID_LIST_ELEMENT,
                        mThesaurusIdListHandler);
            
            
        }
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        @Override
        public void startElement(String url, String local, String qName,
                                 Attributes atts) throws SAXException {
            super.startElement(url,local,qName,atts);
            if (!MeshParser.TERM_ELEMENT.equals(qName)) return;
            mConceptPreferred 
                = "Y".equals(atts.getValue(MeshParser.CONCEPT_PREFERRED_TERM_YN_ATT));
            mIsPermuted = "Y".equals(atts.getValue(MeshParser.IS_PERMUTED_TERM_YN_ATT));
            mLexicalTag = atts.getValue(MeshParser.LEXICAL_TAG_ATT);
            mPrintFlag = "Y".equals(atts.getValue(MeshParser.PRINT_FLAG_YN_ATT));
            mRecordPreferred = "Y".equals(atts.getValue(MeshParser.RECORD_PREFERRED_TERM_YN_ATT));
        }
        public void reset() {
            mUiHandler.reset();
            mReferenceStringHandler.reset();
            mDateHandler.reset();
            mAbbreviationHandler.reset();
            mSortVersionHandler.reset();
            mEntryVersionHandler.reset();
            mThesaurusIdListHandler.reset();
        }
        public MeshTerm getTerm() {
            return new MeshTerm(mUiHandler.getText().trim(),
                                mReferenceStringHandler.getText().trim(),
                                mDateHandler.getDate(),
                                mAbbreviationHandler.getText().trim(),
                                mSortVersionHandler.getText().trim(),
                                mEntryVersionHandler.getText().trim(),
                                mThesaurusIdListHandler.getList(),
                                mConceptPreferred,
                                mIsPermuted,
                                mLexicalTag,
                                mPrintFlag,
                                mRecordPreferred);
        }

    }

    static class ListHandler extends DelegateHandler {
        final List<MeshTerm> mTermList = new ArrayList<MeshTerm>();
        final Handler mHandler; 
        public ListHandler(DelegatingHandler parent) {
            super(parent);
            mHandler = new Handler(parent);
            setDelegate(MeshParser.TERM_ELEMENT,mHandler);
        }
        public void startDocument() throws SAXException {
            super.startDocument();
            reset();
        }
        public void finishDelegate(String qName, DefaultHandler h) {
            mTermList.add(mHandler.getTerm());
        }
        public void reset() {
            mHandler.reset();
            mTermList.clear();
        }
        public List<MeshTerm> getTermList() {
            return new ArrayList<MeshTerm>(mTermList);
        }
    }
    
}