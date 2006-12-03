package com.mucommander.auth;

import com.mucommander.file.FileURL;

/**
 * MappedCredentials extends Credentials to map the login and password pair onto a location designated by a
 * {@link FileURL} and add the notion of persitency used by {@link CredentialsManager}.
 *
 * @see CredentialsManager 
 * @author Maxence Bernard
 */
public class MappedCredentials extends Credentials {

    /** The location credentials are mapped onto */
    private FileURL location;

    /** Should these credentials be saved to disk ? */
    private boolean isPersistent;


    /**
     * Creates a new MappedCredentials instance.
     *
     * @param login the login part as a string
     * @param password the password part as a string
     * @param location the location credentials are mapped onto.
     * @param isPersistent if true, indicates to CredentialsManager that the credentials should be saved when the
     * application is terminated.
     */
    public MappedCredentials(String login, String password, FileURL location, boolean isPersistent) {
        super(login, password);
        this.isPersistent = isPersistent;
        this.location = location;
    }


    /**
     * Creates a new MappedCredentials instance, using the specified Credentials to retrieve the login and password.
     *
     * @param credentials the login and password to use
     * @param location the location credentials are mapped onto.
     * @param isPersistent if true, indicates to CredentialsManager that the credentials should be saved when the
     * application is terminated.
     */
    public MappedCredentials(Credentials credentials, FileURL location, boolean isPersistent) {
        super(credentials.getLogin(), credentials.getPassword());
        this.isPersistent = isPersistent;
        this.location = location;
    }


    /**
     * Returns the location credentials are mapped onto.
     */
    public FileURL getMappedLocation() {
        try {
            FileURL clonedURL = (FileURL) location.clone();
            clonedURL.setCredentials(this);
            return clonedURL;
        }
        catch(CloneNotSupportedException e) {
            // Should never happen, clone is supported by FileURL
            return null;
        }
    }


    /**
     * Returns true if these credentials should be saved when the application is terminated.
     */
    public boolean isPersistent() {
        return isPersistent;
    }


    public boolean equals(Object o) {
        if(!super.equals(o))
            return false;

        return ((MappedCredentials)o).location.equals(this.location);
    }


    public String toString() {
        return location.getStringRep(false);
    }
}
