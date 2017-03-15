package com.marks.mpos.deployment.check.beans;

public class CBODetails {
	private String ServerIP;

	public CBODetails(String serverIP) {
		this.ServerIP = serverIP;
	}

	public CBODetails() {
	}

	public String getServerIP() {
		return ServerIP;
	}

	public void setServerIP(String serverIP) {
		ServerIP = serverIP;
	}

}
