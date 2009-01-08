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

import com.aliasi.xml.DelegateHandler;
import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import com.aliasi.util.Strings;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

//handler for element <HG-Entry>
//single id:  <HG-Entry_hg-id>74384</HG-Entry_hg-id>
//one or more genes:  <HG-Entry_genes>
//top-level element for each gene: <HG-Gene>
class HomologeneHandler extends DelegatingHandler {
    String curGroupId;
    Set<HomologeneGene> curGeneSet = new HashSet<HomologeneGene>();

    //   <HG-Entry_hg-id>74384</HG-Entry_hg-id>
    TextAccumulatorHandler mHgEntryIdHandler 
	= new TextAccumulatorHandler();

    GeneHandler mGeneHandler;

    HomologeneHandler() {
	mGeneHandler = new GeneHandler(this);
	setDelegate(HomologeneTags.HG_ENTRY_ID_ELT,mHgEntryIdHandler);
	setDelegate(HomologeneTags.HG_GENE_ELT,mGeneHandler);
    }

    public void finishDelegate(String qName, DefaultHandler handler) {
	if (qName.equals(HomologeneTags.HG_ENTRY_ID_ELT)) {
	    curGroupId =  mHgEntryIdHandler.getText();
	} else if (qName.equals(HomologeneTags.HG_GENE_ELT)) {
	    curGeneSet.add(mGeneHandler.getGene());
	}
    }

    public void startDocument() throws SAXException {//reset accum structures on new gene
	curGroupId = "";
	curGeneSet.clear();
	mHgEntryIdHandler.reset();
	super.startDocument();
    }

    public HomologeneGroup getGeneGroup() { 
	return new HomologeneGroup(curGroupId,curGeneSet.toArray(new HomologeneGene[curGeneSet.size()]));
    }
}

class GeneHandler extends DelegateHandler {
    String curId;
    String curTaxId;
    String curSymbol;
    String curTitle;
    String[] curAliases;
    TextAccumulatorHandler mIdHandler = new TextAccumulatorHandler();
    TextAccumulatorHandler mSymbolHandler = new TextAccumulatorHandler();
    TextAccumulatorHandler mTitleHandler = new TextAccumulatorHandler();
    TextAccumulatorHandler mTaxIdHandler = new TextAccumulatorHandler();
    GeneAliasesHandler mAliasesHandler;

    GeneHandler(DelegatingHandler parent) {
	super(parent);
	mAliasesHandler = new GeneAliasesHandler(parent);
	setDelegate(HomologeneTags.HG_GENE_GENEID_ELT,mIdHandler);
	setDelegate(HomologeneTags.HG_GENE_SYMBOL_ELT,mSymbolHandler);
	setDelegate(HomologeneTags.HG_GENE_TITLE_ELT,mTitleHandler);
	setDelegate(HomologeneTags.HG_GENE_TAXID_ELT,mTaxIdHandler);
	setDelegate(HomologeneTags.HG_GENE_ALIASES_ELT,mAliasesHandler);
    }

    public void startDocument() {
	mIdHandler.reset();
	mSymbolHandler.reset();
	mTitleHandler.reset();
	mTaxIdHandler.reset();
	curId = "";
	curTaxId = "";
	curSymbol = "";
	curTitle = "";
	curAliases = new String[0];
    }

    public void finishDelegate(String qName, DefaultHandler handler) {
	if (qName.equals(HomologeneTags.HG_GENE_GENEID_ELT)) {
	    curId = mIdHandler.getText();
	} else if (qName.equals(HomologeneTags.HG_GENE_SYMBOL_ELT)) {
	    curSymbol = mSymbolHandler.getText();
	} else if (qName.equals(HomologeneTags.HG_GENE_TITLE_ELT)) {
	    curTitle = mTitleHandler.getText();
	} else if (qName.equals(HomologeneTags.HG_GENE_TAXID_ELT)) {
	    curTaxId = mTaxIdHandler.getText();
	} else if (qName.equals(HomologeneTags.HG_GENE_ALIASES_ELT)) {
	    curAliases = mAliasesHandler.getAliases();
	}
    }
    public HomologeneGene getGene() { 
	return new HomologeneGene(curId,
				  curTaxId,
				  curSymbol,
				  curTitle,
				  curAliases);
    }
}

class GeneAliasesHandler extends DelegateHandler {
    Set<String> mAliasSet = new HashSet<String>();
    TextAccumulatorHandler mAliasHandler = new TextAccumulatorHandler();

    GeneAliasesHandler(DelegatingHandler parent) {
	super(parent);
	setDelegate(HomologeneTags.HG_GENE_ALIASES_E_ELT,mAliasHandler);
    }

    public void startDocument() {
	mAliasSet.clear();
	mAliasHandler.reset();
    }

    public void finishDelegate(String qName, DefaultHandler handler) {
	if (qName.equals(HomologeneTags.HG_GENE_ALIASES_E_ELT)) {
	    String alias = mAliasHandler.getText();
	    mAliasSet.add(mAliasHandler.getText());
	}
    }

    public String[] getAliases() {
	String[] aliases = new String[mAliasSet.size()];
	return mAliasSet.toArray(aliases);
    }


}

