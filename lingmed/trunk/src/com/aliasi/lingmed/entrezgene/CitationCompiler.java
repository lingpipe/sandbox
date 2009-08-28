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

import com.aliasi.lingmed.dao.*;
import com.aliasi.lingmed.medline.*;
import com.aliasi.lingmed.server.*;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.medline.Abstract;
import com.aliasi.medline.Article;
import com.aliasi.medline.MedlineCitation;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Arrays;
import com.aliasi.util.Strings;

import java.io.*;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import org.apache.log4j.Logger;

/**
 * <P>The <code>CitationCompiler</code> command builds
 * a corpus of all PubMed citations that are
 * referenced in an EntrezGene entry.
 *
 * <P>The following arguments are required:
 *
 * <dl>
 * <dt><code>-corpusDir</code></dt>
 * <dd>Name of directory in which to store the corpus.
 * </dd>
 *
 * <dt><code>-host</code></dt>
 * <dd>Name of Lucene search server.
 * If value is &quot;localhost&quot; then search 
 * the local Lucene indexes,
 * else search remote Lucene indexes (via RMI).
 * </dd>
 * </dl>
 *
 * <P>The following arguments are optional:
 *
 * <dl>
 * <dt><code>-entrezgene</code></dt>
 * <dd>Name of remote entrezgene search service, or path to
 * local Lucene entrezgene index dir.
 * Defaults to &quot;entrezgene&quot;.
 * </dd>
 *
 * <dt><code>-medline</code></dt>
 * <dd>Name of remote medline search service, or path to
 * local Lucene medline index dir.
 * Defaults to &quot;medline&quot;.
 * </dd>
 *
 * <dt><code>-maxGeneHits</code></dt>
 * <dd>Maximum number of genes mentioned in an article.
 * Used to exclude texts which are too general.
 * Defaults to 100.
 * </dd>
 *
 * </dl>
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.3
 */

public class CitationCompiler extends AbstractCommand {
    private final Logger mLogger
        = Logger.getLogger(CitationCompiler.class);

    private String mSearchHost;
    private String mEntrezService;
    private String mMedlineService;

    private File mCitationDir;
    private String mCitationDirPath;

    private int mNGram;
    private int mMaxGeneHits;

    private EntrezGeneSearcher mEntrezGeneSearcher;
    private MedlineSearcher mMedlineSearcher;

    private final static String SEARCH_HOST = "host";
    private final static String MEDLINE_SERVICE = "medline";
    private final static String ENTREZGENE_SERVICE = "entrezgene";
    private final static String CITATION_DIR = "citationDir";
    private final static String MAX_GENE_HITS = "maxGeneHits";

    private final static Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(MEDLINE_SERVICE,MEDLINE_SERVICE);
        DEFAULT_PARAMS.setProperty(ENTREZGENE_SERVICE,ENTREZGENE_SERVICE);
        DEFAULT_PARAMS.setProperty(MAX_GENE_HITS,"100");
    }

    private CitationCompiler(String[] args) throws Exception {
        super(args,DEFAULT_PARAMS);
        mSearchHost = getExistingArgument(SEARCH_HOST);
        mMedlineService = getExistingArgument(MEDLINE_SERVICE);
        mEntrezService = getExistingArgument(ENTREZGENE_SERVICE);
        mCitationDirPath = getExistingArgument(CITATION_DIR);
        mMaxGeneHits = getArgumentInt(MAX_GENE_HITS);

        reportParameters();

        mCitationDir = new File(mCitationDirPath);
        FileUtils.ensureDirExists(mCitationDir);

        if (mSearchHost.equals("localhost")) {
            FileUtils.checkIndex(mMedlineService,false);
            Searcher medlineLocalSearcher = new IndexSearcher(mMedlineService);
            mMedlineSearcher = new MedlineSearcherImpl(new MedlineCodec(),medlineLocalSearcher);

            FileUtils.checkIndex(mEntrezService,false);
            Searcher egLocalSearcher = new IndexSearcher(mEntrezService);
            mEntrezGeneSearcher = new EntrezGeneSearcherImpl(new EntrezGeneCodec(),egLocalSearcher);

        } else {
            SearchClient medlineClient = new SearchClient(mMedlineService,mSearchHost,1099);
            Searcher medlineRemoteSearcher = medlineClient.getSearcher();
            mMedlineSearcher = 
                new MedlineSearcherImpl(new MedlineCodec(),medlineRemoteSearcher);

            SearchClient egClient = new SearchClient(mEntrezService,mSearchHost,1099);
            Searcher egRemoteSearcher = egClient.getSearcher();
            mEntrezGeneSearcher = new EntrezGeneSearcherImpl(new EntrezGeneCodec(),egRemoteSearcher);


        }
        mLogger.info("instantiated lucene searchers");
    }

    private void reportParameters() {
        mLogger.info("CitationCompiler "
                     + "\n\tCitation Directory (output dir)=" + mCitationDirPath
                     + "\n\tmax gene hits per pubmed article=" + mMaxGeneHits
                     + "\n\tsearch host=" + mSearchHost
                     );
    }

    public void run() {
        mLogger.info("Begin");
        Set<String> allPmids = new HashSet<String>();
        try {
            for (EntrezGene entrezGene : mEntrezGeneSearcher) {
                mLogger.info("processing EntrezGene Id: "+entrezGene.getGeneId());
                processCitations(entrezGene,allPmids);
            }
            mLogger.info("total unique pubmed references: " + allPmids.size());
            // use allPmids to compile special Lucene index?
        } catch (Exception e) {
            mLogger.warn("Unexpected Exception: "+e.getMessage());
            mLogger.warn("stack trace: "+Logging.logStackTrace(e));
            IllegalStateException e2 
                = new IllegalStateException(e.getMessage());
            e2.setStackTrace(e.getStackTrace());
            throw e2;
        }
    }

    private void processCitations(EntrezGene entrezGene,
                                  Set<String> allPmids) throws DaoException, FileNotFoundException {
        String[] pubMedIds = entrezGene.getUniquePubMedRefs();
        for (String pmid : pubMedIds) {
            SearchResults<EntrezGene> hits = mEntrezGeneSearcher.getGenesForPubmedId(pmid);
            if (mLogger.isDebugEnabled())
                mLogger.debug("pubmed id: "+pmid+"\t hits: "+hits.size());
            if (hits.size() > mMaxGeneHits) continue;
            if (allPmids.contains(pmid)) continue;
            allPmids.add(pmid);
            MedlineCitation citation = mMedlineSearcher.getById(pmid);
            if (citation == null) {
                if (mLogger.isDebugEnabled())
                    mLogger.debug("pubmed id: "+pmid+" not found in index");
                continue;
            }
            outputCitation(citation);
        }
    }

    private void outputCitation(MedlineCitation citation) throws FileNotFoundException {
        String pmid = citation.pmid();
        PrintStream citationOut = new PrintStream(new FileOutputStream(new File(mCitationDir,pmid+".html")));
	citationOut.println("<HTML><BODY>");
        citationOut.println("<H4>PubMed ID: " + citation.pmid() + "</H4>");
        citationOut.println("<H4>" + citation.article().articleTitleText() + "</H4>");
        if (citation.article().abstrct() != null) {
	    citationOut.println("<P>");
            citationOut.println(citation.article().abstrct().textWithoutTruncationMarker());
	    citationOut.println("</P>");
        }
	citationOut.println("</BODY></HTML>");
        citationOut.close();
    }

    public static void main(String[] args) throws Exception {
        CitationCompiler compiler = new CitationCompiler(args);
        compiler.run();
    }


}
