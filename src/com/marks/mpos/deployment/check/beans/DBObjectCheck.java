package com.marks.mpos.deployment.check.beans;

import static com.marks.mpos.deployment.check.queries.IDatabaseQueries.*;

import static com.marks.mpos.deployment.check.properties.IEnvironmentProperties.*;

public class DBObjectCheck {

	private String cboAllObjects;
	private String cboAllTables;
	private String cssAllObjects;
	private String cssAllTables;
	private String ppsAllObjects;
	private String ppsAllTables;
	
	public DBObjectCheck() {
	}

	public String getCboAllObjects() {
		return cboAllObjects;
	}

	public void setCboAllObjects(String cboAllObjects) {
		this.cboAllObjects = CBO_ALL_OBJECTS_QUERY + SEMI_COLON_SYMBOL + "\n" + cboAllObjects + "\n";
	}

	public String getCboAllTables() {
		return cboAllTables;
	}

	public void setCboAllTables(String cboAllTables) {
		this.cboAllTables = CBO_ALL_TABLES_QUERY + SEMI_COLON_SYMBOL + "\n" + cboAllTables + "\n";
	}

	public String getCssAllObjects() {
		return cssAllObjects;
	}

	public void setCssAllObjects(String cssAllObjects) {
		this.cssAllObjects = CSS_ALL_OBJECTS_QUERY + SEMI_COLON_SYMBOL + "\n" + cssAllObjects + "\n";
	}

	public String getCssAllTables() {
		return cssAllTables;
	}

	public void setCssAllTables(String cssAllTables) {
		this.cssAllTables = CSS_ALL_TABLES_QUERY + SEMI_COLON_SYMBOL + "\n" + cssAllTables + "\n";
	}

	public String getPpsAllObjects() {
		return ppsAllObjects;
	}

	public void setPpsAllObjects(String ppsAllObjects) {
		this.ppsAllObjects = PPS_ALL_OBJECTS_QUERY + SEMI_COLON_SYMBOL + "\n" + ppsAllObjects + "\n";
	}

	public String getPpsAllTables() {
		return ppsAllTables;
	}

	public void setPpsAllTables(String ppsAllTables) {
		this.ppsAllTables = PPS_ALL_TABLES_QUERY + SEMI_COLON_SYMBOL + "\n" + ppsAllTables + "\n";
	}
	
}
