package com.mucommander.auth;

import com.mucommander.file.FileURL;
import com.mucommander.file.FileProtocols;
import com.mucommander.Debug;

import java.net.MalformedURLException;

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

        try {
            this.realm = resolveRealm(location);
        }
        catch(MalformedURLException e) {
            // Should never happen, report the error if it does
            if(Debug.ON) Debug.trace("Error: realm could not be resolved for location: "+location);
        }
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
     * Returns the location to the server these credentials are associated with. The returned {@link FileURL} will not
     * contain any credentials.
     */
    public FileURL getRealm() {
        return realm;
    }


    /**
     * Returns the realm of a given location, that the URL to the server and share, if the location's protocol
     * has a notion of share (e.g. SMB). If the realm FileURL could not be created (MalformedURLException was thrown),
     * null is returned, this should not normally happen. 
     *
     * <p>A few examples:
     * <ul>
     * <li>smb://someserver/someshare/somefolder/somefile -> smb://someserver/someshare/
     * <li>ftp://someserver/somefolder/somefile -> ftp://someserver/
     * <li>smb://someserver/ -> smb://someserver/
     * </ul>
     *
     * @param location the location to a resource on a remote server
     * @return the location's realm, or null if it could not be resolved
     * @throws MalformedURLException if the realm corresponding to the given location could not be resolved
     */
    public static FileURL resolveRealm(FileURL location) throws MalformedURLException {
        String protocol = location.getProtocol();
        String newPath = "/";

        if(protocol.equals(FileProtocols.SMB)) {
            String tokens[] = location.getPath().split("[/\\\\]");
            for(int i=0; i<tokens.length; i++) {
                if(!tokens[i].equals("")) {
                    newPath += tokens[i]+'/';
                    break;
                }
            }
        }

        String host = location.getHost();
        if(host==null)
            return new FileURL(protocol+"://"+newPath);
        else
            return new FileURL(protocol+"://"+host+newPath);
    }
    

    /**
     * Returns true if these credentials should be saved when the application terminates.
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
        return realm.getStringRep(false);
    }
}
