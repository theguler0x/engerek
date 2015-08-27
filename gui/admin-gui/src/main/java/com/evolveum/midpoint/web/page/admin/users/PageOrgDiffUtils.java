package com.evolveum.midpoint.web.page.admin.users;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PageOrgDiffUtils implements Serializable {

	private int targetDeleted = 0;
	private int targetAdded = 0;
	private int count = 0;
	private int engerekOrg = 0;
	private int detsisOrg = 0;
	private String orgSayi = "";
	private Map<String, String> deletedMap = new HashMap<String, String>();
	private Map<String, String> addedMap = new HashMap<String, String>();

	

	private static PageOrgDiffUtils instance = null;

	private PageOrgDiffUtils() {

	}
	
	public Map<String, String> getAddedMap() {
		return addedMap;
	}

	public void setAddedMap(Map<String, String> addedMap) {
		this.addedMap = addedMap;
	}

	public Map<String, String> getDeletedMap() {
		return deletedMap;
	}
	

	public void setDeletedMap(Map<String, String> deletedMap) {
		this.deletedMap = deletedMap;
	}

	public synchronized static PageOrgDiffUtils getInstance() {
		if (instance == null) {
			instance = new PageOrgDiffUtils();
		}
		return instance;
	}

	public int getTargetDeleted() {
		return targetDeleted;
	}

	public void setTargetDeleted(int targetDeleted) {
		this.targetDeleted = targetDeleted;
	}

	public int getTargetAdded() {
		return targetAdded;
	}

	public void setTargetAdded(int targetAdded) {
		this.targetAdded = targetAdded;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getEngerekOrg() {
		return engerekOrg;
	}

	public void setEngerekOrg(int engerekOrg) {
		this.engerekOrg = engerekOrg;
	}

	public int getDetsisOrg() {
		return detsisOrg;
	}

	public void setDetsisOrg(int detsisOrg) {
		this.detsisOrg = detsisOrg;
	}

	public String getOrgSayi() {
		return orgSayi;
	}

	public void setOrgSayi(String orgSayi) {
		this.orgSayi = orgSayi;
	}

}
