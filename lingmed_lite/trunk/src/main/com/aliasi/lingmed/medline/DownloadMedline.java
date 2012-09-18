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

import com.aliasi.lingmed.utils.FileUtils;
//import com.aliasi.lingmed.utils.Logging;

import com.aliasi.lingpipe.io.FileExtensionFilter;

import com.aliasi.lingpipe.util.AbstractCommand;
import com.aliasi.lingpipe.util.Files;
import com.aliasi.lingpipe.util.Streams;
import com.aliasi.lingpipe.util.Strings;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.SocketException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import java.util.zip.GZIPInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

// import org.apache.log4j.Logger;

/**
 * <P>The <code>DownloadMedline</code> command downloads
 * of files of XML-formatted MEDLINE citations from NLM's FTP server. 
 * It is used to create and maintain a local repository of medline
 * citations whose structure and contents mirror that of 
 * the NLM repository.  
 *
 * <P>In order to run, there must be network connectivity and
 * adequate disk space.  Note that
 * the completed download of gzip compressed MEDLINE 2008 baseline 
 * requires on the order of 7.8GB of free disk space.
 *
 * <P>The downloader program is designed to be always running.
 * Between download sessions it sleeps for a specified interval.
 * It will abort processing in the case that the local filesystem
 * that the repository is on is full.
 * Other errors encountered during a download session will cause
 * the session to terminate, and the program will sleep.
 * In this way, as new download files on the server become available
 * they will be automatically downloaded to the local repository.
 * Furthermore, if this program is stopped before all files in the
 * repository have been downloaded, on restart it will
 * download only those files which are missing, or which appear
 * to be incomplete or otherwise corrupted.
 *
 * <P>The Medline updates directory contain notes and statistics, 
 * which are not part of MEDLINE and not downloaded by this command.
 * Downloads are also available in zip format, though only the gzipped 
 * versions are downloaded.  This command deals with the varying textual 
 * format of the hexadecmial string-encoded checksums between the baseline 
 * and updates directories.
 *
 * <P>This command is intentionally single threaded so as not to
 * monopolize or stress the NLM data servers.  It is <i>not</i> safe
 * to start multiple copies of this command, either within the same
 * virtual machine or across processes; this will cause duplicate
 * downloads, potentially corrupt results and possibly lead to
 * deadlock depending on how file access is managed across processes.
 *
 * <P>The following arguments are all required:
 *
 * <dl>
 * <dt><code>-domain</code></dt>
 * <dd>Domain name from which to download the citations.  Disclosed
 * to licensees by NLM.
 * </dd>
 *
 * <dt><code>-path</code></dt>
 * <dd>Path on the domain from which to download the citations.  Disclosed
 * to licensees by NLM.
 * </dd>
 *
 * <dt><code>-user</code></dt>
 * <dd>User name assigned by NLM.
 * </dd>
 *
 * <dt><code>-password</code></dt>
 * <dd>Password assigned by NLM.
 * </dd>
 *
 * <dt><code>-repositoryPath</code></dt>
 * <dd>Name of NLM directory where the distribution files are found.
 * Either the path to the baseline or updates repository.
 * </dd>

 * <dt><code>-targetDir</code></dt>
 * <dd> Name of directory where distribution files are downloaded to.
 * If downloading from baseline repository, target should be local baseline directory,
 * and if downloading from updates repository, target should be local updates directory.
 * </dd>
 *
 * </dl>
 *
 * <P>The following arguments are optional:
 *
 * <dl>
 * <dt><code>-maxTries</code></dt> 
 * <dd>Maximum number of download attempts per session.
 * </dd>
 *
 * <dt><code>-sleep</code></dt>
 * <dd>Number of minutes to sleep between download sessions.
 *     If &lt; 1, program will exit after one session.
 * </dd>
 *
 * </dl>
 *
 * <P>For information on obtaining the MEDLINE corpus, which is
 * available free for research or commercial purposes, see:
 * <a href="http://www.nlm.nih.gov/pubs/factsheets/medline.html">MEDLINE Fact Sheet</a>.
 *
 * <P>For information on obtaining MEDLINE see:
 * <a href="http://www.nlm.nih.gov/databases/leased.html">Leasing MEDLINE</a>.
 *
 * <P>This command uses ftp functions from the
 *  <a href="http://commons.apache.org/net/">Apache commons.net</a> library,
 * and logging functions from the
 * <a href="http://logging.apache.org/log4j/index.html">Apache log4j</a> library.
 *
 * @author  Bob Carpenter, Mitzi Morris
 * @version 3.0
 * @since   LingPipe2.2
 */

