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

package com.aliasi.lingmed.homologene;

import com.aliasi.lingmed.entrezgene.EntrezGene;

import java.util.Set;

/**
 * A <code>HomologeneGene</code> contains information from
 * one gene in a <code>HomologeneGroup</code>.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class HomologeneGene {
    private final String mId;
    private final String mTaxonId;
    private final String mSymbol;
    private final String mTitle;
    private final String[] mAliases;

    public HomologeneGene(String id,
			  String taxonId,
			  String symbol,
			  String title,
			  String[] aliases){
	mId = id;
	mTaxonId = taxonId;
	mSymbol = symbol;
	mTitle = title;
	mAliases = aliases;
    }

    public String getId() { return mId; }
    public String getTaxonId() { return mTaxonId; }
    public String getSymbol() { return mSymbol; }
    public String getTitle() { return mTitle; }
    public String[] getAliases() { return mAliases; }

    public String getGeneTaxonId() { return mId + "_" + mTaxonId; }

}
