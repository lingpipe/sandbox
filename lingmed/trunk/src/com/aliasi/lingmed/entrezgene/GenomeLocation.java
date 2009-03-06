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
    private final String mAssembly;
    private final String mStrand;
    private final int mOffsetLower;
    private final int mOffsetUpper;


    String assembly() { return mAssembly; }
    String strand() { return mStrand; }
    int lower() { return mOffsetLower; }
    int upper() { return mOffsetUpper; }

    public String toString() {
        return "chr (assembly.version): " + mAssembly
            + " strand: " + mStrand + ": " + mOffsetLower + "-" + mOffsetUpper;
    }

    public GenomeLocation (String assembly, String strand, int lower, int upper) {
        mAssembly = assembly;
        mStrand = strand;
        mOffsetLower = lower;
        mOffsetUpper = upper;
    }

	public boolean overlaps(GenomeLocation o) {
		return ((mOffsetLower < o.lower() && mOffsetUpper > o.upper())
				|| (mOffsetLower > o.lower() && mOffsetUpper < o.upper())
				|| (mOffsetLower < o.lower() && mOffsetUpper > o.lower())
				|| (mOffsetUpper < o.upper() && mOffsetUpper > o.upper()));
	}

}
