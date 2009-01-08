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

package com.aliasi.lingmed.genelinkage;

import com.aliasi.chunk.Chunk;

import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.entrezgene.EntrezGene;
import com.aliasi.lingmed.entrezgene.EntrezGeneSearcher;
import com.aliasi.lingmed.medline.MedlineCodec;
import com.aliasi.lingmed.medline.MedlineSearcher;

import com.aliasi.medline.Abstract;
import com.aliasi.medline.Article;
import com.aliasi.medline.MedlineCitation;

import com.aliasi.util.NBestSet;
import com.aliasi.util.Pair;
import com.aliasi.util.Strings;

import java.sql.SQLException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * A <code>GeneLinkageSearcher</code> composes searchers
 * for Lucene indexes over  EntrezGene, Medline
 * and a MySQL database of gene article mentions.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class GeneLinkageSearcher {
    private final Logger mLogger
	= Logger.getLogger(GeneLinkageSearcher.class);

    private final EntrezGeneSearcher mEntrezGeneSearcher;
    private final MedlineSearcher mMedlineSearcher;
    private final GeneLinkageDao mGeneLinkageDao;

    public GeneLinkageSearcher(EntrezGeneSearcher entrezGeneSearcher,
			       MedlineSearcher medlineSearcher,
			       GeneLinkageDao geneLinkageDao) {
	mEntrezGeneSearcher = entrezGeneSearcher;
	mMedlineSearcher = medlineSearcher;
	mGeneLinkageDao = geneLinkageDao;
    }

    public EntrezGeneSearcher entrezGeneSearcher() { return mEntrezGeneSearcher; }
    public MedlineSearcher medlineSearcher() { return  mMedlineSearcher; }
    public GeneLinkageDao geneLinkageDao() { return  mGeneLinkageDao; }

    ArticleMention[] findTopMentions(String gene, int limit) 
	throws SQLException, DaoException {
	int geneId = 0;
	try {
	    geneId = Integer.parseInt(gene);
	} catch (NumberFormatException nfe) {
	    return new ArticleMention[0];
	}
	Comparator<ArticleMention> byScore = new ArticleMentionComparator();
	NBestSet<ArticleMention> tops = new NBestSet<ArticleMention>(limit,byScore);
	int limitx10 = limit*10;
	Map<String,Pair<Double,Set<Chunk>>> articleMentions =
	    mGeneLinkageDao.getNArticleMentionsForGeneId(geneId,limitx10);
	if (mLogger.isDebugEnabled())
	    mLogger.info("mentions for geneId "+geneId+ ": "+articleMentions.size());

	for (Iterator it=articleMentions.entrySet().iterator(); it.hasNext(); ) {
	    Entry<String,Pair<Double,Set<Chunk>>> entry = 
		(Entry<String,Pair<Double,Set<Chunk>>>)it.next();
	    String pubmedId = entry.getKey();
	    String pubmedText = getTitleAbstract(pubmedId);
	    Pair<Double,Set<Chunk>> geneMentions = entry.getValue();
	    Double totalScore = geneMentions.a();
	    Set<Chunk> chunkSet = geneMentions.b();
	    for (Chunk chunk : chunkSet) {
		totalScore = totalScore + chunk.score();
		break;
	    }
	    ArticleMention mention = 
		new ArticleMention(pubmedId,pubmedText,totalScore,chunkSet);
	    tops.add(mention);
	}
	ArticleMention[] result = new ArticleMention[tops.size()];
	return tops.toArray(result);
    }

    private String getTitleAbstract(String pubmedId) throws DaoException {
	MedlineCodec codec = new MedlineCodec();
	MedlineCitation citation = mMedlineSearcher.getById(pubmedId);
	return codec.titleAbstract(citation);
    }

    private final static String ENTREZ_PREFIX = 
	"http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=DetailsSearch&Term=";
    private final static String PUBMED_PREFIX = 
	"http://www.ncbi.nlm.nih.gov/sites/entrez?Db=pubmed&Cmd=DetailsSearch&Term=";
    private final static String POSTFIX = "%5Buid%5D";

    public String genHtml(String geneId, ArticleMention[] mentions) throws DaoException {
	EntrezGene entrezGene = mEntrezGeneSearcher.getById(geneId);
	String geneName = entrezGene.getOfficialFullName();
	StringBuffer result = new StringBuffer();
	result.append("<HTML><BODY>");
	result.append("<h2>");
	result.append("<h2><A href=\""
		      +ENTREZ_PREFIX
		      +geneId
		      +POSTFIX
		      +"\">Gene "
		      +geneId
		      +"</A>: "
		      +geneName
		      +"</h2>");
	NumberFormat formatter = new DecimalFormat("#.###");
	for (int i = mentions.length-1; i >= 0; i--) {
	    double geneScore = 0.0;
	    for (Chunk chunk : mentions[i].chunkSet()) {
		geneScore = chunk.score();
		break;
	    }
	    result.append("<h4>PubmedId: ");
	    result.append("<A href=\""
			  +PUBMED_PREFIX
			  +mentions[i].pubmedId()
			  +POSTFIX
			  +"\">"
			  +mentions[i].pubmedId()
			  +"</A></h4>");
	    result.append("<p>Total score: "
			  +formatter.format(mentions[i].totalScore())
			  +" Gene score: "
			  +formatter.format(geneScore)
			  +"</p>");
	    result.append("<p>"
			  +mentions[i].text()
			  +"</p>");
	}
	return result.toString();
    }

}