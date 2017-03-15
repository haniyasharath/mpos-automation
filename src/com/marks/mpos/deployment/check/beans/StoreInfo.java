package com.marks.mpos.deployment.check.beans;

public class StoreInfo {

	private StoreDetails storeDetails;
	private StoreReport storeReport;

	public StoreInfo() { }

	public StoreInfo(StoreDetails storeDetails) {
		this.storeDetails = storeDetails;
	}

	public StoreDetails getStoreDetails() {
		return storeDetails;
	}

	public void setStoreDetails(StoreDetails storeDetails) {
		this.storeDetails = storeDetails;
	}

	public StoreReport getStoreReport() {
		return storeReport;
	}

	public void setStoreReport(StoreReport storeReport) {
		this.storeReport = storeReport;
	}
}
