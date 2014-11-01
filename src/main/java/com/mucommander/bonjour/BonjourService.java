/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

import com.mucommander.commons.file.FileURL;

/**
 * A simple container for a Bonjour service described by a name and URL.
 *
 * @author Maxence Bernard
 */
public class BonjourService {

    /** the unqualified name of the service, e.g. 'foobar' */
    private String name;

    /** the url pointing to the service's location */
    private FileURL url;

    /** the fully qualified name of the service, e.g. 'foobar._http._tcp.local' */
    private String fullyQualifiedName;


    /**
     * Creates a new BonjourService instance using the given name and URL.
     *
     * @param name the unqualified name of the service, e.g. 'foobar'
     * @param url the url pointing to the service's location
     * @param fullyQualifiedName the fully qualified name of the service, e.g. 'foobar._http._tcp.local'
     */
    public BonjourService(String name, FileURL url, String fullyQualifiedName) {
        this.name = name;
        this.url = url;
        this.fullyQualifiedName = fullyQualifiedName;
    }


    /**
     * Returns the unqualified name of this service, e.g. 'foobar'.
     *
     * @return the unqualified name of this service
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the name appended with the URL's scheme.
     *
     * @return the name appended with the URL's scheme.
     */
    public String getNameWithProtocol() {
        return name+" ["+url.getScheme().toUpperCase()+"]";
    }

    /**
     * Returns the location of this service.
     *
     * @return the location of this service.
     */
    public FileURL getURL() {
        return url;
    }

    /**
     * Returns the fully qualified name of this service, e.g. 'foobar._http._tcp.local'
     *
     * @return the fully qualified name of this service
     */
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns <code>true</code> if the given Object is a BonjourService instance with the same fully qualified name.
     */
    public boolean equals(Object o) {
        if(!(o instanceof BonjourService))
            return false;

        return fullyQualifiedName.equals(((BonjourService)o).fullyQualifiedName);
    }


    /**
     * Returns a String representation of this BonjourService in the form name / url.
     */
    public String toString() {
        return name+" / "+url.toString(false);
    }
}
