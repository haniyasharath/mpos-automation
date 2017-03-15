package com.marks.mpos.deployment.check.beans;

public class LBOCheck {

	private String storeNumber;
	private int backofficeCount;
	private int peCount;
	
	public LBOCheck() {
	}

	public String getStoreNumber() {
		return storeNumber;
	}

	public void setStoreNumber(String storeNumber) {
		this.storeNumber = storeNumber;
	}

	public int getBackofficeCount() {
		return backofficeCount;
	}

	public void setBackofficeCount(int backofficeCount) {
		this.backofficeCount = backofficeCount;
	}

	public int getPeCount() {
		return peCount;
	}

	public void setPeCount(int peCount) {
		this.peCount = peCount;
	}
	
}
