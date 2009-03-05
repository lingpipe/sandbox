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

import java.util.ArrayList;
import java.util.HashSet;

/**
 * An <code>EntrezGene</code> contains information from
 * one entry in the 
 * <A href="http://www.ncbi.nlm.nih.gov/sites/entrez?db=gene">
 * NCBI Entrez Gene database</A>.
 * Entrez Gene entries contain general information about 
 * the gene and its associated proteins including:
 * <ul>
 * <li> names, symbols, aliases, and text descriptions
 * <li> genome, genomic location
 * <li> bibliographic references, with annotations
 * <li> gene information, including GO terms
 * <li> protein information 
 * <li> sequence information 
 * <li> links to related databases, database ids
 * <li> entry status
 * </ul>
 * See the <A href="http://www.ncbi.nlm.nih.gov/entrez/query/static/help/genehelp.html">
 * Entrez Gene FAQ</A> for more details.
 *
 * <P>The status of an entry changes as
 * the database is updated.  The method {@link #isStatusLive()}
 * indicates whether or not this entry is in the current database.
 * Each database entry has a unique gene id assigned by NCBI.  
 * The method {@link #getGeneId()} returns the text value of
 * the <code>Gene-track_geneid</code> element.
 * The function {@link #getGeneRefMaploc()} returns the text value of
 * the <code>Gene-ref_maploc</code> element.
 *
 * <P>A gene may have many names.
 * The function {@link #getOfficialSymbol()} returns the official gene symbol,
 * and the function {@link #getOfficialFullName()} returns the official 
 * gene name.
 * The function {@link #getGeneRefName()} returns the LocusLink gene name.
 * The function {@link #getGeneRefDesc()} returns the long version of the name.
 * The function {@link #getGeneRefSyns()} returns the set of synonymous symbols.
 * If the protein associated with this gene in known, the entry contains
 * information about protein names as well as gene names and the methods
 * {@link #getProtRefNames()} and {@link #getProtRefDesc()} are used to
 * fetch them.
 *
 * <P>The entry contains links to PubMed articles, as well as a section which
 * lists links to other Entrez databases.
 * <P>The function {@link #getPubMedRefs()} returns a list of 
 * labeled lists of PubMed references.
 * The label may be empty, as is always
 * the case for the list of curated PubMed articles created by NCBI reviewers.
 * <A href="http://www.ncbi.nlm.nih.gov/projects/GeneRIF/GeneRIFhelp.html">GeneRIF</A>
 * entries associate a short statement of the gene function or functions
 * with a published paper.  GO terms are also used as labels for a list
 * of PubMed references.
 * <P>The element <code>Entrezgene_unique-keys</element> contains links to other databases.
 * The function {@link #getUniqueKeys()} returns this information.
 *
 * @author Mitzi Morris
 * @version 1.1
 * @since   LingMed1.0
 */

public class EntrezGene {

    private final String mGeneId;
    private final String mGeneTrackStatus;
    private final String mEntrezgeneType;

    private final Species mSpecies;
    private final String mGeneRefMaploc;
	private final GenomeLocation mGenomeLocus;

    private final String mOfficialSymbol;
    private final String mOfficialFullName;
    private final String mGeneSummary;

    private final String mGeneRefName;
    private final String[] mGeneRefSyns;
    private final String mGeneRefDesc;

    private final String[] mProtRefNames;
    private final String mProtRefDesc;

    private final Pair<String,String>[] mPubMedRefs;
    private final Pair<String,String[]>[] mDbLinks;
    private String mXmlString;

    public EntrezGene(String entrezStatus,
                      String geneId,
                      String entrezType,
					  Species species,
                      String geneRefMaploc,
					  GenomeLocation geneLocus,
                      String officialSymbol,
                      String officialFullName,
                      String geneSummary,
                      String geneRefName,
                      String[] geneRefSyns,
                      String geneRefDesc,
                      String[] protRefNames,
                      String protRefDesc,
                      Pair<String,String>[] pubMedRefs,
                      Pair<String,String[]>[] dbLinks) {
		mGeneTrackStatus = entrezStatus;
        mGeneId = geneId;
        mEntrezgeneType = entrezType;
		mSpecies = species;
        mGeneRefMaploc = geneRefMaploc;
		mGenomeLocus = geneLocus;
        mOfficialSymbol = officialSymbol;
        mOfficialFullName = officialFullName;
        mGeneSummary = geneSummary;
        mGeneRefName = geneRefName;
        mGeneRefSyns = geneRefSyns;
        mGeneRefDesc = geneRefDesc;
        mProtRefNames = protRefNames;
        mProtRefDesc = protRefDesc;
        mPubMedRefs = pubMedRefs;
        mDbLinks = dbLinks;
    }

