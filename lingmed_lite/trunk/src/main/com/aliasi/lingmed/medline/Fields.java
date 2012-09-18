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

package com.aliasi.lingmed.medline;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Standard field names for Lucene indexes over MEDLINE.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.4
 */
public class Fields {

    public static final String ID_FIELD = "ID";
    public static final String DEFAULT_FIELD = "ID";
    public static final String XML_FIELD = "rawXML";
    public static final String RAW_TEXT_FIELD = "rawTEXT";

    public static final String MEDLINE_DIST_FIELD = "MEDLINE_DIST";
    public static final String MEDLINE_DIST_VALUE = "medline";
    public static final String MEDLINE_FILE_FIELD = "FILENAME";

    public static final String ABSTRACT_FIELD = "abstract";
    public static final String AFFILIATION_FIELD = "affiliation";
    public static final String AUTHOR_FIELD ="author";
    public static final String AUTHOR_FORE_FIELD ="authorFore";
    public static final String AUTHOR_MIDDLE_FIELD ="authorMiddle";
    public static final String AUTHOR_LAST_FIELD ="authorLast";
    public static final String AUTHOR_SUFFIX_FIELD ="authorSuffix";

    public static final String CHEMICAL_NAME_FIELD = "chemName";
    public static final String CHEMICAL_REGISTRY_FIELD = "chemRegistry";

    public static final String DATA_BANK_FIELD = "dataBank";

    public static final String DATE_YEAR_FIELD = "year";
    public static final String DATE_SEASON_FIELD = "season";
    public static final String DATE_MONTH_FIELD = "month";
    public static final String DATE_DAY_FIELD = "day";

    public static final String GENE_SYMBOL_FIELD = "gene";

    public static final String JOURNAL_FIELD = "journal";
    public static final String JOURNAL_ISO_ABBREVIATION_FIELD = "journalIso";
    public static final String JOURNAL_ISSN_FIELD = "journalIssn";
    public static final String JOURNAL_ISSUE_FIELD = "journalIssue";
    public static final String JOURNAL_TITLE_FIELD = "journalTitle";
    public static final String JOURNAL_VOLUME_FIELD = "journalVolume";

    public static final String KEYWORD_FIELD = "keyword";

    public static final String LANGUAGE_FIELD = "language";

    public static final String MESH_MAJOR_FIELD = "meshMaj";
    public static final String MESH_MINOR_FIELD = "meshMin";

    public static final String PUBLICATION_TYPE_FIELD = "publicationType";

    public static final String TITLE_FIELD = "title";

    public static final String EXACT_FIELD_SUFFIX = "X";
    public static final String NGRAM_FIELD_SUFFIX = "N";

    static final String[] TEXT_FIELDS = new String[] {
        ABSTRACT_FIELD,
        AFFILIATION_FIELD,
        AUTHOR_FIELD,
        AUTHOR_FORE_FIELD,
        AUTHOR_LAST_FIELD,
        AUTHOR_MIDDLE_FIELD,
        AUTHOR_SUFFIX_FIELD,
        CHEMICAL_NAME_FIELD,
        CHEMICAL_REGISTRY_FIELD,
        DATA_BANK_FIELD,
        GENE_SYMBOL_FIELD,
        JOURNAL_FIELD,
        JOURNAL_ISO_ABBREVIATION_FIELD,
        JOURNAL_ISSUE_FIELD,
        JOURNAL_TITLE_FIELD,
        KEYWORD_FIELD,
        MESH_MAJOR_FIELD,
        MESH_MINOR_FIELD,
        PUBLICATION_TYPE_FIELD,
        TITLE_FIELD,
    };

    static final String[] SIMPLE_FIELDS = new String[] {
        LANGUAGE_FIELD
    };

    static final String[] KEYWORD_FIELDS = new String[] {
        JOURNAL_VOLUME_FIELD,
        JOURNAL_ISSN_FIELD,
        DATE_DAY_FIELD,
        DATE_MONTH_FIELD,
        DATE_SEASON_FIELD,
        DATE_YEAR_FIELD,
    };

    static final Set<String> TEXT_FIELD_SET = new HashSet<String>(Arrays.<String>asList(TEXT_FIELDS));
    static final Set<String> KEYWORD_FIELD_SET = new HashSet<String>(Arrays.<String>asList(KEYWORD_FIELDS));
    static final Set<String> SIMPLE_FIELD_SET = new HashSet<String>(Arrays.<String>asList(SIMPLE_FIELDS));

    private Fields() {}

}
