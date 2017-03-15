package com.marks.mpos.deployment.check.beans;

public class DTEDetails {
	private String dTEHostName;
	private String storeName;

	public DTEDetails() {
	};

	public DTEDetails(String dTEHostName, String storeName) {
		this.dTEHostName = dTEHostName;
		this.storeName = storeName;
	}
	
	public DTEDetails(String dTEHostName) {
		this.dTEHostName = dTEHostName;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getdTEHostName() {
		return dTEHostName;
	}

	public void setdTEHostName(String dTEHostName) {
		this.dTEHostName = dTEHostName;
	}

}