    String getGeneTrackStatus() { return mGeneTrackStatus; }

    String getEntrezgeneType() { return mEntrezgeneType; }


    /**
     * Returns the Entrez Gene gene id.
     * @return The text value of the <code>Gene-track_geneid</code> element.
     */
    public String getGeneId() { return mGeneId; }

    /**
     * Returns the text value of the <code>Gene-ref_desc</code> element.
     */
    public String getGeneRefDesc() { return mGeneRefDesc; }


    public String getGeneTaxonId() { return mGeneId + "_" + mSpecies.taxonId(); }

    /**
     * Returns the text value of the <code>Org-ref_taxname</code> element.
     */
    public String getSpeciesTaxName() { return mSpecies.taxName(); }

    /**
     * Returns the text value of the <code>Org-ref_common</code> element.
     */
    public String getSpeciesCommonName() { return mSpecies.commonName(); }

    /**
     * Returns the taxon id value embedded in the <code>Org-ref_db</code> element.
     */
    public String getSpeciesTaxonId() { return mSpecies.taxonId(); }

    /**
     * Returns the LocusLink gene symbol, if found, else <code>null</code>.
     * @return The text value of the <code>Gene-ref_locus</code> element.
     */
    public String getGeneRefName() { return mGeneRefName; }

    /**
     * Returns cytoband location, if found, else <code>null</code>.
     * @return The text value of the <code>Gene-ref_maploc</code> element.
     */
    public String getGeneRefMaploc() { return mGeneRefMaploc; }

    public GenomeLocation getGenomeLocus() { return mGenomeLocus; }

    /**
     * Returns a list of alternate gene symbols, if found, else <code>null</code>.
     * @return The list of text values of all <code>Gene-ref_syn_E</code> elements.
     */
    public String[] getGeneRefSyns() { return mGeneRefSyns; }

    /**
     * Returns gene summary, if found, else <code>null</code>.
     * @return The text value of the <code>Entrezgene_summary</code> element.
     */
    public String getGeneSummary() { return mGeneSummary; }

    /**
     * Returns official full name, if found, else <code>null</code>.
     * This information is found nested inside the elements
     *<code>Entrezgene_properties</code><code>Gene-commentary</code>,
     * where the <code>Gene-commentary_label</code> element has the value
     * <code>Official Full Name</code>, and the value is the text of
     * the <code>Gene-commentary_text</code> element.
     */
    public String getOfficialFullName() { return mOfficialFullName; }

    /**
     * Returns official symbol, if found, else <code>null</code>.
     * This information is found nested inside the elements
     *<code>Entrezgene_properties</code><code>Gene-commentary</code>,
     * where the <code>Gene-commentary_label</code> element has the value
     * <code>Official Symbol</code>, and the value is the text of
     * the <code>Gene-commentary_text</code> element.
     */
    public String getOfficialSymbol() { return mOfficialSymbol; }

    /**
     * Returns the preferred protein name, if found, else <code>null</code>.
     * @return The text value of the <code>Prot-ref_desc</code> element.
     */
    public String getProtRefDesc() { return mProtRefDesc; }

    /**
     * Returns a list of protein names, if found, else <code>null</code>.
     * @return The list of text values of all <code>Prot-ref_name_E</code> elements.
     */
    public String[] getProtRefNames() { return mProtRefNames; }

    /**
	 * Returns the list of all (labeled) lists of PubMed article references.
     */
    public Pair<String,String>[] getPubMedRefs() { return mPubMedRefs; }

    /**
	 * Returns the list of all lists of external database ids, 
	 * labelled by database name.
     */
    public Pair<String,String[]>[] getDbLinks() { return mDbLinks; }

    /**
     * Returns the count of unique gene and protein names
     * computed from official symbol, official full name, 
     * gene symbol, gene synonyms, protein names, 
     * preferred protein name.
     */
    public int countUniqueAliases() {
        HashSet<String> names = new HashSet<String>();
        if (mOfficialSymbol != null) names.add(mOfficialSymbol);
        if (mOfficialFullName != null) names.add(mOfficialFullName);
        if (mGeneRefDesc != null)  names.add(mGeneRefDesc);
        if (mGeneRefSyns != null) {
            for (int i=0; i<mGeneRefSyns.length; i++) {
                names.add(mGeneRefSyns[i]);
            }
        }
        if (mProtRefDesc != null) {
            names.add(mProtRefDesc);
        }
        if (mProtRefNames != null) {
            for (int i=0; i<mProtRefNames.length; i++) {
                names.add(mProtRefNames[i]);
            }
        }
        return names.size();
    }

