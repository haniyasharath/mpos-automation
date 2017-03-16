package com.marks.mpos.deployment.check.report;

import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.*;
import static com.marks.mpos.deployment.check.properties.UserProperties.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.TextAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.jcraft.jsch.Session;
import com.marks.mpos.deployment.check.beans.BusinessAreaCounts;
import com.marks.mpos.deployment.check.beans.LaneCount;
import com.marks.mpos.deployment.check.beans.StoreReport;
import com.marks.mpos.deployment.check.beans.StoredDataCounts;
import com.marks.mpos.deployment.check.beans.TransactionTotal;
import com.marks.mpos.deployment.check.connection.ConnectionProvider;
import com.marks.mpos.deployment.check.properties.UserProperties;
import com.marks.mpos.deployment.check.splunk.CreateJSON;
import com.marks.mpos.deployment.check.utils.DateUtil;

public class StoreReportGenerator {

	private static final Logger LOG = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	public static Map<String, Object[]> reportinfo;

	// changed for excel report
	public static void generateReportForExcel(List<StoreReport> storeDataList) throws IOException {

		LOG.info("Writing to excel file");
		final String FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator + POST_CHECK_DIR
				+ File.separator + POST_CHECK_DATE;
		final String fileNameForTransactionDetails = FILE_PATH + File.separator + "Transaction_details" + ".xlsx";
		LOG.info("Writing to excel file name" + fileNameForTransactionDetails);
		List<String> queryList = new ArrayList<String>();
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("TransactionReport");
		sheet.setColumnWidth(1, 4000);
		sheet.setColumnWidth(2, 4000);
		sheet.setColumnWidth(3, 4000);
		sheet.setColumnWidth(4, 4000);
		sheet.setColumnWidth(5, 4000);
		sheet.setColumnWidth(6, 6000);
		sheet.setColumnWidth(7, 6000);
		
		XSSFRow row;
		Map<Integer, Object[]> excelreportinfo = new TreeMap<Integer, Object[]>();
		int i = 0;
		excelreportinfo.put(i,
				new Object[] { "Stores", "LBO stuck Txn #", "LBO/CBO Txn #", "LBO/CBO Txn Amount", "CSS Txn #",
						"CSS Txn Amount",
						"Count difference between CSS/ LBO (-ve indicates CSS count is less than LBO)",
						"Amount Difference between CSS/LBO (-ve indicates CSS amount is less than LBO)" });
		for (StoreReport storeReport : storeDataList) {
			int lboTotalCount = 0;
			int cssTotalCount = 0;
			double lboTotalAmount = 0;
			double cssTotalAmount = 0;
			if (CollectionUtils.isNotEmpty(storeReport.getLboTrxnCount())) {
				lboTotalCount = storeReport.getLboTrxnCount().stream()
						.filter(lane -> WILDCARD_PERCENTAGE_SYMBOL.equals(lane.getLaneNumber())).findAny().get()
						.getLaneTotalCount();
			}
			if (storeReport.getLboTrxnTotal() != null) {
				lboTotalAmount = storeReport.getLboTrxnTotal().getTrxnTotal();
			}
			if (CollectionUtils.isNotEmpty(storeReport.getCssTrxnCount())) {
				cssTotalCount = storeReport.getCssTrxnCount().stream()
						.filter(lane -> WILDCARD_PERCENTAGE_SYMBOL.equals(lane.getLaneNumber())).findAny().get()
						.getLaneTotalCount();
			}
			if (storeReport.getCssTrxnTotal() != null) {
				cssTotalAmount = storeReport.getCssTrxnTotal().getTrxnTotal();
			}
			i++;
			excelreportinfo.put(i, new Object[] { storeReport.getStoreNumber() != null ? Integer.valueOf(storeReport.getStoreNumber()) : "", storeReport.getLboStuckTransactionCount(),
							lboTotalCount, lboTotalAmount, cssTotalCount, cssTotalAmount,
							(cssTotalCount - lboTotalCount),
							BigDecimal.valueOf(cssTotalAmount - lboTotalAmount).setScale(2, RoundingMode.UP).doubleValue()
			});
		}

		Set<Integer> keyid = excelreportinfo.keySet();
		CellStyle style = workbook.createCellStyle();
		style.setWrapText(true);
		/*CellStyle columnStyle = workbook.createCellStyle();
		columnStyle.setWrapText(true);
		columnStyle.setFillBackgroundColor(HSSFColor.GREY_25_PERCENT.index);
		columnStyle.setFillPattern(HSSFCellStyle.ALIGN_FILL);*/
		int rowid = 0;
		for (Integer key : keyid) {
			StringBuilder query = new StringBuilder("INSERT INTO Transaction VALUES (");
			row = sheet.createRow(rowid++);
			Object[] objectArr = excelreportinfo.get(key);
			int cellid = 0;
			for (Object obj : objectArr) {
				Cell cell = row.createCell(cellid++);
				cell.setCellStyle(style);
				if (obj instanceof String) {
					query.append("," + (String) obj);
					cell.setCellValue((String) obj);
					//cell.setCellStyle(columnStyle);
				}
				if (obj instanceof Integer) {
					query.append("," + (Integer) obj);
					cell.setCellValue((Integer) obj);
				}
				if (obj instanceof Double) {
					query.append("," + (Double) obj);
					cell.setCellValue((Double) obj);
				}
			}
			query.append(")");
			String transactionQuery = query.toString().replaceFirst(",", "");
			queryList.add(transactionQuery.toString());
		}
		// Write the workbook in file system
		FileOutputStream out = new FileOutputStream(new File(fileNameForTransactionDetails));
		workbook.write(out);
		workbook.close();
		out.close();
		LOG.info("completed writing");
		LOG.info("query list size is " + queryList.size());
		LOG.info("sample query to insert is " + queryList.get(1));
		LOG.info("sample query to insert is " + queryList.get(queryList.size() - 1));
		writeToCloudTable(queryList);

	}

