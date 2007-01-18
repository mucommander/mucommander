package com.mucommander.bonjour;

import com.mucommander.file.FileURL;

/**
 * A simple container for a Bonjour service described by a name and URL.
 *
 * @author Maxence Bernard
 */
public class BonjourService {

    private String name;
    private FileURL url;


    /**
     * Creates a new BonjourService instance using the given name and URL.
     * @param name the name of the Bonjour service
     * @param url the url pointing to the service's location
     */
    public BonjourService(String name, FileURL url) {
        this.name = name;
        this.url = url;
    }


    /**
     * Returns the service's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name appended with the URL's protocol.
     */
    public String getNameWithProtocol() {
        return name+" ["+url.getProtocol().toUpperCase()+"]";
    }

    
    /**
     * Returns the location of this service.
     */
    public FileURL getURL() {
        return url;
    }


    /**
     * Returns true if the given Object is a BonjourService instance with the same name and URL.
     */
    public boolean equals(Object o) {
        if(!(o instanceof BonjourService))
            return false;

        BonjourService bs = (BonjourService)o;

        return name.equals(bs.name) && url.equals(bs.url);
    }


    /**
     * Returns a String representation of this BonjourService in the form name / url.
     */
    public String toString() {
        return name+" / "+url.toString(false);
    }
}
