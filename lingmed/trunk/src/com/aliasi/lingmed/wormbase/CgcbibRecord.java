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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A <code>CgcbibRecord</code> contains information from
 * one entry from the CGC Bibliography, available from:
 * http://elegans.swmed.edu/wli/cgcbib - 
 * last updated Nov 7, 2005
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.3
 */

public class CgcbibRecord {

    private String mAbstract;
    private String mCitation;
    private String mKey;
    private String mPubmedId;
    private String mTitle;
    private String mType;

    private LinkedHashSet<String> mAuthorSet;
    private LinkedHashSet<String> mGeneSet;

    public CgcbibRecord(){
        mAuthorSet = new LinkedHashSet<String>();
        mGeneSet = new LinkedHashSet<String>();
    }


    public String getAbstract() { return mAbstract; }
    public void setAbstract(String abstrct) { mAbstract = abstrct; }

    public String getCitation() { return mCitation; }
    public void setCitation(String citation) { mCitation = citation; }

    public String getKey() { return mKey; }
    public void setKey(String key) { mKey = key; }

    public String getPubmedId() { return mPubmedId; }
    public void setPubmedId(String pubmedId) { mPubmedId = pubmedId; }

    public String getTitle() { return mTitle; }
    public void setTitle(String title) { mTitle = title; }

    public String getType() { return mType; }
    public void setType(String type) { mType = type; }

    public String[] getAuthors() { 
        String[] result = new String[mAuthorSet.size()];
        result = mAuthorSet.toArray(result);
        return result; 
    }
    public void addAuthor(String author) { mAuthorSet.add(author); }

    public String[] getGenes() { 
        String[] result = new String[mGeneSet.size()];
        result = mGeneSet.toArray(result);
        return result; 
    }
    public void addGene(String gene) { mGeneSet.add(gene); }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(mTitle + "\n" + mAbstract );
        return result.toString();
    }


    void processField(CgcbibField field) {
        if (field.label() == null || field.text() == null) {
            System.err.println("missing field data");
            return;
        }
    }    
}