public class DownloadMedline extends AbstractCommand {
    //    private final Logger mLogger
    //	= Logger.getLogger(DownloadMedline.class);

    private long mStartTime;
    private String mDomainName;
    private String mRepositoryPath;
    private String mUserName;
    private String mPassword;
    private File mTargetDir;
    private File mTargetMd5Dir;
    
    private int mMaxTries;
    private int mSleep;

    private FTPClient mFTPClient;

    private final static int SECOND = 1000;
    private final static int MINUTE = 60*SECOND;
    private static final int FIVE_MINUTES_IN_MS = 5 * 60 * 1000;

    private final static String DOMAIN_NAME_PARAM = "domain";
    private final static String USER_NAME_PARAM = "user";
    private final static String PASSWORD_PARAM = "password";
    private final static String REPOSITORY_PATH_PARAM = "repositoryPath";
    private final static String TARGET_DIR_PARAM = "targetDir";
    private final static String MAX_TRIES_PARAM = "maxTries";
    private final static String SLEEP_PARAM = "sleep";

    private final static Properties DEFAULT_PARAMS = new Properties();
    static {
        DEFAULT_PARAMS.setProperty(MAX_TRIES_PARAM,"1");
        DEFAULT_PARAMS.setProperty(SLEEP_PARAM,"60");
    }

    // Instantiate DownloadMedline object and 
    // initialize instance variables per command line args
    private DownloadMedline(String[] args) throws IOException {
        super(args,DEFAULT_PARAMS);
        mStartTime = System.currentTimeMillis();
        mDomainName = getExistingArgument(DOMAIN_NAME_PARAM);
        mUserName = getExistingArgument(USER_NAME_PARAM);
        mPassword = getExistingArgument(PASSWORD_PARAM);
        mMaxTries = getArgumentInt(MAX_TRIES_PARAM);
        mSleep = getArgumentInt(SLEEP_PARAM);
        mRepositoryPath = getArgument(REPOSITORY_PATH_PARAM);
        if (mRepositoryPath == null) {
            String msg = "Missing parameter "+REPOSITORY_PATH_PARAM;
            throw new IllegalArgumentException(msg);
        }
        mTargetDir = getArgumentDirectory(TARGET_DIR_PARAM);
        reportParameters();
    }

    /**
     * Run the command.  See class documentation above for details on
     * arguments and behavior.
     */
    public void run() {
        //	mLogger.info("start run");
        try {
	    FileUtils.ensureDirExists(mTargetDir);
            //	    mLogger.info("Writing to target directory=" + mTargetDir);
	    mTargetMd5Dir = new File(mTargetDir.getAbsolutePath()+File.separator+"md5");
	    FileUtils.ensureDirExists(mTargetMd5Dir);
            //	    mLogger.info("Writing checksum data to directory=" + mTargetMd5Dir);
	    Set<String> localMd5Names = filterByExtension(mTargetMd5Dir.list(),".gz.md5");

            //	    mLogger.info("Establishing FTP connection.");
            initializeFTPClient();
            //	    mLogger.info("Reading from FTP server path=" + mRepositoryPath);
	    setFTPPath(mRepositoryPath);
            //	    mLogger.info("Reading list of file names from server.");
	    String[] remoteFileNames = mFTPClient.listNames();
	    checkFtpCompletion("Read file names from server.");
	    Set<String> remoteMd5Names = filterByExtension(remoteFileNames,".gz.md5");
            //	    mLogger.info("Data files on NLM: "+remoteMd5Names.size());
            //	    mLogger.debug("Data files=" + java.util.Arrays.asList(remoteMd5Names));

	    boolean noErrors = false;
	    int thisTry = 0;
	    while (!noErrors && thisTry < mMaxTries) {
		if (thisTry > 0) {
                    //		    mLogger.debug("Pause before retry");
		    Thread.sleep(thisTry*3*MINUTE);
		}	
		thisTry++;
                //		mLogger.debug("Download attempt: "+thisTry);

		String[] newFiles = subtract(remoteMd5Names,localMd5Names);
                //		mLogger.info("Files to fetch: "+newFiles.length);
                //		mLogger.debug("Filenames: " + java.util.Arrays.asList(newFiles));

		noErrors = doDownloads(newFiles);
                //		mLogger.debug("Processed files, no errors: "+noErrors);
	    }
	    if (noErrors) {
                //		mLogger.info("MEDLINE DOWNLOAD COMPLETE.");
	    } else {
                //		mLogger.info("MEDLINE DOWNLOAD ENCOUNTERED ERRORS.");
	    }
	} catch (InterruptedException ie) {
	    // ignore InterruptedException
	} catch (IOException ioe) {
            //	    mLogger.warn("Unexpected IOException: "+ioe.getMessage());
            //	    mLogger.warn("stack trace: "+Logging.logStackTrace(ioe));
            //	    mLogger.warn("Last FTP reply: "+ftpLastReply());
            //	    mLogger.warn("Aborting this run");
	} finally {
	    if (mFTPClient != null && mFTPClient.isConnected()) 
		closeFTPClient();
	}
    }

