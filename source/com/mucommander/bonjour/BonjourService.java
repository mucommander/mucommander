/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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
     * @return the service's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name appended with the URL's protocol.
     * @return the name appended with the URL's protocol.
     */
    public String getNameWithProtocol() {
        return name+" ["+url.getProtocol().toUpperCase()+"]";
    }

    
    /**
     * Returns the location of this service.
     * @return the location of this service.
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
