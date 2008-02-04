/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.auth;

import com.mucommander.file.FileURL;

/**
 * CredentialsMapping associates a {@link Credentials} instance with a 'realm' , that is the location to a server.
 * It also adds the notion of persistency, allowing to specify whether the credentials should be saved to disk when the
 * application quits and restored next time the application starts.
 *
 * @see CredentialsManager 
 * @author Maxence Bernard
 */
public final class CredentialsMapping {

    /** User credentials */
    private Credentials credentials;

    /** The location credentials are associated with */
    private FileURL realm;

    /** Should these credentials be saved to disk ? */
    private boolean isPersistent;

    
    /**
     * Creates a new CredentialsMapping instance that associates the specified credentials with the given location.
     *
     * @param credentials user credentials
     * @param realm the location to associate the credentials with
     * @param isPersistent if true, indicates to CredentialsManager that the credentials should be saved when the
     * application terminates.
     */
    public CredentialsMapping(Credentials credentials, FileURL realm, boolean isPersistent) {
        this.credentials = credentials;
        this.isPersistent = isPersistent;
        this.realm = FileURL.resolveRealm(realm);
    }

    /**
     * Returns the credentials.
     *
     * @return the credentials
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Returns the location associated with the credentials.
     * <p>
     * Note: the returned {@link FileURL} does not contain any credentials.
     * </p>
     *
     * @return the location associated with the credentials.
     */
    public FileURL getRealm() {
        return realm;
    }

    /**
     * Returns <code>true</code> if these credentials should be saved when the application terminates.
     *
     * @return <code>true</code> if these credentials should be saved when the application terminates, <code>false</code> otherwise.
     */
    public boolean isPersistent() {
        return isPersistent;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns <code>true</code> if the given Object is a {@link com.mucommander.auth.CredentialsMapping} instance
     * whose credentials and realm are equals to those of this instance.
     *
     * @param o the Object to test for equality
     * @return true if both CredentialsMapping instances are equal
     */
    public boolean equals(Object o) {
        if(!(o instanceof CredentialsMapping))  // Note: CredentialsMapping is final, no need to test classes
            return false;

        CredentialsMapping cm = (CredentialsMapping)o;

        return cm.credentials.equals(this.credentials) && cm.realm.equals(this.realm);
    }

    public String toString() {
        return credentials.toString()+" "+realm.toString(false);
    }
}
