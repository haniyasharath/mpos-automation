package com.marks.mpos.deployment.check.report;

import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.poi.util.StringUtil;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.marks.mpos.deployment.check.beans.BusinessAreaCounts;
import com.marks.mpos.deployment.check.beans.CBODetails;
import com.marks.mpos.deployment.check.beans.CssHealth;
import com.marks.mpos.deployment.check.beans.LaneCount;
import com.marks.mpos.deployment.check.beans.PriceEventLists;
import com.marks.mpos.deployment.check.beans.StoreDetails;
import com.marks.mpos.deployment.check.beans.StoreInfo;
import com.marks.mpos.deployment.check.beans.StoreReport;
import com.marks.mpos.deployment.check.beans.StoredDataCounts;
import com.marks.mpos.deployment.check.beans.TransactionTotal;
import com.marks.mpos.deployment.check.businessAreas.BusinessAreasDownloader;
import com.marks.mpos.deployment.check.calculator.DataCalculator;
import com.marks.mpos.deployment.check.connection.ConnectionProvider;
import com.marks.mpos.deployment.check.properties.StoreProperties;
import com.marks.mpos.deployment.check.properties.UserProperties;
import com.marks.mpos.deployment.check.utils.DateUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import static com.marks.mpos.deployment.check.properties.UserProperties.*;
import static com.marks.mpos.deployment.check.queries.IDatabaseQueries.*;


public class StoreDataBuilder {
	private static final String X_FOR_Y_PRICE_EVENTS = "XForYPriceEvents";
	private static final String BOGO_PRICE_EVENTS = "BogoPriceEvents";
	private static final String SPECIAL_PRICE = "SpecialPrice";
	private static final String BASE_PRICE = "BasePrice";
	private static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static ExecutorService executorService = Executors.newCachedThreadPool();
	private static DataCalculator dataCalculator = new DataCalculator();
	private static Set<String> MISSING_PRICE_EVENTS = new HashSet<String>();
	private static Set<String> MISSING_PRICE_EVENTS_76 = new HashSet<String>();
	private static Set<String> MISSING_BASIC_PRICE_EVENTS = new HashSet<String>();
	private static Set<String> MISSING_SPECIAL_PRICE_EVENTS = new HashSet<String>();
	private static Set<String> MISSING_XFORY_PRICE_EVENTS = new HashSet<String>();
	private static Set<String> MISSING_BOGO_PRICE_EVENTS = new HashSet<String>();
	private static int journalCount = 0;
	private static double voidedTotalLBO = 0;
	private static double nonVoidedTotalLBO = 0;
	private static double trxnTotalLBO = 0;
	private static double voidedTotalCSS = 0;
	private static double nonVoidedTotalCSS = 0;
	private static double trxnTotalCSS = 0;
	
	private static int voidedCountLBO = 0;
	private static int nonVoidedCountLBO = 0;
	private static int trxnCountLBO = 0;
	private static int voidedCountCSS = 0;
	private static int nonVoidedCountCSS = 0;
	private static int trxnCountCSS = 0;

