
package com.mucommander.file;

public class AuthInfo  {
	private String login;
	private String password;
	
	AuthInfo(String login, String password) {
		this.login = login;
		this.password = password;
	}

	String getLogin() {
		return login;
	}
	
	String getPassword() {
		return password;
	}

	public String toString() {
		return "("+login+","+password+")";
	}
}

