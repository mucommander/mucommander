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
 * MappedCredentials extends Credentials to associate the login and password pair to a 'realm', that is the location to
 * a server and share for applicable protocols (like SMB). It also adds the notion of persistency, allowing to specify
 * whether the credentials should be saved to disk when the application quits and restored next time the application starts.
 *
 * @see CredentialsManager 
 * @author Maxence Bernard
 */
public class MappedCredentials extends Credentials {

    /** The location credentials are associated with */
    private FileURL realm;

    /** Should these credentials be saved to disk ? */
    private boolean isPersistent;

    
    /**
     * Creates a new MappedCredentials instance.
     *
     * @param login the login part as a string
     * @param password the password part as a string
     * @param location the server location used to determine the realm these credentials are associated with
     * @param isPersistent if true, indicates to CredentialsManager that the credentials should be saved when the
     * application terminates.
     */
    public MappedCredentials(String login, String password, FileURL location, boolean isPersistent) {
        super(login, password);

        this.isPersistent = isPersistent;
        this.realm = FileURL.resolveRealm(location);
    }


    /**
     * Creates a new MappedCredentials instance, using the specified Credentials to retrieve the login and password.
     *
     * @param credentials the login and password to use
     * @param location the server location used to determine the realm these credentials are associated with
     * @param isPersistent if true, indicates to CredentialsManager that the credentials should be saved when the
     * application terminates.
     */
    public MappedCredentials(Credentials credentials, FileURL location, boolean isPersistent) {
        this(credentials.getLogin(), credentials.getPassword(), location, isPersistent);
    }


    /**
     * Returns the location to the server these credentials are associated with.
     * <p>
     * The returned {@link FileURL} will not contain any credentials.
     * </p>
     * @return the location to the server these credentials are associated with.
     */
    public FileURL getRealm() {
        return realm;
    }


    /**
     * Returns <code>true</code> if these credentials should be saved when the application terminates.
     * @return <code>true</code> if these credentials should be saved when the application terminates, <code>false</code> otherwise.
     */
    public boolean isPersistent() {
        return isPersistent;
    }

    
    /**
     * Returns true if the provided Object is a Credentials instance which login, password and realm are equal
     * to the ones in this instance.
     */
    public boolean equals(Object o) {
        if(!(o instanceof MappedCredentials) || !super.equals(o))
            return false;

        return ((MappedCredentials)o).realm.equals(this.realm);
    }


    public String toString() {
        return realm.toString(false);
    }
}