	public static List<StoreReport> generateStoreDataObjectList() {
		List<StoreReport> storeDataList = new ArrayList<StoreReport>();
		List <StoreDetails> storeList;
		if (UserProperties.SKIP_STORES_ENABLED) {
			storeList = StoreProperties.postCheckStoreList.stream()
					.filter(store -> UserProperties.REQUIRED_STORES.contains(store.getStoreNumber()))
					.collect(Collectors.toList());
		} else {
			storeList = StoreProperties.postCheckStoreList;
		}
		Future<BusinessAreaCounts> cboFeedFuture = null;
		BusinessAreaCounts cBOBusAreaCounts = new BusinessAreaCounts();
		
		if(DETAILED_REPORT) {
	        CBODetails cboDetails = new CBODetails("10.100.34.18");
	        
		    String POS_FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator +  POST_CHECK_DIR + File.separator + POST_CHECK_DATE + File.separator +"business_areas" + File.separator +"CBO"+File.separator +"pos"; 
	        String PE_FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator +  POST_CHECK_DIR + File.separator + POST_CHECK_DATE + File.separator +"business_areas" + File.separator +"CBO"+File.separator +"pe_feeds"; 
	        new File(POS_FILE_PATH).mkdirs();
	        new File(PE_FILE_PATH).mkdirs();
	        
			
			BusinessAreasDownloader downloader = new BusinessAreasDownloader();
			cboFeedFuture = executorService.submit(() -> {
				Session sshSessionCBO = null;
				try {
					sshSessionCBO = ConnectionProvider.openSSHSessionToCBO(cboDetails);
					downloader.downloadFiles(sshSessionCBO, POS_FILE_PATH, STORE_POS_FOLDER, "POS", cBOBusAreaCounts);
					downloader.downloadFiles(sshSessionCBO, PE_FILE_PATH, STORE_PE_FOLDER, "PE", cBOBusAreaCounts);
				} catch (JSchException e) {
					System.out.println(e.getMessage());
					e.printStackTrace();
				} finally {
					if (sshSessionCBO != null && sshSessionCBO.isConnected()) {
						sshSessionCBO.disconnect();
					}
				}
				return cBOBusAreaCounts;
			});
		}
		Future<StoreReport> cboStoredDataCountFuture = executorService.submit(() -> {
			Connection connectionCPE = null;
			Connection connectionPPS = null;
			try {
				connectionCPE = ConnectionProvider.getOracleDBConnection(CBO_DB_NAME);
				connectionPPS = ConnectionProvider.getOracleDBConnection(PPS_DB_NAME);
			} catch (SQLException e) {
				System.out.println("Error while connecting to CBO CPE/PPS -> " + e.getMessage());
				e.printStackTrace();
			}
			StoredDataCounts cboStoredDataCounts = new StoredDataCounts();
			PriceEventLists cboPriceEventLists = new PriceEventLists();
			Map<String, Integer> cboTxnCount = new HashMap<String, Integer>();
			Map<String, Double> cboTxnAmount = new HashMap<String, Double>();
			Map<String, Integer> cboStuckTransactionCount = new HashMap<String, Integer>();
			try {
				ResultSet rsCBOUpcCount = connectionCPE.createStatement().executeQuery(CBO_UPC_COUNT);
				while (rsCBOUpcCount.next()) cboStoredDataCounts.setUpc(rsCBOUpcCount.getInt(1));
				ResultSet rsCBOItemCount = connectionCPE.createStatement().executeQuery(CBO_ITEM_COUNT);
				while (rsCBOItemCount.next()) cboStoredDataCounts.setProduct(rsCBOItemCount.getInt(1));
				String priceEventEffectiveDate = "'" + DateUtil.getFormattedDate(UserProperties.PRICE_EVENT_EFFECTIVE_DATE, "dd-MMM-yy") + "'";
				ResultSet rsCBOBasePriceCount = connectionPPS.createStatement().executeQuery(CBO_BASE_PRICE_COUNT + priceEventEffectiveDate);
				while (rsCBOBasePriceCount.next()) cboPriceEventLists.getBasicPriceEvents().add(rsCBOBasePriceCount.getString("ITEMPRICEEVENTNUMBER"));
				cboStoredDataCounts.setBasePrice(cboPriceEventLists.getBasicPriceEvents().size());
				ResultSet rsCBOBogoCount = connectionPPS.createStatement().executeQuery(CBO_BOGO_COUNT + priceEventEffectiveDate);
				while (rsCBOBogoCount.next()) cboPriceEventLists.getBogoPriceEvents().add(rsCBOBogoCount.getString("PriceDerivRuleEventNumber"));
				cboStoredDataCounts.setBogo(cboPriceEventLists.getBogoPriceEvents().size());
				ResultSet rsCBOSpecialPriceCount = connectionPPS.createStatement().executeQuery(CBO_SPECIAL_PRICE_COUNT + priceEventEffectiveDate);
				while (rsCBOSpecialPriceCount.next()) cboPriceEventLists.getSpecialPriceEvents().add(rsCBOSpecialPriceCount.getString("PriceDerivRuleEventNumber"));
				cboStoredDataCounts.setSpecialPrice(cboPriceEventLists.getSpecialPriceEvents().size());
				ResultSet rsCBOXforYCount = connectionPPS.createStatement().executeQuery(CBO_XFORY_COUNT + priceEventEffectiveDate);
				while (rsCBOXforYCount.next()) cboPriceEventLists.getxForYPriceEvents().add(rsCBOXforYCount.getString("PriceDerivRuleEventNumber"));
				cboStoredDataCounts.setxForY(cboPriceEventLists.getxForYPriceEvents().size());
				
				int txnCount;
				double txnAmount;
				int stuckTransaction;
				for(StoreDetails details : storeList) {
					final String SEARCH_PARAMETER = details.getStoreNumber() + WILDCARD_PERCENTAGE_SYMBOL + POST_CHECK_DATE + WILDCARD_PERCENTAGE_SYMBOL + QUOTE_SYMBOL;
					txnCount = 0;
					txnAmount = 0.0;
					stuckTransaction = 0;
					ResultSet rsCBOTrxnTotalCount = connectionCPE.createStatement().executeQuery(CBO_TRXN_TOTAL_COUNT_QUERY + SEARCH_PARAMETER);
					while (rsCBOTrxnTotalCount.next()) txnCount = rsCBOTrxnTotalCount.getInt(1);
					cboTxnCount.put(details.getStoreNumber(), txnCount);
					
					ResultSet rsCBOTrxnTotalAmount = connectionCPE.createStatement().executeQuery(CBO_TRXN_TOTAL_AMOUNT_QUERY + SEARCH_PARAMETER);
					while (rsCBOTrxnTotalAmount.next()) txnAmount = rsCBOTrxnTotalAmount.getDouble(1);
					cboTxnAmount.put(details.getStoreNumber(), txnAmount);
					
					/*ResultSet rsCboStuckTransactionCount = connectionCPE.createStatement().executeQuery(CBO_STUCK_TRANSACTION_QUERY + SEARCH_PARAMETER);
					while (rsCboStuckTransactionCount.next()) stuckTransaction = rsCboStuckTransactionCount.getInt(1);
					cboStuckTransactionCount.put(details.getStoreNumber(), stuckTransaction);*/
				}
			} catch (Exception e) {
				System.out.println("Error while getting data from CBO CPE/PPS -> " + e.getMessage());
				e.printStackTrace();
			}
			StoreReport cboReportData = new StoreReport();
			cboReportData.setCboDataCounts(cboStoredDataCounts);
			cboReportData.setCboPriceEventLists(cboPriceEventLists);
			cboReportData.setCboStuckTransactionCount(cboStuckTransactionCount);
			cboReportData.setCboTxnCount(cboTxnCount);
			cboReportData.setCboTxnAmount(cboTxnAmount);
			return cboReportData;
		});
		
		storeDataList.addAll(getReportData(storeList));
		if (CollectionUtils.isNotEmpty(DataCalculator.getFailedStores())) {
			int retryCount = 0;
			do {
				System.out.println("Re-trying to get store details for the failed stores with RETRY_COUNT -> " + retryCount);
				System.out.println("Size of the stores before reducing ->"+storeDataList.size());
				System.out.println("Size of the failed stores ->"+DataCalculator.getFailedStores().size());
				storeDataList = storeDataList.stream().filter(store -> !(true == store.hasError())).collect(Collectors.toList());
				System.out.println("Size after reducing ->"+storeDataList.size());
				DataCalculator.showFailedStores();
				List<String> storeFailedList = DataCalculator.getFailedStores();
				List<StoreDetails> reprocessStores = storeList.stream().filter(store -> storeFailedList.contains(store.getStoreNumber())).collect(Collectors.toList());
				DataCalculator.clearFailedStores();
				List<StoreReport> reprocessedStores = getReportData(reprocessStores);
				storeDataList.addAll(reprocessedStores);
				retryCount++;
			} while (Boolean.logicalAnd(retryCount < RETRY_SIZE, CollectionUtils.isNotEmpty(DataCalculator.getFailedStores())));
		}
		if(DETAILED_REPORT) {
			System.out.println("Getting CBO Feed data");
			try {
				cboFeedFuture.get();
			} catch (InterruptedException | ExecutionException e1) {
				System.out.println("Exception while connecting to CBO -> "+ e1.getMessage());
				e1.printStackTrace();
			}
			storeDataList.stream().forEach(storeReport -> {
				storeReport.setCBOCounts(cBOBusAreaCounts);
			});
		}
		System.out.println("Getting CBO Oracle data");
		try {
			final StoreReport cboReport = cboStoredDataCountFuture.get();
			final StoredDataCounts dataCounts = cboReport.getCboDataCounts();
			final PriceEventLists cboPriceEventLists = cboReport.getCboPriceEventLists();
			final Map<String, Integer> cboTxnCount = cboReport.getCboTxnCount();
			StringBuilder builder = new StringBuilder();
			storeDataList.stream().forEach(storeReport -> {
				storeReport.setCboDataCounts(dataCounts);
				storeReport.setCboPriceEventLists(cboPriceEventLists);
				int cboTotalCount = 0;
				if (cboTxnCount.containsKey(storeReport.getStoreNumber())) {
					cboTotalCount = cboTxnCount.get(storeReport.getStoreNumber());
				}
				if(cboTotalCount > 0) {
					builder.append("\n\nAdding cbo data for the store " + storeReport.getStoreNumber());
					if (CollectionUtils.isNotEmpty(storeReport.getLboTrxnCount())) {
						LaneCount totalLaneCount = storeReport.getLboTrxnCount().stream()
								.filter(lane -> WILDCARD_PERCENTAGE_SYMBOL.equals(lane.getLaneNumber()))
								.findAny().get();
						builder.append("\n\nFound total count " + cboTotalCount +" for the store " + storeReport.getStoreNumber());
						builder.append("\nBefore adding cbo count " + totalLaneCount.getLaneTotalCount());
						totalLaneCount.setLaneTotalCount(totalLaneCount.getLaneTotalCount() + cboTotalCount);
						builder.append("\nAfter adding cbo count " + totalLaneCount.getLaneTotalCount());
					}
					if(storeReport.getLboTrxnTotal() != null) {
						TransactionTotal lboTxnTotal = storeReport.getLboTrxnTotal();
						double cboTotalAmount = cboReport.getCboTxnAmount().get(storeReport.getStoreNumber());
						builder.append("\nFound total amount " + cboTotalAmount +" for the store " + storeReport.getStoreNumber());
						builder.append("\nBefore adding cbo amount " + lboTxnTotal.getTrxnTotal());
						lboTxnTotal.setTrxnTotal(BigDecimal.valueOf(lboTxnTotal.getTrxnTotal() + cboTotalAmount)
								.setScale(2, RoundingMode.UP).doubleValue());
						builder.append("\nAfter adding cbo amount " + lboTxnTotal.getTrxnTotal());
					}
				}
			});
			LOG.info(builder.toString());
		} catch (InterruptedException | ExecutionException e1) {
			System.out.println("Exception while connecting to CBO database for master data-> "+ e1.getMessage());
			e1.printStackTrace();
		}
		if (CollectionUtils.isNotEmpty(DataCalculator.getFailedStores())) {
			System.out.println(DataCalculator.getFailedStores().size() + " stores are failed again and stopped retry mechanism for the failed stores.");
			DataCalculator.showFailedStores();
			DataCalculator.clearFailedStores();
		}
		if (DETAILED_REPORT) {
			showFeedFailuresOnEachStores(storeDataList);
		}
		showProductAndPriceEventMismatchForEachStores(storeDataList);
		showMissingPriceEvents();
		if (UserProperties.IS_PRODUCT_CHECK_ENABLED) {
			showMIssingProducts(storeDataList);
		}
		if (UserProperties.IS_PRICE_EVENT_CHECK_ENABLED) {
			showMissingSpecificPriceEvents(storeDataList, UserProperties.PRICEVENTS);
		} 
		if (UserProperties.IS_DELETED_PRICE_EVENT_CHECK_ENABLED) {
			showMissingDeletedPriceEvents(storeDataList);
		}
		showMissingTransactionDetails(storeDataList);
		showLBOHealthDetails(storeDataList);
		showCssHealthDetails();
		shutdown();
		return storeDataList;
	}
	
