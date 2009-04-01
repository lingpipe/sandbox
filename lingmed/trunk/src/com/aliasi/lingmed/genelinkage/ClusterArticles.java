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

import com.aliasi.cluster.CompleteLinkClusterer;
import com.aliasi.cluster.SingleLinkClusterer;
import com.aliasi.cluster.Dendrogram;
import com.aliasi.cluster.LeafDendrogram;
import com.aliasi.cluster.LinkDendrogram;

import com.aliasi.lingmed.dao.DaoException;
import com.aliasi.lingmed.entrezgene.EntrezGene;
import com.aliasi.lingmed.entrezgene.EntrezGeneCodec;
import com.aliasi.lingmed.entrezgene.EntrezGeneSearcher;
import com.aliasi.lingmed.entrezgene.EntrezGeneSearcherImpl;
import com.aliasi.lingmed.lingblast.Constants;
import com.aliasi.lingmed.medline.MedlineCodec;
import com.aliasi.lingmed.medline.MedlineSearcher;
import com.aliasi.lingmed.medline.MedlineSearcherImpl;
import com.aliasi.lingmed.server.SearchClient;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.matrix.ProximityMatrix;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Counter;
import com.aliasi.util.Files;
import com.aliasi.util.NBestSet;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Pair;
import com.aliasi.util.ScoredObject;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintStream;

import java.sql.SQLException;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import org.apache.log4j.Logger;


/**
 * <P>The <code>ClusterArticles</code> command
 * reads in a file containing geneIds, 1 per line
 * and clusters these genes according to co-occurances
 * in MEDLINE citations
 *
 * <P>The following arguments are required:
 *
 * <dl>
 * <dt><code>-geneIds</code></dt>
 * <dd>Name of input file containing geneIds.
 * </dd>
 *
 * <dt><code>-host</code></dt>
 * <dd>Name of Lucene search server.
 * If value is &quot;localhost&quot; then search 
 * the local Lucene indexes,
 * else search remote Lucene indexes (via RMI).
 * </dd>
 *
 * <dt><code>-dbUserName</code></dt>
 * <dd>Name of database user.
 * </dd>
 *
 * <dt><code>-dbUserPassword</code></dt>
 * <dd>Password for database user.
 * </dd>
 * </dl>
 *
 * <P>The following arguments are optional:
 *
 * <dl>
 * <dt><code>-htmlDir</code></dt>
 * <dd>
 * Directory for html pages.
 * Defaults to &quot;html&quot;.
 * </dd>
 *
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
 * <dt><code>-dbUrl</code></dt>
 * <dd>Url of database to connect to (for jdbc).
 * Defaults to &quot;jdbc:mysql://localhost:3306/gene_linkage&quot;
 * </dd>
 *
 * <dt><code>-dbName</code></dt>
 * <dd>Database name.
 * Defaults to &quot;gene_linkage&quot;.
 * </dd>
 *
 * <dt><code>-maxArticles</code></dt>
 * <dd>Maximum number of articles to return.
 * Defaults to 100.
 * </dd>
 *
 * </dl>
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.3
 */

public class ClusterArticles extends AbstractCommand {
    private final Logger mLogger
        = Logger.getLogger(NBestArticles.class);

    private File mGeneIdFile;
    private String mGeneIdFilePath;

    private File mHtmlDir;
    private String mHtmlDirPath;

    private int mMaxArticles;

    private String mSearchHost;
    private String mEntrezService;
    private EntrezGeneSearcher mEntrezGeneSearcher;
    private String mMedlineService;
    private MedlineSearcher mMedlineSearcher;

    private String mDbUrl;
    private String mDbName;
    private String mDbUserName;
    private String mDbUserPassword;
    private GeneLinkageDao mGeneLinkageDao;

    private GeneLinkageSearcher mGeneLinkageSearcher;

    private final static String GENE_IDS = "geneIds";
    private final static String MAX_ARTICLES = "maxArticles";
    private final static String HTML_DIR = "htmlDir";
    private final static String SEARCH_HOST = "host";
    private final static String MEDLINE_SERVICE = "medline";
    private final static String ENTREZGENE_SERVICE = "entrezgene";
    private final static String DB_URL = "dbUrl";
    private final static String DB_NAME = "dbName";
    private final static String DB_USERNAME = "dbUserName";
    private final static String DB_USERPASSWORD = "dbUserPassword";

