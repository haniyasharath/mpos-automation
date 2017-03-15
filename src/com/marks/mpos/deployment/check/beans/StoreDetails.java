package com.marks.mpos.deployment.check.beans;

public class StoreDetails {

	private String storeNumber;
	private String storeHostName;
	
	public StoreDetails() {
	}
	
	public StoreDetails(String storeNumber, String storeHostName) {
		this.storeNumber = storeNumber;
		this.storeHostName = storeHostName;
	}

	public String getStoreNumber() {
		return storeNumber;
	}
	
	public void setStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
	}
	
	public String getStoreHostName() {
		return storeHostName;
	}
	
	public void setStoreHostName(String storeHostName) {
		this.storeHostName = storeHostName;
	}
}
