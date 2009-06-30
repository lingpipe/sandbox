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

package com.aliasi.lingmed.wormbase;

import java.util.HashSet;


/**
 * An <code>EndnoteRecord</code> contains information from
 * one entry from the wormbase literature database,  
 * which is a flat file exported from EndNote.
 * Each entry consists of a set of lines, separated by a blank line.
 * Each line has a label prefix consisting of % and a capital latter,
 * followed by blank space, then value.
 * The labels recognized are:
 * <ul>
 * <li>%0 - Reference type</li>
 * <li>%T - Title</li>
 * <li>%A - Author (multiple lines)</li>
 * <li>%D - year</li>
 * <li>%V - volume</li>
 * <li>%P - pages</li>
 * <li>%J - journal name</li>
 * <li>%M - Wormbase Paper ID (e.g. WBPaper00000003)</li>
 * <li>%X - Abstract</li>
 * </ul>
 *
 * The methods getPubmedId and setPubmedId are used to link
 * this record to a MEDLINE citation.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.3
 */

public class EndnoteRecord {


    private String mAbstract;
    private String[] mAuthors;
    private String mJournal;
    private String mPubmedId;
    private String mTitle;
    private String mType;
    private String mYear;
    private HashSet<String> mAuthorSet;

    public static final String LBL_TYPE = "%O";
    public static final String LBL_TITLE = "%T";
    public static final String LBL_AUTHOR = "%A";
    public static final String LBL_YEAR = "%Y";
    public static final String LBL_JOURNAL = "%J";
    public static final String LBL_ABSTRACT = "%X";

    public EndnoteRecord(){
        mAuthorSet = new HashSet<String>();
    }

    void processLine(String line) {
        String label = line.substring(0,2);
        String value = line.substring(4);
        if (LBL_TYPE.equals(label)) {
            setType(value);
        } else if (LBL_TITLE.equals(label)) {
            setTitle(value);
        } else if (LBL_AUTHOR.equals(label)) {
            addAuthor(value);
        } else if (LBL_YEAR.equals(label)) {
            setYear(value);
        } else if (LBL_JOURNAL.equals(label)) {
            setJournal(value);
        } else if (LBL_ABSTRACT.equals(label)) {
            setAbstract(value);
        }
    }

    public String getAbstract() { return mAbstract; }
    public void setAbstract(String abstrct) { mAbstract = abstrct; }

    public String getJournal() { return mJournal; }
    public void setJournal(String journal) { mJournal = journal; }

    public String getPubmedId() { return mPubmedId; }
    public void setPubmedId(String pubmedId) { mPubmedId = pubmedId; }

    public String getTitle() { return mTitle; }
    public void setTitle(String title) { mTitle = title; }

    public String getType() { return mType; }
    public void setType(String type) { mType = type; }

    public String getYear() { return mYear; }
    public void setYear(String year) { mYear = year; }

    public String[] getAuthors() { 
        String[] result = new String[mAuthorSet.size()];
        result = mAuthorSet.toArray(result);
        return result; 
    }
    public void addAuthor(String author) { mAuthorSet.add(author); }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(mTitle + "\n" + mAbstract );
        return result.toString();
    }

}
