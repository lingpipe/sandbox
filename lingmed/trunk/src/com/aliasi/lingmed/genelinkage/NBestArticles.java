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

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Pair;
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
 * <P>The <code>NBestArticles</code> command
 * reads in a file containing geneIds, 1 per line,
 * and outputs html pages, 1 per geneId, 
 * which list the N best-scoring articles 
 * that mention this gene.
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
 * @since   LingMed1.0
 */

public class NBestArticles extends AbstractCommand {
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
    private NBestArticles(String[] args) throws Exception {
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
        NBestArticles generator = new NBestArticles(args);
        generator.run();
    }

    public void run() {
        mLogger.info("find articleMentions for gene Ids");
        try {
            PrintStream mHtmlIndexOut =
                new PrintStream(new FileOutputStream(new File(mHtmlDir,"index.html")));
            mHtmlIndexOut.println("<HTML><BODY>");

            LineNumberReader in = new LineNumberReader(new FileReader(mGeneIdFile));
            String line = null;
            while ((line = in.readLine()) != null) {
                mLogger.info("geneId: "+line);
                if (line.trim().length() < 1) continue;
				EntrezGene entrezGene = mEntrezGeneSearcher.getById(line);
				String geneId = entrezGene.getGeneId();
				String geneName = entrezGene.getOfficialFullName();
				String geneSymbol = entrezGene.getOfficialSymbol();
				String name = geneName+" ("+geneSymbol+")";
				mHtmlIndexOut.println("<h3>Gene "+geneId+": "+name+"</h3>");
				ArticleMention[] mentions = 
                    mGeneLinkageSearcher.findTopMentions(geneId,mMaxArticles);
				if (mentions.length > 0) {
					String page = genPerGenePage(geneId,name,mentions);
					mHtmlIndexOut.println("<p>See <a href=\""+page+"\">"+mentions.length+" best scoring articles</a> in pubmed.</p>");
				} else {
					mHtmlIndexOut.println("<p>No mentions in pubmed.</p>");
				}
			}
			mHtmlIndexOut.close();
		} catch (Exception e) {
			mLogger.warn("Unexpected Exception: "+e.getMessage());
			mLogger.warn("stack trace: "+Logging.logStackTrace(e));
			IllegalStateException e2 
				= new IllegalStateException(e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
	}

	private String genPerGenePage(String geneId,
								  String name,
								  ArticleMention[] mentions) throws IOException, DaoException {
		File perGeneFile = new File(mHtmlDir,geneId+".html");
		PrintStream htmlOut = new PrintStream(new FileOutputStream(perGeneFile));
		htmlOut.println(mGeneLinkageSearcher.genHtml(geneId,mentions));
		htmlOut.close();
		return perGeneFile.getName();
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