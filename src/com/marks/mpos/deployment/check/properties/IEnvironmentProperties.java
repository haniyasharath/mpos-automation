package com.marks.mpos.deployment.check.properties;

public interface IEnvironmentProperties {

	public static final String MARIA_DB_USERNAME = "ctco-pos";
	public static final String MARIA_DB_PASSWORD = "2XtpPBd3fbDJ4fx4SbQk";
	public static final int MARIA_DB_PORT = 3306;
	public static final String MARIA_DB_HOST = "localhost";
	public static final String MARIA_DB_DRIVER = "com.mysql.jdbc.Driver";
	public static final String MARIA_DB_NAME_BO = "backoffice";
	public static final String MARIA_DB_NAME_PE = "pe";
	public static final String MARIA_DB_NAME_IS = "information_schema";
	public static final String MARIA_DB_URL_PREFIX = "jdbc:mysql://localhost:";
	
	
	public static final String SSH_LOCAL_PORT_PREFIX = "9";
	public static final int SSH_PORT = 22;
	
	public static final String ORACLE_DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
	public static final String ORACLE_DB_URL_PREFIX = "jdbc:oracle:thin:@";
	public static final String ORACLE_DB_HOSTNAME = "10.100.34.23";
	
	public static final String CSS_ORACLE_USERNAME = "mcss";
	public static final String CSS_ORACLE_PASSWORD = "mcssprd_MC2s_1015";
	public static final int CSS_ORACLE_PORT = 25710;
	public static final String CSS_ORACLE_SID ="MCSSPRD";
	
	public static final String CBO_ORACLE_USERNAME = "mcpe";
	public static final String CBO_ORACLE_PASSWORD = "mcpeprd_mCp1e_1015";
	public static final int CBO_ORACLE_PORT = 25712;
	public static final String CBO_ORACLE_SID ="MCPEPRD";
	
	public static final String PPS_ORACLE_USERNAME = "mpps";
	public static final String PPS_ORACLE_PASSWORD = "mcpeprd_M2Ps_1015";
	public static final int PPS_ORACLE_PORT = 25712;
	public static final String PPS_ORACLE_SID ="MCPEPRD";
	
	public static final String CSS_DB_NAME = "CSS";
	public static final String CBO_DB_NAME = "CBO";
	public static final String PPS_DB_NAME = "PPS";
	
	public static final String WILDCARD_PERCENTAGE_SYMBOL = "%";
	public static final String QUOTE_SYMBOL = "'";
	public static final String SEMI_COLON_SYMBOL = ";";
	public static final String COLON_SYMBOL = ":";
	public static final String SLASH_SYMBOL = "/";
	public static final String ZERO_NUMBER = "0";
	
	public static final String USER_HOME = System.getProperty("user.home");
	
	public static final String DEPLOY_CHECK_DIR = "deploy_check";
	public static final String POST_CHECK_DIR = "post_deploy";
	public static final String PRE_CHECK_DIR = "pre_deploy";
	public static final String USER_DIR = "users";
	public static final String REGISTER_DIR = "registers";
	public static final String MASTER_DATA_BO_DIR = "MasterDataBO";
	public static final String MASTER_DATA_PE_DIR = "MasterDataPE";
	
	public static final String CSV_FILE_EXTN = ".csv";
	public static final String USER_FILENAME_PREFIX = "UserList_Store_";
	public static final String REGISTER_FILENAME_PREFIX = "RegisterList_Store_";
	public static final String TAX_REGISTER_FILENAME = "Tax_Register";
	public static final String MASTER_DATA_BO_FILENAME_PREFIX = "MasterData_Backoffice_Store_";
	public static final String MASTER_DATA_PE_FILENAME_PREFIX = "MasterData_PE_Store_";
	
	public static final String DOCX_FILE_EXTN = ".docx";
	public static final String POST_CHECK_FILENAME_PREFIX = "Post Deployment Checks for Store ";
	
	public static final String DB_OBJECT_CHECK_RESULT_FILE = "DB_Object_Check_Result.txt";
	public static final String LBO_CHECK_RESULT_FILE = "LBO_Check_Result.csv";
	
	public static final String ALL_STORES = "ALL";
	
	public static final String STORE_POS_FOLDER = "/apps/pos/pos/feeds/processed";
	public static final String STORE_PE_FOLDER = "/apps/pos/pe_feeds/finished";
	public static final String DTE_FOLDER = "/data/dte/ftp_outboxes";
	
	public static final String DTE_NODE_1 = "10.100.34.10";
	public static final String DTE_NODE_2 = "10.100.34.11";
	public static final String DTE_NODE_3 = "10.100.34.12";
	public static final String DTE_NODE_4 = "10.100.34.13";
	
	public static final String CSS_NODE_1 = "10.100.34.14";
	public static final String CSS_NODE_2 = "10.100.34.15";
	public static final String CSS_NODE_3 = "10.100.34.16";
	public static final String CSS_NODE_4 = "10.100.34.17";
	
	public static final int BATCH_SIZE = 10;
	public static final int RETRY_SIZE = 2;
}