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


    static final String GENE_COMMENTARY_ELT = "Gene-commentary";
    static final String GENE_COMMENTARY_LABEL_ELT = "Gene-commentary_label";
    static final String GENE_COMMENTARY_TEXT_ELT = "Gene-commentary_text";
    static final String GENE_COMMENTARY_REFS_ELT = "Gene-commentary_refs";
    static final String PUBMEDID_ELT = "PubMedId";
    static final String OFFICIAL_SYMBOL_TEXT = "Official Symbol";
    static final String OFFICIAL_FULL_NAME = "Official Full Name";

    static final String ENTREZGENE_UNIQUE_KEYS_ELT = "Entrezgene_unique-keys";

    static final String DBTAG_ELT = "Dbtag";
    static final String DBTAG_DB_ELT = "Dbtag_db";
    static final String DBTAG_TAG_ELT = "Dbtag_tag";
    static final String DBTAG_OBJECTID_ID_ELT = "Object-id_id";

    static final String MIM = "MIM";
    static final String LOCUSLINK = "LocusID";
    static final String HGNC = "HGNC";

    static final String TAXON = "taxon";

    EntrezTags() {}
}
