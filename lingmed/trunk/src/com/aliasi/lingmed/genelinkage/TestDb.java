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
import com.aliasi.lingmed.utils.FileUtils;
import com.aliasi.util.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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

import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;



public class TestDb {

    static private final String DB_USERNAME = "root";
    static private final String DB_PASSWORD = "admin";

    public static void main(String[] args) throws Exception {

	if (args.length < 1) {
	    System.out.println("usage: TestDb <pmid_file>");
	    System.exit(-1);
	}

	InitialContext context = getMysqlContext("jdbc:mysql://192.168.1.98:3306/gene_linkage",
						 "gene_linkage",
						 DB_USERNAME,
						 DB_PASSWORD);
	GeneLinkageDao dao = GeneLinkageDaoImpl.getInstance(context,"jdbc/mysql",DB_USERNAME,DB_PASSWORD);
	File pmidFile = FileUtils.checkInputFile(args[0]);
	System.out.println("Find gene mendions for articles");
	LineNumberReader in = new LineNumberReader(new FileReader(pmidFile));
	Set<String> geneIds = new HashSet<String>();
	String line = null;
	while ((line = in.readLine()) != null) {
	    System.out.println("pmid: "+line);
	    try {
		int pmid = Integer.parseInt(line);
		findGeneMentions(pmid,geneIds,dao);
	    }
	    catch (NumberFormatException nfe) {
		System.out.println("line: "+line+", not a number");
		continue;
	    }
	}
	in.close();
	/*
	System.out.println("\nFind article mentions for genes");
	for (String gene: geneIds) {
	    try {
		int geneId = Integer.parseInt(gene);
		findArticleMentions(geneId,dao);
	    }
	    catch (NumberFormatException nfe) {
		System.out.println("gene: "+gene+", not a number");
		continue;
	    }
	}
	*/
    }

    public static void findArticleMentions(int geneId,
					   GeneLinkageDao dao) 
	throws SQLException {
	Map<String,Pair<Double,Set<Chunk>>> articleMentions =
	    dao.getArticleMentionsForGeneId(geneId);
	System.out.println("articles for gene: "+articleMentions.size());
	for (Iterator it=articleMentions.entrySet().iterator(); it.hasNext(); ) {
	    Entry<String,Pair<Double,Set<Chunk>>> entry = 
		(Entry<String,Pair<Double,Set<Chunk>>>)it.next();
	    String articleId = entry.getKey();
	    Pair<Double,Set<Chunk>> geneMentions = entry.getValue();
	    System.out.println("article: "+articleId+"\tnum mentions: "+geneMentions.b().size());
	}
    }


    public static void findGeneMentions(int pmid,
					Set<String> geneIds,
					GeneLinkageDao dao) 
	throws SQLException {
	Pair<Double,Set<Chunk>> geneMentions = 
	    dao.getGeneMentionsForPubmedId(pmid);
	System.out.println("genomics score: "+geneMentions.a());
	if (geneMentions.b() == null) {
	    System.out.println("no gene mendtions found for pmid: "+pmid);
	    return;
	}
	Set<Chunk> chunkSet = geneMentions.b();
	for (Chunk chunk : chunkSet) {
	    System.out.println("geneId: "+chunk.type()
			       + "\tscore: "+chunk.score()
			       + "\tstart: "+chunk.start()
			       + "\tend: "+chunk.end());
	    geneIds.add(chunk.type());
	}
    }

    public static InitialContext getMysqlContext(String url, String databaseName, String username, String password) 
	throws NamingException {
	InitialContext ic = new InitialContext();
	// Construct Jndi object reference:  arg1:  classname, arg2: factory name, arg3:URL (can be null)
	Reference ref = new Reference("com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource",
				      "com.mysql.jdbc.jdbc2.optional.MysqlDataSourceFactory", null);
	ref.add(new StringRefAddr("driverClassName","com.mysql.jdbc.Driver"));
	ref.add(new StringRefAddr("url", url));
	ref.add(new StringRefAddr("databaseName",databaseName));
	ref.add(new StringRefAddr("username", username));
	ref.add(new StringRefAddr("password", password));
	ic.rebind("jdbc/mysql", ref);
	return ic;
    }
   

}
