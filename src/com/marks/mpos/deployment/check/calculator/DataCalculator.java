package com.marks.mpos.deployment.check.calculator;

import static com.marks.mpos.deployment.check.properties.UserProperties.POST_CHECK_DATE;
import static com.marks.mpos.deployment.check.properties.UserProperties.DETAILED_REPORT;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.marks.mpos.deployment.check.beans.BusinessAreaCounts;
import com.marks.mpos.deployment.check.beans.CssHealth;
import com.marks.mpos.deployment.check.beans.LaneCount;
import com.marks.mpos.deployment.check.beans.MissingPriceEvent;
import com.marks.mpos.deployment.check.beans.PriceEventLists;
import com.marks.mpos.deployment.check.beans.StoreDetails;
import com.marks.mpos.deployment.check.beans.StoreInfo;
import com.marks.mpos.deployment.check.beans.StoreReport;
import com.marks.mpos.deployment.check.beans.StoredDataCounts;
import com.marks.mpos.deployment.check.beans.TransactionTotal;
import com.marks.mpos.deployment.check.businessAreas.BusinessAreasDownloader;
import com.marks.mpos.deployment.check.connection.ConnectionProvider;
import com.marks.mpos.deployment.check.properties.UserProperties;
import com.marks.mpos.deployment.check.utils.DateUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.*;
import static com.marks.mpos.deployment.check.queries.IDatabaseQueries.*;

public class DataCalculator {
	private static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static final Set<String> failedStores = new HashSet<String>();
	private ExecutorService executor;

	public DataCalculator(ExecutorService executor) {
		this.executor = executor;
	}
	
