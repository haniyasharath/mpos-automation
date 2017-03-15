package com.marks.mpos.deployment.check.beans;

public class LaneCount {
	private String laneNumber;
	private int laneVoidedCount;
	private int laneNonVoidedCount;
	private int laneTotalCount;
	
	public LaneCount() {
	}

	public LaneCount(String laneNumber, int laneVoidedCount,
			int laneNonVoidedCount, int laneTotalCount) {
		this.laneNumber = laneNumber;
		this.laneVoidedCount = laneVoidedCount;
		this.laneNonVoidedCount = laneNonVoidedCount;
		this.laneTotalCount = laneTotalCount;
	}

	public String getLaneNumber() {
		return laneNumber;
	}

	public void setLaneNumber(String laneNumber) {
		this.laneNumber = laneNumber;
	}

	public int getLaneVoidedCount() {
		return laneVoidedCount;
	}

	public void setLaneVoidedCount(int laneVoidedCount) {
		this.laneVoidedCount = laneVoidedCount;
	}

	public int getLaneNonVoidedCount() {
		return laneNonVoidedCount;
	}

	public void setLaneNonVoidedCount(int laneNonVoidedCount) {
		this.laneNonVoidedCount = laneNonVoidedCount;
	}

	public int getLaneTotalCount() {
		return laneTotalCount;
	}

	public void setLaneTotalCount(int laneTotalCount) {
		this.laneTotalCount = laneTotalCount;
	}
	
	
}
