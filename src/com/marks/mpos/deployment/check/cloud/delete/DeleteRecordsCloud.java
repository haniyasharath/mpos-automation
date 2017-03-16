package com.marks.mpos.deployment.check.cloud.delete;

import java.sql.Connection;
import java.sql.SQLException;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.marks.mpos.deployment.check.connection.ConnectionProvider;

public class DeleteRecordsCloud {
	public static void  deleteRecordsCloud(String interval){
	
		Connection connectionCloud = null;
		Session sshSession = null;
		try {

			StringBuilder queryTransaction = new StringBuilder("DELETE FROM transaction_details WHERE run_date < (NOW() - INTERVAL"+ interval +" DAY)");
			StringBuilder queryPriceEvent= new StringBuilder("DELETE FROM missing_price_event WHERE run_date < (NOW() - INTERVAL"+ interval +" DAY)");
			StringBuilder queryQStoreEvent= new StringBuilder("DELETE FROM store_missing_price_event WHERE run_date < (NOW() - INTERVAL"+ interval +" DAY)");

			sshSession = ConnectionProvider.openSSHSessionToCloud();
			connectionCloud = ConnectionProvider.connectToCloud();
			String queryTransactionDel = queryTransaction.toString();
			connectionCloud.createStatement().executeUpdate(queryTransactionDel);
			String queryPriceEventDel = queryPriceEvent.toString();
			connectionCloud.createStatement().executeUpdate(queryPriceEventDel);
			String queryQStoreEventDel = queryQStoreEvent.toString();
			connectionCloud.createStatement().executeUpdate(queryQStoreEventDel);
			System.out.println("delete completed");
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connectionCloud != null) {
				try {
					connectionCloud.close();
				} catch (SQLException e) {
//					LOG.warning(e.getMessage());
					e.printStackTrace();
				}
			}
			if(sshSession != null) {
				sshSession.disconnect();
			}
		}
}
}
