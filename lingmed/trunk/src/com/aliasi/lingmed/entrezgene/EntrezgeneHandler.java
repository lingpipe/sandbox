/*
 * LingPipe v. 2.0
 * Copyright (C) 2003-5 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://www.alias-i.com/lingpipe/licenseV1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.lingmed.entrezgene;

import com.aliasi.util.Pair;
import com.aliasi.util.Strings;

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

class EntrezgeneHandler extends DelegatingHandler {

    // <Gene-track_status value="live">0</Gene-track_status>
    AttributeValueHandler mGeneTrackStatusHandler 
        = new AttributeValueHandler();

    // <Gene-track_geneid>101</Gene-track_geneid>
    TextAccumulatorHandler mGeneTrackGeneIdHandler 
        = new TextAccumulatorHandler();

    //<Entrezgene_type value="protein-coding">6</Entrezgene_type>
    AttributeValueHandler mEntrezgeneTypeHandler 
        = new AttributeValueHandler();

    // <Gene-ref_locus>A1BG</Gene-ref_locus>
    TextAccumulatorHandler mGeneRefLocusHandler 
        = new TextAccumulatorHandler();

    // <Gene-ref_desc>alpha-1-B glycoprotein</Gene-ref_desc>
    TextAccumulatorHandler mGeneRefDescHandler 
        = new TextAccumulatorHandler();

    // <Gene-ref_maploc>19q13.4</Gene-ref_maploc>
    TextAccumulatorHandler mGeneRefMaplocHandler 
        = new TextAccumulatorHandler();

    // <Gene-ref_syn>
    //   <Gene-ref_syn_E>A1B</Gene-ref_syn_E>
    TextSetAccumulatorHandler mGeneRefSynsHandler;

    // <Prot-ref_desc>alpha 1B-glycoprotein</Prot-ref_desc>
    TextAccumulatorHandler mProtRefDescHandler
        = new TextAccumulatorHandler();

    // <Prot-ref_name>
    //   <Prot-ref_name_E>alpha 1B-glycoprotein</Prot-ref_name_E>
    TextSetAccumulatorHandler mProtRefNamesHandler;

    // <Entrezgene_summary>The protein encoded by this gene ...</Entrezgene_summary>    
    TextAccumulatorHandler mEntrezgeneSummaryHandler
        = new TextAccumulatorHandler();

    // <Entrezgene_source>
    EntrezgeneSourceHandler mEntrezgeneSourceHandler;

    // <Entrezgene_properties>
    EntrezgenePropertiesHandler mEntrezgenePropertiesHandler;

    // <Entrezgene_comments>
    EntrezgeneCommentsHandler mEntrezgeneCommentsHandler;

    // <Entrezgene_unique-keys>
    EntrezgeneUniqueKeysHandler mEntrezgeneUniqueKeysHandler;

    private String curEntrezStatus;
    private String curGeneId;
    private String curEntrezType;
    private String curSpeciesTaxName;
    private String curSpeciesCommonName;
    private String curSpeciesTaxonId;
    private String curGeneRefDesc;
    private String curGeneRefLocus;
    private String curGeneRefMaploc;
    private String[] curGeneRefSyns;
    private String curGeneSummary;
    private String curOfficialFullName;
    private String curOfficialSymbol;
    private String[] curProtRefNames;
    private String curProtRefDesc;
    private Pair<String,String[]>[] curPubMedRefs;
    private Pair<String,String[]>[] curUniqueKeys;
    private Pair<String,String[]> curAddLinks;

    EntrezgeneHandler() {
        setDelegate(EntrezTags.GENE_TRACK_GENEID_ELT,mGeneTrackGeneIdHandler);
        setDelegate(EntrezTags.GENE_REF_LOCUS_ELT,mGeneRefLocusHandler);
        setDelegate(EntrezTags.GENE_REF_DESC_ELT,mGeneRefDescHandler);
        setDelegate(EntrezTags.GENE_REF_MAPLOC_ELT,mGeneRefMaplocHandler);

        mGeneTrackStatusHandler = new AttributeValueHandler();
        setDelegate(EntrezTags.GENE_TRACK_STATUS_ELT,mGeneTrackStatusHandler);

        mEntrezgeneTypeHandler = new AttributeValueHandler();
        setDelegate(EntrezTags.ENTREZGENE_TYPE_ELT,mEntrezgeneTypeHandler);

        mGeneRefSynsHandler = 
            new TextSetAccumulatorHandler(this,EntrezTags.GENE_REF_SYN_E_ELT);
        setDelegate(EntrezTags.GENE_REF_SYN_ELT,mGeneRefSynsHandler);

        setDelegate(EntrezTags.PROT_REF_DESC_ELT,mProtRefDescHandler);
        mProtRefNamesHandler = 
            new TextSetAccumulatorHandler(this,EntrezTags.PROT_REF_NAME_E_ELT);
        setDelegate(EntrezTags.PROT_REF_NAME_ELT,mProtRefNamesHandler);

        setDelegate(EntrezTags.ENTREZGENE_SUMMARY_ELT,mEntrezgeneSummaryHandler);

        mEntrezgeneSourceHandler = new EntrezgeneSourceHandler(this);
        setDelegate(EntrezTags.ENTREZGENE_SOURCE_ELT,mEntrezgeneSourceHandler);

        mEntrezgenePropertiesHandler = new EntrezgenePropertiesHandler(this);
        setDelegate(EntrezTags.ENTREZGENE_PROPERTIES_ELT,mEntrezgenePropertiesHandler);

        mEntrezgeneCommentsHandler = new EntrezgeneCommentsHandler(this);
        setDelegate(EntrezTags.ENTREZGENE_COMMENTS_ELT,mEntrezgeneCommentsHandler);

        mEntrezgeneUniqueKeysHandler = new EntrezgeneUniqueKeysHandler(this);
        setDelegate(EntrezTags.ENTREZGENE_UNIQUE_KEYS_ELT,mEntrezgeneUniqueKeysHandler);
    }

    public void finishDelegate(String qName, DefaultHandler handler) {
        if (qName.equals(EntrezTags.GENE_TRACK_STATUS_ELT)) {
            curEntrezStatus = mGeneTrackStatusHandler.mValue;
        } else if (qName.equals(EntrezTags.GENE_TRACK_GENEID_ELT)) {
            curGeneId = mGeneTrackGeneIdHandler.getText();
        } else if (qName.equals(EntrezTags.ENTREZGENE_TYPE_ELT)) {
            curEntrezType = mEntrezgeneTypeHandler.mValue;
        } else if (qName.equals(EntrezTags.ENTREZGENE_SOURCE_ELT)) {
            curSpeciesTaxName = mEntrezgeneSourceHandler.getTaxName();
            curSpeciesCommonName = mEntrezgeneSourceHandler.getCommonName();
            curSpeciesTaxonId = mEntrezgeneSourceHandler.getTaxonId();
        } else if (qName.equals(EntrezTags.GENE_REF_DESC_ELT)) {
            curGeneRefDesc = mGeneRefDescHandler.getText();
        } else if (qName.equals(EntrezTags.GENE_REF_LOCUS_ELT)) {
            curGeneRefLocus = mGeneRefLocusHandler.getText();
        } else if (qName.equals(EntrezTags.GENE_REF_MAPLOC_ELT)) {
            curGeneRefMaploc = mGeneRefMaplocHandler.getText();
        } else if (qName.equals(EntrezTags.GENE_REF_SYN_ELT)) {
            curGeneRefSyns = mGeneRefSynsHandler.getTextSet();
        } else if (qName.equals(EntrezTags.ENTREZGENE_SUMMARY_ELT)) {
            curGeneSummary = mEntrezgeneSummaryHandler.getText();
        } else if (qName.equals(EntrezTags.ENTREZGENE_PROPERTIES_ELT)) {
            curOfficialFullName = mEntrezgenePropertiesHandler.getOfficialFullName();
            curOfficialSymbol = mEntrezgenePropertiesHandler.getOfficialSymbol();
        } else if (qName.equals(EntrezTags.PROT_REF_NAME_ELT)) {
            curProtRefNames = mProtRefNamesHandler.getTextSet();
        } else if (qName.equals(EntrezTags.PROT_REF_DESC_ELT)) {
            curProtRefDesc = mProtRefDescHandler.getText();
        } else if (qName.equals(EntrezTags.ENTREZGENE_COMMENTS_ELT)) {
            curPubMedRefs = mEntrezgeneCommentsHandler.getPubMedRefs();
			curAddLinks 
				= new Pair<String,String[]>("",mEntrezgeneCommentsHandler.getAddLinks());
        } else if (qName.equals(EntrezTags.ENTREZGENE_UNIQUE_KEYS_ELT)) {
            curUniqueKeys = mEntrezgeneUniqueKeysHandler.getUniqueKeys();
        }            
    }

    public EntrezGene geneEntry() { 
		Pair<String,String[]>[] keys = append(curUniqueKeys, curAddLinks);
        return new EntrezGene(curEntrezStatus,
                              curGeneId,
                              curEntrezType,
                              curSpeciesTaxName,
                              curSpeciesCommonName,
                              curSpeciesTaxonId,
                              curGeneRefDesc,
                              curGeneRefLocus,
                              curGeneRefMaploc,
                              curGeneRefSyns,
                              curGeneSummary,
                              curOfficialFullName,
                              curOfficialSymbol,
                              curProtRefNames,
                              curProtRefDesc,
                              curPubMedRefs,
                              keys);
    }

    public void startDocument() throws SAXException {
        //reset accum structures on new gene
        mGeneTrackGeneIdHandler.reset();
        mGeneRefLocusHandler.reset();
        mGeneRefDescHandler.reset();
        mProtRefDescHandler.reset();
        mEntrezgeneSummaryHandler.reset();

        curEntrezStatus = null;
        curGeneId = null;
        curEntrezType = null;
        curSpeciesTaxName = null;
        curSpeciesCommonName = null;
        curSpeciesTaxonId = null;
        curGeneRefDesc = null;
        curGeneRefLocus = null;
        curGeneRefMaploc = null;
        curGeneRefSyns = new String[0];
        curGeneSummary = null;
        curOfficialFullName = null;
        curOfficialSymbol = null;
        curProtRefNames = new String[0];
        curProtRefDesc = null;
        curPubMedRefs = null;
        curAddLinks = null;
        curUniqueKeys = null;

        super.startDocument();
    }

	private Pair<String,String[]>[] append(Pair<String,String[]>[] set, Pair<String,String[]> elt) {
		Set<Pair<String,String[]>> keys = new HashSet<Pair<String,String[]>>();
		for (Pair<String,String[]> item : set) {
			keys.add(item);
		}
		keys.add(elt);
		return keys.<Pair<String,String[]>>toArray(new Pair[keys.size()]);
	}


    /**
     * A <code>TextSetAccumulatorHandler</code> accumulates a set of 
     * text elements found in a contained element.
     * The set is cleared with each start document event, 
     * but is never freed entirely from memory.
     */
    class TextSetAccumulatorHandler extends DelegateHandler {
        Set<String> mTextSet = new HashSet<String>();
        String mElementTag;
        TextAccumulatorHandler mTextHandler = new TextAccumulatorHandler();

        /**
         * Construct a text set accumulator handler to collect text from
         * the specified contained element.
         *
         * @param parent A handle to the {@link DelegatingHandler}.
         * @param elementTag The tag name of the contained element.
         */
        public TextSetAccumulatorHandler(DelegatingHandler parent, String elementTag) {
            super(parent);
            mElementTag = elementTag;
            setDelegate(mElementTag,mTextHandler);
        }

        public void startDocument() {
            mTextSet.clear();
            mTextHandler.reset();
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(mElementTag)) {
                mTextSet.add(mTextHandler.getText());
            }
        }

        public String[] getTextSet() {
            if (mTextSet.size() == 0) return null;
            String[] texts = new String[mTextSet.size()];
            return mTextSet.toArray(texts);
        }


    }

    class EntrezgeneSourceHandler extends DelegateHandler {
        String mTaxName;
        String mCommonName;
        String mTaxonId;

        // <Org-ref_taxname>Homo sapiens</Org-ref_taxname>
        TextAccumulatorHandler mTaxNameHandler
            = new TextAccumulatorHandler();


        // <Org-ref_common>Homo sapiens</Org-ref_common>
        TextAccumulatorHandler mCommonNameHandler
            = new TextAccumulatorHandler();

        // <Org-ref_db><...>taxon<...>9606<...><...></Org-ref_db>
        DbTagHandler mDbtagHandler;


        EntrezgeneSourceHandler(DelegatingHandler parent) {
            super(parent);
            setDelegate(EntrezTags.ORG_REF_TAXNAME_ELT,mTaxNameHandler);
            setDelegate(EntrezTags.ORG_REF_COMMON_ELT,mCommonNameHandler);
            mDbtagHandler = new DbTagHandler(parent);
            setDelegate(EntrezTags.ORG_REF_DB_ELT,mDbtagHandler);
        }

        public void startDocument() {
            mTaxName = null;
            mCommonName = null;
            mTaxonId = null;
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(EntrezTags.ORG_REF_DB_ELT)) {
                if (mDbtagHandler.getDbtag() != null
                    && mDbtagHandler.getDbtag().a().equals(EntrezTags.TAXON)) {
                    mTaxonId = mDbtagHandler.getDbtag().b()[0];
                }
            } else if (qName.equals(EntrezTags.ORG_REF_TAXNAME_ELT)) {
                mTaxName =  mTaxNameHandler.getText();
            } else if (qName.equals(EntrezTags.ORG_REF_COMMON_ELT)) {
                mCommonName =  mCommonNameHandler.getText();
            }
        }

        public String getTaxName() { return mTaxName; }
        public String getCommonName() { return mCommonName; }
        public String getTaxonId() { return mTaxonId; }
    }

    class EntrezgenePropertiesHandler extends DelegateHandler {
        String mOfficialSymbol;
        String mOfficialFullName;
        GeneCommentaryHandler mGeneCommentaryHandler;

        EntrezgenePropertiesHandler(DelegatingHandler parent) {
            super(parent);
            mGeneCommentaryHandler = new GeneCommentaryHandler(parent);
            setDelegate(EntrezTags.GENE_COMMENTARY_ELT,mGeneCommentaryHandler);
        }

        public void startDocument() {
            mOfficialSymbol = null;
            mOfficialFullName = null;
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(EntrezTags.GENE_COMMENTARY_ELT)) {
                if (mGeneCommentaryHandler.getLabel() != null
                    && mGeneCommentaryHandler.getLabel().equals(EntrezTags.OFFICIAL_SYMBOL_TEXT)) {
                    mOfficialSymbol = mGeneCommentaryHandler.getText();
                } else if (mGeneCommentaryHandler.getLabel() != null
                           && mGeneCommentaryHandler.getLabel().equals(EntrezTags.OFFICIAL_FULL_NAME)) {
                    mOfficialFullName = mGeneCommentaryHandler.getText();
                }
            }
        }

        String getOfficialSymbol() { return mOfficialSymbol; }
        String getOfficialFullName() { return mOfficialFullName; }
    }

    class EntrezgeneCommentsHandler extends DelegateHandler {
        final ArrayList<Pair<String,String[]>> mPubMedRefs;
        final ArrayList<Pair<String,String[]>> mUniqueKeys;
        final HashSet<String> mAddLinks;
        final GeneCommentaryHandler mGeneCommentaryHandler;


        EntrezgeneCommentsHandler(DelegatingHandler parent) {
            super(parent);
            mPubMedRefs = new ArrayList<Pair<String,String[]>>();
            mUniqueKeys = new ArrayList<Pair<String,String[]>>();
            mAddLinks = new HashSet<String>();
            mGeneCommentaryHandler = new GeneCommentaryHandler(parent);
            setDelegate(EntrezTags.GENE_COMMENTARY_ELT,mGeneCommentaryHandler);
        }

        public void startDocument() {
            mPubMedRefs.clear();
            mUniqueKeys.clear();
            mAddLinks.clear();
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
			if (qName.equals(EntrezTags.GENE_COMMENTARY_ELT)) {
                if (mGeneCommentaryHandler.getPubMedIds() != null) {
                    if (mGeneCommentaryHandler.getText() != null) {
                        Pair<String,String[]> ref = 
                            new Pair<String,String[]>(mGeneCommentaryHandler.getText(),
                                                      mGeneCommentaryHandler.getPubMedIds());
                        mPubMedRefs.add(ref);
                    } else {
                        Pair<String,String[]> ref = 
                            new Pair<String,String[]>("",mGeneCommentaryHandler.getPubMedIds());
                        mPubMedRefs.add(ref);
                    }

				}
                if (mGeneCommentaryHandler.getAddLinks() != null) {
					String[] links = mGeneCommentaryHandler.getAddLinks();
					for (String link : links) mAddLinks.add(link);
				}
			}
        }

        Pair<String,String[]>[] getPubMedRefs() { 
            return mPubMedRefs.<Pair<String,String[]>>toArray(new Pair[mPubMedRefs.size()]);
        }

        String[] getAddLinks() { 
            return mAddLinks.toArray(new String[mAddLinks.size()]);
        }
    }


    class EntrezgeneUniqueKeysHandler extends DelegateHandler {
        final ArrayList<Pair<String,String[]>> mDbKeys;
        final DbTagHandler mDbtagHandler;

        EntrezgeneUniqueKeysHandler(DelegatingHandler parent) {
            super(parent);
            mDbKeys = new ArrayList<Pair<String,String[]>>(); 
            mDbtagHandler = new DbTagHandler(parent);
            setDelegate(EntrezTags.DBTAG_ELT,mDbtagHandler);
        }

        public void startDocument() {
            mDbKeys.clear();
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(EntrezTags.DBTAG_ELT)) {
                if (mDbtagHandler.getDbtag() != null) {
                    mDbKeys.add(mDbtagHandler.getDbtag());
                }
            }
        }

        Pair<String,String[]>[] getUniqueKeys() {
            return mDbKeys.<Pair<String,String[]>>toArray(new Pair[mDbKeys.size()]);
        }

    }

    class DbTagHandler extends DelegateHandler {
        String mDb;
        String[] mDbIds;

        TextAccumulatorHandler mDbHandler = new TextAccumulatorHandler();
        TextSetAccumulatorHandler mObjectIdHandler;

        DbTagHandler(DelegatingHandler parent) {
            super(parent);
            setDelegate(EntrezTags.DBTAG_DB_ELT,mDbHandler);
            mObjectIdHandler = 
                new TextSetAccumulatorHandler(parent,EntrezTags.DBTAG_OBJECTID_ID_ELT);
            setDelegate(EntrezTags.DBTAG_TAG_ELT,mObjectIdHandler);
        }

        public void startDocument() {
            mDbHandler.reset();
            mDbIds = new String[0];
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(EntrezTags.DBTAG_DB_ELT)) {
                mDb = mDbHandler.getText();
            } else if (qName.equals(EntrezTags.DBTAG_TAG_ELT)) {
                mDbIds = mObjectIdHandler.getTextSet();
            }
        }

        public Pair<String,String[]> getDbtag() {
            if (mDb == null || mDbIds == null || mDbIds.length == 0) return null;
            return new Pair<String,String[]>(mDb,mDbIds);
        }
    }

    class GeneCommentaryHandler extends DelegateHandler {
        TextAccumulatorHandler mGeneCommentaryLabel = new TextAccumulatorHandler();
        TextAccumulatorHandler mGeneCommentaryHeading = new TextAccumulatorHandler();
        TextAccumulatorHandler mGeneCommentaryText = new TextAccumulatorHandler();
        TextSetAccumulatorHandler mPubMedIdHandler;
		TextSetAccumulatorHandler mAddLinkHandler;
        String mLabel;
        String mText;
        String[] mPubMedIds;
		String[] mAddLinkIds;
        
        GeneCommentaryHandler(DelegatingHandler parent) {
            super(parent);
            mPubMedIdHandler = new TextSetAccumulatorHandler(parent,EntrezTags.PUBMEDID_ELT);
            mAddLinkHandler = new TextSetAccumulatorHandler(parent,EntrezTags.ADD_LINK_STR_ELT);
            setDelegate(EntrezTags.GENE_COMMENTARY_REFS_ELT,mPubMedIdHandler);
            setDelegate(EntrezTags.GENE_COMMENTARY_LABEL_ELT,mGeneCommentaryLabel);
            setDelegate(EntrezTags.GENE_COMMENTARY_TEXT_ELT,mGeneCommentaryText);
            setDelegate(EntrezTags.GENE_COMMENTARY_SOURCE_ELT,mAddLinkHandler);
        }
        
        public void startDocument() {
            mLabel = null;
            mText = null;
            mPubMedIds = null;
            mAddLinkIds = null;
            mGeneCommentaryLabel.reset();
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(EntrezTags.GENE_COMMENTARY_TEXT_ELT)) {
                mText = mGeneCommentaryText.getText();
            } else if (qName.equals(EntrezTags.GENE_COMMENTARY_LABEL_ELT)) {
                mLabel = mGeneCommentaryLabel.getText();
            } else if (qName.equals(EntrezTags.GENE_COMMENTARY_REFS_ELT)) {
                mPubMedIds = mPubMedIdHandler.getTextSet();
            } else if (qName.equals(EntrezTags.GENE_COMMENTARY_SOURCE_ELT)) {
                mAddLinkIds = mAddLinkHandler.getTextSet();
            }
        }

        String getLabel() { return mLabel; }
        String getText() { return mText; }
        String[] getPubMedIds() { return mPubMedIds; }
        String[] getAddLinks() { return mAddLinkIds; }
	}

    class GeneRefSynHandler extends DelegateHandler {
        Set<String> mSynESet;
        TextAccumulatorHandler mSynEHandler = new TextAccumulatorHandler();

        GeneRefSynHandler(DelegatingHandler parent) {
            super(parent);
            setDelegate(EntrezTags.GENE_REF_SYN_E_ELT,mSynEHandler);
        }

        public void startDocument() {
            mSynESet = new HashSet<String>();
            mSynEHandler.reset();
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(EntrezTags.GENE_REF_SYN_E_ELT)) 
                mSynESet.add(mSynEHandler.getText());
        }

        public String[] getGeneRefSyns() {
            String[] syns = new String[mSynESet.size()];
            return mSynESet.toArray(syns);
        }
    }

    class AttributeValueHandler extends DefaultHandler {
        String mValue;
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts)
            throws SAXException {
            mValue = atts.getValue("value");
        }
    }

    class PubMedIdHandler extends DelegateHandler {
        Set<String> mPubMedIdSet = new HashSet<String>();
        TextAccumulatorHandler mPMIDHandler = new TextAccumulatorHandler();

        public PubMedIdHandler(DelegatingHandler parent) {
            super(parent);
            setDelegate(EntrezTags.PUBMEDID_ELT,mPMIDHandler);
        }

        public void startDocument() {
            mPMIDHandler.reset();
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(EntrezTags.PUBMEDID_ELT)) {
                mPubMedIdSet.add(mPMIDHandler.getText());
            }
        }

        public String[] getPubMedIds() {
            if (mPubMedIdSet.size() == 0) return null;
            String[] ids = new String[mPubMedIdSet.size()];
            return mPubMedIdSet.toArray(ids);
        }

        public void reset() { 
            mPubMedIdSet.clear();
        }
    }


}