    private final static Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(DB_URL,"jdbc:mysql://localhost:3306/gene_linkage");
        DEFAULT_PARAMS.setProperty(DB_NAME,"gene_linkage");
        DEFAULT_PARAMS.setProperty(HTML_DIR,"html");
        DEFAULT_PARAMS.setProperty(MAX_ARTICLES,"100");
        DEFAULT_PARAMS.setProperty(MEDLINE_SERVICE,Constants.MEDLINE_SERVICE);
        DEFAULT_PARAMS.setProperty(ENTREZGENE_SERVICE,Constants.ENTREZ_SERVICE);
    }
    private ClusterArticles(String[] args) throws Exception {
        super(args,DEFAULT_PARAMS);
        mGeneIdFilePath = getExistingArgument(GENE_IDS);
        mHtmlDirPath = getExistingArgument(HTML_DIR);
        mMaxArticles = getArgumentInt(MAX_ARTICLES);
        mSearchHost = getExistingArgument(SEARCH_HOST);
        mMedlineService = getExistingArgument(MEDLINE_SERVICE);
        mEntrezService = getExistingArgument(ENTREZGENE_SERVICE);
        mDbUrl = getExistingArgument(DB_URL);
        mDbName = getExistingArgument(DB_NAME);
        mDbUserName = getExistingArgument(DB_USERNAME);
        mDbUserPassword = getExistingArgument(DB_USERPASSWORD);

        reportParameters();
        processParameters();
    }

    public static void main(String[] args) throws Exception {
        ClusterArticles generator = new ClusterArticles(args);
        generator.run();
    }

    private String[] getGeneIds(File inFile) throws IOException {
        HashSet<String> geneIds = new HashSet<String>();
        String line = null;
        LineNumberReader in = new LineNumberReader(new FileReader(inFile));
        while ((line = in.readLine()) != null) {
            if (line.trim().length() < 1) continue;
            geneIds.add(line.trim());
        }
        in.close();
        String[] result = new String[geneIds.size()];
        result = geneIds.toArray(result);
        return result;
    }
    

    public void run() {
        mLogger.info("find articleMentions for gene Ids");
        try {
            HashSet<String> allArticles = new HashSet<String>();
            HashSet<String> allGenes = new HashSet<String>();

            String[] seedGeneIds = getGeneIds(mGeneIdFile);
            ObjectToCounterMap[] geneCooccurance 
                = new ObjectToCounterMap[seedGeneIds.length];

            for (int i=0; i<seedGeneIds.length; i++) {
                String geneId = seedGeneIds[i];
                geneCooccurance[i] = new ObjectToCounterMap();
                ArticleMention[] mentions
                    = mGeneLinkageSearcher.findTopMentions(geneId,mMaxArticles);
                if (mLogger.isDebugEnabled()) {
                    mLogger.debug("geneId: "+geneId);
                    mLogger.debug("best article mentions: "+ mentions.length);
                }
                for (ArticleMention mention : mentions) {
                    allArticles.add(mention.pubmedId());
                    try {
                        int pmid = Integer.parseInt(mention.pubmedId());
                        Pair<Double,Set<Chunk>> geneMentions 
                            = mGeneLinkageDao.getGeneMentionsForPubmedId(pmid);
                        Set<Chunk> gMentions = geneMentions.b();
                        for (Chunk gMention : gMentions) {
                            allGenes.add(gMention.type());
                            geneCooccurance[i].increment(gMention.type());
                        }
                        if (mLogger.isDebugEnabled()) {
                            mLogger.debug("pmid: "+mention.pubmedId());
                            mLogger.debug("genes mentioned: "+ gMentions.size());
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
            }
            mLogger.info("seed genes: "+ seedGeneIds.length);
            mLogger.info("total articles: "+allArticles.size());
            mLogger.info("all genes: "+ allGenes.size());

            ProximityMatrix prox = new ProximityMatrix(seedGeneIds.length);
            for (int i = 0; i < seedGeneIds.length; ++i) {
                for (int j = i + 1; j < seedGeneIds.length; ++j) {
                    double cosine = cosine(geneCooccurance[i],
                                           geneCooccurance[j]);
                    prox.setValue(i,j,cosine);
                    if (mLogger.isDebugEnabled()) {
                        mLogger.debug("gene_i : " + seedGeneIds[i]
                                      + "gene_j : " + seedGeneIds[j]
                                      + "cosine : " + cosine);
                    }
                }
            }

            CompleteLinkClusterer clusterer = new CompleteLinkClusterer();
            Dendrogram[] dendrograms = clusterer.hierarchicalCluster(prox,1.0);
            for (int i = 0; i < dendrograms.length; ++i) {
                System.out.println("dendogram[" + i + "]: "
                                   + dendrograms[i]);
            }

        } catch (Exception e) {
            mLogger.warn("Unexpected Exception: "+e.getMessage());
            mLogger.warn("stack trace: "+Logging.logStackTrace(e));
            IllegalStateException e2 
                = new IllegalStateException(e.getMessage());
            e2.setStackTrace(e.getStackTrace());
            throw e2;
        }
    }

    private void processParameters() throws Exception {
        mGeneIdFile = FileUtils.checkInputFile(mGeneIdFilePath);

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


        InitialContext ic = new InitialContext();
        // Construct Jndi object reference:  arg1:  classname, arg2: factory name, arg3:URL (can be null)
        Reference ref = new Reference("com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource",
                                      "com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory",null);
        ref.add(new StringRefAddr("driverClassName","com.mysql.jdbc.Driver"));
        ref.add(new StringRefAddr("url",mDbUrl));
        ref.add(new StringRefAddr("databaseName",mDbName));
        ref.add(new StringRefAddr("username", mDbUserName));
        ref.add(new StringRefAddr("password", mDbUserPassword));
        ic.rebind("jdbc/mysql", ref);
        mGeneLinkageDao = 
            GeneLinkageDaoImpl.getInstance(ic,"jdbc/mysql",mDbUserName,mDbUserPassword);
        mLogger.info("instantiated mysql dao");
        mLogger.info("mGeneLinkageDao: "+mGeneLinkageDao);

        mGeneLinkageSearcher = 
            new GeneLinkageSearcher(mEntrezGeneSearcher, mMedlineSearcher, mGeneLinkageDao);

        mHtmlDir = new File(mHtmlDirPath);
        FileUtils.ensureDirExists(mHtmlDir);

        if (mMaxArticles < 1) {
            String msg = "Max Articles must be > 0, value found: "+mMaxArticles;
            throw new IllegalArgumentException(msg);
        }
    }

    static double cosine(ObjectToCounterMap doc1, ObjectToCounterMap doc2) {
        //        System.out.println("Dot product is :" + dotProduct(doc1,doc2));
        return dotProduct(doc1,doc2) / (length(doc1) * length(doc2));
    }

    static double dotProduct(ObjectToCounterMap doc1, 
                             ObjectToCounterMap doc2) {
        double product = 0.0;
        Iterator it = doc1.keySet().iterator();
        while (it.hasNext()) {

            Object key = it.next();
            //  System.out.println("key " + key + " doc1 " + doc1.getCount(key) + " doc2 " + doc2.getCount(key));
            product += doc1.getCount(key) * doc2.getCount(key);
        }
        return product == 0.0?Double.POSITIVE_INFINITY:product;
    }
    
    static double length(ObjectToCounterMap doc1) {
        double sumOfSquares = 0.0;
        Iterator it = doc1.values().iterator();
        while (it.hasNext()) {
            Counter counter = (Counter) it.next();
            double counterVal = counter.doubleValue(); 
            sumOfSquares += counterVal * counterVal;
        }
        return Math.sqrt(sumOfSquares);
    }

    private void reportParameters() {
        mLogger.info("NBestArticles "
                     + "\n\tgeneIds list (inputs)=" + mGeneIdFilePath
                     + "\n\tHTML dir (outputs)=" + mHtmlDirPath
                     + "\n\tmax articles for gene=" + mMaxArticles
                     + "\n\tlucene host=" + mSearchHost
                     + "\n\tmysql host=" + mDbUrl
                     + "\n\tdb name=" + mDbName
                     + "\n\tdb user name=" + mDbUserName
                     );
    }


}