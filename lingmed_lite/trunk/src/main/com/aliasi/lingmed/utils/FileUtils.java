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

package com.aliasi.lingmed.utils;

import java.io.File;
import java.io.IOException;
import java.io.IOException;

/**
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */
public class FileUtils {

    /**
     * Check existence, permissions on index directory.
     * Index must be directory, with read and write permission.
     * Creates index dir, if doesn't exist.
     *
     * @param dirName directory pathname
     * @throws IOException 
     */
    public static File checkIndex(String dirName) throws IOException {
	return checkIndex(dirName, true);
    }

    /**
     * Check existence, permissions on index directory.
     *
     * @param dirName directory pathname
     * @param createIfNotExists whether or not to create new directory for pathname
     * @throws IOException
     */
    public static File checkIndex(String dirName, boolean createIfNotExists) throws IOException {
	File dir = new File(dirName);
	if (!dir.exists() && !createIfNotExists) {
	    String msg = "Error, no such index: " + dir.getAbsolutePath();
	    throw new IOException(msg);
	}
	if (!dir.exists()) {
	    dir.mkdirs();
	    return checkIndex(dirName, false);
	} else {
	    if (!dir.isDirectory()) {
		String msg = "Error, not a directory: " + dir.getAbsolutePath();
		throw new IOException(msg);
	    }
	    if (!dir.canRead()) {
		String msg = "Error, cannot read index file: " + dir.getAbsolutePath();
		throw new IOException(msg);
	    }
	    if (!dir.canWrite()) {
		String msg = "Error, cannot write to index file: " + dir.getAbsolutePath();
		throw new IOException(msg);
	    }
	}
	return dir;
    }

    /**
     * Check existence, permissions on input file.
     *
     * @param name file name
     * @throws IOException
     */
    public static File checkInputFile(String name) {
	File file = new File(name);
        if (!(file.exists() && file.isFile() && file.canRead())) {
	    String msg = "File missing or incorrect: " + name;
	    throw new IllegalArgumentException(msg);
        }
	return file;
    }

    /**
     * Check existence, permissions on input file.
     *
     * @param file input file
     * @throws IOException
     */
    public static boolean checkInputFile(File file) {
        if (!(file.exists() && file.isFile() && file.canRead())) {
	    return false;
        }
	return true;
    }

    /**
     * Check existence, permissions on output file.
     * Create if not exists
     *
     * @param name file name
     * @throws IOException
     */
    public static File checkOutputFile(String name) throws IOException {
	File file = new File(name);
	if (!file.exists()) file.createNewFile();
	if (!(file.isFile() && file.canWrite())) {
	    String msg = "File missing or incorrect: " + name;
	    throw new IllegalArgumentException(msg);
        }
	return file;
    }

    /**
     * Check if dir exists
     *
     * @param name directory name
     * @throws IOException
     */
    public static File checkDir(String name) throws IOException {
	File file = new File(name);
        if (!(file.exists() && file.isDirectory())) {
	    String msg = "No such directory: " + name;
	    throw new IllegalArgumentException(msg);
        }
	return file;
    }

    /**
     * Check that existing file is directory, create dir if not exists.
     *
     * @param dir directory
     * @throws IOException
     */
    public static void ensureDirExists(File dir) throws IOException {
        if (dir.isDirectory()) return;
        if (dir.exists()) {
            String msg = "Existing file must be directory."
                + " Found file=" + dir;
	    throw new IOException(msg);
        }
        dir.mkdirs();  
    }



    private FileUtils() {}
}