	public DataCalculator() {
		this.executor = Executors.newCachedThreadPool();
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public StoreInfo getStoreReport(StoreInfo storeInfo) {
		StoreDetails storeDetails = storeInfo.getStoreDetails();
		StoreReport storeReport = new StoreReport();
		storeReport.setStoreNumber(storeDetails.getStoreNumber());
		List<Future<BusinessAreaCounts>> bACFutureList = new ArrayList<Future<BusinessAreaCounts>>();
		BusinessAreaCounts dteBusinessAreaCounts = new BusinessAreaCounts();
		BusinessAreaCounts lboBusinessAreaCounts = new BusinessAreaCounts();
		List<String> activeLanesList = new ArrayList<String>();

		try {
			final Session sshSessionLbo = ConnectionProvider.openSSHSessionToLBO(storeDetails);
			final Session sshSessionDte = ConnectionProvider.getSshSessionToDte(DTE_NODE_1);
			if(DETAILED_REPORT) {
				String DTE_POS_FOLDER = DTE_FOLDER + "/" + "m" + storeReport.getStoreNumber() + "sps01" + "/" + POST_CHECK_DATE
						+ "/" + "pos";
				String DTE_PE_FOLDER = DTE_FOLDER + "/" + "m" + storeReport.getStoreNumber() + "sps01" + "/" + POST_CHECK_DATE
						+ "/" + "pricing";	
			
				String POS_FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator + POST_CHECK_DIR
						+ File.separator + POST_CHECK_DATE + File.separator + "business_areas" + File.separator
						+ storeReport.getStoreNumber() + File.separator + "DTE" + File.separator + "pos";
				String PE_FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator + POST_CHECK_DIR
						+ File.separator + POST_CHECK_DATE + File.separator + "business_areas" + File.separator
						+ storeReport.getStoreNumber() + File.separator + "DTE" + File.separator + "pe_feeds";
				new File(POS_FILE_PATH).mkdirs();
				new File(PE_FILE_PATH).mkdirs();
				
				BusinessAreasDownloader dtePosDownloader = new BusinessAreasDownloader();
				BusinessAreasDownloader dtePeDownloader = new BusinessAreasDownloader();
				
				List<Callable<BusinessAreaCounts>> dtefeedDownloader = Arrays.asList(
						() -> { dtePosDownloader.downloadFiles(sshSessionDte, POS_FILE_PATH, DTE_POS_FOLDER, "POS", dteBusinessAreaCounts); return dteBusinessAreaCounts;},
				        () -> { dtePeDownloader.downloadFiles(sshSessionDte, PE_FILE_PATH, DTE_PE_FOLDER, "PE", dteBusinessAreaCounts); return dteBusinessAreaCounts;}
				);
				bACFutureList.addAll(executor.invokeAll(dtefeedDownloader));
				
				/*Future<BusinessAreaCounts> dtePosFeedFuture = executor.submit(() -> {
					dtePosDownloader.downloadFiles(sshSessionDte, POS_FILE_PATH, DTE_POS_FOLDER, "POS", dteBusAreaCounts);
					return dteBusAreaCounts;
				});
				Future<BusinessAreaCounts> dtePeFeedFuture = executor.submit(() -> {
					dtePeDownloader.downloadFiles(sshSessionDte, PE_FILE_PATH, DTE_PE_FOLDER, "PE", dteBusAreaCounts);
					return dteBusAreaCounts;
				});
				bACFutureList.add(dtePosFeedFuture);
				bACFutureList.add(dtePeFeedFuture);*/
				
				String POS_FILE_PATH2 = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator + POST_CHECK_DIR
						+ File.separator + POST_CHECK_DATE + File.separator + "business_areas" + File.separator
						+ storeReport.getStoreNumber() + File.separator + "LBO" + File.separator + "pos";
				String PE_FILE_PATH2 = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator + POST_CHECK_DIR
						+ File.separator + POST_CHECK_DATE + File.separator + "business_areas" + File.separator
						+ storeReport.getStoreNumber() + File.separator + "LBO" + File.separator + "pe_feeds";
				new File(POS_FILE_PATH2).mkdirs();
				new File(PE_FILE_PATH2).mkdirs();
	
				BusinessAreasDownloader lboPosDownloader = new BusinessAreasDownloader();
				BusinessAreasDownloader lboPeDownloader = new BusinessAreasDownloader();
				List<Callable<BusinessAreaCounts>> lbofeedDownloader = Arrays.asList(
						() -> { lboPosDownloader.downloadFiles(sshSessionLbo, POS_FILE_PATH2, STORE_POS_FOLDER, "POS", lboBusinessAreaCounts); return lboBusinessAreaCounts;},
				        () -> { lboPeDownloader.downloadFiles(sshSessionLbo, PE_FILE_PATH2, STORE_PE_FOLDER, "PE", lboBusinessAreaCounts); return lboBusinessAreaCounts;}
				);
				bACFutureList.addAll(executor.invokeAll(lbofeedDownloader));
				/*Future<BusinessAreaCounts> lboPosFeedFuture = executor.submit(() -> {
					lboPosDownloader.downloadFiles(sshSessionLbo, POS_FILE_PATH2, STORE_POS_FOLDER, "POS", businessAreaCounts);
					return businessAreaCounts;
				});
				Future<BusinessAreaCounts> lboPeFeedFuture = executor.submit(() -> {
					lboPeDownloader.downloadFiles(sshSessionLbo, PE_FILE_PATH2, STORE_PE_FOLDER, "PE", businessAreaCounts);
					return businessAreaCounts;
				});
				bACFutureList.add(lboPosFeedFuture);
				bACFutureList.add(lboPeFeedFuture);*/
			}
			final Connection connectionLBO = ConnectionProvider.getMariaDBConnection(storeDetails, MARIA_DB_NAME_BO);
			ResultSet rsActiveLanes = connectionLBO.createStatement().executeQuery(LBO_ACTIVE_LANES_QUERY);
			while(rsActiveLanes.next()) {
				activeLanesList.add(rsActiveLanes.getString(1));
			}
			activeLanesList.add(WILDCARD_PERCENTAGE_SYMBOL);
			
			Future<StoreReport> lboQuery = executor.submit(() -> {
				StoreReport lboReport = new StoreReport();
				if (UserProperties.IS_TRANSACTION_CHECK_ENABLED) {
					try {
						List<LaneCount> lboLaneCountList = new ArrayList<LaneCount>();
						for (String laneNumberStr : activeLanesList) {
							LaneCount laneCountLBO = new LaneCount();					
							laneCountLBO.setLaneNumber(laneNumberStr);
							final String LANE_COUNT_SEARCH_PARAMETER = storeDetails.getStoreNumber() + laneNumberStr + POST_CHECK_DATE + WILDCARD_PERCENTAGE_SYMBOL + QUOTE_SYMBOL + SEMI_COLON_SYMBOL;
							
							ResultSet rsLaneVoidedCount = connectionLBO.createStatement().executeQuery(LBO_LANE_VOIDED_COUNT_QUERY + LANE_COUNT_SEARCH_PARAMETER);
							while (rsLaneVoidedCount.next()) laneCountLBO.setLaneVoidedCount(rsLaneVoidedCount.getInt(1));
							ResultSet rsLaneNonVoidedCount = connectionLBO.createStatement().executeQuery(LBO_LANE_NON_VOIDED_COUNT_QUERY + LANE_COUNT_SEARCH_PARAMETER);
							while (rsLaneNonVoidedCount.next()) laneCountLBO.setLaneNonVoidedCount(rsLaneNonVoidedCount.getInt(1));
							ResultSet rsLaneTotalCount = connectionLBO.createStatement().executeQuery(LBO_LANE_TOTAL_COUNT_QUERY + LANE_COUNT_SEARCH_PARAMETER);
							while (rsLaneTotalCount.next()) laneCountLBO.setLaneTotalCount(rsLaneTotalCount.getInt(1));
							
							lboLaneCountList.add(laneCountLBO);
						}
						lboReport.setLboTrxnCount(lboLaneCountList);
						
						TransactionTotal lboTrxnTotal = new TransactionTotal();
						final String TRXN_TOTAL_SEARCH_PARAMETER = storeDetails.getStoreNumber() + WILDCARD_PERCENTAGE_SYMBOL + POST_CHECK_DATE + WILDCARD_PERCENTAGE_SYMBOL + QUOTE_SYMBOL + SEMI_COLON_SYMBOL;
						
						ResultSet rsVoidedTotal = connectionLBO.createStatement().executeQuery(LBO_VOIDED_TOTAL_QUERY + TRXN_TOTAL_SEARCH_PARAMETER);
						while (rsVoidedTotal.next()) lboTrxnTotal.setVoidedTotal(rsVoidedTotal.getDouble(1));
						ResultSet rsNonVoidedTotal = connectionLBO.createStatement().executeQuery(LBO_NON_VOIDED_TOTAL_QUERY + TRXN_TOTAL_SEARCH_PARAMETER);
						while (rsNonVoidedTotal.next()) lboTrxnTotal.setNonVoidedTotal(rsNonVoidedTotal.getDouble(1));
						ResultSet rsVTrxnTotal = connectionLBO.createStatement().executeQuery(LBO_TRXN_TOTAL_QUERY + TRXN_TOTAL_SEARCH_PARAMETER);
						while (rsVTrxnTotal.next()) lboTrxnTotal.setTrxnTotal(rsVTrxnTotal.getDouble(1));
						lboReport.setLboTrxnTotal(lboTrxnTotal);
						
						ResultSet rsGetQueueDepth = connectionLBO.createStatement().executeQuery(LBO_QUEUE_DEPTH_CHECK);
						while (rsGetQueueDepth.next()) lboReport.setLboQueueDepth(rsGetQueueDepth.getInt(1));
						ResultSet rsGetStuckTransactionCount = connectionLBO.createStatement().executeQuery(LBO_STUCK_TRANSACTION_QUERY);
						while (rsGetStuckTransactionCount.next()) lboReport.setLboStuckTransactionCount(rsGetStuckTransactionCount.getInt(1));
					} catch (SQLException e) {
						System.out.println("SQL Exception while generating LBO data for the store -> " + storeDetails.getStoreNumber());
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
				return lboReport;
			});
			
			Future<StoreReport> lboStoredDataCountFuture = executor.submit(() -> {
				StoreReport lboReportData = new StoreReport();
				StoredDataCounts lboStoredDataCounts = new StoredDataCounts();
				PriceEventLists lboPriceEventLists = new PriceEventLists();
				try {
					ResultSet rsLBOUpcCount = connectionLBO.createStatement().executeQuery(LBO_UPC_COUNT);
					while (rsLBOUpcCount.next()) lboStoredDataCounts.setUpc(rsLBOUpcCount.getInt(1));
					ResultSet rsLBOItemCount = connectionLBO.createStatement().executeQuery(LBO_ITEM_COUNT);
					while (rsLBOItemCount.next()) lboStoredDataCounts.setProduct(rsLBOItemCount.getInt(1));
					String priceEventEffectiveDate = "'" + DateUtil.getFormattedDate(UserProperties.PRICE_EVENT_EFFECTIVE_DATE, "yyyy-MM-dd") + "'";
					ResultSet rsLBOBasePriceCount = connectionLBO.createStatement().executeQuery(LBO_BASE_PRICE_COUNT + priceEventEffectiveDate);
					while (rsLBOBasePriceCount.next()) lboPriceEventLists.getBasicPriceEvents().add(rsLBOBasePriceCount.getString("ITEMPRICEEVENTNUMBER"));
					lboStoredDataCounts.setBasePrice(lboPriceEventLists.getBasicPriceEvents().size());
					ResultSet rsLBOBogoCount = connectionLBO.createStatement().executeQuery(LBO_BOGO_COUNT + priceEventEffectiveDate);
					while (rsLBOBogoCount.next()) lboPriceEventLists.getBogoPriceEvents().add(rsLBOBogoCount.getString("PriceDerivRuleEventNumber"));
					lboStoredDataCounts.setBogo(lboPriceEventLists.getBogoPriceEvents().size());
					ResultSet rsLBOSpecialPriceCount = connectionLBO.createStatement().executeQuery(LBO_SPECIAL_PRICE_COUNT + priceEventEffectiveDate);
					while (rsLBOSpecialPriceCount.next()) lboPriceEventLists.getSpecialPriceEvents().add(rsLBOSpecialPriceCount.getString("PriceDerivRuleEventNumber"));
					lboStoredDataCounts.setSpecialPrice(lboPriceEventLists.getSpecialPriceEvents().size());
					ResultSet rsLBOXforYCount = connectionLBO.createStatement().executeQuery(LBO_XFORY_COUNT + priceEventEffectiveDate);
					while (rsLBOXforYCount.next()) lboPriceEventLists.getxForYPriceEvents().add(rsLBOXforYCount.getString("PriceDerivRuleEventNumber"));
					lboStoredDataCounts.setxForY(lboPriceEventLists.getxForYPriceEvents().size());
					if (UserProperties.IS_PRODUCT_CHECK_ENABLED) {
						List<String> searchedProducts = new ArrayList<String>();
						StringBuilder productToBeSearched = new StringBuilder();
						for (String product : UserProperties.PRODUCTS) {
							productToBeSearched.append(",");
							productToBeSearched.append(QUOTE_SYMBOL + product + QUOTE_SYMBOL);
						}
						productToBeSearched.append(")");
						String queryString = productToBeSearched.toString().replaceFirst(",", "");
						ResultSet rsGetItem = connectionLBO.createStatement().executeQuery(LBO_GET_ITEM + queryString);
						while (rsGetItem.next()) searchedProducts.add(rsGetItem.getString("item_number"));
						lboReportData.setSearchedProducts(searchedProducts);
					}
					if (UserProperties.IS_PRICE_EVENT_CHECK_ENABLED) {
						Set<String> searchedPriceEvents = new HashSet<String>();
						StringBuilder priceEventsToBeSearched = new StringBuilder();
						for (String priceEvent : UserProperties.PRICEVENTS) {
							priceEventsToBeSearched.append(",");
							priceEventsToBeSearched.append(QUOTE_SYMBOL + priceEvent + QUOTE_SYMBOL);
						}
						priceEventsToBeSearched.append(")");
						String queryString = priceEventsToBeSearched.toString().replaceFirst(",", "");
						ResultSet rsGetPriceEvents = connectionLBO.createStatement()
								.executeQuery(LBO_GET_PRICE_EVENT + queryString);
						while (rsGetPriceEvents.next()) searchedPriceEvents.add(rsGetPriceEvents.getString("PriceDerivRuleEventNumber"));
						
						ResultSet rsGetItemPriceEvents = connectionLBO.createStatement()
								.executeQuery(LBO_GET_ITEM_PRICE_EVENT + queryString);
						while (rsGetItemPriceEvents.next()) searchedPriceEvents.add(rsGetItemPriceEvents.getString("ITEMPRICEEVENTNUMBER"));
						
						lboReportData.setSearchedPriceEvents(searchedPriceEvents);
					} 
					if (UserProperties.IS_DELETED_PRICE_EVENT_CHECK_ENABLED) {
						Set<String> deletedPriceEvents = new HashSet<String>();
						ResultSet rsGetItemPriceEvents = connectionLBO.createStatement()
								.executeQuery(LBO_GET_DELETED_PRICE_EVENT);
						while (rsGetItemPriceEvents.next()) {
							deletedPriceEvents.add(rsGetItemPriceEvents.getString("MerchandiseHierarchyNumber"));
						}
						lboReportData.setDeletedPriceEvents(deletedPriceEvents);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				lboReportData.setLboDataCounts(lboStoredDataCounts);
				lboReportData.setLboPriceEventLists(lboPriceEventLists);
				return lboReportData;
			});
			
			Future<StoreReport> cssQuery = executor.submit(() -> {
				Connection connectionCSS = null;
				StoreReport cssReport = new StoreReport();
				if (UserProperties.IS_TRANSACTION_CHECK_ENABLED) {
					try {
						connectionCSS = ConnectionProvider.getOracleDBConnection(CSS_DB_NAME);
						
						List<LaneCount> cssLaneCountList = new ArrayList<LaneCount>();
						for (String laneNumberStr : activeLanesList) {
							LaneCount laneCountCSS = new LaneCount();					
							laneCountCSS.setLaneNumber(laneNumberStr);
							final String LANE_COUNT_SEARCH_PARAMETER = storeDetails.getStoreNumber() + laneNumberStr + POST_CHECK_DATE + WILDCARD_PERCENTAGE_SYMBOL + QUOTE_SYMBOL;
							
							ResultSet rsLaneVoidedCount = connectionCSS.createStatement().executeQuery(CSS_LANE_VOIDED_COUNT_QUERY + LANE_COUNT_SEARCH_PARAMETER);
							while (rsLaneVoidedCount.next()) laneCountCSS.setLaneVoidedCount(rsLaneVoidedCount.getInt(1));
							ResultSet rsLaneNonVoidedCount = connectionCSS.createStatement().executeQuery(CSS_LANE_NON_VOIDED_COUNT_QUERY + LANE_COUNT_SEARCH_PARAMETER);
							while (rsLaneNonVoidedCount.next()) laneCountCSS.setLaneNonVoidedCount(rsLaneNonVoidedCount.getInt(1));
							ResultSet rsLaneTotalCount = connectionCSS.createStatement().executeQuery(CSS_LANE_TOTAL_COUNT_QUERY + LANE_COUNT_SEARCH_PARAMETER);
							while (rsLaneTotalCount.next()) laneCountCSS.setLaneTotalCount(rsLaneTotalCount.getInt(1));
							
							cssLaneCountList.add(laneCountCSS);
						}
						cssReport.setCssTrxnCount(cssLaneCountList);
						
						TransactionTotal cssTrxnTotal = new TransactionTotal();
						final String TRXN_TOTAL_SEARCH_PARAMETER = storeDetails.getStoreNumber() + WILDCARD_PERCENTAGE_SYMBOL + POST_CHECK_DATE + WILDCARD_PERCENTAGE_SYMBOL + QUOTE_SYMBOL;
						
						ResultSet rsVoidedTotal = connectionCSS.createStatement().executeQuery(CSS_VOIDED_TOTAL_QUERY + TRXN_TOTAL_SEARCH_PARAMETER);
						while (rsVoidedTotal.next()) cssTrxnTotal.setVoidedTotal(rsVoidedTotal.getDouble(1));
						ResultSet rsNonVoidedTotal = connectionCSS.createStatement().executeQuery(CSS_NON_VOIDED_TOTAL_QUERY + TRXN_TOTAL_SEARCH_PARAMETER);
						while (rsNonVoidedTotal.next()) cssTrxnTotal.setNonVoidedTotal(rsNonVoidedTotal.getDouble(1));
						ResultSet rsVTrxnTotal = connectionCSS.createStatement().executeQuery(CSS_TRXN_TOTAL_QUERY + TRXN_TOTAL_SEARCH_PARAMETER);
						while (rsVTrxnTotal.next()) cssTrxnTotal.setTrxnTotal(rsVTrxnTotal.getDouble(1));
						
						cssReport.setCssTrxnTotal(cssTrxnTotal);	
						
						//Getting performance issue, so holding this query for now
						/*ResultSet rsJournalCount = connectionCSS.createStatement().executeQuery(CSS_JOURNAL_COUNT_QUERY + TRXN_TOTAL_SEARCH_PARAMETER);
						while (rsJournalCount.next()) cssReport.setJournalCount(rsJournalCount.getInt(1));*/
						
					} catch (SQLException e) {
						System.out.println("SQL Exception while generating CSS data for the store -> " + storeDetails.getStoreNumber());
						System.out.println(e.getMessage());
						e.printStackTrace();
					} finally {
						if (connectionCSS != null) {
							try {
								connectionCSS.close();
							} catch (SQLException e) {
								System.out.println(e.getMessage());
								e.printStackTrace();
							}
						}
					}
				}
				return cssReport;
			});
			try {
				StoreReport lboReport = lboQuery.get(1, TimeUnit.MINUTES);
				storeReport.setLboTrxnCount(lboReport.getLboTrxnCount());
				storeReport.setLboTrxnTotal(lboReport.getLboTrxnTotal());
				storeReport.setLboQueueDepth(lboReport.getLboQueueDepth());
				
				StoreReport lboReportForDataCounts = lboStoredDataCountFuture.get(2, TimeUnit.MINUTES);
				StoredDataCounts lboDataCounts = lboReportForDataCounts.getLboDataCounts();
				PriceEventLists lboPriceEventLists = lboReportForDataCounts.getLboPriceEventLists();
				storeReport.setLboDataCounts(lboDataCounts);
				storeReport.setLboPriceEventLists(lboPriceEventLists);
				storeReport.setSearchedProducts(lboReportForDataCounts.getSearchedProducts());
				storeReport.setSearchedPriceEvents(lboReportForDataCounts.getSearchedPriceEvents());
				storeReport.setDeletedPriceEvents(lboReportForDataCounts.getDeletedPriceEvents());
			} catch (InterruptedException | ExecutionException ex) {
				System.out.println("Exception while generating data from LBO for store -> " + storeDetails.getStoreNumber());
				System.out.println(ex.getMessage());
				ex.printStackTrace();
			} finally {
				if (connectionLBO != null) {
					try {
						connectionLBO.close();
					} catch (SQLException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}
			
			try {
				StoreReport cssReport = cssQuery.get(1, TimeUnit.MINUTES);
				storeReport.setCssTrxnCount(cssReport.getCssTrxnCount());
				storeReport.setCssTrxnTotal(cssReport.getCssTrxnTotal());
				storeReport.setJournalCount(cssReport.getJournalCount());
			} catch (InterruptedException | ExecutionException ex) {
				System.out.println("Exception while generating data from CSS for store -> " + storeDetails.getStoreNumber());
				System.out.println(ex.getMessage());
				ex.printStackTrace();
			}
			System.out.println("Fetching transaction data is completed for store " + storeDetails.getStoreNumber());
			for(Future<BusinessAreaCounts> bACFuture : bACFutureList) {
				bACFuture.get(1, TimeUnit.MINUTES);
			}
			if (sshSessionLbo != null && sshSessionLbo.isConnected()) {
				sshSessionLbo.disconnect();
			}
			if (sshSessionDte != null && sshSessionDte.isConnected()) {
				sshSessionDte.disconnect();
			}
			storeReport.setDTECounts(dteBusinessAreaCounts);
			storeReport.setLBOCounts(lboBusinessAreaCounts);
			
			String FOLDER_DELETE = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator + POST_CHECK_DIR
					+ File.separator + POST_CHECK_DATE + File.separator + "business_areas";
			new File(FOLDER_DELETE).deleteOnExit();

		} catch (InterruptedException e) {
			storeReport.setError(true);
			addToFailedList(storeDetails.getStoreNumber());
			System.out.println("Interrupted Exception while generating report for store -> " + storeDetails.getStoreNumber());
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (ExecutionException e) {
			storeReport.setError(true);
			addToFailedList(storeDetails.getStoreNumber());
			System.out.println("Execution Exception while generating report for store -> " + storeDetails.getStoreNumber());
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			storeReport.setError(true);
			addToFailedList(storeDetails.getStoreNumber());
			System.out.println("SQL Exception while generating report for store -> " + storeDetails.getStoreNumber());
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			storeReport.setError(true);
			addToFailedList(storeDetails.getStoreNumber());
			System.out.println("General Exception while generating report for store -> " + storeDetails.getStoreNumber());
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		storeInfo.setStoreReport(storeReport);
		return storeInfo;
	}
	
	public void shutdown() {
		if (!executor.isShutdown()) {
			executor.shutdown();
		}
	}

	private static void addToFailedList(String storeNumber) {
		failedStores.add(storeNumber);
	}

	public static void showFailedStores() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Failed stores--->>");
		failedStores.stream().forEach(store -> stringBuilder.append(store + ", "));
		System.out.println(stringBuilder.toString());
	}

	public static List<String> getFailedStores() {
		final List<String> storeList = new ArrayList<>(failedStores.size());
		failedStores.stream().forEach(store -> storeList.add(store));
		return storeList;
	}

	public static void clearFailedStores() {
		failedStores.clear();
	}
	
	public Optional<CssHealth> getCSSHealth() {
		Future<CssHealth> cssHealthFuture = executor.submit(() -> {
			CssHealth cssHealth = new CssHealth();
			Connection connectionCSS = null;
			try {
				connectionCSS = ConnectionProvider.getOracleDBConnection(CSS_DB_NAME);
				ResultSet rsQueueDepth = connectionCSS.createStatement().executeQuery(CSS_QUEUE_DEPTH_CHECK);
				while (rsQueueDepth.next()) cssHealth.setUnSentQueueDepth((rsQueueDepth.getInt(1)));
			} catch (SQLException e) {
				System.out.println("SQL Exception while getting CSS queue depth");
				System.out.println(e.getMessage());
				e.printStackTrace();
			} finally {
				if (connectionCSS != null) {
					try {
						connectionCSS.close();
					} catch (SQLException e) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				}
			}
			ClientConfig config = new DefaultClientConfig();
			try {
				Client client = Client.create(config);
				WebResource resource = client.resource(UriBuilder.fromUri("http://" + CSS_NODE_1).port(9000).build());
				cssHealth.setNode1(resource.path("health").accept(MediaType.APPLICATION_JSON).get(String.class));
	
				resource = client.resource(UriBuilder.fromUri("http://" + CSS_NODE_2).port(9000).build());
				cssHealth.setNode2(resource.path("health").accept(MediaType.APPLICATION_JSON).get(String.class));
	
				resource = client.resource(UriBuilder.fromUri("http://" + CSS_NODE_3).port(9000).build());
				cssHealth.setNode3(resource.path("health").accept(MediaType.APPLICATION_JSON).get(String.class));
	
				resource = client.resource(UriBuilder.fromUri("http://" + CSS_NODE_4).port(9000).build());
				cssHealth.setNode4(resource.path("health").accept(MediaType.APPLICATION_JSON).get(String.class));
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				ex.printStackTrace();
			}
			return cssHealth;
		});
		Optional<CssHealth> cssHealth = Optional.empty();
		try {
			cssHealth = Optional.of(cssHealthFuture.get(1, TimeUnit.MINUTES));
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			System.out.println("CSS server health check error" + e.getMessage());
			e.printStackTrace();
		}
		return cssHealth;
	}
	
	public void updateMissingPriceEventsToCloud(Set<MissingPriceEvent> missingPriceEvents) {
		Connection connectionCloud = null;
		Session sshSession = null;
		try {
			LocalDateTime runningTime = getCurrentTime();
			StringBuilder query = new StringBuilder("INSERT INTO missing_price_event VALUES ");
			for(MissingPriceEvent priceEvent : missingPriceEvents) {
				query.append(",('").append(runningTime).append("',");
				query.append("'").append(priceEvent.getPriceEvent()).append("',");
				query.append("'").append(priceEvent.getStartDate()).append("',");
				query.append("'").append(priceEvent.getEndDate()).append("',");
				query.append("'").append(priceEvent.getEventType()).append("')");
			}
			sshSession = ConnectionProvider.openSSHSessionToCloud();
			connectionCloud = ConnectionProvider.connectToCloud();
			String queryToInsert = query.toString().replaceFirst(",", "");
			LOG.info("Running query --> " + queryToInsert);
			connectionCloud.createStatement().executeUpdate(queryToInsert);
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connectionCloud != null) {
				try {
					connectionCloud.close();
				} catch (SQLException e) {
					LOG.warning(e.getMessage());
					e.printStackTrace();
				}
			}
			if(sshSession != null) {
				sshSession.disconnect();
			}
		}
	}
	
	public void updateStorewiseMissingPriceEventsToCloud(String store, Set<MissingPriceEvent> missingPriceEvents) {
		Connection connectionCloud = null;
		Session sshSession = null;
		try {
			LocalDateTime runningTime = getCurrentTime();
			StringBuilder query = new StringBuilder("INSERT INTO store_missing_price_event VALUES ");
			for(MissingPriceEvent priceEvent : missingPriceEvents) {
				query.append(",('").append(runningTime).append("',");
				query.append("'").append(store).append("',");
				query.append("'").append(priceEvent.getPriceEvent()).append("',");
				query.append("'").append(priceEvent.getStartDate()).append("',");
				query.append("'").append(priceEvent.getEndDate()).append("',");
				query.append("'").append(priceEvent.getEventType()).append("')");
			}
			sshSession = ConnectionProvider.openSSHSessionToCloud();
			connectionCloud = ConnectionProvider.connectToCloud();
			String queryToInsert = query.toString().replaceFirst(",", "");
			LOG.info("Running query --> " + queryToInsert);
			connectionCloud.createStatement().executeUpdate(queryToInsert);
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connectionCloud != null) {
				try {
					connectionCloud.close();
				} catch (SQLException e) {
					LOG.warning(e.getMessage());
					e.printStackTrace();
				}
			}
			if(sshSession != null) {
				sshSession.disconnect();
			}
		}
	}

	private LocalDateTime getCurrentTime() {
		DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		LocalDateTime runningTime = LocalDateTime.now();
		runningTime.format(DATE_TIME_FORMATTER);
		return runningTime;
	}

	public static void main(String[] args) {
		/*System.out.println("Count-->" +new DataCalculator().getCSSHealth().getUnSentQueueDepth());
		try {
			final Connection connectionCloud = ConnectionProvider.openSSHSessionToCloudAndConnect();
			ResultSet rsCloudCheck = connectionCloud.createStatement().executeQuery("Select * from users");
			while (rsCloudCheck.next()) System.out.println(rsCloudCheck.getString("name"));
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
}
