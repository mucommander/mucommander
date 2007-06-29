/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


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
     * @param login    the login part as a string
     * @param password the password part as a string
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

        // Do not test password, only login so that if password changes Credentials are replaced by CredentialsManager
        // and not duplicated
        return credentials.login.equals(this.login);
    }


    /**
     * Returns a cloned instance of these Credentials.
     */
    public Object clone() {
        try {
            return super.clone();
        }
        catch(CloneNotSupportedException e) {
            // Should never happen
            return null;
        }
    }

	
    public String toString() {
        return login;
    }
}

