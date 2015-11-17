package com.evolveum.midpoint.model.impl.bulkmodify;

public class ModifyUserBean {
	
	private String userName;
	private String status;
	private String role; //normally it's a list 1-n relationship
	
	
	public ModifyUserBean(String userName, String status, String role){
		
		this.userName = userName;
		this.status = status;
		this.role = role;
	}
	
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
	
	
	

}
