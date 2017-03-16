package com.marks.mpos.deployment.check.queries;

public class IDatabaseQueries {

	// POST DEPLOY CHECK
	
	// LBO
	
	public static final String LBO_ACTIVE_LANES_QUERY = "select lane_number from workstation where workstation_active_flag = true order by lane_number;";

	public static final String LBO_LANE_VOIDED_COUNT_QUERY = "select count(*) from transaction where voided_flag != 'false' and transaction_id LIKE '";
	
	public static final String LBO_LANE_NON_VOIDED_COUNT_QUERY = "select count(*) from transaction where voided_flag = 'false' and transaction_id LIKE '";
	
	public static final String LBO_LANE_TOTAL_COUNT_QUERY = "select count(*) from transaction where transaction_id LIKE '";
	
	public static final String LBO_STUCK_TRANSACTION_QUERY = "SELECT count(*) FROM transaction_processing tp join transaction t on tp.transaction_id = t.transaction_id where t.end_timestamp < (NOW() - INTERVAL 30 MINUTE)";
	
	public static final String LBO_VOIDED_TOTAL_QUERY = "SELECT SUM(total_amount) FROM retail_transaction rt, transaction t WHERE t.transaction_id = rt.transaction_id AND voided_flag != 'false' AND t.transaction_id LIKE '";
	
	public static final String LBO_NON_VOIDED_TOTAL_QUERY = "SELECT SUM(total_amount) FROM retail_transaction rt, transaction t WHERE t.transaction_id = rt.transaction_id AND voided_flag = 'false' AND t.transaction_id LIKE '";
	
	public static final String LBO_TRXN_TOTAL_QUERY = "SELECT SUM(total_amount) FROM retail_transaction rt, transaction t WHERE t.transaction_id = rt.transaction_id AND t.transaction_id LIKE '";
	
	
	//CBO
	
	public static final String CBO_TRXN_TOTAL_COUNT_QUERY = "select count(*) from transaction where transaction_id LIKE '";
	
	public static final String CBO_TRXN_TOTAL_AMOUNT_QUERY = "SELECT SUM(total_amount) FROM retail_transaction rt, transaction t WHERE t.transaction_id = rt.transaction_id AND t.transaction_id LIKE '";
	
	public static final String CBO_STUCK_TRANSACTION_QUERY = "SELECT count(*) FROM transaction_processing tp join transaction t on tp.transaction_id = t.transaction_id where t.end_timestamp < (current_date()) AND t.transaction_id LIKE '";
	
	// CSS
	
	public static final String CSS_LANE_VOIDED_COUNT_QUERY = "SELECT COUNT(*) FROM transaction WHERE voided=1 AND transaction_id LIKE '";
	
	public static final String CSS_LANE_NON_VOIDED_COUNT_QUERY = "SELECT COUNT(*) FROM transaction WHERE voided=0 AND transaction_id LIKE '";
	
	public static final String CSS_LANE_TOTAL_COUNT_QUERY = "SELECT COUNT(*) FROM transaction WHERE transaction_id LIKE '";
	
	public static final String CSS_VOIDED_TOTAL_QUERY = "SELECT SUM(gross_amount) FROM retail_transaction RT, transaction t WHERE rt.RETAIL_TXN_ID = t.transaction_id AND t.voided=1 AND transaction_id LIKE '";
	
	public static final String CSS_NON_VOIDED_TOTAL_QUERY = "SELECT SUM(gross_amount) FROM retail_transaction RT, transaction t WHERE rt.RETAIL_TXN_ID = t.transaction_id AND t.voided=0 AND transaction_id LIKE '";
	
	public static final String CSS_TRXN_TOTAL_QUERY = "SELECT SUM(gross_amount) FROM retail_transaction RT, transaction t WHERE rt.RETAIL_TXN_ID = t.transaction_id AND transaction_id LIKE '";
	
	public static final String CSS_JOURNAL_COUNT_QUERY = "SELECT COUNT(DISTINCT transaction_id) FROM pos_journal WHERE transaction_id LIKE '";
	
	
	// PRE DEPLOY CHECK
	
	// CBO
	
	public static final String CBO_USER_CHECK_QUERY = "SELECT OP.operator_id, OP.first_name ||' ' || OP.Last_name User_Name, ORS.retail_store_number, OG.workgroup_id, TW.description, OP.locked_out_timestamp, OP.account_disabled_flag, OP.language_code, OP.password_expired_flag, OP.right_hand_flag FROM OPERATOR OP, OPERATOR_GROUP OG, OPERATOR_RETAIL_STORE ORS, TRANSLATION_WORKGROUP TW WHERE op.operator_id = og.operator_id AND ors.operator_id = op.operator_id AND tw.workgroup_id = og.workgroup_id AND tw.language_code = op.language_code AND ORS.RETAIL_STORE_NUMBER = '";
	
