package com.mucommander.auth;

import com.mucommander.file.FileURL;

/**
 * @author Maxence Bernard
 */
public class MappedCredentials extends Credentials {

    private FileURL url;
    private boolean isPersistent;

    public MappedCredentials(String login, String password, FileURL url, boolean isPersistent) {
        super(login, password);
        this.isPersistent = isPersistent;
        this.url = url;
    }

    public MappedCredentials(String login, String password, FileURL url) {
        this(login, password, url, false);
    }
    

    public FileURL getURL() {
        try {
            FileURL clonedURL = (FileURL)url.clone();
            clonedURL.setCredentials(this);
            return clonedURL;
        }
        catch(CloneNotSupportedException e) {
            // Should never happen, clone is supported by FileURL
            return null;
        }
    }


    public boolean isPersistent() {
        return isPersistent;
    }


    public boolean equals(Object o) {
        if(!super.equals(o))
            return false;

        return ((MappedCredentials)o).url.equals(this.url);
    }


    public String toString() {
        return url.getStringRep(false);
    }
}