    // process 1 file
    // 1. download checksum file from NLM (via FTP)
    // 2. check contents of checksum file
    // pause
    // 3. download corresponding data file from NLM
    // now checksum file is older than data file
    // 4. verify gz file checksum matches checksum in gz.md5 file
    // pause
    // 5. touch .gzmd5 - indicates data file is OK
    // now checksum file is younger than data file
    // 6. sanity check:  verify lastModified times
    private boolean doDownload(String fileName) throws InterruptedException {
	File gzmd5 = null;
	File gz = null;
	File target = null;
	try {
	    downloadFile(fileName,mTargetMd5Dir);
            //	    mLogger.info("Downloaded checksum file: " + fileName);
	    gzmd5 = new File(mTargetMd5Dir,fileName);
	    if (!checkChecksum(gzmd5)) {
                //		mLogger.warn("Bad checksum file: " + fileName);
		gzmd5.delete();
                //		mLogger.info("Deleted checksum file: " + fileName);
		return false;
	    }
            //	    mLogger.info("File contains a proper checksum");
	    Thread.sleep(SECOND);
	    String gzFileName = fileName;
            int idxDot = fileName.lastIndexOf(".");
            if (idxDot > 0) gzFileName = fileName.substring(0,idxDot);
	    downloadFile(gzFileName,mTargetMd5Dir);
            //	    mLogger.info("Downloaded file: " + gzFileName);
	    gz = new File(mTargetMd5Dir,gzFileName);
	    if (!verifyChecksum(gzmd5,gz)) {
                //		mLogger.warn("Expected checksum doesn't match actual checksum, file: " + fileName);
		gzmd5.delete();
                //		mLogger.info("Deleted checksum file: " + fileName);
		gz.delete();
                //		mLogger.info("Deleted data file: " + gzFileName);
		return false;
	    }
            //	    mLogger.info("Expected checksum matches actual checksum, file: " + fileName);
	    target = new File(mTargetDir,gzFileName);
	    if (!gz.renameTo(target)) {
                //		mLogger.warn("Cannot move data file to target dir: " + fileName);
		gzmd5.delete();
                //		mLogger.info("Deleted checksum file: " + fileName);
		gz.delete();
                //		mLogger.info("Deleted data file: " + gzFileName);
		target.delete();
		return false;
	    }
            //	    mLogger.info("Download successful, file: " + fileName);
	    return true;
	} catch (IOException ioe) {
	    if (gzmd5 != null) gzmd5.delete();
	    if (gz != null) gz.delete();
	    if (target != null) target.delete();
	    return false;
	}
    }

    // attempt to download all data files
    // latch keeps track of any failures - 
    // once set to false, it remains set at false
    private boolean doDownloads(String[] fileNames) 
	throws IOException, InterruptedException {
	boolean latch = true;
	for (int i=0; i<fileNames.length; i++) {
            //	    mLogger.debug("Download filename: " + fileNames[i]);
	    boolean result = doDownload(fileNames[i]);
	    if (result) { 
                //		mLogger.debug("Download successful");
	    } else {
                //		mLogger.debug("Download failed");
	    }	    
	    if (latch) { 
		latch = result; 
	    }		
	}
	return latch;
    }


    // /////// helper methods for ftp client

    private void checkFtpCompletion(String description) throws IOException {
	checkFtpCompletion(description,true);
    }