	private static void showMissingSpecificPriceEvents(List<StoreReport> storeDataList, String[] priceEvents) {
		Map<String, Set<String>> missingPriceEvents = new HashMap<String, Set<String>>();
		storeDataList.stream().forEach(store -> {
			Set<String> searchedPriceEvents = store.getSearchedPriceEvents();
			if (searchedPriceEvents != null) {
				for (String priceEvent : priceEvents) {
					if (!searchedPriceEvents.contains(priceEvent)) {
						if (missingPriceEvents.containsKey(priceEvent)) {
							missingPriceEvents.get(priceEvent).add(store.getStoreNumber());
						} else {
							Set<String> storeList = new HashSet<String>();
							storeList.add(store.getStoreNumber());
							missingPriceEvents.put(priceEvent, storeList);
						}
					}
				}
			}
		});
		StringBuilder builder = new StringBuilder();
		if (missingPriceEvents.keySet().isEmpty()) {
			builder.append("\n\nAll given price events are present in the specified stores\n");
		} else {
			builder.append("\n\nMissing stores for specific price events -> ");
			for (String priceEvent : priceEvents) {
				builder.append(priceEvent + " ");
			}
		}
		for (String priceEvent : missingPriceEvents.keySet()) {
			builder.append("\n\n Missing price event " + priceEvent + " in below mentioned stores\n");
			missingPriceEvents.get(priceEvent).stream().forEach(store -> builder.append(store + " "));
		}
		builder.append("\n===========\nMissing Price Events DETAILS  ::\n===========\n");
		LOG.info(builder.toString());
	}
	
