package com.marks.mpos.deployment.check.beans;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StoreReport {

	private String storeNumber;
	private List<LaneCount> lboTrxnCount;
	private List<LaneCount> cssTrxnCount;
	private int journalCount;
	private TransactionTotal lboTrxnTotal;
	private TransactionTotal cssTrxnTotal;

	private BusinessAreaCounts LBOCounts;
	private BusinessAreaCounts DTECounts;
	private BusinessAreaCounts CBOCounts;
	
	private PriceEventLists lboPriceEventLists;
	private PriceEventLists cboPriceEventLists;
	
	private boolean hasError;
	
	private StoredDataCounts cboDataCounts;
	
	private StoredDataCounts lboDataCounts;
	
	private int lboQueueDepth;
	private int lboStuckTransactionCount;
	
	private Map<String, Integer> cboTxnCount;
	private Map<String, Double> cboTxnAmount;
	private Map<String, Integer> cboStuckTransactionCount;
	
	private List<String> searchedProducts;
	private Set<String> searchedPriceEvents;
	private Set<String> deletedPriceEvents;

	public StoreReport() {
	}

	public StoreReport(String storeNumber, List<LaneCount> lboTrxnCount, List<LaneCount> cssTrxnCount, int journalCount,
			TransactionTotal lboTrxnTotal, TransactionTotal cssTrxnTotal, BusinessAreaCounts lboCounts,
			BusinessAreaCounts dTECounts, BusinessAreaCounts cBOCounts, StoredDataCounts cBackOfficeDataCounts, StoredDataCounts lBackOfficeDataCounts) {
		this.storeNumber = storeNumber;
		this.lboTrxnCount = lboTrxnCount;
		this.cssTrxnCount = cssTrxnCount;
		this.journalCount = journalCount;
		this.lboTrxnTotal = lboTrxnTotal;
		this.cssTrxnTotal = cssTrxnTotal;
		this.LBOCounts = lboCounts;
		this.DTECounts = dTECounts;
		this.CBOCounts = cBOCounts;
		this.cboDataCounts = cBackOfficeDataCounts;
		this.lboDataCounts = lBackOfficeDataCounts;
	}

	public BusinessAreaCounts getCBOCounts() {
		return CBOCounts;
	}

	public void setCBOCounts(BusinessAreaCounts cBOCounts) {
		CBOCounts = cBOCounts;
	}

	public BusinessAreaCounts getDTECounts() {
		return DTECounts;
	}

	public void setDTECounts(BusinessAreaCounts dTECounts) {
		DTECounts = dTECounts;
	}

	public BusinessAreaCounts getLBOCounts() {
		return LBOCounts;
	}

	public void setLBOCounts(BusinessAreaCounts lBOCounts) {
		LBOCounts = lBOCounts;
	}

	public String getStoreNumber() {
		return storeNumber;
	}

	public void setStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
	}

	public List<LaneCount> getLboTrxnCount() {
		return lboTrxnCount;
	}

	public void setLboTrxnCount(List<LaneCount> lboTrxnCount) {
		this.lboTrxnCount = lboTrxnCount;
	}

	public List<LaneCount> getCssTrxnCount() {
		return cssTrxnCount;
	}

	public void setCssTrxnCount(List<LaneCount> cssTrxnCount) {
		this.cssTrxnCount = cssTrxnCount;
	}

	public int getJournalCount() {
		return journalCount;
	}

	public void setJournalCount(int journalCount) {
		this.journalCount = journalCount;
	}

	public TransactionTotal getLboTrxnTotal() {
		return lboTrxnTotal;
	}

	public void setLboTrxnTotal(TransactionTotal lboTrxnTotal) {
		this.lboTrxnTotal = lboTrxnTotal;
	}

	public TransactionTotal getCssTrxnTotal() {
		return cssTrxnTotal;
	}

	public void setCssTrxnTotal(TransactionTotal cssTrxnTotal) {
		this.cssTrxnTotal = cssTrxnTotal;
	}

	public boolean hasError() {
		return hasError;
	}

	public void setError(boolean hasError) {
		this.hasError = hasError;
	}

	public StoredDataCounts getCboDataCounts() {
		return cboDataCounts;
	}

	public void setCboDataCounts(StoredDataCounts cboDataCounts) {
		this.cboDataCounts = cboDataCounts;
	}

	public StoredDataCounts getLboDataCounts() {
		return lboDataCounts;
	}

	public void setLboDataCounts(StoredDataCounts lboDataCounts) {
		this.lboDataCounts = lboDataCounts;
	}

	public PriceEventLists getLboPriceEventLists() {
		return lboPriceEventLists;
	}

	public void setLboPriceEventLists(PriceEventLists lboPriceEventLists) {
		this.lboPriceEventLists = lboPriceEventLists;
	}

	public PriceEventLists getCboPriceEventLists() {
		return cboPriceEventLists;
	}

	public void setCboPriceEventLists(PriceEventLists cboPriceEventLists) {
		this.cboPriceEventLists = cboPriceEventLists;
	}

	public int getLboQueueDepth() {
		return lboQueueDepth;
	}

	public void setLboQueueDepth(int lboQueueDepth) {
		this.lboQueueDepth = lboQueueDepth;
	}

	public int getLboStuckTransactionCount() {
		return lboStuckTransactionCount;
	}

	public void setLboStuckTransactionCount(int lboStuckTransactionCount) {
		this.lboStuckTransactionCount = lboStuckTransactionCount;
	}

	public Map<String, Integer> getCboTxnCount() {
		return cboTxnCount;
	}

	public void setCboTxnCount(Map<String, Integer> cboTxnCount) {
		this.cboTxnCount = cboTxnCount;
	}

	public Map<String, Double> getCboTxnAmount() {
		return cboTxnAmount;
	}

	public void setCboTxnAmount(Map<String, Double> cboTxnAmount) {
		this.cboTxnAmount = cboTxnAmount;
	}

	public Map<String, Integer> getCboStuckTransactionCount() {
		return cboStuckTransactionCount;
	}

	public void setCboStuckTransactionCount(Map<String, Integer> cboStuckTransactionCount) {
		this.cboStuckTransactionCount = cboStuckTransactionCount;
	}

	public List<String> getSearchedProducts() {
		return searchedProducts;
	}

	public void setSearchedProducts(List<String> searchedProducts) {
		this.searchedProducts = searchedProducts;
	}

	public Set<String> getSearchedPriceEvents() {
		return searchedPriceEvents;
	}

	public void setSearchedPriceEvents(Set<String> searchedPriceEvents) {
		this.searchedPriceEvents = searchedPriceEvents;
	}

	public Set<String> getDeletedPriceEvents() {
		return deletedPriceEvents;
	}

	public void setDeletedPriceEvents(Set<String> deletedPriceEvents) {
		this.deletedPriceEvents = deletedPriceEvents;
	}
}
