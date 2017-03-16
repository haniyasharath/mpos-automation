package com.marks.mpos.deployment.check.connection;

import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.*;
import static com.marks.mpos.deployment.check.properties.UserProperties.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.marks.mpos.deployment.check.beans.CBODetails;
import com.marks.mpos.deployment.check.beans.DTEDetails;
import com.marks.mpos.deployment.check.beans.StoreDetails;
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;
import com.sun.media.sound.InvalidDataException;

public class ConnectionProvider {
	
	private static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public static Session openSSHSessionToLBO(StoreDetails storeDetails) throws JSchException {
		Session sshSession = null;
		final String SSH_STORE_HOST = storeDetails.getStoreHostName();
		final int LOCAL_PORT = Integer.parseInt(SSH_LOCAL_PORT_PREFIX + storeDetails.getStoreNumber());
		
		sshSession = new JSch().getSession(SSH_STORE_USERNAME, SSH_STORE_HOST , SSH_PORT);
		sshSession.setPassword(SSH_STORE_PASSWORD);
		sshSession.setConfig("StrictHostKeyChecking", "no");	
		sshSession.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
		System.out.println("Connecting to LBO for Store: " + storeDetails.getStoreNumber());
		sshSession.connect();
		sshSession.setPortForwardingL(LOCAL_PORT, MARIA_DB_HOST, MARIA_DB_PORT);		
		System.out.println("Fetching data from Store: " + storeDetails.getStoreNumber());
		System.out.println("------------------------------------------");
		return sshSession;
	}
	
	public static Session openSSHSessionToCloud() throws JSchException {
		final String SSH_STORE_HOST = "139.59.25.49";
		final int LOCAL_PORT = Integer.parseInt("7123");
		Session sshSession = new JSch().getSession("automation", SSH_STORE_HOST, SSH_PORT);
		sshSession.setPassword("automation");
		sshSession.setConfig("StrictHostKeyChecking", "no");
		sshSession.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
		System.out.println("Connecting to Cloud: ");
		sshSession.connect();
		sshSession.setPortForwardingL(LOCAL_PORT, MARIA_DB_HOST, MARIA_DB_PORT);
		System.out.println("PortForwarding done");
		
		return sshSession;
	}
	
	public static Connection connectToCloud() {
		Connection connection = null;
		final int LOCAL_PORT = Integer.parseInt("9123");
		try {
			final String LOCAL_DATABASE_URL = MARIA_DB_URL_PREFIX + LOCAL_PORT + SLASH_SYMBOL + "portal";

			Class.forName(MARIA_DB_DRIVER).newInstance();
			connection = DriverManager.getConnection(LOCAL_DATABASE_URL, "portal", "portal");
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException
				 | SQLException ex) {
			System.out.println("Exception while creating maria-db connection for the cloud");
			ex.printStackTrace();
			throw new IllegalStateException(ex.getMessage());
		}
		return connection;
	}

	public static Session openSSHSessionToDTE(DTEDetails dteDetails) throws JSchException {
		Session sshSession = null;
		final String DTE_HOST = dteDetails.getdTEHostName();
		
		sshSession = new JSch().getSession(DTE_USERNAME, DTE_HOST , SSH_PORT);
		sshSession.setPassword(DTE_PASSWORD);
		sshSession.setConfig("StrictHostKeyChecking", "no");	
		sshSession.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
		sshSession.connect();
		//sshSession.setPortForwardingL(DTE_PORT, MARIA_DB_HOST, MARIA_DB_PORT);		
		System.out.println("------------------------------------------");
		return sshSession;
	}
	
