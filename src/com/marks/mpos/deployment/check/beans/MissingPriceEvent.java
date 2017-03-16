package com.marks.mpos.deployment.check.beans;

import org.joda.time.DateTime;

public class MissingPriceEvent {

	private DateTime scriptRunDate;
	private String priceEvent;
	private String startDate;
	private String endDate;
	private String eventType;
	
	public DateTime getScriptRunDate() {
		return scriptRunDate;
	}
	public void setScriptRunDate(DateTime scriptRunDate) {
		this.scriptRunDate = scriptRunDate;
	}
	public String getPriceEvent() {
		return priceEvent;
	}
	public void setPriceEvent(String priceEvent) {
		this.priceEvent = priceEvent;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
}