    private void checkFtpCompletion(String description, boolean exceptionIfNotComplete) 
	throws IOException {
	int replyCode = mFTPClient.getReplyCode();
        //	mLogger.debug("Ftp completion code: "+replyCode);
        if (FTPReply.isPositiveCompletion(replyCode)) return;
	if (exceptionIfNotComplete) {
	    raiseIOException(description);
	}
    }

    private void closeFTPClient() {
        try {
            mFTPClient.disconnect();
        } catch (IOException e) {
            //            mLogger.warn("IOException Closing FTP Client.");
        }
    }

    private String ftpLastReply() {
        String reply = mFTPClient.getReplyString();
        int replyCode = mFTPClient.getReplyCode();
        return "FTP server reply code=" + replyCode
            + "\tFTP reply=" + reply;
    }

    private void initializeFTPClient() throws IOException {
	mFTPClient = new FTPClient();
	mFTPClient.setDataTimeout(FIVE_MINUTES_IN_MS);
        //	mLogger.info("Connecting to NLM");
	mFTPClient.connect(mDomainName);
	checkFtpCompletion("Connecting to server");
        //	mLogger.info("Connected.");
        //	mLogger.info("Logging in.");
	mFTPClient.login(mUserName,mPassword);
	checkFtpCompletion("Login");
        //	mLogger.info("Logged in to NLM FTP Server.");
    }

    private void raiseIOException(String description) throws IOException {
	IOException ioe = new IOException(description+ " FTP failure: "+ftpLastReply());
	ioe.setStackTrace(Thread.currentThread().getStackTrace());
	throw ioe;
    }

    private void reconnectFTPClient() throws IOException {
        closeFTPClient();
        initializeFTPClient();
    }

    private void setFTPPath(String path) throws IOException {
        if (!mFTPClient.changeWorkingDirectory(path)) {
            raiseIOException("Server error changing directory to path=" + path);
        }
        if (!mFTPClient.setFileType(FTP.BINARY_FILE_TYPE)) {
            raiseIOException("Server error changing type to binary.");
        }
    }

    // throws IO exception on bad FTP completion code
    private void downloadFile(String fileName, File targetDir) throws IOException {
	boolean success = false;
        File targetFile = new File(targetDir,fileName);
        OutputStream out = null;
        BufferedOutputStream bufOut = null;
        try {
            out = new FileOutputStream(targetFile);
            bufOut = new BufferedOutputStream(out);
            mFTPClient.retrieveFile(fileName,bufOut);
	    // successful download results in reply code 226: "Transfer complete"
	    checkFtpCompletion("File download",true);
	    success = true;
	} catch (SocketException e) {
            //            mLogger.info("SocketException=" + e);
            //            mLogger.info("Reconnecting to server.");
            reconnectFTPClient();
            //            mLogger.info("Server reply from Retrieve file="+ fileName+": "+ftpLastReply());
        } finally {
            Streams.closeOutputStream(bufOut);
            Streams.closeOutputStream(out);
        }
	if (success) {
	    Date date = new Date();
	    targetFile.setLastModified(date.getTime());
	}
    }

    // //// methods for getting and computing checksums

    private boolean checkChecksum(File file) {
        if (!file.exists()) {
            return false;
        }
        if (!file.isFile()) {
            //            mLogger.info("Checksum file not ordinary file. File=" + file);
            return false;
        }
        try {
            getExpectedMD5String(file);
            return true;
        } catch (IOException e) {
            //            mLogger.info("Checksum file corrupt. File=" + file);
            //            mLogger.info("Exception=" + e);
            return false;
        }
    }

    private boolean checkDownload(File file) {
        if (!file.exists()) {
            return false;
        }
        if (!file.isFile()) {
            //            mLogger.info("Data file not ordinary file. File=" + file);
            return false;
        }
	return true;
    }

    private boolean verifyChecksum(File checksumFile, File testFile) throws IOException {
        if (!checksumFile.isFile()) {
            //            mLogger.info("Could not find checksum file=" + checksumFile);
            return false;
        }
        if (!testFile.isFile()) {
            //            mLogger.info("Could not find downloaded file=" + testFile);
            return false;
        }
        String expectedChecksum = getExpectedMD5String(checksumFile);
        String foundChecksum = getMD5HexString(testFile);
        boolean match = expectedChecksum.equals(foundChecksum);
        if (match) {
            //            mLogger.debug("OK checksum, file=" + testFile.getCanonicalPath());
	} else {
            //            mLogger.info("Expected checksum=" + expectedChecksum
            //			 + " found checksum=" + foundChecksum);
            return false;
        }
        return true;
    }

