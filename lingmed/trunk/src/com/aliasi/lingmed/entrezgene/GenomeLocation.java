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
 * <code>Species</code> information consists of taxonomic name, id, and common name.
 * Object instantiated via static factory method <code>valueOf</code>.
 * 
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.3
 */

public class GenomeLocation {
    private final String mAccession;
    private final String mStrand;
    private final int mLowerOffset;
    private final int mUpperOffset;


    String accession() { return mAccession; }
    String strand() { return mStrand; }
    int lower() { return mLowerOffset; }
    int upper() { return mUpperOffset; }

    public String toString() {
        return "chr (assembly.version): " + mAccession
            + " strand: " + mStrand + ": " + mLowerOffset + "-" + mUpperOffset;
    }

    public GenomeLocation (String accession, String strand, int lower, int upper) {
        mAccession = accession;
        mStrand = strand;
        mLowerOffset = lower;
        mUpperOffset = upper;
    }
}