	private static void showMissingDeletedPriceEvents(List<StoreReport> storeDataList) {
		StringBuilder builder = new StringBuilder();
		builder.append("\nDeleted price events for specific styles -> \n");
		storeDataList.stream().forEach(store -> {
			Set<String> deletedPriceEvents = store.getDeletedPriceEvents();
			if (CollectionUtils.isNotEmpty(deletedPriceEvents)) {
				deletedPriceEvents.stream()
						.forEach(style -> builder.append(store.getStoreNumber() + " --> " + style + "\n"));
			}
		});
		LOG.info(builder.toString());
	}

	private static void showLBOHealthDetails(List<StoreReport> storeDataList) {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\nLBO Health Details\n");
		List<StoreReport> requiredStoreListForQueueDepth = storeDataList.stream()
			.filter(store -> BigDecimal.ZERO.equals(store.getLboQueueDepth())).collect(Collectors.toList());
		if(!requiredStoreListForQueueDepth.isEmpty()) {
			builder.append("\n\nUnsent Queue Depth For the Stores -- \n");
			requiredStoreListForQueueDepth.forEach(store -> {
					builder.append(store.getStoreNumber() + " --> " + store.getLboQueueDepth() + "\n");
				});
		} else {
			builder.append("\n\nUnsent Queue Depth is Empty for all the Stores \n");
		}
		LOG.info(builder.toString());
	}

	public static void main(String[] args) {
		StringBuilder builder = new StringBuilder();
		callEachLBOUrlAndUpdateLog(builder);
		System.out.println(builder.toString());
	}

	private static void callEachLBOUrlAndUpdateLog(StringBuilder builder) {
		final String storeUrl = "https://m%ssps01.unix.ctcwest.ctc:8443/api/v1/pos/project";
		ClientConfig config = new DefaultClientConfig(); 		
		Client client = Client.create(config);
		builder.append("\n\nLBO URL response for all the Stores");
		StoreProperties.postCheckStoreList.stream().forEach(store ->{
			WebResource resource = client.resource(UriBuilder.fromUri(String.format(storeUrl, store.getStoreNumber())).build());
			System.out.println(resource);
			builder.append(resource
				.accept(MediaType.APPLICATION_JSON).get(String.class));
			builder.append("\n\n");
		});
	}

	public static void showCssHealthDetails() {
		StringBuilder builder = new StringBuilder();
		Optional<CssHealth> cssHealth = new DataCalculator().getCSSHealth();
		builder.append("\n\nCSS Health Details\n");
		if(cssHealth.isPresent()) {
			builder.append("\n\nUnsent Queue Depth -- " + cssHealth.get().getUnSentQueueDepth());
			builder.append("\n\nNode 1 -- " + cssHealth.get().getNode1());
			builder.append("\n\nNode 2 -- " + cssHealth.get().getNode2());
			builder.append("\n\nNode 3 -- " + cssHealth.get().getNode3());
			builder.append("\n\nNode 4 -- " + cssHealth.get().getNode4());
		} else {
			builder.append("\n!!! CSS Nodes ate seens to be down. Nodes are not responding !!!\n");
		}
		LOG.info(builder.toString());
	}

