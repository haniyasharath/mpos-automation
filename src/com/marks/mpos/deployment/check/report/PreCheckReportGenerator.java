package com.marks.mpos.deployment.check.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.marks.mpos.deployment.check.beans.DBObjectCheck;
import com.marks.mpos.deployment.check.beans.LBOCheck;
import com.marks.mpos.deployment.check.beans.MasterDataBO;
import com.marks.mpos.deployment.check.beans.MasterDataPE;
import com.marks.mpos.deployment.check.beans.StoreDetails;
import com.marks.mpos.deployment.check.connection.ConnectionProvider;
import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.*;

import static com.marks.mpos.deployment.check.properties.MasterDataProperties.*;
import static com.marks.mpos.deployment.check.properties.StoreProperties.*;
import com.opencsv.CSVWriter;
import com.opencsv.bean.BeanToCsv;
import com.opencsv.bean.ColumnPositionMappingStrategy;

import static com.marks.mpos.deployment.check.properties.UserProperties.*;

import static com.marks.mpos.deployment.check.queries.IDatabaseQueries.*;

public class PreCheckReportGenerator {

	public static void generatePreCheckReports() {
		final String FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator + PRE_CHECK_DIR + File.separator + PRE_CHECK_DATE;
		new File(FILE_PATH + File.separator + USER_DIR).mkdirs();
		new File(FILE_PATH + File.separator + REGISTER_DIR).mkdirs();
		DBObjectCheck dbObjCheck = new DBObjectCheck();
		Connection connectionCBO = null;
		try {
			connectionCBO = ConnectionProvider.getOracleDBConnection(CBO_DB_NAME);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			ResultSet rsAllObjCBO = connectionCBO.createStatement().executeQuery(CBO_ALL_OBJECTS_QUERY);
			while (rsAllObjCBO.next()) dbObjCheck.setCboAllObjects(rsAllObjCBO.getString(1));
			ResultSet rsAllTblCBO = connectionCBO.createStatement().executeQuery(CBO_ALL_TABLES_QUERY);
			while (rsAllTblCBO.next()) dbObjCheck.setCboAllTables(rsAllTblCBO.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			CSVWriter csvWriterTax = new CSVWriter(new FileWriter(FILE_PATH + File.separator + TAX_REGISTER_FILENAME + CSV_FILE_EXTN));
			ResultSet rsUser = connectionCBO.createStatement().executeQuery(CBO_TAX_CHECK_QUERY);
			csvWriterTax.writeAll(rsUser, true);
			csvWriterTax.close();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		List <StoreDetails> storeList = preCheckStoreList;
		for (StoreDetails storeDetails : storeList) {	
			String storeNumberStr = storeDetails.getStoreNumber();
			if (ZERO_NUMBER.equals(storeNumberStr.substring(0, 1))) storeNumberStr = storeNumberStr.substring(1, 3);
			try {
				CSVWriter csvWriterUser = new CSVWriter(new FileWriter(FILE_PATH + File.separator + USER_DIR + File.separator + USER_FILENAME_PREFIX + storeDetails.getStoreNumber() + CSV_FILE_EXTN));
				ResultSet rsUser = connectionCBO.createStatement().executeQuery(CBO_USER_CHECK_QUERY + storeNumberStr + QUOTE_SYMBOL);
				csvWriterUser.writeAll(rsUser, true);
				csvWriterUser.close();
			} catch (SQLException | IOException e) {
				e.printStackTrace();
			}
			try {
				CSVWriter csvWriterRegister = new CSVWriter(new FileWriter(FILE_PATH + File.separator + REGISTER_DIR + File.separator + REGISTER_FILENAME_PREFIX + storeDetails.getStoreNumber() + CSV_FILE_EXTN));
				ResultSet rsUser = connectionCBO.createStatement().executeQuery(CBO_REGISTER_CHECK_QUERY + storeNumberStr + QUOTE_SYMBOL);
				csvWriterRegister.writeAll(rsUser, true);
				csvWriterRegister.close();
			} catch (SQLException | IOException e) {
				e.printStackTrace();
			}
		}
		try {
		} finally {
			if (connectionCBO != null) {
				try {
					connectionCBO.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		Connection connectionCSS= null;
		try {
			connectionCSS = ConnectionProvider.getOracleDBConnection(CSS_DB_NAME);
			ResultSet rsAllObjCSS = connectionCSS.createStatement().executeQuery(CSS_ALL_OBJECTS_QUERY);
			while (rsAllObjCSS.next()) dbObjCheck.setCssAllObjects(rsAllObjCSS.getString(1));
			ResultSet rsAllTblCSS = connectionCSS.createStatement().executeQuery(CSS_ALL_TABLES_QUERY);
			while (rsAllTblCSS.next()) dbObjCheck.setCssAllTables(rsAllTblCSS.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connectionCSS != null) {
				try {
					connectionCSS.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		Connection connectionPPS= null;
		try {
			connectionPPS = ConnectionProvider.getOracleDBConnection(PPS_DB_NAME);
			ResultSet rsAllObjPPS = connectionPPS.createStatement().executeQuery(PPS_ALL_OBJECTS_QUERY);
			while (rsAllObjPPS.next()) dbObjCheck.setPpsAllObjects(rsAllObjPPS.getString(1));
			ResultSet rsAllTblPPS = connectionPPS.createStatement().executeQuery(PPS_ALL_TABLES_QUERY);
			while (rsAllTblPPS.next()) dbObjCheck.setPpsAllTables(rsAllTblPPS.getString(1));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connectionPPS != null) {
				try {
					connectionPPS.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		try {
			PrintWriter out = new PrintWriter(FILE_PATH + File.separator + DB_OBJECT_CHECK_RESULT_FILE);
			out.println(dbObjCheck.getCboAllObjects());
			out.println(dbObjCheck.getCboAllTables());
			out.println(dbObjCheck.getCssAllObjects());
			out.println(dbObjCheck.getCssAllTables());
			out.println(dbObjCheck.getPpsAllObjects());
			out.println(dbObjCheck.getPpsAllTables());
			out.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		List<LBOCheck> lboCheckList = new ArrayList<LBOCheck>();
		for (StoreDetails storeDetails : storeList) {	
			Session sshSessionLBO = null;
			Connection infoSchemaConnectionLBO = null;
			LBOCheck lboCheck = new LBOCheck();
			lboCheck.setStoreNumber(storeDetails.getStoreNumber());
			try {
				sshSessionLBO = ConnectionProvider.openSSHSessionToLBO(storeDetails);
				infoSchemaConnectionLBO = ConnectionProvider.getMariaDBConnection(storeDetails, MARIA_DB_NAME_IS);
				ResultSet rsPECount = infoSchemaConnectionLBO.createStatement().executeQuery(LBO_INFO_SCHEMA_PE_CHECK);
				while (rsPECount.next()) lboCheck.setPeCount(rsPECount.getInt(1)); 
				ResultSet rsBOCount = infoSchemaConnectionLBO.createStatement().executeQuery(LBO_INFO_SCHEMA_BO_CHECK);
				while (rsBOCount.next()) lboCheck.setBackofficeCount(rsBOCount.getInt(1)); 
				lboCheckList.add(lboCheck);
			} catch (SQLException | JSchException ex) {
				ex.printStackTrace();
			} finally {
				if (infoSchemaConnectionLBO != null) {
					try {
						infoSchemaConnectionLBO.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (sshSessionLBO != null && sshSessionLBO.isConnected()) {
					sshSessionLBO.disconnect();
				}
			}
			BeanToCsv<LBOCheck> beanToCsvLBOCheck = new BeanToCsv<LBOCheck>();
			ColumnPositionMappingStrategy<LBOCheck> strategyIS = new ColumnPositionMappingStrategy<LBOCheck>();
			strategyIS.setType(LBOCheck.class);
			String [] columns = {"storeNumber", "peCount", "backofficeCount"};
			strategyIS.setColumnMapping(columns);
			
			CSVWriter csvWriterIS = null;
			try {
				csvWriterIS = new CSVWriter(new FileWriter(FILE_PATH + File.separator + LBO_CHECK_RESULT_FILE));
				beanToCsvLBOCheck.write(strategyIS, csvWriterIS, lboCheckList);
				csvWriterIS.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		new File(FILE_PATH + File.separator + MASTER_DATA_BO_DIR).mkdirs();
		new File(FILE_PATH + File.separator + MASTER_DATA_PE_DIR).mkdirs();
		
		List<MasterDataBO> masterDataBOAllStoresList = new ArrayList<MasterDataBO>();
		String [] masterDataBOTableArray = masterDataBOTables;
		BeanToCsv<MasterDataBO> beanToCsvMDBO = new BeanToCsv<MasterDataBO>();
		
		List<MasterDataPE> masterDataPEAllStoresList = new ArrayList<MasterDataPE>();
		String [] masterDataPETableArray = masterDataPETables;
		BeanToCsv<MasterDataPE> beanToCsvMDPE = new BeanToCsv<MasterDataPE>();
		
		for (StoreDetails storeDetails : storeList) {	
			Session sshSessionLBO = null;
			Connection boConnectionLBO = null;
			Connection peConnectionLBO = null;
			
			try {
				sshSessionLBO = ConnectionProvider.openSSHSessionToLBO(storeDetails);
				boConnectionLBO = ConnectionProvider.getMariaDBConnection(storeDetails, MARIA_DB_NAME_BO);
				peConnectionLBO = ConnectionProvider.getMariaDBConnection(storeDetails, MARIA_DB_NAME_PE);
				
				MasterDataBO masterDataBO = new MasterDataBO();
				Map<String, Integer> masterDataBOMap = new HashMap<String, Integer>();
				for (String boTableName : masterDataBOTableArray) {
					ResultSet resultSetMDBO = boConnectionLBO.createStatement().executeQuery(MASTER_DATA_QUERY + boTableName + SEMI_COLON_SYMBOL);
					while (resultSetMDBO.next()) masterDataBOMap.put(boTableName, resultSetMDBO.getInt(1));
				}
				BeanUtils.populate(masterDataBO, masterDataBOMap);
				
				List<MasterDataBO> masterDataBOList = new ArrayList<MasterDataBO>();
				masterDataBOList.add(masterDataBO);
				
				
				ColumnPositionMappingStrategy<MasterDataBO> strategyBO = new ColumnPositionMappingStrategy<MasterDataBO>();
				strategyBO.setType(MasterDataBO.class);
				strategyBO.setColumnMapping(masterDataBOTableArray);
				
				CSVWriter csvWriterMDBO = new CSVWriter(new FileWriter(FILE_PATH + File.separator + MASTER_DATA_BO_DIR + File.separator + MASTER_DATA_BO_FILENAME_PREFIX + storeDetails.getStoreNumber() + CSV_FILE_EXTN));
				
				beanToCsvMDBO.write(strategyBO, csvWriterMDBO, masterDataBOList);
				csvWriterMDBO.close();
				
				masterDataBO.setStoreNumber(storeDetails.getStoreNumber());
				masterDataBOAllStoresList.add(masterDataBO);
				
				MasterDataPE masterDataPE = new MasterDataPE();
				Map<String, Integer> masterDataPEMap = new HashMap<String, Integer>();
				for (String peTableName : masterDataPETableArray) {
					ResultSet resultSetMDPE = peConnectionLBO.createStatement().executeQuery(MASTER_DATA_QUERY + peTableName + SEMI_COLON_SYMBOL);
					while (resultSetMDPE.next()) masterDataPEMap.put(peTableName, resultSetMDPE.getInt(1));
				}
				BeanUtils.populate(masterDataPE, masterDataPEMap);
				
				List<MasterDataPE> masterDataPEList = new ArrayList<MasterDataPE>();
				masterDataPEList.add(masterDataPE);
				
				
				ColumnPositionMappingStrategy<MasterDataPE> strategyPE = new ColumnPositionMappingStrategy<MasterDataPE>();
				strategyPE.setType(MasterDataPE.class);
				strategyPE.setColumnMapping(masterDataPETableArray);
				
				CSVWriter csvWriterMDPE = new CSVWriter(new FileWriter(FILE_PATH + File.separator + MASTER_DATA_PE_DIR + File.separator + MASTER_DATA_PE_FILENAME_PREFIX + storeDetails.getStoreNumber() + CSV_FILE_EXTN));
				
				beanToCsvMDPE.write(strategyPE, csvWriterMDPE, masterDataPEList);
				csvWriterMDPE.close();
				
				masterDataPE.setStoreNumber(storeDetails.getStoreNumber());
				masterDataPEAllStoresList.add(masterDataPE);
				
			} catch (SQLException | JSchException | IOException | IllegalAccessException | InvocationTargetException ex) {
				ex.printStackTrace();
			} finally {
				if (boConnectionLBO != null) {
					try {
						boConnectionLBO.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (peConnectionLBO != null) {
					try {
						peConnectionLBO.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (sshSessionLBO != null && sshSessionLBO.isConnected()) {
					sshSessionLBO.disconnect();
				}
			}
			
		}
		
		List<String> columnMappingBOList = new LinkedList<String>(Arrays.asList(masterDataBOTableArray));
		columnMappingBOList.add(0,"storeNumber");
		masterDataBOTableArray = columnMappingBOList.toArray(new String[columnMappingBOList.size()]);
		
		ColumnPositionMappingStrategy<MasterDataBO> strategyBOAll = new ColumnPositionMappingStrategy<MasterDataBO>();
		strategyBOAll.setType(MasterDataBO.class);
		strategyBOAll.setColumnMapping(masterDataBOTableArray);
		
		try {
			CSVWriter csvWriterMDBOAll = new CSVWriter(new FileWriter(FILE_PATH + File.separator + MASTER_DATA_BO_FILENAME_PREFIX + ALL_STORES + CSV_FILE_EXTN));
			beanToCsvMDBO.write(strategyBOAll, csvWriterMDBOAll, masterDataBOAllStoresList);
			csvWriterMDBOAll.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<String> columnMappingPEList = new LinkedList<String>(Arrays.asList(masterDataPETableArray));
		columnMappingPEList.add(0,"storeNumber");
		masterDataPETableArray = columnMappingPEList.toArray(new String[columnMappingPEList.size()]);
		
		ColumnPositionMappingStrategy<MasterDataPE> strategyPEAll = new ColumnPositionMappingStrategy<MasterDataPE>();
		strategyPEAll.setType(MasterDataPE.class);
		strategyPEAll.setColumnMapping(masterDataPETableArray);
		
		try {
			CSVWriter csvWriterMDPEAll = new CSVWriter(new FileWriter(FILE_PATH + File.separator + MASTER_DATA_PE_FILENAME_PREFIX + ALL_STORES + CSV_FILE_EXTN));
			beanToCsvMDPE.write(strategyPEAll, csvWriterMDPEAll, masterDataPEAllStoresList);
			csvWriterMDPEAll.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
