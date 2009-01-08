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

import com.aliasi.chunk.*;
import com.aliasi.dict.*;
import com.aliasi.tokenizer.*;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Pair;
import com.aliasi.util.Strings;

import com.aliasi.lingmed.dao.*;
import com.aliasi.lingmed.entrezgene.*;
import com.aliasi.lingmed.medline.*;
import com.aliasi.lingmed.server.*;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.medline.Abstract;
import com.aliasi.medline.Article;
import com.aliasi.medline.MedlineCitation;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Arrays;
import com.aliasi.util.Files;

import java.io.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;

import org.apache.log4j.Logger;

/**
 * <P>The <code>LingBlastMedline</code> command
 * runs each medline citation through {@link LingBlast#lingblast}
 * and outputs its results as a set of tables
 * in tab-delimited text file format.
 * 
 * The tables are:
 * <ul>
 * <li>article_score.sql:  genomics score for each citation<br>
 * columns: pmid, genomic score
 *
 * <li>gene_article_score.sql:  per-gene score for each gene found in a citation<br>
 * columns: geneid, pmid, per-gene score, total (genomic score + per-gene score)
 * 
 * <li>gene_article_mention.sql: all gene mentions found in a citation.<br>
 * columns: geneid, pmid, text, offset
 * </ul>
 *
 * <P>The following arguments are required:
 *
 * <dl>
 * <dt><code>-dictionary</code></dt>
 * <dd>Name of file for serialized dictionary
 * </dd>
 *
 * <dt><code>-modelDir</code></dt>
 * <dd>Name of directory for compiled Language Model files.
 * </dd>
 *
 * <dt><code>-sqlDir</code></dt>
 * <dd>Name of directory to output sql tables to.
 * </dd>
 *
 * <dt><code>-genomicsThreshold</code></dt>
 * <dd>maximum allowable genomics score for citation.
 * Citations with high genomics score are not run through lingblast.
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
 * <dt><code>-medline</code></dt>
 * <dd>Name of remote medline search service, or path to
 * local Lucene medline index dir.
 * Defaults to &quot;medline&quot;.
 * </dd>
 * </dl>
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class LingBlastMedline extends AbstractCommand {
    private final Logger mLogger
	= Logger.getLogger(LingBlastMedline.class);

    private boolean mGenHtml;
    private PrintStream mHtmlOut;
    private PrintStream mPerGeneHtmlOut;

    private String mSearchHost;
    private String mEntrezService;
    private String mMedlineService;

    private File mModelDir;
    private String mModelDirPath;

    private File mSqlDir;
    private String mSqlDirPath;

    private BufferedOutputStream mArticleScoreOut;
    private BufferedOutputStream mGeneArticleScoreOut;
    private BufferedOutputStream mGeneArticleMentionOut;

    private File mDictFile;
    private String mDictFilePath;

    private int mNGram;

    private double mGenomicsThreshold;

    private EntrezGeneSearcher mEntrezGeneSearcher;
    private MedlineSearcher mMedlineSearcher;

    private final static String SEARCH_HOST = "host";
    private final static String MEDLINE_SERVICE = "medline";
    private final static String DICTIONARY = "dictionary";
    private final static String MODEL_DIR = "modelDir";
    private final static String SQL_DIR = "sqlDir";
    private final static String GENOMICS_THRESHOLD = "genomicsThreshold";

    private final static Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(MEDLINE_SERVICE,Constants.MEDLINE_SERVICE);
    }

    // Instantiate LingBlastMedline object and 
    // initialize instance variables per command line args
    private LingBlastMedline(String[] args) throws Exception {
        super(args,DEFAULT_PARAMS);
        mMedlineService = getExistingArgument(MEDLINE_SERVICE);
        mSearchHost = getExistingArgument(SEARCH_HOST);
	mModelDirPath = getExistingArgument(MODEL_DIR);
	mSqlDirPath = getExistingArgument(SQL_DIR);
	mDictFilePath = getExistingArgument(DICTIONARY);
        mGenomicsThreshold = getArgumentDouble(GENOMICS_THRESHOLD);

	reportParameters();

	mDictFile = FileUtils.checkInputFile(mDictFilePath);
	mModelDir = FileUtils.checkDir(mModelDirPath);

	mSqlDir = new File(mSqlDirPath);
	FileUtils.ensureDirExists(mSqlDir);

	if (mSearchHost.equals("localhost")) {
	    FileUtils.checkIndex(mMedlineService,false);
	    Searcher medlineLocalSearcher = new IndexSearcher(mMedlineService);
 	    mMedlineSearcher = new MedlineSearcherImpl(new MedlineCodec(),medlineLocalSearcher);
	} else {
	    SearchClient medlineClient = new SearchClient(mMedlineService,mSearchHost,1099);
	    Searcher medlineRemoteSearcher = medlineClient.getSearcher();
	    mMedlineSearcher = 
		new MedlineSearcherImpl(new MedlineCodec(),medlineRemoteSearcher);
	}
	mLogger.info("instantiated medline searcher");
    }

    private void reportParameters() {
        mLogger.info("LingBlastMedline "
		     + "\n\tModels Directory=" + mModelDirPath
		     + "\n\tDictionary=" + mDictFilePath
		     + "\n\tsearch host=" + mSearchHost
		     + "\n\tMedlineService=" + mMedlineService
		     + "\n\tgenomics threshold=" + mGenomicsThreshold
		     + "\n\tSql Directory=" + mSqlDirPath
		     );
    }

    public void run() {
	mLogger.info("Begin");
	try {
	    openSqlFiles(mSqlDir);
	    TrieDictionary dictionary = (TrieDictionary)
		AbstractExternalizable.readObject(mDictFile);
	    mLogger.info("Read dictionary "+mDictFilePath);
	    ExactDictionaryChunker dictionaryChunkerTT
		= new ExactDictionaryChunker(dictionary,
					     IndoEuropeanTokenizerFactory.FACTORY,
					     true,true);
	    LingBlast lb = new LingBlast(mMedlineSearcher,
					 mEntrezGeneSearcher,
					 dictionaryChunkerTT,
					 mModelDir,
					 mGenomicsThreshold);

	    int tot = 0;
	    for (MedlineCitation citation : mMedlineSearcher) {
		if (mLogger.isDebugEnabled())
		    mLogger.debug((++tot)+". citation: "+citation.pmid());
		if (citation.article() == null 
		    || citation.article().articleTitleText() == null) { 
		    if (mLogger.isDebugEnabled())
			mLogger.debug("skip - no text");
		    continue;
		}
		StringBuffer textBuf = new StringBuffer();
		textBuf.append(citation.article().articleTitleText());
		textBuf.append(Strings.NEWLINE_CHAR);
		if (citation.article().abstrct() != null) {
		    textBuf.append(citation.article().abstrct().textWithoutTruncationMarker());
		    textBuf.append(Strings.NEWLINE_CHAR);
		}
		String text = textBuf.toString();
		if (mLogger.isDebugEnabled())
		    mLogger.debug("lingblast text:\n"+text);
		Pair<Double,Chunking> lingblastCitation = 
		    lb.lingblast(text);
		recordChunking(citation.pmid(),lingblastCitation);
	    }
	    closeSqlFiles();
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

    private void openSqlFiles(File sqlDir) throws IOException {
	File articleScoreFile = 
	    new File(sqlDir,Constants.ARTICLE_SCORE_SQL);
	File geneArticleScoreFile = 
	    new File(sqlDir,Constants.GENE_ARTICLE_SCORE_SQL);
	File geneArticleMentionFile = 
	    new File(sqlDir,Constants.GENE_ARTICLE_MENTION_SQL);

	mArticleScoreOut = 
	    new BufferedOutputStream(new FileOutputStream(articleScoreFile));
	mGeneArticleScoreOut = 
	    new BufferedOutputStream(new FileOutputStream(geneArticleScoreFile));
	mGeneArticleMentionOut = 
	    new BufferedOutputStream(new FileOutputStream(geneArticleMentionFile));

	String articleScoreHeader = 
	    "PMID\tSCORE\n";
	mArticleScoreOut.write(articleScoreHeader.getBytes(),
			       0,articleScoreHeader.length());

	String geneArticleScoreHeader = 
	    "GENEID\tPMID\tGENE SCORE\tTOTAL SCORE\n";
	mGeneArticleScoreOut.write(geneArticleScoreHeader.getBytes(),
				   0,geneArticleScoreHeader.length());

	String geneArticleMentionHeader = 
	    "GENEID\tPMID\tTEXT\tOFFSET\n";
	mGeneArticleMentionOut.write(geneArticleMentionHeader.getBytes(),
				     0,geneArticleMentionHeader.length());
    }

    private void closeSqlFiles() throws IOException {
	mArticleScoreOut.flush();
	mArticleScoreOut.close();
	mGeneArticleScoreOut.flush();
	mGeneArticleScoreOut.close();
	mGeneArticleMentionOut.flush();
	mGeneArticleMentionOut.close();
    }
    
    private void recordChunking(String pmid, Pair<Double,Chunking> scoredCitation) throws IOException {
	NumberFormat formatter = new DecimalFormat("#.######");
	double genomicsScore = scoredCitation.a();
	String articleScoreRecord = 
	    pmid+"\t"
	    +formatter.format(genomicsScore)
	    +"\n";
	mArticleScoreOut.write(articleScoreRecord.getBytes(),
			       0,articleScoreRecord.length());
	if (scoredCitation.b() == null) return;

	Chunking chunking = scoredCitation.b();
	Set<String> genes = new HashSet<String>();
	String text = chunking.charSequence().toString();
	for (Chunk chunk : chunking.chunkSet()) {
	    int start = chunk.start();
	    int end = chunk.end();
	    String phrase = text.substring(start,end);
	    String geneId = chunk.type();
	    double score = chunk.score();
	    if (mLogger.isDebugEnabled())
		mLogger.debug("     phrase=|" + phrase + "|"
			      + " start=" + start
			      + " geneId=" + geneId
			      + " perGeneScore=" + score
			      + " genomicsGeneScore=" + genomicsScore
			      );
	    if (!genes.contains(geneId))  {
		genes.add(geneId);
		double total = genomicsScore + score;
		String geneArticleScoreRecord = 
		    geneId+"\t"
		    +pmid+"\t"
		    +formatter.format(score)+"\t"
		    +formatter.format(total)+"\n";
		mGeneArticleScoreOut.write(geneArticleScoreRecord.getBytes(),
					   0,geneArticleScoreRecord.length());
	    }
	    String geneArticleMentionRecord = 
		geneId + "\t"
		+ pmid + "\t"
		+ quoteForMysql(phrase) + "\t"
		+ start + "\n";
	    mGeneArticleMentionOut.write(geneArticleMentionRecord.getBytes(),
				       0,geneArticleMentionRecord.length());
	}
    }

    private String quoteForMysql(String elt) {
	StringBuffer result = new StringBuffer();
	//	result.append('"');
        for (int i = 0; i < elt.length(); ++i) {
            char c = elt.charAt(i);
            if (c == '\n') {
                result.append(' ');
		continue;
	    }
            if (c == '\'')
                result.append('\\');
            result.append(c);
        }
	//        result.append('"');
	return result.toString();
    }

    public static void main(String[] args) throws Exception {
        LingBlastMedline blast = new LingBlastMedline(args);
	blast.run();
    }
}
