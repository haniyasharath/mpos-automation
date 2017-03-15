package com.marks.mpos.deployment.check.beans;

public class BusinessAreaCounts {
	private int hierarchyCount;
	private int productCount;
	private int buCount;
	private int priceCount;
	private int priceAdvCount;
	private int taxRatesCount;
	private int taxExemptionCount;
	private int promoCount;

	public BusinessAreaCounts() {
	}

	public BusinessAreaCounts(int hierarchyCount, int productCount, int buCount, int priceCount, int priceAdvCount,
			int taxRatesCount, int taxExemptionCount, int promoCount) {
		this.hierarchyCount = hierarchyCount;
		this.productCount = productCount;
		this.buCount = buCount;
		this.priceCount = priceCount;
		this.priceAdvCount = priceAdvCount;
		this.taxRatesCount = taxRatesCount;
		this.taxExemptionCount = taxExemptionCount;
		this.promoCount = promoCount;
	}

	public int getHierarchyCount() {
		return hierarchyCount;
	}

	public void setHierarchyCount(int hierarchyCount) {
		this.hierarchyCount = hierarchyCount;
	}

	public int getProductCount() {
		return productCount;
	}

	public void setProductCount(int productCount) {
		this.productCount = productCount;
	}

	public int getBuCount() {
		return buCount;
	}

	public void setBuCount(int buCount) {
		this.buCount = buCount;
	}

	public int getPriceCount() {
		return priceCount;
	}

	public void setPriceCount(int priceCount) {
		this.priceCount = priceCount;
	}

	public int getPriceAdvCount() {
		return priceAdvCount;
	}

	public void setPriceAdvCount(int priceAdvCount) {
		this.priceAdvCount = priceAdvCount;
	}

	public int getTaxRatesCount() {
		return taxRatesCount;
	}

	public void setTaxRatesCount(int taxRatesCount) {
		this.taxRatesCount = taxRatesCount;
	}

	public int getTaxExemptionCount() {
		return taxExemptionCount;
	}

	public void setTaxExemptionCount(int taxExemptionCount) {
		this.taxExemptionCount = taxExemptionCount;
	}

	public int getPromoCount() {
		return promoCount;
	}

	public void setPromoCount(int promoCount) {
		this.promoCount = promoCount;
	}
	
	

}
