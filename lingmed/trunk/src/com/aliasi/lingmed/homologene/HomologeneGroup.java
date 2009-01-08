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
import com.aliasi.util.Strings;

import java.util.Set;
import java.util.HashSet;

/**
 * A <code>HomologeneGroup</code> contains information from
 * one entry in the
 * <A href="http://www.ncbi.nlm.nih.gov/sites/entrez?db=homologene">
 * Homologene database</A>.

 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class HomologeneGroup {
    private final String mGroupId;
    private final HomologeneGene[] mGenes;
    private String mXmlString;

    public HomologeneGroup(String groupId,
			   HomologeneGene[] genes) {
	mGroupId = groupId;
	mGenes = genes;
    }

    public String getGroupId() { return mGroupId; }
    public HomologeneGene[] getGenes() { return mGenes; }

    public String[] getUniqueAliases() {
	Set<String> uniqueAliases = new HashSet<String>();
	for (int i=0; i< mGenes.length; i++) {
	    if (mGenes[i].getSymbol().length() > 0)
		uniqueAliases.add(mGenes[i].getSymbol());
	    if (mGenes[i].getTitle().length() > 0)
		uniqueAliases.add(mGenes[i].getTitle());
	    String[] aliases = mGenes[i].getAliases(); 
	    if (aliases.length > 0) {
		for (int j=0; j < aliases.length; j++) {
		    uniqueAliases.add(aliases[j]);
		}
	    }
	}
	return uniqueAliases.toArray(new String[uniqueAliases.size()]);
    }

    public String[] getGeneTaxonIds() {
	Set<String> ids = new HashSet<String>();
	for (int i=0; i< mGenes.length; i++) {
	    ids.add(mGenes[i].getGeneTaxonId());
	}
	return ids.toArray(new String[ids.size()]);
    }

    void setXmlString(String xml) { mXmlString = xml; }
    public String getXmlString() { return mXmlString; }

    public String toString() { 
	StringBuffer result = new StringBuffer();
	result.append("Homologene Id: "+mGroupId);
	result.append(" # genes: "+mGenes.length+"\n");
	for (int i=0; i< mGenes.length; i++) {
	    result.append("\tEntrezGeneId: "+mGenes[i].getId()
			  +"\tTaxon: "+mGenes[i].getTaxonId()
			  +"\tName: "+mGenes[i].getTitle()
			  +"\n");
	}
	String[] aliases = getUniqueAliases();
	for (int i=0; i < aliases.length; i++) {
	    result.append("\taliases: "+aliases[i]+"\n");
	}
	return result.toString();
    }
}