	private static void showMissingTransactionDetails(List<StoreReport> storeDataList) {
		StringBuilder builder = new StringBuilder();
		storeDataList.stream().forEach(store -> {
			TransactionTotal lboTrxnTotal = store.getLboTrxnTotal();
			if(lboTrxnTotal != null) {
				nonVoidedTotalLBO += lboTrxnTotal.getNonVoidedTotal();
				voidedTotalLBO += lboTrxnTotal.getVoidedTotal();
				trxnTotalLBO += lboTrxnTotal.getTrxnTotal();
			}
		    
		    TransactionTotal cssTrxnTotal = store.getCssTrxnTotal();
		    if(cssTrxnTotal != null) {
			    nonVoidedTotalCSS += cssTrxnTotal.getNonVoidedTotal();
			    voidedTotalCSS += cssTrxnTotal.getVoidedTotal();
			    trxnTotalCSS += cssTrxnTotal.getTrxnTotal();
		    }
		    
		    if(store.getLboTrxnCount() != null) {
			    for(LaneCount laneCount : store.getLboTrxnCount()) {
			    	String laneNumber = laneCount.getLaneNumber();
					if(StringUtils.equals(laneNumber, "%")) {
						voidedCountLBO += laneCount.getLaneVoidedCount();
						nonVoidedCountLBO += laneCount.getLaneNonVoidedCount();
						trxnCountLBO += laneCount.getLaneTotalCount();
					}
			    }
		    }
		    if(store.getCssTrxnCount() != null) {
			    for(LaneCount laneCount : store.getCssTrxnCount()) {
			    	String laneNumber = laneCount.getLaneNumber();
					if(StringUtils.equals(laneNumber, "%")) {
						voidedCountCSS += laneCount.getLaneVoidedCount();
						nonVoidedCountCSS += laneCount.getLaneNonVoidedCount();
						trxnCountCSS += laneCount.getLaneTotalCount();
					}
			    }
		    }
		    journalCount += store.getJournalCount();
		});
		builder.append(String.format("\n===========\nTotal Transaction Details for the checkDate %s for all of the Stores ::\n===========\n", UserProperties.GIVEN_DATE));
		builder.append("\nnonVoidedTotalLBO -- $" +BigDecimal.valueOf(nonVoidedTotalLBO).setScale(2, RoundingMode.UP));
		builder.append("\nvoidedTotalLBO -- $" +BigDecimal.valueOf(voidedTotalLBO).setScale(2, RoundingMode.UP));
		builder.append("\ntrxnTotalLBO -- $" +BigDecimal.valueOf(trxnTotalLBO).setScale(2, RoundingMode.UP));
		builder.append("\n\n\nnonVoidedTotalCSS -- $" +BigDecimal.valueOf(nonVoidedTotalCSS).setScale(2, RoundingMode.UP));
		builder.append("\nvoidedTotalCSS -- $" +BigDecimal.valueOf(voidedTotalCSS).setScale(2, RoundingMode.UP));
		builder.append("\ntrxnTotalCSS -- $" +BigDecimal.valueOf(trxnTotalCSS).setScale(2, RoundingMode.UP));
		builder.append("\n\nTransaction Amount Difference between CSS and LBO -- $" + BigDecimal.valueOf(trxnTotalLBO - trxnTotalCSS).setScale(2, RoundingMode.UP));
		builder.append("\n\n\nvoidedCountLBO -- " +voidedCountLBO);
		builder.append("\nnonVoidedCountLBO -- " +nonVoidedCountLBO);
		builder.append("\ntrxnCountLBO -- " +trxnCountLBO);
		builder.append("\n\n\nvoidedCountCSS -- " +voidedCountCSS);
		builder.append("\nnonVoidedCountCSS -- " +nonVoidedCountCSS);
		builder.append("\ntrxnCountCSS -- " +trxnCountCSS);
		builder.append("\n\nTransaction Count Difference between CSS and LBO -- " + (trxnCountLBO - trxnCountCSS));
		//builder.append("\n\n\njournalCount -- " +journalCount);
		LOG.info(builder.toString());
	}

	private static void showMIssingProducts(List<StoreReport> storeDataList) {
		Map<String, Set<String>> missingProducts = new HashMap<String, Set<String>>();
		storeDataList.stream().forEach(store -> {
			List<String> searchedProducts = store.getSearchedProducts();
			if (searchedProducts != null) {
				for (String product : UserProperties.PRODUCTS) {
					if (!searchedProducts.contains(product)) {
						if (missingProducts.containsKey(product)) {
							missingProducts.get(product).add(store.getStoreNumber());
						} else {
							Set<String> storeList = new HashSet<String>();
							storeList.add(store.getStoreNumber());
							missingProducts.put(product, storeList);
						}
					}
				}
			}
		});
		StringBuilder builder = new StringBuilder();
		if (missingProducts.keySet().isEmpty()) {
			builder.append("\n\nAll the given products are present in the specified stores\n");
		} else {
			for (String product : missingProducts.keySet()) {
				builder.append(String.format("\nMissing product '%s' for the stores : \n", product));
				missingProducts.get(product).stream().forEach(store -> builder.append(store + " "));
			}
		}
		builder.append("\n===========\nMissing Products DETAILS  ::\n===========\n");
		LOG.info(builder.toString());
	}

	private static void showMissingPriceEvents() {
		StringBuilder builder = new StringBuilder();
		builder.append("\n\nMissing Price Events count " + MISSING_PRICE_EVENTS.size() + "\n");
		MISSING_PRICE_EVENTS.stream().forEach(event -> builder.append(event + " "));
		
		/*builder.append("\n\nMissing Price Events count for store 76 " + MISSING_PRICE_EVENTS_76.size() + "\n");
		MISSING_PRICE_EVENTS_76.stream().forEach(event -> builder.append(event + " "));
		
		builder.append("\n\nMissing Basic Price Events count " + MISSING_BASIC_PRICE_EVENTS.size() + "\n");
		MISSING_BASIC_PRICE_EVENTS.stream().forEach(event -> builder.append(event + " "));*/
		
		builder.append("\n\nMissing Special Price Events count " + MISSING_SPECIAL_PRICE_EVENTS.size() + "\n");
		MISSING_SPECIAL_PRICE_EVENTS.stream().forEach(event -> builder.append(event + " "));
		
		builder.append("\n\nMissing XForY Events count " + MISSING_XFORY_PRICE_EVENTS.size() + "\n");
		MISSING_XFORY_PRICE_EVENTS.stream().forEach(event -> builder.append(event + " "));
		
		builder.append("\n\nMissing BOGO Price Events count " + MISSING_BOGO_PRICE_EVENTS.size() + "\n");
		MISSING_BOGO_PRICE_EVENTS.stream().forEach(event -> builder.append(event + " "));
		
		LOG.info(builder.toString());
	}

