
package com.mucommander.auth;

import com.mucommander.bookmark.XORCipher;


/**
 * This class is a container for a login and password pair, used to authenticate a location on a remote filesystem.
 *
 * @see com.mucommander.file.FileURL
 * @author Maxence Bernard
 */
public class Credentials {

    private String login;
    private String password;
	
    /**
     * Creates a new instance with the supplied login and password.
     * Any provided null values will be replaced by empty strings.
     *
     * @param login the login part as a string
     * @param login the password part as a string
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
     * Returns the login part. Returned login may be an empty string but never null.
     */
    public String getLogin() {
        return login;
    }
	
    /**
     * Returns the password part. Returned password may be an empty string but never null.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the password as a masked string, each of the characters replaced by '*' characters. 
     */
    public String getMaskedPassword() {
        int passwordLength = password.length();
        StringBuffer maskedPasswordSb = new StringBuffer(passwordLength);
        for(int i=0; i<passwordLength; i++)
            maskedPasswordSb.append('*');

        return maskedPasswordSb.toString();
    }

    /**
     * Returns the password as a weakly encrypted string.
     */
    public String getEncryptedPassword() {
        return XORCipher.encryptXORBase64(password);
    }


    /**
     * Returns true if these credentials are empty, that is both the login and password are empty strings.
     */
    public boolean isEmpty() {
        return "".equals(login) && "".equals(password);
    }


    /**
     * Returns true if the login of the provided credentials (as returned by {@link #getLogin()} equals to one in
     * this Credentials instance, false otherwise. Two Credentials instances with the same login but a different
     * password will thus be equal. If null is passed, true will be returned if these Credentials are empty, as returned
     * by {@link #isEmpty()}.
     */
    public boolean equals(Object o) {
        // Empty Credentials and null are equivalent
        if(o==null)
            return isEmpty();

        if(!(o instanceof Credentials))
            return false;

        Credentials credentials = (Credentials)o;

        return credentials.login.equals(this.login) && credentials.password.equals(this.password);
    }

	
    public String toString() {
        return login;
    }
}

