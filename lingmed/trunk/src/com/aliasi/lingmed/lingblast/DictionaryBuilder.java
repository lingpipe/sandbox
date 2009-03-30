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

import com.aliasi.dict.*;

import com.aliasi.lingmed.dao.*;
import com.aliasi.lingmed.entrezgene.*;
import com.aliasi.lingmed.medline.*;
import com.aliasi.lingmed.server.*;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Arrays;

import java.io.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import org.apache.log4j.Logger;

/**
 * <P>The <code>DictionaryBuilder</code> command creates 
 * an exact-match dictionary over a set of gene names from 
 * EntrezGene where all matches are scored 1.0.
 * The category assigned to a dictionary entry is the set
 * of all geneIds for which the entry is a name or alias.
 *
 * <P>The following arguments are required:
 *
 * <dl>
 * <dt><code>-dictFile</code></dt>
 * <dd>Name of file for serialized dictionary
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
 * <dt><code>-minNameLen</code></dt>
 * <dd>Minimum allowed name length of dictionary entry.
 * Defaults to 1.
 * </dd>
 *
 * <dt><code>-maxNameLen</code></dt>
 * <dd>Maxmimum allowed name length of dictionary entry.
 * Defaults to 1024.
 * </dd>
 *
 * <dt><code>-maxGenesPerAlias</code></dt>
 * <dd>For any dictionary entry, maxmimum number of genes which 
 * which have this entry among their aliases.  This is used to
 * eliminate overly ambiguous aliases such as &quot;hypothetical protein&quot;.
 * Defaults to 100.
 * </dd>
 *
 * <dt><code>-maxPubmedHitsPerAlias</code></dt>
 * <dd>For any dictionary entry, maxmimum number of pubmed articles 
 * which contain this entry.  This is used to eliminate uninformative 
 * aliases, such as &quot;Is&quot;.
 * Defaults to 10000.
 * </dd>
 *
 * <dt><code>-allowedNames</code></dt>
 * <dd>Name of file containing dictionary entries which should
 * be included in dictionary, even if they exceed maxGenesPerAlias
 * or maxPubmedHitsPerAlias.
 * </dd>
 *
 * <dt><code>-genHtml</code></dt>
 * <dd>If true, the program will output an html page 
 * which contains a list of all entries found, and whether or not
 * they were used in the dictionary.
 * Defaults to false.
 * </dd>
 *
 * </dl>
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class DictionaryBuilder extends AbstractCommand {
    private final Logger mLogger
        = Logger.getLogger(DictionaryBuilder.class);

    private boolean mGenHtml;
    private PrintStream mHtmlOut;

    private File mDictFile;
    private String mDictFileName;
    private String mAllowedNamesFileName;

    private String mSearchHost;
    private String mEntrezService;
    private String mMedlineService;

    private int mMinNameLen;
    private int mMaxNameLen;
    private int mMaxPubmedHits;

    private final HashMap<String,Set<String>> mDictMap = new HashMap<String,Set<String>>();
    private final HashSet<String> mAllowed = new HashSet<String>();

    private EntrezGeneSearcher mEntrezGeneSearcher;
    private MedlineSearcher mMedlineSearcher;

    private final static String GEN_HTML = "genHtml";
    private final static String DICT_FILE = "dictFile";
    private final static String ALLOWED_NAMES_FILE = "allowedNames";
    private final static String SEARCH_HOST = "host";
    private final static String ENTREZ_SERVICE = "entrezgene";
    private final static String MEDLINE_SERVICE = "medline";
    private final static String MIN_NAME_LENGTH = "minNameLen";
    private final static String MAX_NAME_LENGTH = "maxNameLen";
    private final static String MAX_GENE_MATCHES = "maxGenesPerAlias";
    private final static String MAX_PUBMED_HITS = "maxPubmedHitsPerAlias";

    private final static Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(GEN_HTML,"false");
        DEFAULT_PARAMS.setProperty(ENTREZ_SERVICE,"entrezgene");
        DEFAULT_PARAMS.setProperty(MEDLINE_SERVICE,"medline");
        DEFAULT_PARAMS.setProperty(MIN_NAME_LENGTH,"1");
        DEFAULT_PARAMS.setProperty(MAX_NAME_LENGTH,"1024");
        DEFAULT_PARAMS.setProperty(MAX_GENE_MATCHES,"100");
        DEFAULT_PARAMS.setProperty(MAX_PUBMED_HITS,"10000");
    }

    // Instantiate DictionaryBuilder object and 
    // initialize instance variables per command line args
    private DictionaryBuilder(String[] args) throws Exception {
        super(args,DEFAULT_PARAMS);
        mGenHtml = Boolean.valueOf(getArgument(GEN_HTML));
        mDictFileName = getExistingArgument(DICT_FILE);
        mSearchHost = getExistingArgument(SEARCH_HOST);
        mEntrezService = getExistingArgument(ENTREZ_SERVICE);
        mMedlineService = getExistingArgument(MEDLINE_SERVICE);
        mAllowedNamesFileName = getArgument(ALLOWED_NAMES_FILE);
        mMinNameLen = getArgumentInt(MIN_NAME_LENGTH);
        mMaxNameLen = getArgumentInt(MAX_NAME_LENGTH);
        mMaxPubmedHits = getArgumentInt(MAX_PUBMED_HITS);
        reportParameters();

        mDictFile = FileUtils.checkOutputFile(mDictFileName);

        if (mSearchHost.equals("localhost")) {
            FileUtils.checkIndex(mEntrezService,false);
            Searcher egLocalSearcher = new IndexSearcher(mEntrezService);
            mEntrezGeneSearcher = new EntrezGeneSearcherImpl(new EntrezGeneCodec(),egLocalSearcher);

            FileUtils.checkIndex(mMedlineService,false);
            Searcher medlineLocalSearcher = new IndexSearcher(mMedlineService);
            mMedlineSearcher = new MedlineSearcherImpl(new MedlineCodec(),medlineLocalSearcher);
        } else {
            SearchClient egClient = new SearchClient(mEntrezService,mSearchHost,1099);
            Searcher egRemoteSearcher = egClient.getSearcher();
            mEntrezGeneSearcher = new EntrezGeneSearcherImpl(new EntrezGeneCodec(),egRemoteSearcher);

            SearchClient medlineClient = new SearchClient(mMedlineService,mSearchHost,1099);
            Searcher medlineRemoteSearcher = medlineClient.getSearcher();
            mMedlineSearcher = new MedlineSearcherImpl(new MedlineCodec(),medlineRemoteSearcher);
        }
        if (mGenHtml) {
            openHtml();
        }
        if (mAllowedNamesFileName != null) {
            getAllowedNames(mAllowedNamesFileName);
        }
    }

    public void run() {
        mLogger.info("\nBegin");
        try {
            mapEntrezGene();

            mLogger.info("\nCreate dictionary");
            TrieDictionary<String> mTrieDict = new TrieDictionary<String>();

            for (Iterator dictIt=mDictMap.entrySet().iterator(); dictIt.hasNext(); ) {
                Entry<String,Set<String>> entry = (Entry<String,Set<String>>)dictIt.next();
                String alias = entry.getKey();
                Set<String> ids = entry.getValue();
                try {
                    int pubmedHits = mMedlineSearcher.numExactPhraseMatches(alias);
                    if (pubmedHits > mMaxPubmedHits && !mAllowed.contains(alias)) {
                        mLogger.info("alias: " + alias + " hits:" + pubmedHits + " exceeds max");
                    }
                    if (mGenHtml) {
                        mHtmlOut.print("<TR><TD>"+pubmedHits+"</TD><TD>"+ids.size()+"</TD><TD>"+alias+"</TD>");
                    }
                    if (pubmedHits > mMaxPubmedHits && !mAllowed.contains(alias)) {
                        if (mGenHtml) mHtmlOut.println("<TD><B>no</B></TD></TR>");
                        continue;
                    } else {
                        if (mGenHtml) mHtmlOut.println("<TD>yes</TD></TR>");
                    }

                    String[] categoryArray = new String[ids.size()];
                    categoryArray = ids.toArray(categoryArray);
                    String category = Arrays.arrayToCSV(categoryArray);
                    DictionaryEntry<String> dictEntry = new DictionaryEntry<String>(alias,category,1.00d);
                    mTrieDict.addEntry(dictEntry);
                } catch (DaoException de) {
                    mLogger.info("MEDLINE search threw DaoException: "+de.getMessage());
                    mLogger.info("bad alias: "+alias);
                    if (mGenHtml) {
                        mHtmlOut.print("<TR><TD>n/a</TD><TD>n/a</TD><TD>"
                                       + alias 
                                       + "</TD><TD><B>no</B></TD></TR>");
                    }
                }
            }
            if (mGenHtml) {
                mHtmlOut.println("</TABLE></BODY></HTML>");
                mHtmlOut.close();
            }

            mLogger.info("\nCompile dictionary");
            ObjectOutputStream compiledDict = new ObjectOutputStream(new FileOutputStream(mDictFileName));
            mTrieDict.compileTo(compiledDict);
            compiledDict.close();

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

    private void getAllowedNames(String filename) throws IOException {
        File allowedNames = FileUtils.checkInputFile(filename);
        String line = null;
        LineNumberReader in = new LineNumberReader(new FileReader(allowedNames));
        while ((line = in.readLine()) != null) {
            mAllowed.add(line);
        }
        in.close();
        if (mLogger.isDebugEnabled()) 
            mLogger.debug("total allowed gene names: "+mAllowed.size());
    }

    private void mapEntrezGene() {
        for (EntrezGene entrezGene : mEntrezGeneSearcher) {
            String geneId = entrezGene.getGeneId();
            if (mLogger.isDebugEnabled())
                mLogger.debug("\nprocessing EntrezGene Id: "+geneId);

            HashSet<String> names = new HashSet<String>();
            String[] aliases = entrezGene.getUniqueAliases();
            for (String alias : aliases) {
                mLogger.debug("alias: "+alias);
                // minimally informative names
                if (alias.startsWith("LOC") 
                    || alias.startsWith("OTTHUMP")) {
                    names.add(alias);
                    continue;
                }
                // uninformative names
                if (alias.toLowerCase().startsWith("similar to")
                    || alias.toLowerCase().startsWith("hypothetical")) {
                    mLogger.debug("ignore alias: "+alias);
                    continue;
                }
                String[] variants = GeneNameMutator.getVariants(alias);
                for (String variant : variants) {
                    names.add(variant);
                    mLogger.debug("variant: "+variant);
                }
            }
            String[] linkIds = entrezGene.getLinkIds();
            for (String linkId : linkIds) {
                try {
                    Float.valueOf(linkId);
                    mLogger.debug("ignore link id: "+linkId);
                } catch (NumberFormatException e) {
                    names.add(linkId);
                }
            }
            if (mLogger.isDebugEnabled()) 
                mLogger.debug("entrez names: "+names.size());

            for (String name: names) {
                if (name.length() >= mMinNameLen
                    && name.length() <= mMaxNameLen ) {
                    Set<String> ids = null;
                    if (!mDictMap.containsKey(name)) {
                        ids = new HashSet<String>();
                    } else {
                        if (mLogger.isDebugEnabled())
                            mLogger.debug("ambiguous name: "+name);
                        ids = mDictMap.get(name);
                    }
                    ids.add(geneId);
                    mDictMap.put(name,ids);
                } else {
                    if (mLogger.isDebugEnabled())
                        mLogger.debug("not using name: "+name+ ", geneId: "+geneId);
                }
            }
        }
    }

    private void openHtml() throws IOException {
            File htmlFile = FileUtils.checkOutputFile(mDictFileName+".html");
            mHtmlOut = new PrintStream(new FileOutputStream(htmlFile));
            mHtmlOut.println("<HTML><BODY><TABLE BORDER=\"1\" CELLPADDING=\"1\">");
            mHtmlOut.println("<TR><TH>Pubmed Hits</TH><TH>EntrezGene Hits</TH><TH>Phrase</TH><TH>Include?</TH></TR>");
    }

    private void reportParameters() {
        mLogger.info("DictionaryBuilder "
                     + "\n\tDictionary=" + mDictFileName
                     + "\n\tAllowedNames=" + mAllowedNamesFileName
                     + "\n\tmaximum pubmed articles per name=" + mMaxPubmedHits
                     + "\n\tminimum name length=" + mMinNameLen
                     + "\n\tmaximum name length=" + mMaxNameLen
                     + "\n\tsearch host=" + mSearchHost
                     + "\n\tEntrezService=" + mEntrezService
                     + "\n\tMedlineService=" + mMedlineService
                     + "\n\tgenerate Html?=" + mGenHtml
                     );
    }

    public static void main(String[] args) throws Exception {
        DictionaryBuilder builder = new DictionaryBuilder(args);
        builder.run();
    }
}