	private static void showProductAndPriceEventMismatchForEachStores(List<StoreReport> storeDataList) {
		StoredDataCounts lboDataCounts;
		Optional<StoredDataCounts> cboDataCounts = Optional.empty();
		PriceEventLists lboPriceEventLists;
		PriceEventLists cboPriceEventLists;
		StringBuilder builder = new StringBuilder();
		cboPriceEventLists = storeDataList.get(0).getCboPriceEventLists();
		//Code for showing up active CBO price events
		/*builder.append("\nCBO Basic Price Events - > ");
		cboPriceEventLists.getBasicPriceEvents().stream().forEach(events -> builder.append(events + " "));
		builder.append("\nCBO Special Price Events - > ");
		cboPriceEventLists.getSpecialPriceEvents().stream().forEach(events -> builder.append(events + " "));
		builder.append("\nCBO BOGO Price Events - > ");
		cboPriceEventLists.getBogoPriceEvents().stream().forEach(events -> builder.append(events + " "));
		builder.append("\nCBO XForY Price Events - > ");
		cboPriceEventLists.getxForYPriceEvents().stream().forEach(events -> builder.append(events + " "));*/
		Map<String, Map<Integer, List<String>>> priceEventPattern = new HashMap<String, Map<Integer, List<String>>>();
		int storeCount = 0;
		for (StoreReport storeReport : storeDataList) {
			lboDataCounts = storeReport.getLboDataCounts();
			cboDataCounts = Optional.of(storeReport.getCboDataCounts());
			lboPriceEventLists = storeReport.getLboPriceEventLists();
			cboPriceEventLists = storeReport.getCboPriceEventLists();
			if (lboDataCounts != null && cboDataCounts != null
					&& (/*(Integer.compare(lboDataCounts.getUpc(), cboDataCounts.getUpc()) != 0)
							|| (Integer.compare(lboDataCounts.getProduct(), cboDataCounts.getProduct()) != 0)
							|| */(Integer.compare(lboDataCounts.getBasePrice(), cboDataCounts.get().getBasePrice()) != 0)
							|| (Integer.compare(lboDataCounts.getSpecialPrice(), cboDataCounts.get().getSpecialPrice()) != 0)
							|| (Integer.compare(lboDataCounts.getBogo(), cboDataCounts.get().getBogo()) != 0)
							|| (Integer.compare(lboDataCounts.getxForY(),
									cboDataCounts.get().getxForY()) != 0))) {
				builder.append("\nData mismatch for the Store " + storeReport.getStoreNumber());
				builder.append("\n");
				/*if(Integer.compare(lboDataCounts.getUpc(), cboDataCounts.getUpc()) != 0) {
					builder.append("Mismatch in UPC\n");
					builder.append("CBO data - > " + cboDataCounts.getUpc());
					builder.append(" LBO data - > " + lboDataCounts.getUpc());
					builder.append("\n");
				}*/
				/*if(Integer.compare(lboDataCounts.getProduct(), cboDataCounts.getProduct()) != 0) {
					builder.append("Mismatch in Products\n");
					builder.append("CBO data - > " + cboDataCounts.getProduct());
					builder.append(" LBO data - > " + lboDataCounts.getProduct());
					builder.append("\n");
				}*/
				if(Integer.compare(lboDataCounts.getBasePrice(), cboDataCounts.get().getBasePrice()) != 0) {
					builder.append("Mismatch in Basic Price\n");
					builder.append("CBO data count - > " + cboDataCounts.get().getBasePrice());
					builder.append(", LBO data count - > " + lboDataCounts.getBasePrice());
					Set<String> priceEvents = updateMissingPriceEvents(cboPriceEventLists.getBasicPriceEvents(), lboPriceEventLists.getBasicPriceEvents(), builder, storeReport.getStoreNumber());
					if("076".equalsIgnoreCase(storeReport.getStoreNumber())) {
						MISSING_PRICE_EVENTS_76.addAll(priceEvents);
					} else {
						MISSING_PRICE_EVENTS.addAll(priceEvents);
					}
					builder.append("\n");
					updateBasePricePattern(priceEventPattern, storeReport);
					MISSING_BASIC_PRICE_EVENTS.addAll(priceEvents);
				}
				if(Integer.compare(lboDataCounts.getSpecialPrice(), cboDataCounts.get().getSpecialPrice()) != 0) {
					builder.append("Mismatch in Special Price Events\n");
					builder.append("CBO data count - > " + cboDataCounts.get().getSpecialPrice());
					builder.append(", LBO data count - > " + lboDataCounts.getSpecialPrice());
					Set<String> priceEvents = updateMissingPriceEvents(cboPriceEventLists.getSpecialPriceEvents(), lboPriceEventLists.getSpecialPriceEvents(), builder, storeReport.getStoreNumber());
					builder.append("\n");
					updateSpecialPricePattern(priceEventPattern, storeReport);
					MISSING_SPECIAL_PRICE_EVENTS.addAll(priceEvents);
				}
				if(Integer.compare(lboDataCounts.getBogo(), cboDataCounts.get().getBogo()) != 0) {
					builder.append("Mismatch in BOGO Price Events\n");
					builder.append("CBO data count - > " + cboDataCounts.get().getBogo());
					builder.append(", LBO data count - > " + lboDataCounts.getBogo());
					Set<String> priceEvents = updateMissingPriceEvents(cboPriceEventLists.getBogoPriceEvents(), lboPriceEventLists.getBogoPriceEvents(), builder, storeReport.getStoreNumber());
					builder.append("\n");
					updateBogoPricePattern(priceEventPattern, storeReport);
					MISSING_BOGO_PRICE_EVENTS.addAll(priceEvents);
				}
				if(Integer.compare(lboDataCounts.getxForY(), cboDataCounts.get().getxForY()) != 0) {
					builder.append("Mismatch in XForY Price Events\n");
					builder.append("CBO data count - > " + cboDataCounts.get().getxForY());
					builder.append(", LBO data count - > " + lboDataCounts.getxForY());
					Set<String> priceEvents = updateMissingPriceEvents(cboPriceEventLists.getxForYPriceEvents(), lboPriceEventLists.getxForYPriceEvents(), builder, storeReport.getStoreNumber());
					builder.append("\n");
					updateXForYPricePattern(priceEventPattern, storeReport);
					MISSING_XFORY_PRICE_EVENTS.addAll(priceEvents);
				}
				storeCount++;
			}
		}
		if(storeCount != 0) {
			showPriceEventMissingPattern(priceEventPattern, cboDataCounts);
			LOG.info("\n===========\nDETAILS  ::\n===========\n");
			LOG.info(String.format("Master Data not matching for the %s Store(s) for the effective date %s\n", storeCount, UserProperties.PRICE_EVENT_EFFECTIVE_DATE) + builder.toString());
		}
	}

