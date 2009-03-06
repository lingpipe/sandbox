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

package com.aliasi.lingmed.lingblast;

import com.aliasi.lingmed.dao.*;
import com.aliasi.lingmed.entrezgene.*;
import com.aliasi.lingmed.lingblast.Constants;
import com.aliasi.lingmed.medline.*;
import com.aliasi.lingmed.server.*;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.lm.NGramProcessLM;
import com.aliasi.lm.TrieCharSeqCounter;

import com.aliasi.medline.Abstract;
import com.aliasi.medline.Article;
import com.aliasi.medline.MedlineCitation;

import com.aliasi.stats.Statistics;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Arrays;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Strings;

import java.io.*;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import org.apache.log4j.Logger;

/**
 * <P>The <code>ModelCompiler</code> command builds
 * a set of language models for the genes in EntrezGene.
 * A gene-specific language model is created for those genes
 * which have descriptive text.
 * A general genomics language model is created from the 
 * union of all these descriptive texts.
 *
 * <P>The following arguments are required:
 *
 * <dl>
 * <dt><code>-modelDir</code></dt>
 * <dd>Name of directory for compiled Language Model files.
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
 * <dt><code>-maxNGram</code></dt>
 * <dd>Maximum length nGram for language model.
 * Defaults to 5.
 * </dd>
 *
 * <dt><code>-maxGeneHits</code></dt>
 * <dd>Maximum number of genes mentioned in an article.
 * Used to exclude texts which are too general.
 * Defaults to 100.
 * </dd>
 *
 * <dt><code>-genHtml</code></dt>
 * <dd>If true, the program will create html pages as well as
 * languages models.  Writes a page for each per-gene
 * language model listing the texts used to create that model.
 * Also creates an index page over all gene entries.
 * Defaults to false.
 * </dd>
 *
 * </dl>
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class ModelCompiler extends AbstractCommand {
    private final Logger mLogger
        = Logger.getLogger(ModelCompiler.class);

    private boolean mGenHtml;
    private PrintStream mHtmlOut;
    private PrintStream mPerGeneHtmlOut;

    private String mSearchHost;
    private String mEntrezService;
    private String mMedlineService;

    private File mModelDir;
    private String mModelDirPath;

    private int mNGram;
    private int mMaxGeneHits;

    private EntrezGeneSearcher mEntrezGeneSearcher;
    private MedlineSearcher mMedlineSearcher;

    private final static String SEARCH_HOST = "host";
    private final static String MEDLINE_SERVICE = "medline";
    private final static String ENTREZGENE_SERVICE = "entrezgene";
    private final static String GEN_HTML = "genHtml";
    private final static String MODEL_DIR = "modelDir";
    private final static String MAX_NGRAM = "maxNGram";
    private final static String MAX_GENE_HITS = "maxGeneHits";

    private final static Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(MEDLINE_SERVICE,Constants.MEDLINE_SERVICE);
        DEFAULT_PARAMS.setProperty(ENTREZGENE_SERVICE,Constants.ENTREZ_SERVICE);
        DEFAULT_PARAMS.setProperty(GEN_HTML,"false");
        DEFAULT_PARAMS.setProperty(MAX_NGRAM,"5");
        DEFAULT_PARAMS.setProperty(MAX_GENE_HITS,"100");
    }

    private ModelCompiler(String[] args) throws Exception {
        super(args,DEFAULT_PARAMS);
        mSearchHost = getExistingArgument(SEARCH_HOST);
        mMedlineService = getExistingArgument(MEDLINE_SERVICE);
        mEntrezService = getExistingArgument(ENTREZGENE_SERVICE);
        mGenHtml = Boolean.valueOf(getArgument(GEN_HTML));
        mModelDirPath = getExistingArgument(MODEL_DIR);
        mNGram = getArgumentInt(MAX_NGRAM);
        mMaxGeneHits = getArgumentInt(MAX_GENE_HITS);

        reportParameters();

        mModelDir = new File(mModelDirPath);
        FileUtils.ensureDirExists(mModelDir);

        if (mGenHtml) {
            mHtmlOut = new PrintStream(new FileOutputStream(new File(mModelDir,"Models.html")));
            mHtmlOut.println("<HTML><BODY>");
        }

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
        mLogger.info("ModelCompiler "
                     + "\n\tModel Directory (output dir)=" + mModelDirPath
                     + "\n\tmax ngram=" + mNGram
                     + "\n\tmax gene hits per pubmed article=" + mMaxGeneHits
                     + "\n\tsearch host=" + mSearchHost
                     + "\n\tgenerate Html?=" + mGenHtml
                     );
    }

    public void run() {
        mLogger.info("Begin");
        try {
            Set<String> allTexts = new HashSet<String>();
            for (EntrezGene entrezGene : mEntrezGeneSearcher) {
                mLogger.info("processing EntrezGene Id: "+entrezGene.getGeneId());
                if (mGenHtml) outputEntrezGeneName(entrezGene);
                Set<String> perGeneTexts = new HashSet<String>();
                addEntrezGeneTexts(entrezGene,allTexts,perGeneTexts);
                addPubMedTexts(entrezGene,allTexts,perGeneTexts);
                compilePerGeneLM(entrezGene,perGeneTexts);
                if (mGenHtml) mPerGeneHtmlOut.close();
            }
            if (mLogger.isDebugEnabled())
                mLogger.debug("Compiled all per-gene models, now do genomics model");
            compileGenomicsLM(allTexts);
            if (mGenHtml) mHtmlOut.close();
            mLogger.info("Processing complete.");
        } catch (Exception e) {
            mLogger.warn("Unexpected Exception: "+e.getMessage());
            mLogger.warn("stack trace: "+Logging.logStackTrace(e));
            IllegalStateException e2 
                = new IllegalStateException(e.getMessage());
            e2.setStackTrace(e.getStackTrace());
            throw e2;
        }
    }

    private void addEntrezGeneTexts(EntrezGene entrezGene,
                                     Set<String> allTexts,
                                    Set<String> perGeneTexts) throws DaoException {
        String geneSummary = entrezGene.getGeneSummary();
        if (geneSummary != null) {
            allTexts.add(geneSummary);
            perGeneTexts.add(geneSummary);
        }
        String[] geneRifLabels = entrezGene.getGeneRifLabels();
        for (String label : geneRifLabels) {
            allTexts.add(label);
            perGeneTexts.add(label);
        }
    }

    private void addPubMedTexts(EntrezGene entrezGene,
                                Set<String> allTexts,
                                Set<String> perGeneTexts) throws DaoException {
        String[] pubMedIds = entrezGene.getUniquePubMedRefs();
        for (String pmid : pubMedIds) {
            SearchResults<EntrezGene> hits = mEntrezGeneSearcher.getGenesForPubmedId(pmid);
            if (mLogger.isDebugEnabled())
                mLogger.debug("pubmed id: "+pmid+"\t hits: "+hits.size());
            if (hits.size() > mMaxGeneHits) continue;

            MedlineCitation citation = mMedlineSearcher.getById(pmid);
            if (citation == null) {
                if (mLogger.isDebugEnabled())
                    mLogger.debug("pubmed id: "+pmid+" not found in index");
                continue;
            }
            
            String titleAbstract = MedlineCodec.titleAbstract(citation);
            allTexts.add(titleAbstract);
            perGeneTexts.add(titleAbstract);

            if (mGenHtml)
                outputPubmedTexts(citation);
        }
    }

    private void compileGenomicsLM(Set<String> texts) throws IOException {
        NGramProcessLM genomicLM = new NGramProcessLM(mNGram);
        for (String text : texts) genomicLM.train(text);
        File outputFile = new File(mModelDir,Constants.GENOMICS_LM);
        AbstractExternalizable.compileTo(genomicLM,outputFile);
        if (mGenHtml)
            outputTopNGrams(genomicLM.substringCounter());
        if (mLogger.isDebugEnabled())
            mLogger.debug("compiled genomic  model to file: "+Constants.GENOMICS_LM);
    }

    private void compilePerGeneLM(EntrezGene entrezGene,Set<String> texts) throws IOException {
        NGramProcessLM perGeneLM = new NGramProcessLM(mNGram);
        for (String text : texts) perGeneLM.train(text);
        if (perGeneLM.observedCharacters().length == 0) {
            mLogger.warn("cannot build model for EntrezGene Id: "+entrezGene.getGeneId());
            return;
        }
        String perGeneFileName = entrezGene.getGeneId()+Constants.LM_SUFFIX;
        File outputFile = new File(mModelDir,perGeneFileName);
        AbstractExternalizable.compileTo(perGeneLM,outputFile);
        if (mGenHtml)
            outputTopNGrams(perGeneLM.substringCounter());
        if (mLogger.isDebugEnabled())
            mLogger.debug("compiled modelto file: "+perGeneFileName);
    }

    private void outputEntrezGeneName(EntrezGene entrezGene) throws FileNotFoundException {
        String geneId = entrezGene.getGeneId();
        mHtmlOut.println("<H3>EntrezGene ID: "+geneId+" Name: "+entrezGene.getOfficialFullName()+"</H3>");
        mHtmlOut.println("<A HREF=\""+geneId+".html\">text</A><BR>");
        mPerGeneHtmlOut = new PrintStream(new FileOutputStream(new File(mModelDir,geneId+".html")));
        mPerGeneHtmlOut.println("<HTML><BODY><H2>EntrezGene ID: "+geneId+" Name: "+entrezGene.getOfficialFullName()+"</H2>");
    }


    private void outputEntrezTexts(EntrezGene entrezGene) {
        mPerGeneHtmlOut.println("<H4>gene summary</H4>");
        mPerGeneHtmlOut.println(entrezGene.getGeneSummary());
        String[] geneRifLabels = entrezGene.getGeneRifLabels();
        mPerGeneHtmlOut.println("<H4>gene RIF</H4><UL>");
        for (String label : geneRifLabels)
            mPerGeneHtmlOut.println("<LI>"+label);
        mPerGeneHtmlOut.println("</UL>");
    }

    private void outputPubmedTexts(MedlineCitation citation) {
        mPerGeneHtmlOut.println("<H4>PubMed ID: "+citation.pmid()+" title</H4>");
        mPerGeneHtmlOut.println(citation.article().articleTitleText());
        if (citation.article().abstrct() != null) {
            mPerGeneHtmlOut.println("<H4>PubMed ID: "+citation.pmid()+" abstract</H4>");
            mPerGeneHtmlOut.println(citation.article().abstrct().textWithoutTruncationMarker());
        }
    }

    private void outputTopNGrams(TrieCharSeqCounter seqCounter) {
        for (int i = 0; i <= mNGram; ++i) {
            ObjectToCounterMap<String> topNGrams = seqCounter.topNGrams(i,mNGram);
            Object[] keysByCount = topNGrams.keysOrderedByCount();
            if (keysByCount.length > 0) {
                mHtmlOut.print(i + ",");
                for (int j = 0; j < keysByCount.length; ++j) {
                    String nGram = keysByCount[j].toString();
                    int count = topNGrams.getCount(nGram);
                    String csvNGram = '"' + nGram.replaceAll("\"","\\\"") + '"';
                    mHtmlOut.print("  \"" + nGram + "\"," + count);
                }
                mHtmlOut.println("<BR>");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ModelCompiler compiler = new ModelCompiler(args);
        compiler.run();
    }


}