	public static void writeToCloudTable(List<String> queryList) {
		Connection connectionCloud = null;
		Session sshSession = null;
		try {
			sshSession = ConnectionProvider.openSSHSessionToCloud();
			connectionCloud = ConnectionProvider.connectToCloud();
			System.out.println("before executing insert query");
			// connectionCloud.createStatement().executeUpdate(queryList.get(20));
			for (int i=1;i<queryList.size()-1;i++) {
				connectionCloud.createStatement().executeUpdate(queryList.get(i));
			}
			System.out.println("after execution of insert query");

		} catch (Exception e) {
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

	public static void generateReportsForStores(List<StoreReport> storeDataList) {

		final String FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator +  POST_CHECK_DIR + File.separator + POST_CHECK_DATE; 
		new File(FILE_PATH).mkdirs();
		XWPFTable transactionTable = null;
		XWPFDocument transactionReportDoc = null;
		if (UserProperties.PREPARE_EXCEL) {
			try {
				generateReportForExcel(storeDataList);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			transactionReportDoc = new XWPFDocument();

			XWPFParagraph transactionP = transactionReportDoc.createParagraph();
			XWPFRun transactionR = transactionP.createRun();
			transactionR.setText("Transaction details for all the Stores as on " + DateUtil.getFormattedDate());
			transactionR.setBold(true);
			transactionR.setFontFamily("Calibri");
			transactionR.setFontSize(18);
			addBlankLine(transactionReportDoc);

			transactionTable = createTransactionDataTable(transactionReportDoc);
		}
        
		for (StoreReport storeReport : storeDataList) {
			XWPFDocument storeReportDoc = new XWPFDocument();
            
			XWPFParagraph p = storeReportDoc.createParagraph();
	        XWPFRun r = p.createRun();
	        r.setText("Post Deployment Checks for Store " + storeReport.getStoreNumber());
	        r.setBold(true);
	        r.setFontFamily("Calibri");
	        r.setFontSize(18);
	        
	        generateSubHeading(storeReportDoc, "Business Area");
	        generateBusinessAreaTable(storeReportDoc, Optional.ofNullable(storeReport.getLBOCounts()), Optional.ofNullable(storeReport.getDTECounts()), Optional.ofNullable(storeReport.getCBOCounts()));
	        addBlankLine(storeReportDoc);
	        generateSubHeading(storeReportDoc, "Stored Data Count");
	        generateStoredDataTable(storeReportDoc, Optional.ofNullable(storeReport.getCboDataCounts()), Optional.ofNullable(storeReport.getLboDataCounts()));
	        addBlankLine(storeReportDoc);
	        generateSubHeading(storeReportDoc, "Count of Transactions in LBO");
	        generateCountTable(storeReportDoc, CollectionUtils.isNotEmpty(storeReport.getLboTrxnCount()) ? storeReport.getLboTrxnCount() : ListUtils.EMPTY_LIST);
	        addBlankLine(storeReportDoc);
	        generateSubHeading(storeReportDoc, "Count of Transactions in CSS");
	        generateCountTable(storeReportDoc, CollectionUtils.isNotEmpty(storeReport.getCssTrxnCount()) ? storeReport.getCssTrxnCount() : ListUtils.EMPTY_LIST);
	        addBlankLine(storeReportDoc);
	        generateSubHeading(storeReportDoc, "Count of transactions in Journals: " + storeReport.getJournalCount());
	        addBlankLine(storeReportDoc);
	        addBlankLine(storeReportDoc);
	        generateSubHeading(storeReportDoc, "Transaction Totals in LBO");
	        generateTotalTable(storeReportDoc, storeReport.getLboTrxnTotal());
	        addBlankLine(storeReportDoc);
	        generateSubHeading(storeReportDoc, "Transaction Totals in CSS");
	        generateTotalTable(storeReportDoc, storeReport.getCssTrxnTotal());
	        addBlankLine(storeReportDoc);
	        
	        if(!UserProperties.PREPARE_EXCEL) {
	        	updateTransactionDataTable(transactionTable, storeReport);
	        }
	        
	        final String fileName = FILE_PATH + File.separator + POST_CHECK_FILENAME_PREFIX + storeReport.getStoreNumber() + DOCX_FILE_EXTN;
	        FileOutputStream out;
			try {
				out = new FileOutputStream(fileName);
				storeReportDoc.write(out);
		        out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!UserProperties.PREPARE_EXCEL) {
			final String fileNameForTransactionDetails = FILE_PATH + File.separator + "Transaction_details" + DOCX_FILE_EXTN;
	        FileOutputStream outForTransactionDetails;
			try {
				outForTransactionDetails = new FileOutputStream(fileNameForTransactionDetails);
				transactionReportDoc.write(outForTransactionDetails);
				outForTransactionDetails.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static XWPFTable createTransactionDataTable(XWPFDocument doc) {
		XWPFTable table = doc.createTable(1, 8);
		table.getRow(0).getCell(0).setText("Stores");
		table.getRow(0).getCell(1).setText("LBO stuck Txn #");
		table.getRow(0).getCell(2).setText("LBO/CBO Txn #");
		table.getRow(0).getCell(3).setText("LBO/CBO Txn Amount");
		table.getRow(0).getCell(4).setText("CSS Txn #");
		table.getRow(0).getCell(5).setText("CSS Txn Amount");
		table.getRow(0).getCell(6).setText("Count difference between CSS/ LBO (-ve indicates CSS count is less than LBO)");
		table.getRow(0).getCell(7).setText("Amount Difference between CSS/LBO (-ve indicates CSS amount is less than LBO)");

		return table;
	}
	
	private static void updateTransactionDataTable(XWPFTable table, StoreReport storeReport) {
		XWPFTableRow newRow = table.createRow();
		newRow.getCell(0).setText(storeReport.getStoreNumber());
		int lboTotalCount = 0;
		int cssTotalCount = 0;
		double lboTotalAmount = 0.0;
		double cssTotalAmount = 0.0;
		newRow.getCell(1).setText(String.valueOf(storeReport.getLboStuckTransactionCount()));
		if(CollectionUtils.isNotEmpty(storeReport.getLboTrxnCount())){
			lboTotalCount = storeReport.getLboTrxnCount().stream()
					.filter(lane ->WILDCARD_PERCENTAGE_SYMBOL.equals(lane.getLaneNumber()))
					.findAny().get().getLaneTotalCount();
			newRow.getCell(2).setText(String.valueOf(lboTotalCount));
		}
		if(storeReport.getLboTrxnTotal() != null){
			lboTotalAmount = storeReport.getLboTrxnTotal().getTrxnTotal();
			newRow.getCell(3).setText(String.valueOf(lboTotalAmount));
		}
		if(CollectionUtils.isNotEmpty(storeReport.getCssTrxnCount())){
			cssTotalCount = storeReport.getCssTrxnCount().stream()
					.filter(lane -> WILDCARD_PERCENTAGE_SYMBOL.equals(lane.getLaneNumber()))
					.findAny().get().getLaneTotalCount();
			newRow.getCell(4).setText(String.valueOf(cssTotalCount));
		}
		if(storeReport.getCssTrxnTotal() != null){
			cssTotalAmount = storeReport.getCssTrxnTotal().getTrxnTotal();
			newRow.getCell(5).setText(String.valueOf(cssTotalAmount));
		}
		newRow.getCell(6).setText(String.valueOf(cssTotalCount - lboTotalCount));
		newRow.getCell(7).setText(String.valueOf(BigDecimal.valueOf(cssTotalAmount - lboTotalAmount).setScale(2, RoundingMode.UP)));
	}

	private static void generateStoredDataTable(XWPFDocument doc, Optional<StoredDataCounts> cboDataCount, Optional<StoredDataCounts> lboDataCount) {
		XWPFTable table = doc.createTable(10, 5);
		table.getRow(1).getCell(0).setText("UPC");
		table.getRow(2).getCell(0).setText("Product");
		table.getRow(3).getCell(0).setText("Basic Price");
		table.getRow(4).getCell(0).setText("BOGO");
        table.getRow(5).getCell(0).setText("Special Price");
		table.getRow(6).getCell(0).setText("XForY");
		table.getRow(7).getCell(0).setText(""); 
		table.getRow(0).getCell(1).setText("Number of records available in LBO"); 
		table.getRow(0).getCell(2).setText("Number of records available in CBO"); 
		table.getRow(0).getCell(3).setText("Status"); 
		
		if(cboDataCount.isPresent()) {
			insertDataToTable(table, cboDataCount.get(), 2);
		}
		if(lboDataCount.isPresent()) {
			insertDataToTable(table, lboDataCount.get(), 1);
		}
	}

	private static void insertDataToTable(XWPFTable table, StoredDataCounts storedDataCounts, int cell) {
		table.getRow(1).getCell(cell).setText(Integer.toString(storedDataCounts.getUpc()));
		table.getRow(2).getCell(cell).setText(Integer.toString(storedDataCounts.getProduct()));
		table.getRow(3).getCell(cell).setText(Integer.toString(storedDataCounts.getBasePrice()));
		table.getRow(4).getCell(cell).setText(Integer.toString(storedDataCounts.getBogo()));
		table.getRow(5).getCell(cell).setText(Integer.toString(storedDataCounts.getSpecialPrice()));
		table.getRow(6).getCell(cell).setText(Integer.toString(storedDataCounts.getxForY()));
		table.getRow(8).getCell(cell).setText("");
	}

	public static void generateReportsForSplunk(List<StoreReport> storeDataList) {
		final String FILE_PATH = USER_HOME + File.separator + DEPLOY_CHECK_DIR + File.separator +  POST_CHECK_DIR + File.separator + POST_CHECK_DATE ; 
		new File(FILE_PATH).mkdirs();
		
		for (StoreReport storeReport : storeDataList) {
			String STORE_FILE_PATH = FILE_PATH + File.separator + storeReport.getStoreNumber();
			new File(STORE_FILE_PATH).mkdirs();
			if(Optional.ofNullable(storeReport.getLBOCounts()).isPresent()) {
				generateJSON(Optional.ofNullable(storeReport.getLBOCounts().getHierarchyCount()), "LBO: Hierarchy Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_Hierarchy_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getLBOCounts().getBuCount()), "LBO: Business Unit Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_Business_Unit_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getLBOCounts().getProductCount()), "LBO: Product Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_Product_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getLBOCounts().getPriceCount()), "LBO: Price Basic Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_Price_Basic_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getLBOCounts().getPriceAdvCount()), "LBO: Price Advanced Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_Price_Advanced_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getLBOCounts().getTaxRatesCount()), "LBO: Tax Rates Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_Tax_Rates_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getLBOCounts().getTaxExemptionCount()), "LBO: Tax Exemption Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_Tax_Exemption_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getLBOCounts().getPromoCount()), "LBO: Promos Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_Promos_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getLBOCounts().getBuCount()), "LBO: Business Unit Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_Business_Unit_Count.json");
			}			
			
			if(Optional.ofNullable(storeReport.getDTECounts()).isPresent()) {
				generateJSON(Optional.ofNullable(storeReport.getDTECounts().getHierarchyCount()), "DTE: Hierarchy Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_DTE_Hierarchy_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getDTECounts().getBuCount()), "DTE: Business Unit Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_DTE_Business_Unit_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getDTECounts().getProductCount()), "DTE: Product Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_DTE_Product_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getDTECounts().getPriceCount()), "DTE: Price Basic Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_DTE_Price_Basic_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getDTECounts().getPriceAdvCount()), "DTE: Price Advanced Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_DTE_Price_Advanced_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getDTECounts().getTaxRatesCount()), "DTE: Tax Rates Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_DTE_Tax_Rates_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getDTECounts().getTaxExemptionCount()), "DTE: Tax Exemption Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_DTE_Tax_Exemption_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getDTECounts().getPromoCount()), "DTE: Promos Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_DTE_Promos_Count.json");
			}
			
			if(Optional.ofNullable(storeReport.getCBOCounts()).isPresent()) {
				generateJSON(Optional.ofNullable(storeReport.getCBOCounts().getHierarchyCount()), "CBO: Hierarchy Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CBO_Hierarchy_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getCBOCounts().getBuCount()), "CBO: Business Unit Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CBO_Business_Unit_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getCBOCounts().getProductCount()), "CBO: Product Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CBO_Product_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getCBOCounts().getPriceCount()), "CBO: Price Basic Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CBO_Price_Basic_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getCBOCounts().getPriceAdvCount()), "CBO: Price Advanced Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CBO_Price_Advanced_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getCBOCounts().getTaxRatesCount()), "CBO: Tax Rates Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CBO_Tax_Rates_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getCBOCounts().getTaxExemptionCount()), "CBO: Tax Exemption Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CBO_Tax_Exemption_Count.json");
				generateJSON(Optional.ofNullable(storeReport.getCBOCounts().getPromoCount()), "CBO: Promos Count", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CBO_Promos_Count.json");
			}
			
			//LBO transaction Details
			if(CollectionUtils.isNotEmpty(storeReport.getLboTrxnCount())) {
				for(LaneCount laneCount : storeReport.getLboTrxnCount()) {
					String laneNumber = laneCount.getLaneNumber();
					if(StringUtils.equals(laneNumber, "%")) {
						laneNumber = "All_lanes";
					}
					generateJSON(Optional.ofNullable(laneCount.getLaneVoidedCount()), "LBO: Voided Count by Lane " + laneNumber, STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_LBO_Voided_Lane_" + laneNumber + "_Count.json");
					generateJSON(Optional.ofNullable(laneCount.getLaneNonVoidedCount()), "LBO: Non-Voided Count by Lane " + laneNumber, STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_LBO_NonVoided_Lane_" + laneNumber + "_Count.json");
					generateJSON(Optional.ofNullable(laneCount.getLaneTotalCount()), "LBO: Total Count by Lane " + laneNumber, STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_LBO_Total_Lane_" + laneNumber + "_Count.json");
				}
			}
			if(Optional.ofNullable(storeReport.getLboTrxnTotal()).isPresent()) {
				generateJSON(Optional.ofNullable(storeReport.getLboTrxnTotal().getVoidedTotal()), "LBO: Total Voided Transaction", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_LBO_Total_Voided_Transaction.json");
				generateJSON(Optional.ofNullable(storeReport.getLboTrxnTotal().getNonVoidedTotal()), "LBO: Total Non-Voided Transaction", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_LBO_Total_NonVoided_Transaction.json");
				generateJSON(Optional.ofNullable(storeReport.getLboTrxnTotal().getTrxnTotal()), "LBO: Total Transaction", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_LBO_Total_Transaction.json");
			}
			
			//CSS transaction Details
			if(CollectionUtils.isNotEmpty(storeReport.getCssTrxnCount())) {
				for(LaneCount laneCount : storeReport.getCssTrxnCount()) {
					String laneNumber = laneCount.getLaneNumber();
					if(StringUtils.equals(laneNumber, "%")) {
						laneNumber = "All_lanes";
					}
					generateJSON(Optional.ofNullable(laneCount.getLaneVoidedCount()), "CSS: Voided Count by Lane " + laneNumber, STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CSS_Voided_Lane_" + laneNumber + "_Count.json");
					generateJSON(Optional.ofNullable(laneCount.getLaneNonVoidedCount()), "CSS: Non-Voided Count by Lane " + laneNumber, STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CSS_NonVoided_Lane_" + laneNumber + "_Count.json");
					generateJSON(Optional.ofNullable(laneCount.getLaneTotalCount()), "CSS: Total Count by Lane " + laneNumber, STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CSS_Total_Lane_" + laneNumber + "_Count.json");
				}
			}
			if(Optional.ofNullable(storeReport.getCssTrxnTotal()).isPresent()) {
				generateJSON(Optional.ofNullable(storeReport.getCssTrxnTotal().getVoidedTotal()), "CSS: Total Voided Transaction", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CSS_Total_Voided_Transaction.json");
				generateJSON(Optional.ofNullable(storeReport.getCssTrxnTotal().getNonVoidedTotal()), "CSS: Total Non-Voided Transaction", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CSS_Total_NonVoided_Transaction.json");
				generateJSON(Optional.ofNullable(storeReport.getCssTrxnTotal().getTrxnTotal()), "CSS: Total Transaction", STORE_FILE_PATH + File.separator + storeReport.getStoreNumber() + "_CSS_Total_Transaction.json");
			}
		}
	}

	private static void generateJSON(Optional<Number> value, String valueType, String filePath) {
		if(value.isPresent()) {
			CreateJSON createJSON = new CreateJSON();
			try {
				createJSON.generate(value.get(), valueType, filePath);
			} catch (IOException e) {
				e.printStackTrace();
				LOG.info(String.format(
						"Exception while generating JSOn file for the value-{%s}, valueType-{%s}, filePath-{%s}",
						value.get(), valueType, filePath));
			}
		}
	}

	private static void addBlankLine(XWPFDocument doc) {
		XWPFParagraph p = doc.createParagraph();
		XWPFRun r = p.createRun();
		r.setText("");
	}

	private static void generateSubHeading(XWPFDocument doc, String headingMessage) {
		XWPFParagraph p = doc.createParagraph();
		XWPFRun r = p.createRun();
		r.setText(headingMessage);
		r.setBold(true);
		r.setFontFamily("Calibri");
		r.setFontSize(11);
	}
	
	private static void generateBusinessAreaTable(XWPFDocument doc, Optional<BusinessAreaCounts> businessAreaCounts, Optional<BusinessAreaCounts> dteCounts, Optional<BusinessAreaCounts> cboCounts) {
		XWPFTable table = doc.createTable(10, 5);
		table.getRow(0).getCell(0).setText("Subject Area");
		table.getRow(1).getCell(0).setText("Product Hierarchy");
		table.getRow(2).getCell(0).setText("Products");
		table.getRow(3).getCell(0).setText("Business Unit");
		table.getRow(4).getCell(0).setText("Price Basic");
		table.getRow(5).getCell(0).setText("Price Advanced");
		table.getRow(6).getCell(0).setText("Tax Rates");
		table.getRow(7).getCell(0).setText("Tax Exemptions");
		table.getRow(8).getCell(0).setText(""); 
		table.getRow(9).getCell(0).setText("Promotion Description");
		table.getRow(0).getCell(1).setText("Number of records processed by DTE"); 
		table.getRow(0).getCell(2).setText("Number of records processed by LBO"); 
		table.getRow(0).getCell(3).setText("Number of records processed by CBO"); 
		table.getRow(0).getCell(4).setText("Status"); 
		
		if(businessAreaCounts.isPresent()) {
			table.getRow(1).getCell(2).setText(Integer.toString(businessAreaCounts.get().getHierarchyCount()));
			table.getRow(2).getCell(2).setText(Integer.toString(businessAreaCounts.get().getProductCount()));
			table.getRow(3).getCell(2).setText(Integer.toString(businessAreaCounts.get().getBuCount()));
			table.getRow(4).getCell(2).setText(Integer.toString(businessAreaCounts.get().getPriceCount()));
			table.getRow(5).getCell(2).setText(Integer.toString(businessAreaCounts.get().getPriceAdvCount()));
			table.getRow(6).getCell(2).setText(Integer.toString(businessAreaCounts.get().getTaxRatesCount()));
			table.getRow(7).getCell(2).setText(Integer.toString(businessAreaCounts.get().getTaxExemptionCount()));
			table.getRow(8).getCell(2).setText("");
			table.getRow(9).getCell(2).setText(Integer.toString(businessAreaCounts.get().getPromoCount()));
		}
		
		if(dteCounts.isPresent()) {
			table.getRow(1).getCell(1).setText(Integer.toString(dteCounts.get().getHierarchyCount()));
			table.getRow(2).getCell(1).setText(Integer.toString(dteCounts.get().getProductCount()));
			table.getRow(3).getCell(1).setText(Integer.toString(dteCounts.get().getBuCount()));
			table.getRow(4).getCell(1).setText(Integer.toString(dteCounts.get().getPriceCount()));
			table.getRow(5).getCell(1).setText(Integer.toString(dteCounts.get().getPriceAdvCount()));
			table.getRow(6).getCell(1).setText(Integer.toString(dteCounts.get().getTaxRatesCount()));
			table.getRow(7).getCell(1).setText(Integer.toString(dteCounts.get().getTaxExemptionCount()));
			table.getRow(8).getCell(1).setText("");
			table.getRow(9).getCell(1).setText(Integer.toString(dteCounts.get().getPromoCount()));
		}
		
		if(cboCounts.isPresent()) {
			table.getRow(1).getCell(3).setText(Integer.toString(cboCounts.get().getHierarchyCount()));
			table.getRow(2).getCell(3).setText(Integer.toString(cboCounts.get().getProductCount()));
			table.getRow(3).getCell(3).setText(Integer.toString(cboCounts.get().getBuCount()));
			table.getRow(4).getCell(3).setText(Integer.toString(cboCounts.get().getPriceCount()));
			table.getRow(5).getCell(3).setText(Integer.toString(cboCounts.get().getPriceAdvCount()));
			table.getRow(6).getCell(3).setText(Integer.toString(cboCounts.get().getTaxRatesCount()));
			table.getRow(7).getCell(3).setText(Integer.toString(cboCounts.get().getTaxExemptionCount()));
			table.getRow(8).getCell(3).setText("");
			table.getRow(9).getCell(3).setText(Integer.toString(cboCounts.get().getPromoCount()));	
		}

	}
	
	private static void generateCountTable(XWPFDocument doc, List<LaneCount> laneCountList) {
        XWPFTable table = doc.createTable(4, 1 + laneCountList.size());
        table.getRow(1).getCell(0).setText("Voided");
        table.getRow(2).getCell(0).setText("Non Voided");
        table.getRow(3).getCell(0).setText("Total");
        
        for (int i = 0; i < laneCountList.size(); i++) {
			LaneCount laneCount = laneCountList.get(i);
        	if (WILDCARD_PERCENTAGE_SYMBOL.equals(laneCount.getLaneNumber())) {
				table.getRow(0).getCell(i + 1).setText("ALL Lanes");
			} else {
				table.getRow(0).getCell(i + 1).setText("Lane " + laneCount.getLaneNumber());
			}
        	table.getRow(1).getCell(i + 1).setText(Integer.toString(laneCount.getLaneVoidedCount()));
        	table.getRow(2).getCell(i + 1).setText(Integer.toString(laneCount.getLaneNonVoidedCount()));
        	table.getRow(3).getCell(i + 1).setText(Integer.toString(laneCount.getLaneTotalCount()));
		}

	}
	
	private static void generateTotalTable(XWPFDocument doc, TransactionTotal lboTrxnTotal) {
		XWPFTable table = doc.createTable(3, 2);
		
        table.getRow(0).getCell(0).setText("Voided");
        table.getRow(1).getCell(0).setText("Non Voided");
        table.getRow(2).getCell(0).setText("Total");
        if(Optional.ofNullable(lboTrxnTotal).isPresent()) {
	        table.getRow(0).getCell(1).setText(Double.toString(lboTrxnTotal.getVoidedTotal()));
	        table.getRow(1).getCell(1).setText(Double.toString(lboTrxnTotal.getNonVoidedTotal()));
	        table.getRow(2).getCell(1).setText(Double.toString(lboTrxnTotal.getTrxnTotal()));
        }
		
	}
	
}
