package com.aliasi.lingmed.mesh;

import com.aliasi.lingpipe.xml.DelegateHandler;
import com.aliasi.lingpipe.xml.DelegatingHandler;
import com.aliasi.lingpipe.xml.TextAccumulatorHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A {@code MeshTerm} is the atomic unit of the MeSH vocabulary, containing
 * a free-text string as well as metadata about the term.  The term and
 * its unique identifier (UI) are available through the method {@link #nameUi()}.
 *
 * <p>Each term has a lexical type given by {@link #lexicalTag()}, which
 * takes on one of the following values:
 *
 * <blockquote><table border="1" cellpadding="5">
 * <tr><th>Lexical Type</th><th>Description</th></tr>
 * <tr><td>ABB</td><td>Abbreviation</td>
 * <tr><td>ABX</td><td>Embedded abbreviation</td></tr>
 * <tr><td>ACR</td><td>Acronym</td></tr>
 * <tr><td>ACX</td><td>Embedded acronym</td></tr>
 * <tr><td>EPO</td><td>Eponym</td></tr>
 * <tr><td>LAB</td><td>Lab number</td></tr>
 * <tr><td>NAM</td><td>Proper name</td></tr>
 * <tr><td>NON</td><td>None</td></tr>
 * <tr><td>TRD</td><td>Trade name</td></tr>
 * </table></blockquote>
 *
 * <p>Whether a term is the preferred term for a concept is
 * indicated on the term using the method {@link #isConceptPreferred()}.
 * For records, whether a term is preferred is indicated
 * using {@link #isRecordPreferred()}.
 * 
 * @author Bob Carpenter
 * @version 1.3
 * @since LingMed1.3
 */
public class MeshTerm {

    private final MeshNameUi mNameUi;

    private final MeshDate mDateCreated;
    private final String mAbbreviation;
    private final String mSortVersion;
    private final String mEntryVersion;
    private final List<String> mThesaurusIdList;
    private final boolean mIsPreferred;
    private final boolean mIsPermuted;
    private final String mLexicalTag;
    private final boolean mPrintFlag;
    private final boolean mIsRecordPreferred;

    MeshTerm(String referenceUi,
             String referenceString,
             MeshDate dateCreated,
             String abbreviation,
             String sortVersion,
             String entryVersion,
             List<String> thesaurusIdList,
             boolean isConceptPreferred,
             boolean isPermuted,
             String lexicalTag,
             boolean printFlag,
             boolean isRecordPreferred) {
        mNameUi = new MeshNameUi(referenceString,referenceUi);
        mDateCreated = dateCreated;
        mAbbreviation = abbreviation.length() == 0 ? null : abbreviation;
        mSortVersion = sortVersion.length() == 0 ? null : sortVersion;
        mEntryVersion = entryVersion.length() == 0 ? null : entryVersion;
        mThesaurusIdList = thesaurusIdList;
        mIsPreferred = isConceptPreferred;
        mIsPermuted = isPermuted;
        mLexicalTag = lexicalTag;
        mPrintFlag = printFlag;
        mIsRecordPreferred = isRecordPreferred;
    }

    /**
     * Returns the official name and unique identifier (UI) for
     * this term.
     *
     * @return Name and UI for this term.
     */
    public MeshNameUi nameUi() {
        return mNameUi;
    }

    /**
     * Returns the date when the term was first entered in to the MeSH
     * data entry system.  
     *
     * @return The date this term was created.
     */ 
    public MeshDate dateCreated() {
        return mDateCreated;
    }

    /**
     * Returns a two-letter abbreviation for this term if it is a
     * qualifier or {@code null} if there is no abbreviation.
     *
     * @return Abbreviation for this term if it is a qualifier.
     */
    public String abbreviation() {
        return mAbbreviation;
    }

    /**
     * Returns a rewritten version of this term that is used
     * for alpha-numeric sorting of terms, or {@code null} if
     * there is no sort version.
     *
     * @return Version of term for sorting.
     */
    public String sortVersion() {
        return mSortVersion;
    }

    /**
     * An all uppercase custom short form of this term used for
     * indexing and searching at NLM, or {@code null} if there
     * is no entry version.
     *
     * @return Entry version for this term.
     */
    public String entryVersion() {
        return mEntryVersion;
    }

    /**
     * Returns a list of free text names and years of thesauri
     * in which this term occurs.
     *
     * @return List of thesaurus names and years.
     */
    public List<String> thesaurusIdList() {
        return Collections.unmodifiableList(mThesaurusIdList);
    }

    /**
     * Returns {@code true} if this is the preferred term for
     * its concept.
     *
     * @return {@code true} if this is the preferred term for its
     * concept.
     */
    public boolean isConceptPreferred() {
        return mIsPreferred;
    }

    /**
     * Returns {@code true} if the term is created automatically
     * in the MeSH editing application as a variant of another term in
     * the record in which it appears.
     *
     * @return {@code true} if the term is a variant of another term.
     */
    public boolean isPermuted() {
        return mIsPermuted;
    }

    /**
     * Returns the lexical tag for the type of this term.  See the
     * class documentatino above for possible values.
     *
     * @return Lexical tag for the type of this term.
     */
    public String lexicalTag() {
        return mLexicalTag;
    }

    /**
     * Return {@code true} if this term appears in the printed
     * version of MeSH.
     *
     * @return {@code true} if this term appears in the printed
     * version of MeSH.
     */
    public boolean printFlag() {
        return mPrintFlag;
    }

    /**
     * Returns {@code true} if this term is the preferred term for the
     * record in which it appears. Note that this method is context
     * sensitive and not a property of the term per se.
     *
     * @return {@code true} if this term is the preferred term
     * for the record in which it appears.
     */
    public boolean isRecordPreferred() {
        return mIsRecordPreferred;
    }

    /**
     * Returns a complete string-based representation of this term.
     * All of the information included in the string is available
     * programatically from the other methods in this class.
     *
     * @return String representation of this term.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name/UI=" + mNameUi.toString());
        sb.append("\n    Concept Preferred=" + isConceptPreferred());
        sb.append("\n    Is Permuted=" + isPermuted());
        sb.append("\n    Lexical Tag=" + lexicalTag());
        sb.append("\n    Print Flag=" + printFlag());
        sb.append("\n    Record Preferred=" + isRecordPreferred());
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
                

    static class Handler extends BaseHandler<MeshTerm> {
        final TextAccumulatorHandler mUiHandler;
        final TextAccumulatorHandler mReferenceStringHandler;
        final MeshDate.Handler mDateHandler;
        final TextAccumulatorHandler mAbbreviationHandler;
        final TextAccumulatorHandler mSortVersionHandler;
        final TextAccumulatorHandler mEntryVersionHandler;
        final Mesh.ListHandler mThesaurusIdListHandler;
        boolean mIsPreferred;
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
        public void startElement(String url, String local, String qName,
                                 Attributes atts) throws SAXException {
            super.startElement(url,local,qName,atts);
            if (!MeshParser.TERM_ELEMENT.equals(qName)) return;
            mIsPreferred 
                = "Y".equals(atts.getValue(MeshParser.CONCEPT_PREFERRED_TERM_YN_ATT));
            mIsPermuted = "Y".equals(atts.getValue(MeshParser.IS_PERMUTED_TERM_YN_ATT));
            mLexicalTag = atts.getValue(MeshParser.LEXICAL_TAG_ATT);
            mPrintFlag = "Y".equals(atts.getValue(MeshParser.PRINT_FLAG_YN_ATT));
            mRecordPreferred = "Y".equals(atts.getValue(MeshParser.RECORD_PREFERRED_TERM_YN_ATT));
        }
        @Override
        public void reset() {
            mUiHandler.reset();
            mReferenceStringHandler.reset();
            mDateHandler.reset();
            mAbbreviationHandler.reset();
            mSortVersionHandler.reset();
            mEntryVersionHandler.reset();
            mThesaurusIdListHandler.reset();
        }
        public MeshTerm getObject() {
            return new MeshTerm(mUiHandler.getText().trim(),
                                mReferenceStringHandler.getText().trim(),
                                mDateHandler.getObject(),
                                mAbbreviationHandler.getText().trim(),
                                mSortVersionHandler.getText().trim(),
                                mEntryVersionHandler.getText().trim(),
                                mThesaurusIdListHandler.getList(),
                                mIsPreferred,
                                mIsPermuted,
                                mLexicalTag,
                                mPrintFlag,
                                mRecordPreferred);
        }

    }

    static class ListHandler extends BaseListHandler<MeshTerm> {
        public ListHandler(DelegatingHandler parent) {
            super(parent,
                  new Handler(parent),
                  MeshParser.TERM_ELEMENT);
        }
    }
    
}
