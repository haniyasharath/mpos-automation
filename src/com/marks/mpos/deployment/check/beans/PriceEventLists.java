package com.marks.mpos.deployment.check.beans;

import java.util.ArrayList;
import java.util.List;

public class PriceEventLists {

	private List<String> basicPriceEvents;
	private List<String> specialPriceEvents;
	private List<String> bogoPriceEvents;
	private List<String> xForYPriceEvents;

	public List<String> getBasicPriceEvents() {
		if(basicPriceEvents == null) {
			basicPriceEvents = new ArrayList<String>();
		}
		return basicPriceEvents;
	}

	public void setBasicPriceEvents(List<String> basicPriceEvents) {
		this.basicPriceEvents = basicPriceEvents;
	}

	public List<String> getSpecialPriceEvents() {
		if(specialPriceEvents == null) {
			specialPriceEvents = new ArrayList<String>();
		}
		return specialPriceEvents;
	}

	public void setSpecialPriceEvents(List<String> specialPriceEvents) {
		this.specialPriceEvents = specialPriceEvents;
	}

	public List<String> getBogoPriceEvents() {
		if(bogoPriceEvents == null) {
			bogoPriceEvents = new ArrayList<String>();
		}
		return bogoPriceEvents;
	}

	public void setBogoPriceEvents(List<String> bogoPriceEvents) {
		this.bogoPriceEvents = bogoPriceEvents;
	}

	public List<String> getxForYPriceEvents() {
		if(xForYPriceEvents == null) {
			xForYPriceEvents = new ArrayList<String>();
		}
		return xForYPriceEvents;
	}

	public void setxForYPriceEvents(List<String> xForYPriceEvents) {
		this.xForYPriceEvents = xForYPriceEvents;
	}
}