	public static final String CBO_REGISTER_CHECK_QUERY = "SELECT terminal_number Serial_Number, decode (workstation_active_flag, 1, 'Active', 0, 'Inactive') Status, central_lane_number, lane_number, retail_store_number, pinpad_type, pin_entry_device_number, electronic_cash_register_id, merchant_id, last_transaction_number FROM WORKSTATION WHERE RETAIL_STORE_NUMBER = '";
	
	public static final String CBO_TAX_CHECK_QUERY = "SELECT rs.retail_store_number, tr.registration_number, tr.tax_type_code, rs.retail_store_name, rs.retail_store_short_name, rs.business_unit_type_code, rs.retail_store_active_flag, rs.ADDRESS_LINE1_TEXT, rs.ADDRESS_LINE2_TEXT, rs.ADDRESS_LINE3_TEXT, rs.ADDRESS_LINE4_TEXT, rs.CITY_NAME, rs.PROVINCE_STATE_NAME, rs.POSTAL_CODE, rs.LANGUAGE_CODE, rs.TAX_REGION_CODE, rs.TIME_ZONE_CODE, rs.TIME_ZONE_NAME FROM retail_store RS, retail_store_tax_registration RSTR, tax_registration TR WHERE RSTR.RETAIL_STORE_NUMBER = RS.RETAIL_STORE_NUMBER AND RSTR.TAX_REGISTRATION_ID = TR.TAX_REGISTRATION_ID";
	
	// MasterData for BackOffice and PE
	
	public static final String MASTER_DATA_QUERY = "select count(*) from ";
	
	// DB Object check in CSS, CBO and PPS
	
	public static final String CBO_ALL_OBJECTS_QUERY = "SELECT COUNT(*) FROM all_objects WHERE owner = 'MCPE'";
	
	public static final String CBO_ALL_TABLES_QUERY = "SELECT COUNT(*) FROM all_tables WHERE owner = 'MCPE'";
	
	public static final String CSS_ALL_OBJECTS_QUERY = "SELECT COUNT(*) FROM all_objects WHERE owner = 'MCSS'";
	
	public static final String CSS_ALL_TABLES_QUERY = "SELECT COUNT(*) FROM all_tables WHERE owner = 'MCSS'";
	
	public static final String PPS_ALL_OBJECTS_QUERY = "SELECT COUNT(*) FROM all_objects WHERE owner = 'MPPS'";
	
	public static final String PPS_ALL_TABLES_QUERY = "SELECT COUNT(*) FROM all_tables WHERE owner = 'MPPS'";
	
	// LBO check
	
	public static final String LBO_INFO_SCHEMA_PE_CHECK = "SELECT COUNT(*)  FROM tables WHERE table_schema = 'pe';";
	
	public static final String LBO_INFO_SCHEMA_BO_CHECK = "SELECT COUNT(*)  FROM tables WHERE table_schema = 'backoffice';";
	
	public static final String CBO_UPC_COUNT = "SELECT COUNT(*) FROM UPC";
	
	public static final String CBO_ITEM_COUNT = "SELECT COUNT(*) FROM ITEM where ITEM_ACTIVE_FLAG = 1 AND SELLING_STATUS_CODE NOT IN ('WRITTEN OFF', 'DISCONTINUED')";
	
	public static final String CBO_BASE_PRICE_COUNT = "SELECT distinct ITEMPRICEEVENTNUMBER FROM ITEMPRICEMAINTENANCE where trunc(sysdate) BETWEEN trunc(CURRSALEUNITRETAILPRICEEFFDT) AND trunc(CURRSALEUNITRETAILPRICEEXPDT) AND trunc(CURRSALEUNITRETAILPRICEEFFDT) >= ";
	
	public static final String CBO_BOGO_COUNT = "select distinct pdr.PriceDerivRuleEventNumber from pricederivationrule pdr, map_bogo_getmerchhierarchy mbm, promotionzone pz "
			+ "where pdr.PriceDerivRuleEventNumber = pz.PriceDerivRuleEventNumber and mbm.PriceDerivRuleEventNumber = pdr.PriceDerivRuleEventNumber "
			+ "and trunc(sysdate) between trunc(pz.EFFECTIVEDT) and trunc(pz.EXPIRATIONDT)"
			+ "and trunc(pz.EFFECTIVEDT) >= ";
	
	public static final String CBO_SPECIAL_PRICE_COUNT = "select distinct pdr.PriceDerivRuleEventNumber from pricederivationrule pdr, MAP_ITEMREWARD_MERCHHIERNUM mim, promotionzone pz "
			+ "where pdr.PriceDerivRuleEventNumber = pz.PriceDerivRuleEventNumber and mim.PriceDerivRuleEventNumber = pdr.PriceDerivRuleEventNumber "
			+ "and trunc(sysdate) between trunc(pz.EFFECTIVEDT) and trunc(pz.EXPIRATIONDT)"
			+ "and trunc(pz.EFFECTIVEDT) >= ";
	
