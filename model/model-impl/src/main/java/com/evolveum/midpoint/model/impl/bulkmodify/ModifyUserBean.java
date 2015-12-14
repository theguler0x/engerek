package com.evolveum.midpoint.model.impl.bulkmodify;

public class ModifyUserBean {
	
	private String userName;
	private String status;
	private String role; //normally it's a list 1-n relationship


	private String email;
	private String firstname;
	private String surname;
	private String phoneNumber;


	public ModifyUserBean(){
		this.userName = "";
		this.status = "";
		this.role = "";
		this.setEmail("");
		this.setFirstname("");
		this.setSurname("");
		this.setPhoneNumber("");

	}
	
	
	public ModifyUserBean(String userName, String status, String role){
		
		this.userName = userName;
		this.status = status;
		this.role = role;
	}

	public ModifyUserBean(String userName, String status, String role, String email, String firstname, String surname, String phoneNumber){

		this.userName = userName;
		this.status = status;
		this.role = role;
		this.setEmail(email);
		this.setFirstname(firstname);
		this.setSurname(surname);
		this.setPhoneNumber(phoneNumber);
	}
	


	
	
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String toString(){
		return "Username: " + this.userName + "\n " +
				"Administrative Status: " + this.status+"\n " +
				"Role: " + this.role+"\n";
	}

	//encapsulate fields
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
}