    private String getMD5HexString(File file) throws IOException {
        byte[] md5Bytes = getMD5Bytes(file);
        return Strings.bytesToHex(md5Bytes);
    }

    private byte[] getMD5Bytes(File file) throws IOException {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Couldn't find MD5 algorithm. Exception=" + e);
        }
        InputStream fileIn = null;
        byte[] buffer = new byte[1024];
        try {
            fileIn = new FileInputStream(file);
            int numRead;
            do {
                numRead = fileIn.read(buffer);
                if (numRead > 0) {
                    digest.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            return digest.digest();
        } finally {
            Streams.closeInputStream(fileIn);
        }
    }

    private String getExpectedMD5String(File file) throws IOException {
        char[] cs = Files.readCharsFromFile(file,"ISO8859-1");
        String s = new String(cs);
        if (s.indexOf(" = ") < 0) {
            String msg = "Bad checksum file (no '='). Found=" + s;
            throw new IOException(msg);
        }
        String checksumString = s.substring(s.indexOf(" = ")+3).trim();
        if (checksumString.length() != 32) {
            String msg = "Bad checksum length. Found=" + s;
            throw new IOException(msg);
        }
        return checksumString;
    }

    ///////// helper methods to sort, compare lists of names (of files)

    private String[] subtract(Set<String> setA, Set<String> setB) {
	Iterator<String> it = setB.iterator();
	while (it.hasNext()) {
	    setA.remove(it.next());
	}
	String[] result = new String[setA.size()];
	result = setA.toArray(result);
	StringComparator byName = new StringComparator();
	Arrays.sort(result,byName);
        //	mLogger.debug("result=" + java.util.Arrays.asList(result));
	return result;
    }

    private Set<String> filterByExtension(String[] fileNames, String extension) {
	Set<String> set = new HashSet<String>();
	for (int i=0; i< fileNames.length; i++) {
	    if (fileNames[i].endsWith(extension)) {
		set.add(fileNames[i]);
	    }
	}
	return set;
    }

    private Set<String> filterByExtensions(String[] fileNames, String[] extensions) {
	Set<String> set = new HashSet<String>();
	for (int i=0; i< fileNames.length; i++) {
	    for (int j=0; j<extensions.length; j++) {
		if (fileNames[i].endsWith(extensions[j])) {
		    set.add(fileNames[i]);
		    break;
		}
	    }
	}
	return set;
    }

    // ////////////// helper methods for command, param processing, reporting

    private void reportParameters() {
        /*
        mLogger.info("Downloading MEDLINE"
		     + "\n\tStart time=" + new Date(mStartTime)
		     + "\n\tUser name=" + mUserName
		     + "\n\tPassword=" + mPassword
		     + "\n\tDomain=" + mDomainName
		     + "\n\tRepository Path on Domain=" + mRepositoryPath
		     + "\n\tTarget Directory=" + getArgument(TARGET_DIR_PARAM)
		     + "\n\tMax tries=" + mMaxTries
		     + "\n\tSleep interval in minutes=" + mSleep
		     );
        */
    }

    private String elapsedTime() {
        return Strings.msToString(System.currentTimeMillis()-mStartTime);
    }

    private int sleepMins() { return mSleep; }


    /**
     * Main method to be called from the command-line.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) throws Exception {
        DownloadMedline downloader = new DownloadMedline(args);
	while (true) {
	    try {
                //		Logger.getLogger(DownloadMedline.class).info("Run downloader");
		downloader.run();
                //		Logger.getLogger(DownloadMedline.class).info("Run completed");
	    } catch (Exception e) {
		String msg = "Unexpected exception=" + e;
                //		Logger.getLogger(DownloadMedline.class).warn(msg);
		IllegalStateException e2 
		    = new IllegalStateException(msg);
		e2.setStackTrace(e.getStackTrace());
		throw e2;
	    }
	    if (downloader.sleepMins() < 1) break;
	    Thread.sleep(downloader.sleepMins()*MINUTE);
	}
    }

    static class StringComparator implements Comparator<String> {
	public int compare(String a, String b) {
	    return a.compareTo(b);
	}
    }

}
