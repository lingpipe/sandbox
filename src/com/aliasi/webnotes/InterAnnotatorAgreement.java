package com.aliasi.webnotes;

import com.aliasi.classify.ConfusionMatrix;

import javax.naming.InitialContext;

public class InterAnnotatorAgreement {

    static private final String DB_USERNAME = "wikiuser";
    static private final String DB_PASSWORD = "hello";

    public static void main(String[] args) throws Exception {

	InitialContext context = Utils.getMysqlContext("jdbc:mysql://localhost:3306/annotation_wiki",
						 "annotation_wiki",
						 DB_USERNAME,
						 DB_PASSWORD);

	WikiDao dao = WikiDaoImpl.getInstance(context,"jdbc/mysql",DB_USERNAME,DB_PASSWORD);

	UserRec user1 = dao.getUserByName(args[0]);
	UserRec user2 = dao.getUserByName(args[1]);
	ConfusionMatrix cm = dao.interAnnotatorAgreement(user1.id(),user2.id());
	System.out.println("interannotator agreement between  user1: "+user1.name()+" user2: "+user2.name());
	System.out.println(cm);
    }

}