
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
    public AuthInfo(String login, String password) {
        this.login = login;
        this.password = password;
    }

    /**
     * Returns the login information.
     */
    public String getLogin() {
        return login;
    }
	
    /**
     * Returns the password information.
     */
    public String getPassword() {
        return password;
    }

	
    /** 
     * Returns the login and password information contained in this url, wrapped in an AuthInfo instance.
     * Returns <code>null</code> if there is no login in the supplied URL.
     */
    public static AuthInfo getAuthInfo(FileURL fileURL) {
        String login = fileURL.getLogin();
        if(login==null)
            return null;
		
        String password = fileURL.getPassword();
        return new AuthInfo(login, password==null?"":password);
    }

	
    public String toString() {
        return "("+login+","+password+")";
    }
}

