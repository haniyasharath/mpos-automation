package com.marks.mpos.deployment.check.properties;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.marks.mpos.deployment.check.utils.DateUtil;

public class UserProperties {

	static {
		loadProperties();
	}

	private static Properties prop;

	private static void loadProperties() {
		try {
			prop = new Properties();
			InputStream in = new FileInputStream("./config.properties");
			prop.load(in);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static final String SSH_STORE_USERNAME = prop.getProperty("ssh_store_username");
	public static final String SSH_STORE_PASSWORD = prop.getProperty("ssh_store_password");
	public static String POST_CHECK_DATE;
	public static String GIVEN_DATE;
	public static boolean DETAILED_REPORT = false;
	public static boolean SEND_EMAIL = false;
	public static boolean PREPARE_EXCEL = true;
	public static boolean IS_PRODUCT_CHECK_ENABLED = false;
	public static boolean IS_PRICE_EVENT_CHECK_ENABLED = false;
	public static boolean IS_DELETED_PRICE_EVENT_CHECK_ENABLED = false;
	public static boolean IS_TRANSACTION_CHECK_ENABLED = true;
	public static boolean SKIP_STORES_ENABLED = false;
	public static boolean CSS_HEALTH_CHECK_ONLY_ENABLED = false;
	public static String[] PRODUCTS;
	public static String[] PRICEVENTS;
	public static Set<String> REQUIRED_STORES = new HashSet<String>();
	public static final String PRE_CHECK_DATE = prop.getProperty("pre_check_date");
	public static final String DTE_USERNAME = prop.getProperty("dte_username");
	public static final String DTE_PASSWORD = prop.getProperty("dte_password");
	public static final String DEFAULT_PRICE_EVENT_EFFECTIVE_DATE = "12/01/2016";
	public static final String PRICE_EVENT_EFFECTIVE_DATE = setPriceEventEffectiveDate(prop.getProperty("priceEventEffectiveDate"));

	public static void setPostCheckDate(String transactionDate) {
		if (StringUtils.isBlank(transactionDate)) {
			POST_CHECK_DATE = DateUtil.getTransactionDate();
			GIVEN_DATE = StringUtils.EMPTY;
		} else {
			POST_CHECK_DATE = DateUtil.getFormattedDate(transactionDate);
			GIVEN_DATE = transactionDate;
		}
	}

	private static String setPriceEventEffectiveDate(String priceEventEffectiveDate) {
		if (StringUtils.isBlank(priceEventEffectiveDate)) {
			return DEFAULT_PRICE_EVENT_EFFECTIVE_DATE;
		} else {
			return priceEventEffectiveDate.trim();
		}
	}

	public static void setPriceEvents(String priceEvents) {
		PRICEVENTS = priceEvents.split(",");
	}

	public static void setProducts(String products) {
		PRODUCTS = products.split(",");
	}

	public static void initRequiredStores(String requiredStores) {
		for (String requiredStore : requiredStores.split(",")) {
			REQUIRED_STORES.add(requiredStore.length() == 2 ? "0" + requiredStore : requiredStore);
		}
	}
}