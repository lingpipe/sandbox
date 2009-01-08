package com.aliasi.lingmed.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * Base implementation for {@link MysqlDao}.
 *
 * @author Mitzi Morris
 * @version 1.0
 * @since   LingMed1.0
 */

public class MysqlDaoImpl implements MysqlDao {
    private final Logger mLogger
	= Logger.getLogger(MysqlDaoImpl.class);

    private static MysqlDao sInstance;
    private static DataSource sDs;

    private static boolean isStandalone;
    private static String sUserName;
    private static String sPassword; 

    public static synchronized MysqlDao getInstance(Context context,String name) throws NamingException {
	if (sInstance == null) {
	    isStandalone = false;
	    sDs = (DataSource)context.lookup(name);
	    if ( sDs == null ) {
		throw new IllegalStateException("Data source not found!");
	    }
	    sInstance = new MysqlDaoImpl();
	}
	return sInstance;
    }

    public static synchronized MysqlDao getInstance(Context context,String name, 
						   String userName, String password) throws NamingException {
	if (sInstance == null) {
	    sUserName = userName;
	    sPassword = password;
	    isStandalone = true;
	    sDs = (DataSource)context.lookup(name);
	    if ( sDs == null ) {
		throw new IllegalStateException("Data source not found!");
	    }
	    sInstance = new MysqlDaoImpl();
	}
	return sInstance;
    }


    public Connection getConnection() throws SQLException {
	if (!isStandalone) {
	    return sDs.getConnection();
	}
	return sDs.getConnection(sUserName,sPassword);
    }

    public void closeConnection(Connection con) {
	if (con == null) return;
	try {
	    con.close();
	} catch (SQLException e) {
	    String msg = "Could not close connection.";
	    mLogger.error(msg,e);
	    // eat exception
	}
    }

    public void closePreparedStatement(PreparedStatement pstmt) {
	if (pstmt == null) return;
	try {
	    pstmt.close();
	} catch (SQLException e) {
	    String msg = "Could not close prepared statement.";
	    mLogger.error(msg,e);
	    // eat exception
	}
    }

    public void closeResultSet(ResultSet rs) {
	if (rs == null) return;
	try {
	    rs.close();
	} catch (SQLException e) {
	    String msg = "Could not close result set.";
	    mLogger.error(msg,e);
	    // eat exception
	}
    }

}