    /**
     * Returns the set of unique gene and protein names
     * computed from official symbol, official full name, 
     * gene symbol, gene synonyms, protein names, 
     * preferred protein name.
     */
    public String[] getUniqueAliases() {
        HashSet<String> names = new HashSet<String>();
        if (mOfficialSymbol != null) names.add(mOfficialSymbol);
        if (mOfficialFullName != null) names.add(mOfficialFullName);
        if (mGeneRefDesc != null)  names.add(mGeneRefDesc);
        if (mGeneRefSyns != null) {
            for (int i=0; i<mGeneRefSyns.length; i++) {
                names.add(mGeneRefSyns[i]);
            }
        }
        if (mProtRefDesc != null) {
            names.add(mProtRefDesc);
        }
        if (mProtRefNames != null) {
            for (int i=0; i<mProtRefNames.length; i++) {
                names.add(mProtRefNames[i]);
            }
        }
        String[] result = new String[names.size()];
        return names.toArray(result);
    }

    /**
	 * Returns array of MIM ids, or empty array if entry not linked to MIM.
     */
    public String[] getMimIds() {
        if (mDbLinks.length > 0) {
            for (Pair<String,String[]> dbLink : mDbLinks) {
                if (dbLink.a().equals("MIM")) {
                    return dbLink.b();
                }
            }
        }
        return new String[0];
    }
    

    /**
	 * Returns the set of ids linking to other databases.
     */
    public String[] getLinkIds() {
        if (mDbLinks == null || mDbLinks.length == 0) return new String[0];
        HashSet<String> idSet = new HashSet<String>();
        for (Pair<String,String[]> link : mDbLinks) {
            String[] ids = link.b();
            for (String id : ids) {
                idSet.add(id);
            }
        }
        String[] result = new String[idSet.size()];
        return idSet.toArray(result);
    }

    /**
	 * Returns the ct of ids linking to other databases.
     */
    public int ctLinkIds() {
        String[] ids = getLinkIds();
        return ids.length;
    }

    /**
	 * Returns the set of unique PubMed ids from the list of PubMedRefs.
     */
    public String[] getUniquePubMedRefs() {
        if (mPubMedRefs == null || mPubMedRefs.length == 0) return new String[0];
        HashSet<String> refs = new HashSet<String>();
        for (Pair<String,String> ref : mPubMedRefs) {
            refs.add(ref.a());
        }
        String[] result = new String[refs.size()];
        return refs.toArray(result);
    }

    /**
	 * Returns the count of unique PubMed ids from the list of PubMedRefs.
     */
    public int countUniquePubMedRefs() {
        String[] refs = getUniquePubMedRefs();
        return refs.length;
    }

    /**
     * Returns true if entry contains any text elements
     * which are stored in a <code>EntrezGene</code>.
     */
    public boolean hasTextAnnotations() {
        if (mGeneRefName != null
            || mGeneRefDesc != null
            || mGeneSummary != null
            || mOfficialFullName != null
            || mOfficialSymbol != null
            || mGeneRefSyns != null
            || mProtRefDesc != null
            || mProtRefNames != null)
            return true;
        return false;
    }

    /**
     * Returns true if the <code>Gene-track_status</code> element
     * attribute "value" is "live"
     */
    public boolean isStatusLive() {
        if (mGeneTrackStatus == null) return false;
        if (mGeneTrackStatus.equals("live")) return true;
        return false;
    }

    /**
     * Returns true if the <code>Entrezgene_type</code> element
     * attribute "value" is not "other" or "unknown"
     */
    public boolean isTypeGene() {
        if (mEntrezgeneType == null 
            || mEntrezgeneType.equals("other")
            || mEntrezgeneType.equals("unknown")) return false;
        return true;
    }

