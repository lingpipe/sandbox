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

/**
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

class EntrezTags {
    static final String ENTREZGENE_SET_ELT = "Entrezgene-Set";
    static final String ENTREZGENE_ELT = "Entrezgene";
    static final String ENTREZGENE_TYPE_ELT = "Entrezgene_type";
    static final String GENE_TRACK_GENEID_ELT = "Gene-track_geneid";
    static final String GENE_TRACK_STATUS_ELT = "Gene-track_status";
    static final String GENE_TRACK_STATUS_LIVE = "0";

    static final String ENTREZGENE_SOURCE_ELT = "Entrezgene_source";
    static final String ORG_REF_TAXNAME_ELT = "Org-ref_taxname";
    static final String ORG_REF_COMMON_ELT = "Org-ref_common";
    static final String ORG_REF_DB_ELT = "Org-ref_db";

    static final String ENTREZGENE_GENE_ELT = "Entrezgene_gene";
    static final String GENE_REF_LOCUS_ELT = "Gene-ref_locus";
    static final String GENE_REF_MAPLOC_ELT = "Gene-ref_maploc";
    static final String GENE_REF_DESC_ELT = "Gene-ref_desc";
    static final String GENE_REF_SYN_ELT = "Gene-ref_syn";
    static final String GENE_REF_SYN_E_ELT = "Gene-ref_syn_E";


    static final String ENTREZGENE_PROT_ELT = "Entrezgene_prot";
    static final String PROT_REF_NAME_ELT = "Prot-ref_name";
    static final String PROT_REF_NAME_E_ELT = "Prot-ref_name_E";
    static final String PROT_REF_DESC_ELT = "Prot-ref_desc";

    static final String ENTREZGENE_SUMMARY_ELT = "Entrezgene_summary";

    static final String ENTREZGENE_PROPERTIES_ELT = "Entrezgene_properties";
    static final String ENTREZGENE_COMMENTS_ELT = "Entrezgene_comments";

    static final String ENTREZGENE_LOCUS_ELT = "Entrezgene_locus";

    static final String GENE_COMMENTARY_ELT = "Gene-commentary";
    static final String GENE_COMMENTARY_TYPE_ELT = "Gene-commentary_type";
    static final String GENE_COMMENTARY_HEADING_ELT = "Gene-commentary_heading";
    static final String GENE_COMMENTARY_LABEL_ELT = "Gene-commentary_label";
    static final String GENE_COMMENTARY_TEXT_ELT = "Gene-commentary_text";
    static final String GENE_COMMENTARY_REFS_ELT = "Gene-commentary_refs";
    static final String GENE_COMMENTARY_ACCESSION_ELT = "Gene-commentary_accession";
    static final String GENE_COMMENTARY_VERSION_ELT = "Gene-commentary_version";
    static final String GENE_COMMENTARY_COMMENT_ELT = "Gene-commentary_comment";
    static final String GENE_COMMENTARY_SOURCE_ELT = "Gene-commentary_source";

    static final String GENE_COMMENTARY_SEQS_ELT = "Gene-commentary_seqs";
    static final String SEQ_INTERVAL_ELT = "Seq-interval";
    static final String SEQ_INTERVAL_FROM_ELT = "Seq-interval_from";
    static final String SEQ_INTERVAL_TO_ELT = "Seq-interval_to";
    static final String SEQ_INTERVAL_STRAND_ELT = "Seq-interval_strand";
    static final String NA_STRAND_ELT = "Na-strand";
    static final String SEQ_ID_GI_ELT = "Seq-id_gi";


    static final String PUBMEDID_ELT = "PubMedId";
    static final String OFFICIAL_SYMBOL = "Official Symbol";
    static final String OFFICIAL_FULL_NAME = "Official Full Name";

    static final String ADDITIONAL_LINKS_TEXT = "Additional Links";

	static final String REFERENCE_HEADING_TEXT = "Reference assembly";

    static final String ENTREZGENE_UNIQUE_KEYS_ELT = "Entrezgene_unique-keys";

    static final String DBTAG_ELT = "Dbtag";
    static final String DBTAG_DB_ELT = "Dbtag_db";
    static final String DBTAG_TAG_ELT = "Dbtag_tag";
    static final String DBTAG_OBJECTID_ID_ELT = "Object-id_id";
    static final String DBTAG_OBJECTID_STR_ELT = "Object-id_str";


    static final String MIM = "MIM";
    static final String LOCUSLINK = "LocusID";
    static final String HGNC = "HGNC";

    static final String TAXON = "taxon";

    static final String GENOMIC = "genomic";

    static final String GENERIF = "generif";

    static final String PROPERTY = "property";

    EntrezTags() {}
}
