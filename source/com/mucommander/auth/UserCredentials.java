package com.mucommander.auth;

import com.mucommander.file.FileURL;

import java.net.MalformedURLException;

/**
 * UserCredentials is a container for credentials entered by the user. It extends {@link MappedCredentials} to add the
 * notion of persistency used by {@link CredentialsManager}.
 *
 * @author Maxence Bernard
 */
public class UserCredentials extends MappedCredentials {

    /** Should these credentials be saved to disk ? */
    private boolean isPersistent;


    /**
     * Creates a new UserCredentials instance.
     *
     * @param login the login part as a string
     * @param password the password part as a string
     * @param location the server location used to determine the realm these credentials are associated with
     * @param isPersistent if true, indicates to CredentialsManager that the credentials should be saved when the
     * application is terminated.
     */
    public UserCredentials(String login, String password, FileURL location, boolean isPersistent) {
        super(login, password, location);
        this.isPersistent = isPersistent;
    }


    /**
     * Creates a new UserCredentials instance, using the specified Credentials to retrieve the login and password.
     *
     * @param credentials the login and password to use
     * @param location the server location used to determine the realm these credentials are associated with
     * @param isPersistent if true, indicates to CredentialsManager that the credentials should be saved when the
     * application is terminated.
     */
    public UserCredentials(Credentials credentials, FileURL location, boolean isPersistent) {
        super(credentials, location);
        this.isPersistent = isPersistent;
    }


    /**
     * Returns true if these credentials should be saved when the application is terminated.
     */
    public boolean isPersistent() {
        return isPersistent;
    }
}
