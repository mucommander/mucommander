
package com.mucommander.file;


/**
 * This class is a container for a login and password combo, used to authenticate
 * on a remote filesystem.
 *
 * @author Maxence Bernard
 */
public class AuthInfo  {

	private String login;
	private String password;
	
	/**
	 * Creates a new instance with the supplied login and password (password may be <code>null</code>.
	 */
	AuthInfo(String login, String password) {
		this.login = login;
		this.password = password;
	}

	/**
	 * Returns the login information.
	 */
	String getLogin() {
		return login;
	}
	
	/**
	 * Returns the password information.
	 */
	String getPassword() {
		return password;
	}

	
	/** 
	 * Returns the login and password information contained in this url, wrapped in an AuthInfo instance.
	 * Returns <code>null</code> if there is no login in the supplied URL.
	 */
	static AuthInfo getAuthInfo(FileURL fileURL) {
		String login = fileURL.getLogin();
		if(login==null)
			return null;
		
		return new AuthInfo(login, fileURL.getPassword());
	}

	
	public String toString() {
		return "("+login+","+password+")";
	}
}