	private static Set<String> updateMissingPriceEvents(List<String> cboPriceEvents, List<String> lboPriceEvents,
			StringBuilder builder, String store) {
		Set<String> cboBasicPriceEvents = SetUtils.EMPTY_SET;
		if (cboPriceEvents.size() > lboPriceEvents.size()) {
			builder.append("\nMissing Price Events - > ");
			Set<String> lboBasicPriceEvents = new HashSet<>(lboPriceEvents);
			cboBasicPriceEvents = new HashSet<>(cboPriceEvents);

			cboBasicPriceEvents.removeAll(lboBasicPriceEvents);
			cboBasicPriceEvents.stream().forEach(events -> builder.append(events + " "));
		}
		return cboBasicPriceEvents;
	}

	private static void showPriceEventMissingPattern(Map<String, Map<Integer, List<String>>> priceEventPattern,
			Optional<StoredDataCounts> cboDataCounts) {
		StringBuilder builder = new StringBuilder();
		builder.append("===========\n");
		builder.append("SUMMARY  ::\n");
		builder.append("===========\n");
		builder.append("\nMissing price event pattern for all the Stores ");
		builder.append("\n");
		Map<Integer, List<String>> pricePattern;
		for (String patternFor : priceEventPattern.keySet()) {
			pricePattern = priceEventPattern.get(patternFor);
			builder.append(patternFor + " ::\n");
			updateForEachPriceCount(pricePattern, builder, cboDataCounts, patternFor);
		}
		System.out.println(builder.toString());
	}

	private static void updateForEachPriceCount(Map<Integer, List<String>> pricePattern, StringBuilder builder,
			Optional<StoredDataCounts> cboDataCounts, String patternFor) {
		int cboCount;
		for (Integer count : pricePattern.keySet()) {
			cboCount = getCBOCount(patternFor, cboDataCounts);
			builder.append("CBO Count     : " + cboCount + "\n");
			builder.append("LBO Count     : " + count + "\n");
			builder.append("Missing Count : " + (cboCount - count) + "\nStores : ");
			pricePattern.get(count).stream().forEach(store -> builder.append(store + " "));
			builder.append("\n\n");
		}
	}

	private static Integer getCBOCount(String pricePattern, Optional<StoredDataCounts> cboDataCounts) {
		switch (pricePattern) {
		case BASE_PRICE:
			return cboDataCounts.orElse(new StoredDataCounts()).getBasePrice();
		case SPECIAL_PRICE:
			return cboDataCounts.orElse(new StoredDataCounts()).getSpecialPrice();
		case BOGO_PRICE_EVENTS:
			return cboDataCounts.orElse(new StoredDataCounts()).getBogo();
		case X_FOR_Y_PRICE_EVENTS:
			return cboDataCounts.orElse(new StoredDataCounts()).getxForY();
		default:
			return 0;
		}
	}

	private static void updateBasePricePattern(Map<String, Map<Integer, List<String>>> priceEventPattern,
			StoreReport storeReport) {
		if (!priceEventPattern.containsKey(BASE_PRICE)) {
			Map<Integer, List<String>> basePricePattern = new HashMap<Integer, List<String>>();
			priceEventPattern.put(BASE_PRICE, basePricePattern);
		}
		updatePattern(priceEventPattern.get(BASE_PRICE), storeReport.getLboDataCounts().getBasePrice(), storeReport.getStoreNumber());
	}
	
	private static void updateSpecialPricePattern(Map<String, Map<Integer, List<String>>> priceEventPattern,
			StoreReport storeReport) {
		if (!priceEventPattern.containsKey(SPECIAL_PRICE)) {
			Map<Integer, List<String>> specialPricePattern = new HashMap<Integer, List<String>>();
			priceEventPattern.put(SPECIAL_PRICE, specialPricePattern);
		}
		updatePattern(priceEventPattern.get(SPECIAL_PRICE), storeReport.getLboDataCounts().getSpecialPrice(), storeReport.getStoreNumber());
	}
	
	private static void updateBogoPricePattern(Map<String, Map<Integer, List<String>>> priceEventPattern,
			StoreReport storeReport) {
		if (!priceEventPattern.containsKey(BOGO_PRICE_EVENTS)) {
			Map<Integer, List<String>> bogoPricePattern = new HashMap<Integer, List<String>>();
			priceEventPattern.put(BOGO_PRICE_EVENTS, bogoPricePattern);
		}
		updatePattern(priceEventPattern.get(BOGO_PRICE_EVENTS), storeReport.getLboDataCounts().getBogo(), storeReport.getStoreNumber());
	}
	