	public static final String CBO_XFORY_COUNT = "select distinct pdr.PriceDerivRuleEventNumber from pricederivationrule pdr, MAP_PD_BUYMERCHHIERARCHY mpb, promotionzone pz "
			+ "where pdr.PriceDerivRuleEventNumber = pz.PriceDerivRuleEventNumber and mpb.PriceDerivRuleEventNumber = pdr.PriceDerivRuleEventNumber "
			+ "and trunc(sysdate) between trunc(pz.EFFECTIVEDT) and trunc(pz.EXPIRATIONDT)"
			+ "and trunc(pz.EFFECTIVEDT) >= ";
	
	public static final String LBO_UPC_COUNT = "SELECT COUNT(*) FROM backoffice.UPC";
	
	public static final String LBO_ITEM_COUNT = "SELECT COUNT(*) FROM backoffice.ITEM where ITEM_ACTIVE_FLAG = 1 AND SELLING_STATUS_CODE NOT IN ('WRITTEN OFF', 'DISCONTINUED')";
	
	public static final String LBO_BASE_PRICE_COUNT = "select distinct ITEMPRICEEVENTNUMBER from pe.itempricemaintenance "
			+ "where now() between CURRSALEUNITRETAILPRICEEFFDT and CURRSALEUNITRETAILPRICEEXPDT "
			+ "and CURRSALEUNITRETAILPRICEEFFDT >= ";
	
	public static final String LBO_BOGO_COUNT = "select distinct pdr.PriceDerivRuleEventNumber from pe.pricederivationrule pdr, pe.map_bogo_getmerchhierarchy mbm, pe.promotionzone pz "
			+ "where pdr.PriceDerivRuleEventNumber = pz.PriceDerivRuleEventNumber and mbm.PriceDerivRuleEventNumber = pdr.PriceDerivRuleEventNumber "
			+ "and now() between pz.EFFECTIVEDT and pz.EXPIRATIONDT "
			+ "and pz.EFFECTIVEDT >= ";
	
	public static final String LBO_SPECIAL_PRICE_COUNT = "select distinct pdr.PriceDerivRuleEventNumber from pe.pricederivationrule pdr, pe.MAP_ITEMREWARD_MERCHHIERNUM mim, pe.promotionzone pz "
			+ "where pdr.PriceDerivRuleEventNumber = pz.PriceDerivRuleEventNumber and mim.PriceDerivRuleEventNumber = pdr.PriceDerivRuleEventNumber "
			+ "and now() between pz.EFFECTIVEDT and pz.EXPIRATIONDT "
			+ "and pz.EFFECTIVEDT >= ";
	
	public static final String LBO_XFORY_COUNT = "select distinct pdr.PriceDerivRuleEventNumber from pe.pricederivationrule pdr, pe.MAP_PD_BUYMERCHHIERARCHY mpb, pe.promotionzone pz "
			+ "where pdr.PriceDerivRuleEventNumber = pz.PriceDerivRuleEventNumber and mpb.PriceDerivRuleEventNumber = pdr.PriceDerivRuleEventNumber "
			+ "and now() between pz.EFFECTIVEDT and pz.EXPIRATIONDT "
			+ "and pz.EFFECTIVEDT >= ";
	
	public static final String LBO_GET_ITEM = "SELECT item_number FROM backoffice.ITEM where item_number IN (";
	
	public static final String LBO_GET_PRICE_EVENT = "select PriceDerivRuleEventNumber from pe.promotionzone where PriceDerivRuleEventNumber IN (";
	
	public static final String CBO_GET_PRICE_EVENT = "select * from promotionzone pz, pricederivationrule pr where pr.PriceDerivRuleEventNumber=pz.PriceDerivRuleEventNumber AND pz.PriceDerivRuleEventNumber IN (";
	
	public static final String LBO_GET_DELETED_PRICE_EVENT = "select * from pe.itempricemaintenance where STATUS ='DELETED' AND ItemPriceEventNumber IN ('348340')";
	public static final String LBO_GET_ITEM_PRICE_EVENT = "select distinct ITEMPRICEEVENTNUMBER from pe.itempricemaintenance where ITEMPRICEEVENTNUMBER IN (";
	
	public static final String CBO_GET_ITEM_PRICE_EVENT = "select distinct ITEMPRICEEVENTNUMBER, CURRSALEUNITRETAILPRICEEFFDT, CURRSALEUNITRETAILPRICEEXPDT from itempricemaintenance where ITEMPRICEEVENTNUMBER IN (";
	public static final String CSS_QUEUE_DEPTH_CHECK = "SELECT COUNT(*) FROM JMS_TEXT_MESSAGE WHERE STATUS != 'SENT'";
	
	public static final String LBO_QUEUE_DEPTH_CHECK = "SELECT COUNT(*) FROM OUTGOING_TEXT_MESSAGE WHERE STATUS != 'SENT'";
}