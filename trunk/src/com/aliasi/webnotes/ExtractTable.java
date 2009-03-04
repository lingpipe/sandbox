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

package com.aliasi.webnotes;

import com.aliasi.io.FileExtensionFilter;
import com.aliasi.util.Files;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/* quick and dirty program to strip out head and tail from .html files */

public class ExtractTable {

    public static void main(String[] args) throws Exception {

	if (args.length < 2) {
	    System.out.println("usage: ExtractTable <inDir> <outDir>");
	    System.exit(-1);
	}
	File inDir = new File(args[0]);
	File[] files = getHtmlFiles(inDir);

	String outDirName = args[1];
	File outDir = new File(outDirName);
	if (!outDir.exists()) {
	    boolean success = (new File(outDirName)).mkdirs();
	    if (!success) {
		System.out.println("ERROR: cannot make output directory: "+outDirName);
		System.exit(-1);
	    }
	}
	if (!outDir.canWrite()) {
		System.out.println("ERROR: can't write to output directory: "+outDirName);
		System.exit(-1);
	}

	for (File file : files) {
	    String[] lines = Files.readLinesFromFile(file,Strings.UTF8);
	    Pattern pat = Pattern.compile(".?Next</a>(<table>.*</table>)<a href=\".*\">Prev</a> <a href=\".*\">Next</a></body></html>");
	    Matcher match = pat.matcher(lines[lines.length-1]);

	    if (match.find()) {
		String table = match.group(1);
		System.out.println("file: "+file.getName()+"\ttable: "+table.length());

		File outFile = new File(outDir,file.getName());
		Files.writeStringToFile(table,outFile);
	    } else {
		System.out.println("match failed for file: "+file.getName());
	    }		
	}

    }

    static File[] getHtmlFiles(File dir) throws IOException {
	if (dir.isDirectory()) {
	    String[] extensions = {"html" };
	    FileFilter filter = new FileExtensionFilter(extensions,false);
	    File[] files = dir.listFiles(filter);
	    return files;
	} else {
		String msg = dir+": not a directory.";
		throw new IOException(msg);
	}
    }
}