	private static void updateXForYPricePattern(Map<String, Map<Integer, List<String>>> priceEventPattern,
			StoreReport storeReport) {
		if (!priceEventPattern.containsKey(X_FOR_Y_PRICE_EVENTS)) {
			Map<Integer, List<String>> xForYPricePattern = new HashMap<Integer, List<String>>();
			priceEventPattern.put(X_FOR_Y_PRICE_EVENTS, xForYPricePattern);
		}
		updatePattern(priceEventPattern.get(X_FOR_Y_PRICE_EVENTS), storeReport.getLboDataCounts().getxForY(), storeReport.getStoreNumber());
	}
	
	private static void updatePattern(Map<Integer, List<String>> priceEventPattern, int counts, String store) {
		if (!priceEventPattern.containsKey(counts)) {
			priceEventPattern.put(counts, new ArrayList<String>());
		}
		priceEventPattern.get(counts).add(store);
	}

	private static void showFeedFailuresOnEachStores(List<StoreReport> storeDataList) {
		BusinessAreaCounts lBOCounts;
		BusinessAreaCounts dTECounts;
		StringBuilder builder = new StringBuilder();
		int storeCount = 0;
		for (StoreReport storeReport : storeDataList) {
			lBOCounts = storeReport.getLBOCounts();
			dTECounts = storeReport.getDTECounts();
			if (lBOCounts != null && dTECounts != null
					&& ((lBOCounts.getHierarchyCount() < dTECounts.getHierarchyCount())
							|| (lBOCounts.getBuCount() < dTECounts.getBuCount())
							|| (lBOCounts.getPriceAdvCount() < dTECounts.getPriceAdvCount())
							|| (lBOCounts.getPriceCount() < dTECounts.getPriceCount())
							|| (lBOCounts.getProductCount() < dTECounts.getProductCount())
							|| (lBOCounts.getTaxExemptionCount() < dTECounts.getTaxExemptionCount()))) {
				builder.append("\nFeed mismatch for the Store " + storeReport.getStoreNumber());
				builder.append("\n");
				if (lBOCounts.getHierarchyCount() < dTECounts.getHierarchyCount()) {
					builder.append("Mismatch in HierarchyCount\n");
				}
				if (lBOCounts.getBuCount() < dTECounts.getBuCount()) {
					builder.append("Mismatch in BuCount\n");
				}
				if (lBOCounts.getPriceAdvCount() < dTECounts.getPriceAdvCount()) {
					builder.append("Mismatch in PriceAdvCount\n");
				}
				if (lBOCounts.getPriceCount() < dTECounts.getPriceCount()) {
					builder.append("Mismatch in PriceCount\n");
				}
				if (lBOCounts.getProductCount() < dTECounts.getProductCount()) {
					builder.append("Mismatch in ProductCount\n");
				}
				if (lBOCounts.getTaxExemptionCount() < dTECounts.getTaxExemptionCount()) {
					builder.append("Mismatch in TaxExemptionCount\n");
				}
				storeCount++;
			}
		}
		LOG.info(String.format("Feeds are not matching for the date %s for the %s Stores \n", UserProperties.GIVEN_DATE,
				storeCount) + builder.toString());
	}

	private static List<StoreReport> getReportData(List<StoreDetails> storeList) {
		int storeSize = storeList.size();
		int batchSize = storeSize / BATCH_SIZE;
		if (storeSize % BATCH_SIZE != 0) {
			batchSize += 1;
		}
		System.out.println("\nStoreSize->" + storeSize);
		System.out.println("batchSize->" + batchSize);
		List<StoreReport> storeDataList = new ArrayList<StoreReport>();
		for (int i = 0; i < batchSize; i++) {
			int fromIndex = i * BATCH_SIZE;
			int toIndex = fromIndex + BATCH_SIZE;
			if (toIndex > storeSize) {
				toIndex = storeSize;
			}
			System.out.println("fromIndex->" + fromIndex);
			System.out.println("toIndex->" + toIndex);
			List<StoreDetails> customStoreList = storeList.subList(fromIndex, toIndex);
			System.out.println("Staring to process stores size -> "+customStoreList.size());
			StringBuilder stringBuilder = new StringBuilder();
			customStoreList.stream().forEach(store -> stringBuilder.append(store.getStoreNumber() + " ,"));
			System.out.println(stringBuilder.toString());
			List<Future<StoreInfo>> futureList = new ArrayList<Future<StoreInfo>>();
			System.out.println("\nSubmitting parallel calls to generate store report");
			for (StoreDetails storeDetails : customStoreList) {
				StoreInfo storeInfoInput = new StoreInfo(storeDetails);
				Future<StoreInfo> reportFuture = executorService.submit(() -> dataCalculator.getStoreReport(storeInfoInput));
				futureList.add(reportFuture);
			}
			System.out.println("Finished submittion of parallel calls for all the stores");
			for (Future<StoreInfo> future : futureList) {
				StoreReport storeReport;
				try {
					StoreInfo storeInfoOutput = future.get(2, TimeUnit.MINUTES);
					storeReport = storeInfoOutput.getStoreReport();
					if(storeReport != null) {
						System.out.println("Finished parallel call for the store " + storeInfoOutput.getStoreDetails().getStoreNumber());
					} else {
						System.out.println("Error while executing parallel call for the store " + storeInfoOutput.getStoreDetails().getStoreNumber());
					}
				} catch (InterruptedException e) {
					storeReport = new StoreReport();
					System.out.println(e.getMessage());
					e.printStackTrace();
				} catch (ExecutionException e) {
					storeReport = new StoreReport();
					System.out.println(e.getMessage());
					e.printStackTrace();
				} catch (TimeoutException e) {
					storeReport = new StoreReport();
					System.out.println(e.getMessage());
					e.printStackTrace();
				}
				storeDataList.add(storeReport);
			}
		}
		
		return storeDataList;
	}

	public static void shutdown() {
		dataCalculator.shutdown();
        executorService.shutdown();
    }
}
