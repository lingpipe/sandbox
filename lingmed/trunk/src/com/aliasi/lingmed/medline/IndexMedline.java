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

package com.aliasi.lingmed.medline;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;

import com.aliasi.io.FileExtensionFilter;

import com.aliasi.lingmed.lucene.Fields;
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.lingmed.utils.Logging;

import com.aliasi.medline.MedlineCitation;
import com.aliasi.medline.MedlineHandler;
import com.aliasi.medline.MedlineParser;

import com.aliasi.util.AbstractCommand;
import com.aliasi.util.Files;
import com.aliasi.util.Reflection;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import java.lang.reflect.Constructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.zip.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;

import org.apache.lucene.store.FSDirectory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

/** 
 * <P>The <code>IndexMedline</code> command processes a
 * set of files of XML-formatted MEDLINE citations and adds
 * them to a Lucene Index on the local filesystem.
 *
 * <P>There are 2 kinds of MEDLINE citations files: 
 * baseline and updates files.  
 * Baseline files contain unique citation entries.  
 * Updates files contain new citation entries and 
 * revisions of existing entries.  Updates files may also
 * contain instructions to delete existing entries.
 * A command-line argument is used to indicate whether or
 * not the files to process are baseline or updates files.
 *
 * <P>In order to maintain data integrity, we must process
 * all of the files in the baseline distribution before processing the updates,
 * and then the updates files should be processed 
 * in the order in which they are released.
 * Baseline files should only be processed once, 
 * else this will create duplicate entries in the index.
 * The distribution files from NLM are named using a naming convention
 * which reflects the chronological order of the files.
 * The program sorts the files by filename in order to process them
 * in the correct order.
 * 
 * <P>The indexer program is designed to be always running.
 * Between indexing attempts it sleeps for a specified interval.
 *
 * <P>The following arguments are all required:
 *
 * <dl>
 * <dt><code>-distType</code></dt>
 * <dd>If value is &quot;baseline&quot; then all citations are added
 * to the index, and deletions are not allowed.
 * Otherwise the files will be processed as updates files.
 * </dd>
 *
 * <dt><code>-index</code></dt>
 * <dd>Path to the Lucene index file.
 * </dd>
 *
 * <dt><code>-distDir</code></dt>
 * <dd>Path to the directory containing the distribution files.
 * All files in the directory which end in &quot;.xml&quot;
 * or &quot;.xml.gz&quot; will be processed.
 * </dd>
 *
 * <dt><code>-codec</code></dt>
 * <dd>The name of the class which is used to transform a MedlineCitation object
 * to a Lucene Document.  Must implement <code>com.aliasi.lingmed.dao.Codec</code>.
 * </dd>
 *
 * </dl>
 *
 * <P>The following arguments are optional:
 *
 * <dl>
 * <dt><code>-sleep</code></dt>
 * <dd>Number of minutes to sleep between indexing sessions.
 * </dd>
 * </dl>
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class IndexMedline extends AbstractCommand {

    private final Logger mLogger
        = Logger.getLogger(IndexMedline.class);

    private File mIndex;
    private File mDistDir;
    private int mSleep;
    private String mIndexName;
    private String mDistDirPath;
    private String mType;

    private final static double RAM_BUF_SIZE = 256d;

    private final static int SECOND = 1000;
    private final static int MINUTE = 60*SECOND;

    private final static String DIST_DIR = "distDir";
    private final static String DIST_TYPE = "distType";
    private final static String LUCENE_INDEX = "index";
    private final static String SLEEP_PARAM = "sleep";
    private final static String CODEC_PARAM = "codec";

    static boolean sIsBaseline;

    private final static Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(SLEEP_PARAM,"60");
    }

    private final MedlineCodec mCodec;

    // Instantiate IndexMedline object and 
    // initialize instance variables per command line args
    private IndexMedline(String[] args) throws Exception {
        super(args,DEFAULT_PARAMS);
        mIndexName = getExistingArgument(LUCENE_INDEX);
        mDistDirPath = getExistingArgument(DIST_DIR);
        mType = getExistingArgument(DIST_TYPE);
        mSleep = getArgumentInt(SLEEP_PARAM);
        reportParameters();
        if (mType.equalsIgnoreCase("baseline")) sIsBaseline = true;
        if (sIsBaseline) mIndex = FileUtils.checkIndex(mIndexName,true);
        else mIndex = FileUtils.checkIndex(mIndexName,false);
        mDistDir = new File(mDistDirPath);
        FileUtils.ensureDirExists(mDistDir);

        String codecClassName = getExistingArgument(CODEC_PARAM);
        Class clss = Class.forName(codecClassName);
        Constructor cons = clss.getConstructor();
        mCodec = (MedlineCodec) cons.newInstance();
        mLogger.info("Codec class=" + mCodec.getClass());
    }

    /**
     * Run the command.  See class documentation above for details on
     * arguments and behavior.
     */
    public void run() {
        mLogger.info("start run");
        try {
            File[] files = getLaterFiles(mIndex);
            mLogger.info("Total files to process: " + files.length);
            if (mLogger.isDebugEnabled())
                mLogger.debug("File names:" + java.util.Arrays.asList(files));
            if (files.length > 0) {
                MedlineParser parser = new MedlineParser(true); // true = save raw XML
                IndexWriter indexWriter = 
                    new IndexWriter(FSDirectory.getDirectory(mIndex),
                                    mCodec.getAnalyzer(),
                                    new IndexWriter.MaxFieldLength(IndexWriter.DEFAULT_MAX_FIELD_LENGTH));
				indexWriter.setRAMBufferSizeMB(RAM_BUF_SIZE);
                for (File file: files) {
                    mLogger.info("processing file:" + file);
                    MedlineIndexer indexer = new MedlineIndexer(indexWriter,mCodec);
                    parser.setHandler(indexer);
                    parseFile(parser,file);
                    indexer.close();
                    recordFile(indexWriter,file.getName());
                    mLogger.info("completed processing file:" + file);
                }
                mLogger.info("All files parsed, now optimize index");
                indexWriter.optimize();
                indexWriter.close();
            }
            mLogger.info("Processing complete.");
        } catch (Exception e) {
            mLogger.warn("Unexpected Exception: "+e.getMessage());
            mLogger.warn("stack trace: "+Logging.logStackTrace(e));
            mLogger.warn("Aborting this run");
            IllegalStateException e2 
                = new IllegalStateException(e.getMessage());
            e2.setStackTrace(e.getStackTrace());
            throw e2;
        }
    }

    static final String LOW_SORT_STRING = "";

    private String getLastUpdate(File index) throws IOException {
        IndexReader reader = null;
        IndexSearcher searcher = null;
		try {
			FSDirectory fsDirectory = FSDirectory.getDirectory(index);
			if (isNewDirectory(fsDirectory)) return LOW_SORT_STRING;
			reader = IndexReader.open(fsDirectory,true); // open reader read-only
			searcher = new IndexSearcher(reader);
			Term term = new Term(Fields.MEDLINE_DIST_FIELD,Fields.MEDLINE_DIST_VALUE);
			Sort sort = new Sort(Fields.MEDLINE_FILE_FIELD,true);
			Query query = new TermQuery(term);
			TopFieldDocs results = searcher.search(query,null,1,sort);
			if (results.totalHits == 0) {
				searcher.close();
				reader.close();
				return LOW_SORT_STRING;
			}
            if (mLogger.isDebugEnabled())
				mLogger.debug("num MEDLINE_FILE docs:" + results.totalHits);
			Document d = searcher.doc(results.scoreDocs[0].doc);
			return d.get(Fields.MEDLINE_FILE_FIELD);
		} finally {
			if (searcher != null) searcher.close();
			if (reader != null) reader.close();
		}
    }

    private File[] getLaterFiles(File index) throws IOException {
        String lastFileName = getLastUpdate(index);
        mLogger.debug("lastFileName: |"+lastFileName+"|");
        String[] extensions = {"xml", "gz"};
        FileFilter filter = new FileExtensionFilter(extensions,false);
        File[] files = mDistDir.listFiles(filter);
        Comparator<File> byName = new FileNameComparator();
        Arrays.sort(files,byName);
        int idx = 0;
        while (idx < files.length) {
            if (files[idx].getName().compareTo(lastFileName) > 0) {
                int len = files.length - idx;
                File[] result = new File[len];
                System.arraycopy(files,idx,result,0,len);
                return result;
            }
            idx++;
        }
        mLogger.debug("no update filenames > than lastFileName");
        return new File[0];
    }

    private boolean isNewDirectory(FSDirectory fsDir) {
        String[] contents = fsDir.list();
        if (contents.length == 0) return true;
        return false;
    }

    void parseFile(MedlineParser parser, File file) throws IOException, SAXException {
        if (Files.suffix(file.getName()).equals("xml")) {
            InputSource inSource = new InputSource(file.getAbsolutePath());
            parser.parse(inSource);
        } else if (Files.suffix(file.getName()).equals("gz")) {
            parseGZip(parser,file);
        } else { 
            String msg = "Unknown file extension. File=" + file.getName();
            throw new IllegalArgumentException(msg);
        }
    }

    static void parseGZip(MedlineParser parser, File file)
        throws IOException, SAXException {
        FileInputStream fileIn = null;
        GZIPInputStream gzipIn = null;
        InputStreamReader inReader = null;
        BufferedReader bufReader = null;
        InputSource inSource = null;
        try {
            fileIn = new FileInputStream(file);
            gzipIn = new GZIPInputStream(fileIn);
            inReader = new InputStreamReader(gzipIn,Strings.UTF8);
            bufReader = new BufferedReader(inReader);
            inSource = new InputSource(bufReader);
            inSource.setSystemId(Files.fileToURLName(file));
            parser.parse(inSource);
        } finally {
            Streams.closeReader(bufReader);
            Streams.closeReader(inReader);
            Streams.closeInputStream(gzipIn);
            Streams.closeInputStream(fileIn);
        }
    }

    private void recordFile(IndexWriter indexWriter, String fileName)
        throws IOException {
		if (mLogger.isDebugEnabled())
			mLogger.debug("record file:" + fileName);
        Document doc = new Document(); 
        Field tagField = new Field(Fields.MEDLINE_DIST_FIELD,
                                   Fields.MEDLINE_DIST_VALUE,
                                   Field.Store.YES,
                                   Field.Index.NOT_ANALYZED_NO_NORMS);
        doc.add(tagField);
        Field nameField = new Field(Fields.MEDLINE_FILE_FIELD,
                                    fileName,
                                    Field.Store.YES,
                                    Field.Index.NOT_ANALYZED_NO_NORMS);
        doc.add(nameField);
        indexWriter.addDocument(doc);
		if (mLogger.isDebugEnabled())
			mLogger.debug("added doc: " + doc.toString());

    }

    private void reportParameters() {
        mLogger.info("Indexing MEDLINE "
                     + "\n\tStart time=" + Strings.msToString(startTimeMillis())
                     + "\n\tIndex=" + mIndexName
                     + "\n\tMedline directory=" + mDistDirPath
                     + "\n\tDist type (baseline or updates)=" + mType
                     + "\n\tSleep interval in minutes=" + mSleep
                     );
    }

    private int sleepMins() { return mSleep; }

    /**
     * Main method to be called from the command-line.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) throws Exception {
        IndexMedline indexer = new IndexMedline(args);
        while (true) {
            try {
                indexer.run();
            } catch (Exception e) {
                String msg = "Unexpected exception=" + e;
                Logger.getLogger(IndexMedline.class).warn(msg);
                IllegalStateException e2 
                    = new IllegalStateException(msg);
                e2.setStackTrace(e.getStackTrace());
                throw e2;
            }
            if (sIsBaseline) break;
            if (indexer.sleepMins() < 1) break;
            Thread.sleep(indexer.sleepMins()*MINUTE);
        }
    }

    static class MedlineIndexer implements MedlineHandler {
        private final Logger mLogger
            = Logger.getLogger(MedlineIndexer.class);
        final IndexWriter mIndexWriter;
        final MedlineCodec mMedlineCodec;
        final IndexReader mReader;
        final IndexSearcher mSearcher;

        public MedlineIndexer(IndexWriter indexWriter, MedlineCodec codec) 
            throws IOException {
            mIndexWriter = indexWriter;
            mMedlineCodec = codec;
            mReader = IndexReader.open(indexWriter.getDirectory(),true); // open reader read-only
            mSearcher = new IndexSearcher(mReader);
        }

        public void handle(MedlineCitation citation) {
            Document doc = mMedlineCodec.toDocument(citation);
            try {
                if (sIsBaseline) {
                    mIndexWriter.addDocument(doc);  
                } else {
                    Term idTerm = new Term(Fields.ID_FIELD,citation.pmid());
                    if (mSearcher.docFreq(idTerm) > 0) {
                        mLogger.debug("revise existing citation: "+citation.pmid());
                        mIndexWriter.updateDocument(idTerm,doc);
                    } else {
                        mLogger.debug("add new citation: "+citation.pmid());
                        mIndexWriter.addDocument(doc);  
                    }
                }
            } catch (IOException e) {
                mLogger.warn("handle citation: index access error, term: "+citation.pmid());
            }
        }

        public void delete(String pmid) {
            if (sIsBaseline) {
                String msg = "Cannot handle deleteions.";
                throw new UnsupportedOperationException(msg);
            }
            Term idTerm = new Term(Fields.ID_FIELD,pmid);
            mLogger.debug("delete citation: "+pmid);
            try {
                mIndexWriter.deleteDocuments(idTerm);
            } catch (IOException e) {
                mLogger.warn("delete citation: index access error, term: "+pmid);
            }
        }

        public void close() throws IOException { 
            mSearcher.close();
            mReader.close();
            mIndexWriter.commit();
        }
    }

    static class FileNameComparator implements Comparator<File> {
        public int compare(File filea, File fileb) {
            return filea.getName().compareTo(fileb.getName());
        }
    }

}