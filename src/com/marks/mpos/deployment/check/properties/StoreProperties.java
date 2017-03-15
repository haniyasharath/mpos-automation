package com.marks.mpos.deployment.check.properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.marks.mpos.deployment.check.beans.StoreDetails;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;

public class StoreProperties {

	private static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public static final List<StoreDetails> postCheckStoreList = loadCSVData("post_check_store_list.csv");
	public static final List<StoreDetails> preCheckStoreList = null;//loadCSVData("pre_check_store_list.csv");
	
	private static List<StoreDetails> loadCSVData(String fileName) {
		CsvToBean<StoreDetails> csvToBean = new CsvToBean<StoreDetails>();

		Map<String, String> columnMapping = new HashMap<String, String>();
		columnMapping.put("store_number", "storeNumber");
		columnMapping.put("store_hostname", "storeHostName");

		HeaderColumnNameTranslateMappingStrategy<StoreDetails> strategy = new HeaderColumnNameTranslateMappingStrategy<StoreDetails>();
		strategy.setType(StoreDetails.class);
		strategy.setColumnMapping(columnMapping);

		CSVReader reader;
		try {
			FileInputStream file = new FileInputStream("./" + fileName);
			reader = new CSVReader(new InputStreamReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			LOG.warning(e.getLocalizedMessage());
			LOG.warning("Unable to read post_check_store_list.csv file, reading from default location ");
			//Disabled for failover case
			//reader = new CSVReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)));
			reader = null;
		}
		
		return csvToBean.parse(strategy, reader);
	}
	
	public static void main(String[] str) {
		List<StoreDetails> list = StoreProperties.postCheckStoreList;
		Map<String, String> uniqueStores = new HashMap<String, String>();
		list.stream().forEach(item -> {uniqueStores.put(item.getStoreNumber(), item.getStoreHostName());});
		for(String str1 : uniqueStores.keySet()) {
			System.out.println((str1.length() == 2 ? "0" + str1 : str1) + "," + uniqueStores.get(str1));
		}
	}
	
}