package com.marks.mpos.deployment.check.test;

import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.MARIA_DB_NAME_BO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.marks.mpos.deployment.check.beans.StoreDetails;
import com.marks.mpos.deployment.check.connection.ConnectionProvider;
import static com.marks.mpos.deployment.check.properties.StoreProperties.*;

public class TestLBOConnection {

	public static void main(String[] args) {
		/*System.out.println("#######    Checking Connection for stores in PRE list    #######");
		checkLBOAndMariaDBConnectionForStores(preCheckStoreList);*/
		System.out.println("#######    Checking Connection for stores in POST list    #######");
		checkLBOAndMariaDBConnectionForStores(postCheckStoreList);
	}

	private static void checkLBOAndMariaDBConnectionForStores(List<StoreDetails> storeList) {
		for (StoreDetails storeDetails : storeList) {
			System.out.println("Store Number is " + storeDetails.getStoreNumber() + " and Host name is " + storeDetails.getStoreHostName());
			Session sshSession = null;
			Connection connection = null;
			try {
				sshSession = ConnectionProvider.openSSHSessionToLBO(storeDetails);
				connection = ConnectionProvider.getMariaDBConnection(storeDetails, MARIA_DB_NAME_BO);
				System.out.println("DB connection aquired " + connection);
			} catch (SQLException | JSchException ex) {
				ex.printStackTrace();
			} finally {
				if (connection != null) {
					try {
						connection.close();
						System.out.println("DB connection closed");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (sshSession != null && sshSession.isConnected()) {
					sshSession.disconnect();
					System.out.println("SSH session disconnected");
				}
			}
		}
	}

}
