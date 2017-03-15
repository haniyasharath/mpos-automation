package com.marks.mpos.deployment.check.test;

import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.DEPLOY_CHECK_DIR;
import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.POST_CHECK_DIR;
import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.USER_HOME;
import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.WILDCARD_PERCENTAGE_SYMBOL;
import static com.marks.mpos.deployment.check.properties.UserProperties.POST_CHECK_DATE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import com.marks.mpos.deployment.check.beans.LaneCount;
import com.marks.mpos.deployment.check.beans.StoreReport;
import com.marks.mpos.deployment.check.logger.AutomationLogger;
import com.marks.mpos.deployment.check.report.StoreDataBuilder;
import com.marks.mpos.deployment.check.report.StoreReportGenerator;

public class SplunkReportGenerator {

	private static final Logger  LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static void main(String [] args) {
		try {
			AutomationLogger.setup();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// For POST check
		LocalDateTime startingTime = LocalDateTime.now();
		LOG.info("Starting Time " +startingTime);
		StoreReportGenerator.generateReportsForSplunk(StoreDataBuilder.generateStoreDataObjectList());
		LocalDateTime endingTime = LocalDateTime.now();
		LOG.info("Finshing Time " + endingTime);
		Duration elapsedTime = Duration.between(startingTime, endingTime);
		StringBuilder formatedTime = new StringBuilder();
		formatedTime.append(String.format("Elapsed time HH:MM:SS --> %s:%s:%s", elapsedTime.toHours(), elapsedTime.toMinutes(), elapsedTime.getSeconds()%60));
		LOG.info(formatedTime.toString());
	}

	private static void printPostCheckData() {
		List<StoreReport> storeDataList = StoreDataBuilder.generateStoreDataObjectList();
		final String FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator + POST_CHECK_DIR + File.separator + POST_CHECK_DATE;
		new File(FILE_PATH).mkdirs();
		String fileName = "TransactionData_" + POST_CHECK_DATE + "_Stores_ALL.txt";
		PrintStream printStream = null;
		try {
			printStream = new PrintStream(new File(FILE_PATH + File.separator + fileName));
			System.setOut(printStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (StoreReport storeReport : storeDataList) {
			System.out.println("==================================================");
			System.out.println("STORE NUMBER - " + storeReport.getStoreNumber());
			System.out.println("==================================================");
			System.out.println();
			System.out.println("LBO Transaction Counts");
			System.out.println("------------------------------------------");
			List<LaneCount> lboLaneCountList = storeReport.getLboTrxnCount();
			for (LaneCount laneCount : lboLaneCountList) {
				if (WILDCARD_PERCENTAGE_SYMBOL.equals(laneCount.getLaneNumber()))
					System.out.println("Lane Number - " + "ALL Lanes");
				else 
					System.out.println("Lane Number      - " + laneCount.getLaneNumber());
				System.out.println("Voided Count     - " + laneCount.getLaneVoidedCount());
				System.out.println("Non-Voided Count - " + laneCount.getLaneNonVoidedCount());
				System.out.println("Total Count      - " + laneCount.getLaneTotalCount());
				System.out.println();
			}
			System.out.println();
			System.out.println("CSS Transaction Counts");
			System.out.println("------------------------------------------");
			List<LaneCount> cssLaneCountList = storeReport.getCssTrxnCount();
			for (LaneCount laneCount : cssLaneCountList) {
				if (WILDCARD_PERCENTAGE_SYMBOL.equals(laneCount.getLaneNumber()))
					System.out.println("Lane Number - " + "ALL Lanes");
				else 
					System.out.println("Lane Number      - " + laneCount.getLaneNumber());
				System.out.println("Voided Count     - " + laneCount.getLaneVoidedCount());
				System.out.println("Non-Voided Count - " + laneCount.getLaneNonVoidedCount());
				System.out.println("Total Count      - " + laneCount.getLaneTotalCount());
				System.out.println();
			}
			System.out.println();
			System.out.println("---------------------------------------------");
			System.out.println("CSS Journal Count  - " + storeReport.getJournalCount());
			System.out.println("---------------------------------------------");
			System.out.println();
			System.out.println("LBO Transaction Total");
			System.out.println("-------------------------------");
			System.out.println("Voided Total      - " + storeReport.getLboTrxnTotal().getVoidedTotal());
			System.out.println("Non-Voided Total  - " + storeReport.getLboTrxnTotal().getNonVoidedTotal());
			System.out.println("Transaction Total - " + storeReport.getLboTrxnTotal().getTrxnTotal());
			System.out.println();
			System.out.println("CSS Transaction Total");
			System.out.println("-------------------------------");
			System.out.println("Voided Total      - " + storeReport.getCssTrxnTotal().getVoidedTotal());
			System.out.println("Non-Voided Total  - " + storeReport.getCssTrxnTotal().getNonVoidedTotal());
			System.out.println("Transaction Total - " + storeReport.getCssTrxnTotal().getTrxnTotal());
			System.out.println();
			System.out.println();
			System.out.println();
		}
		if (printStream != null) printStream.close();
	}
}
