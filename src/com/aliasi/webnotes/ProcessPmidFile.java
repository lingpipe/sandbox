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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintStream;


import java.util.HashSet;
import java.util.Properties;

import javax.naming.InitialContext;


import java.util.Iterator;

/* quick and dirty program to create load file for db
   load file of EntrezGene and Homologene ids. */

public class ProcessPmidFile {

    static private final String DB_USERNAME = "wikiuser";
    static private final String DB_PASSWORD = "hello";


    public static void main(String[] args) throws Exception {

	if (args.length < 2) {
	    System.out.println("usage: ProcessPmidFile <username> <file>");
	    System.exit(-1);
	}

	InitialContext context = Utils.getMysqlContext("jdbc:mysql://localhost:3306/annotation_wiki",
						 "annotation_wiki",
						 DB_USERNAME,
						 DB_PASSWORD);

	WikiDao dao = WikiDaoImpl.getInstance(context,"jdbc/mysql",DB_USERNAME,DB_PASSWORD);
	String userName = args[0];
	UserRec user = dao.getUserByName(userName);

	String outFileName = user.name()+"_annotations.sql";
	PrintStream out = new PrintStream(new FileOutputStream(new File(outFileName)));
	File pmidFile = new File(args[1]);
	LineNumberReader in = new LineNumberReader(new FileReader(pmidFile));
	String line = null;
	while ((line = in.readLine()) != null) {
	    String[] tokens = line.split("[):,]");
	    if (tokens.length < 2) {
		System.out.println("error at line "+in.getLineNumber()+" bad data: "+line);
		System.exit(-1);
	    }
	    int pmid = 0;
	    try {
		pmid = Integer.parseInt(tokens[1]);
	    } catch (NumberFormatException nfe) {
		System.out.println("error at line "+in.getLineNumber()+" bad data: "+line);
		System.exit(-1);
	    }
	    ArticleRec article = dao.getArticleByPmid(pmid);
	    int ctEntities = tokens.length-2;
	    out.print("INSERT INTO user_article_count(user_id,article_id,ct_entities) ");
	    out.println(" VALUES ("+user.id()+","+article.id()+","+ctEntities+");");

	    EntityRec[] entities = new EntityRec[ctEntities];
	    for (int i=2; i<tokens.length; i++) {
		EntityRec entity = null;
		if (tokens[i].startsWith("h")) { 
		    int idx = tokens[i].indexOf("_");
		    String entityId = tokens[i].substring(idx+1);
		    entity = dao.getEntityByOtherIds("hg",entityId);
		} else {
		    entity = dao.getEntityByOtherIds("eg",tokens[i]);
		}
		if (entity != null) {
		    out.print("INSERT INTO annotation(article_id,entity_id,user_id) ");
		    out.println(" VALUES ("+article.id()+","+entity.id()+","+user.id()+");");
		} else {
		    System.out.println("error at line "+in.getLineNumber()+" bad data: "+line);
		    System.exit(-1);
		}
	    }
	}
	in.close();
	out.close();
    }
}