    /**
     * Returns <code>EntrezGene</code> contents, formatted with tabs and linefeeds.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("\nEntrezgene_GeneId: " + getGeneId());
        if (getGeneTrackStatus() != null) 
            result.append("\tStatus: " + getGeneTrackStatus());
        if (getEntrezgeneType() != null) 
            result.append("\tType: " + getEntrezgeneType());
        result.append("\n");
        if (getOfficialSymbol() != null) 
            result.append("\tOfficial_Symbol: " + getOfficialSymbol());
        if (getOfficialFullName() != null) 
            result.append("\tOfficial_FullName: " + getOfficialFullName());
        if (getSpeciesTaxName() != null) 
            result.append("\n\tOrg-ref_taxname: " + getSpeciesTaxName());
        if (getSpeciesTaxonId() != null) 
            result.append("\tOrg-ref_db taxon id: " + getSpeciesTaxonId());
        if (getSpeciesCommonName() != null) 
            result.append("\tOrg-ref_common: " + getSpeciesCommonName());
        if (getGeneRefMaploc() != null) 
            result.append("\n\tGene-ref_maploc: " + getGeneRefMaploc());
        if (getGenomeLocus() != null) 
            result.append("\n\tEntrezgene_locus: " + getGenomeLocus().toString());
        if (getGeneRefName() != null) 
            result.append("\n\tGene-ref_locus (LocusLink name): "+getGeneRefName());
        if (getGeneRefSyns() != null) {
            String[] syns = getGeneRefSyns();
            result.append("\n\tGene-ref_syns: ");
            for (int i=0;i<syns.length;i++) {
                result.append(syns[i]+", ");
            }
        }
        if (getGeneRefDesc() != null) 
            result.append("\n\tGene-ref_desc: "+getGeneRefDesc());
        if (getProtRefNames() != null) {
            String[] names = getProtRefNames();
            result.append("\n\tProt-ref_names: ");
            for (int i=0;i<names.length;i++) {
                result.append(names[i]+", ");
            }
        }
        if (getProtRefDesc() != null) 
            result.append("\n\tProt-ref_desc (Preferred name): "+getProtRefDesc());
        if (getGeneSummary() != null) 
            result.append("\n\tEntrezgene_summary: "+getGeneSummary());

        if (getPubMedRefs() != null) {
            result.append("\n\tPubmed article refs: "+countUniquePubMedRefs());
            for (Pair<String,String> ref : mPubMedRefs) {
                result.append("\n\t" + ref.a());
                if (ref.b() != null) {
                    result.append(": " + ref.b());
                }
            }
        }
        if (getDbLinks() != null) {
            result.append("\n\tDatabase ids: "+mDbLinks.length);
            for (Pair<String,String[]> dbLink : mDbLinks) {
                result.append("\n\t" + dbLink.a() + ": ");
                for (String dbid : dbLink.b()) {
                    result.append(dbid + ", ");
                }
            }
        }
        return result.toString();
    }

    /**
     * Returns comma-separated concatentation of all 
     * natural language texts from an <code>EntrezGene</code>.
     * Includes GenRIF labels, omits PubMed and database ids.
     */
    public String getTextData() {
        StringBuffer result = new StringBuffer();
        if (getOfficialSymbol() != null) 
            result.append(getOfficialSymbol()+", ");
        if (getOfficialFullName() != null) 
            result.append(getOfficialFullName()+", ");
        if (getGeneRefName() != null) 
            result.append(getGeneRefName()+", ");
        if (getGeneRefSyns() != null) {
            String[] syns = getGeneRefSyns();
            for (int i=0;i<syns.length;i++) {
                result.append(syns[i]+", ");
            }
        }
        if (getGeneRefDesc() != null) 
            result.append(getGeneRefDesc()+", ");
        if (getProtRefNames() != null) {
            String[] names = getProtRefNames();
            for (int i=0;i<names.length;i++) {
                result.append(names[i]+", ");
            }
        }
        if (getProtRefDesc() != null) 
            result.append(getProtRefDesc()+", ");
        if (getGeneSummary() != null) 
            result.append(getGeneSummary()+", ");

        if (getPubMedRefs() != null) {
            String[] geneRifs = getGeneRifLabels();
            for (String geneRif : geneRifs) {
                    result.append(geneRif + ", ");
            }
        }
        return result.toString();
    }
    /**
     * Returns array of all GeneRIF labels.
     */
    public String[] getGeneRifLabels() {
        HashSet<String> texts = new HashSet<String>();
        if (getPubMedRefs() != null) {
            for (Pair<String,String> ref : mPubMedRefs) {
                if (ref.b() != null)
                    texts.add(ref.b());
            }
        }
        String[] result = new String[texts.size()];
        return texts.toArray(result);
    }

    void setXmlString(String xmlString) {
        mXmlString = xmlString;
    }
    /**
     * Returns the XML underlying this citation as a string.  Note
     * that there will be no XML declaration in this string, nor
     * will there be a DTD reference.  All entities and defaults
     * provided by the DTD will be expanded into the string.
     *
     * @return The XML underlying this citation as a string.
     */
    public String xmlString() {
        return mXmlString;
    }
}
