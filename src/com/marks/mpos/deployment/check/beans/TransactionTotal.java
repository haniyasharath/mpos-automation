package com.marks.mpos.deployment.check.beans;

public class TransactionTotal {

	private double voidedTotal;
	private double nonVoidedTotal;
	private double trxnTotal;
	
	public TransactionTotal() {
	}
	
	public TransactionTotal(double voidedTotal, double nonVoidedTotal,
			double trxnTotal) {
		this.voidedTotal = voidedTotal;
		this.nonVoidedTotal = nonVoidedTotal;
		this.trxnTotal = trxnTotal;
	}

	public double getVoidedTotal() {
		return voidedTotal;
	}

	public void setVoidedTotal(double voidedTotal) {
		this.voidedTotal = voidedTotal;
	}

	public double getNonVoidedTotal() {
		return nonVoidedTotal;
	}

	public void setNonVoidedTotal(double nonVoidedTotal) {
		this.nonVoidedTotal = nonVoidedTotal;
	}

	public double getTrxnTotal() {
		return trxnTotal;
	}

	public void setTrxnTotal(double trxnTotal) {
		this.trxnTotal = trxnTotal;
	}
	
}
