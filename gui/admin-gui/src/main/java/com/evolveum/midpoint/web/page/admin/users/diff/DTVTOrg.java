package com.evolveum.midpoint.web.page.admin.users.diff;

/**
 * Created by arda.nural on 7/1/2015.
 */
public class DTVTOrg {
	private String name;
	private String description;
	private String displayName;
	private String identifier;

	private String orgType;
	private String locality;
	private String parentOrgRef;

	private String mailDomain;
	private String detsisNo;
	private String detsisStatus;
	private String organisationStatus;
	private String ustBirimKimlik;
	private String idareKimlikNo;
	private String birimTip;
	private String antet;
	private String yonetici;
	private String iletisimBilgisi;
	private String disMuhattap;
	private String icMuhattap;
	private String hiyerarsiSira;
	private String administrativeStatus;

	public String getOrgType() {
		return orgType;
	}

	public void setOrgType(String orgType) {
		this.orgType = orgType;
	}

	public String getLocality() {
		return locality;
	}

	public void setLocality(String locality) {
		this.locality = locality;
	}

	public DTVTOrg() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getParentOrgRef() {
		return parentOrgRef;
	}

	public void setParentOrgRef(String parentOrgRef) {
		this.parentOrgRef = parentOrgRef;
	}

	public String getDetsisNo() {
		return detsisNo;
	}

	public void setDetsisNo(String detsisNo) {
		this.detsisNo = detsisNo;
	}

	public String getDetsisStatus() {
		return detsisStatus;
	}

	public void setDetsisStatus(String detsisStatus) {
		this.detsisStatus = detsisStatus;
	}

	public String getOrganisationStatus() {
		return organisationStatus;
	}

	public void setOrganisationStatus(String organisationStatus) {
		this.organisationStatus = organisationStatus;
	}

	public String getIdentifier() {
		return identifier;

	}

	public String getAdministrativeStatus() {
		return administrativeStatus;
	}

	public void setAdministrativeStatus(String administrativeStatus) {
		this.administrativeStatus = administrativeStatus;
	}

}
