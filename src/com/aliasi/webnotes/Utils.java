package com.aliasi.webnotes;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

public class Utils {
    private Utils() {}

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
