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

import com.aliasi.medline.*;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.Parser;
import com.aliasi.util.Files;

import java.io.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

/** 
 * Split Medline citation set into files 1 per MedlineCitation
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class SplitMedlineXml {

    public static void main(String[] args) throws Exception {
	if (args.length < 2) {
	    System.out.println("usage: SplitMedlineXml <file> <outputDir>");
	    System.exit(-1);
	}
	String inFileName = args[0];
	File inFile = FileUtils.checkInputFile(inFileName);
	File outDir = new File(args[1]);
	FileUtils.ensureDirExists(outDir);

	Logger.getLogger(SplitMedlineXml.class).info("writing output to: "+outDir.getCanonicalPath());
	MedlineParser parser = new MedlineParser(true);
	SimpleHandler handler = new SimpleHandler(outDir);
	parser.setHandler(handler);
	InputSource inSource = new InputSource(inFileName);
	parser.parse(inSource);

    }

    static class SimpleHandler implements MedlineHandler {
	final File mOutDir;

        public SimpleHandler(File outDir) {
	    mOutDir = outDir;
        }

        public void handle(MedlineCitation citation) {
	    System.out.println("processing citation: "+citation.pmid());
	    String xml = citation.xmlString();
	    String fileName = citation.pmid()+".xml";
	    try {
		PrintStream out = new PrintStream(new FileOutputStream(new File(mOutDir,fileName)));
		out.println(xml);
		out.close();
	    } catch (IOException ioe) {
		System.out.println(ioe.getMessage());
		ioe.printStackTrace(System.out);
	    }
	}

	public void delete(String pmid) {
	    System.out.println("delete request: "+pmid);
	}

    }

}
