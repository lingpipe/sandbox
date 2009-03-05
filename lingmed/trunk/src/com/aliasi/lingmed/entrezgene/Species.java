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

import java.util.HashMap;

/**
 * <code>Species</code> information consists of taxonomic name, id, and common name.
 * Object instantiated via static factory method <code>valueOf</code>.
 * 
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.3
 */

public class Species {
    private final String mCommonName;
    private final String mTaxName;
    private final String mTaxonId;

    private static final HashMap<String,Species> cache = new HashMap<String,Species>();

    String commonName() { return mCommonName; }
    String taxName() { return mTaxName; }
    String taxonId() { return mTaxonId; }

    public String toString() {
        return mCommonName + ", " + mTaxName + ", " + mTaxonId;
    }

    private Species(String commonName, String taxName, String taxonId) {
        mCommonName = commonName;
        mTaxName = taxName;
        mTaxonId = taxonId;
    }

    public static Species valueOf(String taxonId,
                          String taxName,
                          String commonName) {
        if (cache.get(taxonId) != null) {
            return cache.get(taxonId);
        }
        Species s = new Species(commonName,taxName,taxonId);
        cache.put(taxonId,s);
        return s;
    }
}
