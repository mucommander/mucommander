
package com.mucommander.auth;

import com.mucommander.bookmark.XORCipher;


/**
 * This class is a container for a login and password combo, used to authenticate
 * on a remote filesystem.
 *
 * @author Maxence Bernard
 */
public class Credentials {

    private String login;
    private String password;
	
    /**
     * Creates a new instance with the supplied login and password.
     */
    public Credentials(String login, String password) {
        // Replace null values by empty strings
        if(login==null)
            login="";

        if(password==null)
            password="";

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
     * Returns the (weakly) encrypted password.
     */
    public String getEncryptedPassword() {
        return XORCipher.encryptXORBase64(password);
    }


    public boolean equals(Object o) {
        if(!(o instanceof Credentials))
            return false;

        Credentials credentials = (Credentials)o;

        return credentials.login.equals(this.login) && credentials.password.equals(this.password);
    }

	
    public String toString() {
        return login;
    }
}

