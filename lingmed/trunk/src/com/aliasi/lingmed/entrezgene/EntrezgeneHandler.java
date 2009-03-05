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
import java.util.Iterator;
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

    // <Entrezgene_gene-source>
    // <Entrezgene_prot>

    // <Entrezgene_locus>
    EntrezgeneLocusHandler mEntrezgeneLocusHandler;

    // <Entrezgene_comments>
    EntrezgeneCommentsHandler mEntrezgeneCommentsHandler;


    private String curGeneTrackStatus;
    private String curGeneId;
    private String curEntrezgeneType;

    private Species curSpecies;
    private String curGeneRefMaploc;
	private GenomeLocation curGenomeLocation;

    private String curOfficialSymbol;
    private String curOfficialFullName;
    private String curGeneSummary;

    private String curGeneRefName;
    private String[] curGeneRefSyns;
    private String curGeneRefDesc;

    private String[] curProtRefNames;
    private String curProtRefDesc;

    private Pair<String,String>[] curPubMedRefs;
    private Pair<String,String[]>[] curDbLinks;
    //	private HashMap<String,String> curPubMedRefs;
    //	private HashMap<String,HashSet<String>> curDbLinks;

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

        mEntrezgeneLocusHandler = new EntrezgeneLocusHandler(this);
        setDelegate(EntrezTags.ENTREZGENE_LOCUS_ELT,mEntrezgeneLocusHandler);

        mEntrezgeneSourceHandler = new EntrezgeneSourceHandler(this);
        setDelegate(EntrezTags.ENTREZGENE_SOURCE_ELT,mEntrezgeneSourceHandler);

        mEntrezgenePropertiesHandler = new EntrezgenePropertiesHandler(this);
        setDelegate(EntrezTags.ENTREZGENE_PROPERTIES_ELT,mEntrezgenePropertiesHandler);

        mEntrezgeneCommentsHandler = new EntrezgeneCommentsHandler(this);
        setDelegate(EntrezTags.ENTREZGENE_COMMENTS_ELT,mEntrezgeneCommentsHandler);
    }

    public void finishDelegate(String qName, DefaultHandler handler) {
        if (qName.equals(EntrezTags.GENE_TRACK_STATUS_ELT)) {
            curGeneTrackStatus = mGeneTrackStatusHandler.value();
        } else if (qName.equals(EntrezTags.GENE_TRACK_GENEID_ELT)) {
            curGeneId = mGeneTrackGeneIdHandler.getText();
        } else if (qName.equals(EntrezTags.ENTREZGENE_TYPE_ELT)) {
            curEntrezgeneType = mEntrezgeneTypeHandler.value();
        } else if (qName.equals(EntrezTags.ENTREZGENE_SOURCE_ELT)) {
			curSpecies = mEntrezgeneSourceHandler.getSpecies();
        } else if (qName.equals(EntrezTags.GENE_REF_DESC_ELT)) {
            curGeneRefDesc = mGeneRefDescHandler.getText();
        } else if (qName.equals(EntrezTags.GENE_REF_LOCUS_ELT)) {
            curGeneRefName = mGeneRefLocusHandler.getText();
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
        } else if (qName.equals(EntrezTags.ENTREZGENE_LOCUS_ELT)) {
			curGenomeLocation = mEntrezgeneLocusHandler.getLocation();
		} else if (qName.equals(EntrezTags.ENTREZGENE_COMMENTS_ELT)) {
            curPubMedRefs= mEntrezgeneCommentsHandler.getPubMedRefs();
            curDbLinks= mEntrezgeneCommentsHandler.getDbLinks();
		}            
	}

	public EntrezGene geneEntry() { 
		return new EntrezGene(curGeneTrackStatus,
                              curGeneId,
                              curEntrezgeneType,
                              curSpecies,
                              curGeneRefMaploc,
							  curGenomeLocation,
                              curOfficialSymbol,
                              curOfficialFullName,
                              curGeneSummary,
                              curGeneRefName,
                              curGeneRefSyns,
                              curGeneRefDesc,
                              curProtRefNames,
                              curProtRefDesc,
                              curPubMedRefs,
							  curDbLinks);
    }

    public void startDocument() throws SAXException {
        //reset accum structures on new gene
        mGeneTrackGeneIdHandler.reset();
        mGeneRefLocusHandler.reset();
        mGeneRefDescHandler.reset();
        mProtRefDescHandler.reset();
        mEntrezgeneSummaryHandler.reset();

        curGeneTrackStatus = null;
        curGeneId = null;
        curEntrezgeneType = null;
        curSpecies = null;
        curGeneRefDesc = null;
        curGeneRefName = null;
        curGeneRefMaploc = null;
        curGeneRefSyns = new String[0];
        curGeneSummary = null;
		curGenomeLocation = null;
        curOfficialFullName = null;
        curOfficialSymbol = null;
        curProtRefNames = new String[0];
        curProtRefDesc = null;
        curPubMedRefs = null;
        curDbLinks = null;

        super.startDocument();
    }


	/* get genome information from ENTREZGENE_SOURCE element */
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
				mTaxonId = mDbtagHandler.getDbId();
            } else if (qName.equals(EntrezTags.ORG_REF_TAXNAME_ELT)) {
                mTaxName =  mTaxNameHandler.getText();
            } else if (qName.equals(EntrezTags.ORG_REF_COMMON_ELT)) {
                mCommonName =  mCommonNameHandler.getText();
            }
        }

		public Species getSpecies() {
			return Species.valueOf(mTaxonId,mTaxName,mCommonName);
		}
    }

    class EntrezgenePropertiesHandler extends DelegateHandler {
        String mOfficialSymbol;
        String mOfficialFullName;
		String mType;
		String mLabel;
		String mText;

		AttributeValueHandler mTypeHandler = new AttributeValueHandler();
		TextAccumulatorHandler mLabelHandler = new TextAccumulatorHandler();
 		TextAccumulatorHandler mTextHandler = new TextAccumulatorHandler();

        EntrezgenePropertiesHandler(DelegatingHandler parent) {
            super(parent);
			setDelegate(EntrezTags.GENE_COMMENTARY_TYPE_ELT,mTypeHandler);
			setDelegate(EntrezTags.GENE_COMMENTARY_LABEL_ELT,mLabelHandler);
			setDelegate(EntrezTags.GENE_COMMENTARY_TEXT_ELT,mTextHandler);
        }

        public void startDocument() {
            mOfficialSymbol = null;
            mOfficialFullName = null;
			mTypeHandler.reset();
			mLabelHandler.reset();
			mTextHandler.reset();
        }

        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(EntrezTags.GENE_COMMENTARY_TYPE_ELT)) {
				mType = mTypeHandler.value();
				mLabel = null;
				mText = null;
			} else if (qName.equals(EntrezTags.GENE_COMMENTARY_LABEL_ELT)) {
				mLabel = mLabelHandler.getText();
			} else if (qName.equals(EntrezTags.GENE_COMMENTARY_TEXT_ELT)) {
				mText = mTextHandler.getText();
				if (EntrezTags.PROPERTY.equalsIgnoreCase(mType)) {
					if (EntrezTags.OFFICIAL_SYMBOL.equalsIgnoreCase(mLabel)) {
						mOfficialSymbol = mText;
					} else if (EntrezTags.OFFICIAL_FULL_NAME.equalsIgnoreCase(mLabel)) {
						mOfficialFullName = mText;
					}
				}
            }
        }

        String getOfficialSymbol() { return mOfficialSymbol; }
        String getOfficialFullName() { return mOfficialFullName; }
    }


	/* EntrezGeneLocus handler:  collects GenomeLocation
	   <Entrezgene_locus>
 	     <Gene-commentary_type value="genomic">1</Gene-commentary_type>
	     <Gene-commentary_heading>Reference assembly</Gene-commentary_heading>
	     <Gene-commentary_accession>NC_000019</Gene-commentary_accession>
	     <Gene-commentary_version>8</Gene-commentary_version>
	     <Gene-commentary_seqs><...><Seq-interval><...>
	*/
	class EntrezgeneLocusHandler extends DelegateHandler {
		String mAccession;
		String mVersion;
		String mStrand;
		int mFrom;
		int mTo;
		boolean mIsGenomic;
		boolean mIsRefAssembly;
		int mSectionCount;  // multiple sections: <Gene-commentary>
		GenomeLocation mLocation;

		AttributeValueHandler mTypeHandler = new AttributeValueHandler();
		TextAccumulatorHandler mHeadingHandler = new TextAccumulatorHandler();
		TextAccumulatorHandler mAccessionHandler = new TextAccumulatorHandler();
		TextAccumulatorHandler mVersionHandler= new TextAccumulatorHandler();
		SeqIntervalHandler mSeqHandler;

		EntrezgeneLocusHandler(DelegatingHandler parent) {
			super(parent);
			setDelegate(EntrezTags.GENE_COMMENTARY_TYPE_ELT,mTypeHandler);
			setDelegate(EntrezTags.GENE_COMMENTARY_HEADING_ELT,mHeadingHandler);
			setDelegate(EntrezTags.GENE_COMMENTARY_ACCESSION_ELT,mAccessionHandler);
			setDelegate(EntrezTags.GENE_COMMENTARY_VERSION_ELT,mVersionHandler);
			mSeqHandler = new SeqIntervalHandler(parent);
			setDelegate(EntrezTags.GENE_COMMENTARY_SEQS_ELT,mSeqHandler);
		}

		public void startDocument() {
			mHeadingHandler.reset();
			mAccessionHandler.reset();
			mVersionHandler.reset();
			mAccession = null;
			mVersion = null;
			mStrand = null;
			mFrom = -1;
			mTo = -1;
			mIsGenomic = false;
			mIsRefAssembly = false;
			mSectionCount = 0;
			mLocation = null;
		}

		public void finishDelegate(String qName, DefaultHandler handler) {
			if (qName.equals(EntrezTags.GENE_COMMENTARY_TYPE_ELT)) {
				if (EntrezTags.GENOMIC.equalsIgnoreCase(mTypeHandler.value())) {
					mIsGenomic = true;
					mSectionCount++;
				}
			} else if (qName.equals(EntrezTags.GENE_COMMENTARY_HEADING_ELT)) {
				if (EntrezTags.REFERENCE_HEADING_TEXT.equalsIgnoreCase(mHeadingHandler.getText())
					&& mIsGenomic 
					&& mSectionCount == 1) {
					mIsRefAssembly = true;
				} else {
					mIsRefAssembly = false;
				}
			} else if (qName.equals(EntrezTags.GENE_COMMENTARY_ACCESSION_ELT)
					   && mIsRefAssembly) {
				mAccession = mAccessionHandler.getText();
			} else if (qName.equals(EntrezTags.GENE_COMMENTARY_VERSION_ELT)
					   && mIsRefAssembly) {
				mVersion = mVersionHandler.getText();
				if (mAccession != null 
					&& mAccession.length() > 0 
					&& mVersion != null
					&& mVersion.length() > 0) {
					mAccession = mAccession + "." + mVersion;
				}
			} else if (qName.equals(EntrezTags.GENE_COMMENTARY_SEQS_ELT)
					   && mIsRefAssembly) {
				mStrand = mSeqHandler.getStrand();
				mFrom = mSeqHandler.getLower();
				mTo = mSeqHandler.getUpper();
				mLocation = new GenomeLocation(mAccession,mStrand,mFrom,mTo);
			}
		}

		GenomeLocation getLocation() { 
			return mLocation;
		}
	}

	/* SeqIntervalHandler
	   <Gene-commentary_seqs><...>
	   <Seq-interval_from>DDD</Seq-interval_from>
	   <Seq-interval_to>DDD</Seq-interval_to>
	   <Na-strand value="plus"/><...></Gene-commentary_seqs>
	*/
	class SeqIntervalHandler extends DelegateHandler {
		TextAccumulatorHandler mFromHandler = new TextAccumulatorHandler();
		TextAccumulatorHandler mToHandler = new TextAccumulatorHandler();
		AttributeValueHandler mStrandHandler = new AttributeValueHandler();
		int mFrom;
		int mTo;
		String mStrand;

		SeqIntervalHandler(DelegatingHandler parent) {
			super(parent);
			setDelegate(EntrezTags.SEQ_INTERVAL_FROM_ELT,mFromHandler);
			setDelegate(EntrezTags.SEQ_INTERVAL_TO_ELT,mToHandler);
			setDelegate(EntrezTags.NA_STRAND_ELT,mStrandHandler);
		}

		public void startDocument() {
			mFromHandler.reset();
			mToHandler.reset();
			mFrom = -1;
			mTo = -1;
			mStrand = null;
		}

		public void finishDelegate(String qName, DefaultHandler handler) {
			if (qName.equals(EntrezTags.SEQ_INTERVAL_FROM_ELT)) {
				try {
					mFrom = Integer.parseInt(mFromHandler.getText());
				} catch (NumberFormatException e) {
				}
			} else if (qName.equals(EntrezTags.SEQ_INTERVAL_TO_ELT)) {
				try {
					mTo = Integer.parseInt(mToHandler.getText());
				} catch (NumberFormatException e) {
				}
			} else if (qName.equals(EntrezTags.NA_STRAND_ELT)) {
				mStrand = mStrandHandler.value();
			}
		}

		public String getStrand() { return mStrand; }
		public int getLower() { return mFrom < mTo ? mFrom : mTo; }
		public int getUpper() { return mFrom > mTo ? mFrom : mTo; }
	}

	/* Entrezgene_comments section contains diverse annotations
	   <Entrezgene_comments>
	   divided into sub-sections of GENE_COMMENTARY elements
       one <Gene-commentary_type> elt per section:
   	       <Gene-commentary_type value="comment">254</Gene-commentary_type>
   	   1. RefSeq status
	   2. Pubmed Bibliography - enclosing elts:
          <Gene-commentary_refs>...<PubMedId>DDDDDDDDD</PubMedId>
	   3. RefSeq sequences - sequence data, coords, accession names
	   4. Related sequences - more sequence data 
	   5. Additional Links section  - enclosing elements:
          <Gene-commentary_heading>Additional Links</Gene-commentary_heading>
          <Gene-commentary_comment>...<Dbtag_db>Name</Dbtag_db>...
                  <Object-id_str>ID</Object-id_str> OR <Object-id_id>ID</Object-id_id>
	   6-N - gene rifs:
 	     <Gene-commentary_type value="generif">18</Gene-commentary_type>
 	     <Gene-commentary_text>comment goes here</Gene-commentary_type>
         <PubMedId>DDDDDDDDD</PubMedId>
	   N+1 more stuff...
	*/
	class EntrezgeneCommentsHandler extends DelegateHandler {
		final HashMap<String,String> mPmidNotesMap;
		final HashMap<String,HashSet<String>> mDbNameIdsMap;
		String mType;
		String mHeading;
		String mGeneRif;

		AttributeValueHandler mTypeHandler = new AttributeValueHandler(); // Gene-commentary_type
		TextAccumulatorHandler mHeadingHandler = new TextAccumulatorHandler();
		TextAccumulatorHandler mGeneRifHandler = new TextAccumulatorHandler();
		TextAccumulatorHandler mPubMedIdHandler = new TextAccumulatorHandler();

		DbTagHandler mDbTagHandler;

		EntrezgeneCommentsHandler(DelegatingHandler parent) {
			super(parent);
			mPmidNotesMap = new HashMap<String,String>();
			mDbNameIdsMap = new HashMap<String,HashSet<String>>();
			setDelegate(EntrezTags.GENE_COMMENTARY_TYPE_ELT,mTypeHandler);
			setDelegate(EntrezTags.GENE_COMMENTARY_HEADING_ELT,mHeadingHandler);
			setDelegate(EntrezTags.GENE_COMMENTARY_TEXT_ELT,mGeneRifHandler);
			setDelegate(EntrezTags.PUBMEDID_ELT,mPubMedIdHandler);

			mDbTagHandler = new DbTagHandler(parent);
			setDelegate(EntrezTags.DBTAG_ELT,mDbTagHandler);
		}

		public void startDocument() {
			mTypeHandler.reset();
			mHeadingHandler.reset();
			mGeneRifHandler.reset();
			mPubMedIdHandler.reset();
			mPmidNotesMap.clear();
			mDbNameIdsMap.clear();
			mType = null;
			mGeneRif = null;
		}

		public void finishDelegate(String qName, DefaultHandler handler) {
			if (qName.equals(EntrezTags.GENE_COMMENTARY_TYPE_ELT)) {
				mType = mTypeHandler.value();
				mGeneRif = null;
			} else if (qName.equals(EntrezTags.GENE_COMMENTARY_HEADING_ELT)) {
				mHeading = mHeadingHandler.getText();
			} else if (qName.equals(EntrezTags.GENE_COMMENTARY_TEXT_ELT)) {
				if (EntrezTags.GENERIF.equalsIgnoreCase(mType)) {
					mGeneRif = mGeneRifHandler.getText();
				}
			} else if (qName.equals(EntrezTags.PUBMEDID_ELT)) {
				mPmidNotesMap.put(mPubMedIdHandler.getText(),mGeneRif);
			} else if (qName.equals(EntrezTags.DBTAG_ELT)) {
				if (EntrezTags.ADDITIONAL_LINKS_TEXT.equalsIgnoreCase(mHeading)) {
					String dbName = mDbTagHandler.getDbName();
					String dbId = mDbTagHandler.getDbId();
                    // only get dbIds that are different than geneId
                    if (dbId != null && !(dbId.equals(curGeneId))) {
                        HashSet<String> ids = null;
                        if (mDbNameIdsMap.containsKey(dbName)) {
                            ids = mDbNameIdsMap.get(dbName);
                        } else {
                            ids = new HashSet<String>();
                        }
                        ids.add(dbId);
                        mDbNameIdsMap.put(dbName,ids);
                    }
                }
			}
		}

        public Pair<String,String>[] getPubMedRefs() {
            if (mPmidNotesMap == null || mPmidNotesMap.size() == 0) return null;
            Pair<String,String>[] results = new Pair[mPmidNotesMap.size()];
            int i = 0;
            for (Iterator it = mPmidNotesMap.entrySet().iterator(); it.hasNext(); i++) {
                Map.Entry<String,String> entry = (Map.Entry<String,String>)it.next();
                results[i] = new Pair<String,String>(entry.getKey(),entry.getValue());
            }
            return results;
        }

        public Pair<String,String[]>[] getDbLinks() {
            if (mDbNameIdsMap == null || mDbNameIdsMap.size() == 0) return null;
            Pair<String,String[]>[] results = new Pair[mDbNameIdsMap.size()];
            int i = 0;
            for (Iterator it = mDbNameIdsMap.entrySet().iterator(); it.hasNext(); i++) {
                Map.Entry<String,HashSet<String>> entry = (Map.Entry<String,HashSet<String>>)it.next();
                HashSet<String> idSet = entry.getValue();
                String ids[] = new String[idSet.size()];
                ids = idSet.toArray(ids);
                results[i] = new Pair<String,String[]>(entry.getKey(),ids);
            }
            return results;
        }
	}

	class DbTagHandler extends DelegateHandler {
		String mDbName;
		String mDbId;

		TextAccumulatorHandler mDbNameHandler = new TextAccumulatorHandler();
		TextAccumulatorHandler mDbIdHandler = new TextAccumulatorHandler();
		TextAccumulatorHandler mDbStrHandler = new TextAccumulatorHandler();

		DbTagHandler(DelegatingHandler parent) {
			super(parent);
			setDelegate(EntrezTags.DBTAG_DB_ELT,mDbNameHandler);
			setDelegate(EntrezTags.DBTAG_OBJECTID_ID_ELT,mDbIdHandler);
			setDelegate(EntrezTags.DBTAG_OBJECTID_STR_ELT,mDbStrHandler);
		}

		public void startDocument() {
			mDbNameHandler.reset();
			mDbIdHandler.reset();
			mDbStrHandler.reset();
			mDbName = null;
			mDbId = null;
		}

		public void finishDelegate(String qName, DefaultHandler handler) {
			if (qName.equals(EntrezTags.DBTAG_DB_ELT)) {
				mDbName = mDbNameHandler.getText();
			} else if (qName.equals(EntrezTags.DBTAG_OBJECTID_ID_ELT)) {
				mDbId = mDbIdHandler.getText();
			} else if (qName.equals(EntrezTags.DBTAG_OBJECTID_STR_ELT)) {
				mDbId = mDbStrHandler.getText();
			}
		}

		public String getDbName() { return mDbName; }

		public String getDbId() { return mDbId; }
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

	class AttributeValueHandler extends DefaultHandler {
		private String mValue;
		public void startElement(String namespaceURI, String localName,
								 String qName, Attributes atts)
			throws SAXException {
			mValue = atts.getValue("value");
		}
		public void reset() { mValue = null; }
		public String value() { return mValue; }
	}

}