	public static Session openSSHSessionToCBO(CBODetails cboDetails) throws JSchException {
		Session sshSession = null;
		final String CBO_IP = cboDetails.getServerIP();
		
		sshSession = new JSch().getSession(DTE_USERNAME, CBO_IP , SSH_PORT);
		sshSession.setPassword(DTE_PASSWORD);
		sshSession.setConfig("StrictHostKeyChecking", "no");	
		sshSession.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
		sshSession.connect();
		//sshSession.setPortForwardingL(DTE_PORT, MARIA_DB_HOST, MARIA_DB_PORT);		
		System.out.println("Fetching Data from CBO");
		System.out.println("------------------------------------------");
		return sshSession;
	}	
	
	public static Connection getMariaDBConnection(StoreDetails storeDetails, String databaseName) throws SQLException {
		Connection connection = null;
		try {
			final int LOCAL_PORT = Integer.parseInt(SSH_LOCAL_PORT_PREFIX + storeDetails.getStoreNumber());
			final String LOCAL_DATABASE_URL = MARIA_DB_URL_PREFIX + LOCAL_PORT + SLASH_SYMBOL + databaseName;
			
			Class.forName(MARIA_DB_DRIVER).newInstance();
			connection = DriverManager.getConnection(LOCAL_DATABASE_URL, MARIA_DB_USERNAME, MARIA_DB_PASSWORD); 
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | CommunicationsException ex) {
			System.out.println("Exception while creating maria-db connection for the store -" + storeDetails.getStoreNumber());
			ex.printStackTrace();
			throw new IllegalStateException(ex.getMessage());
		} 
		return connection;
	}
	
	public static Connection getOracleDBConnection(String dbName) throws SQLException {
		Connection connection = null;
		String ORACLE_DATABASE_URL = "";
		try {
			Class.forName(ORACLE_DB_DRIVER).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		if (CSS_DB_NAME.equals(dbName)) {
			ORACLE_DATABASE_URL = ORACLE_DB_URL_PREFIX + ORACLE_DB_HOSTNAME + COLON_SYMBOL + CSS_ORACLE_PORT + SLASH_SYMBOL + CSS_ORACLE_SID;
			connection = DriverManager.getConnection(ORACLE_DATABASE_URL, CSS_ORACLE_USERNAME, CSS_ORACLE_PASSWORD); 
		} else if (CBO_DB_NAME.equals(dbName)) {
			ORACLE_DATABASE_URL = ORACLE_DB_URL_PREFIX + ORACLE_DB_HOSTNAME + COLON_SYMBOL + CBO_ORACLE_PORT + SLASH_SYMBOL + CBO_ORACLE_SID;
			connection = DriverManager.getConnection(ORACLE_DATABASE_URL, CBO_ORACLE_USERNAME, CBO_ORACLE_PASSWORD); 
		} else if (PPS_DB_NAME.equals(dbName)) {
			ORACLE_DATABASE_URL = ORACLE_DB_URL_PREFIX + ORACLE_DB_HOSTNAME + COLON_SYMBOL + PPS_ORACLE_PORT + SLASH_SYMBOL + PPS_ORACLE_SID;
			connection = DriverManager.getConnection(ORACLE_DATABASE_URL, PPS_ORACLE_USERNAME, PPS_ORACLE_PASSWORD); 
		}
		
		return connection;
	}
	
	public static Session getSshSessionToDte(String dteHost) {
		int DTE_ERROR_COUNT = 0;
		try {
			return ConnectionProvider.openSSHSessionToDTE(new DTEDetails(dteHost));
		} catch (JSchException e) {
			e.printStackTrace();
			try {
				return handleDteNodes(++DTE_ERROR_COUNT);
			} catch (InvalidDataException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	private static Session handleDteNodes(int dteNode) throws InvalidDataException {
		final Session sshSessionToDte;
		switch (dteNode) {
		case 1:
			sshSessionToDte = getSshSessionToDte(DTE_NODE_2);
			break;
		case 2:
			sshSessionToDte = getSshSessionToDte(DTE_NODE_3);
			break;
		case 3:
			sshSessionToDte = getSshSessionToDte(DTE_NODE_4);
			break;
		default:
			throw new InvalidDataException();
		}
		return sshSessionToDte;
	}
}
