/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.xml;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * Container for XML attributes.
 * <p>
 * This class is meant for use with {@link com.mucommander.xml.XmlWriter}.
 * It's used to hold a list of XML attributes that will be passed to one of
 * the {@link com.mucommander.xml.XmlWriter#startElement(String,XmlAttributes) element opening}
 * methods.
 * </p>
 * @author Nicolas Rinaudo
 */
public class XmlAttributes {
    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Contains the XML attributes. */
    private Hashtable attributes;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Builds a new, empty XmlAttributes instance.
     */
    public XmlAttributes() {attributes = new Hashtable();}



    // - Content handling ------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Returns the value associated with the specified attribute name.
     * @param name name of the attribute whose value should be retrieved.
     * @return the value associated with the specified attribute name if found,
     *         <code>null</code> otherwise.
     */
    public String getValue(String name) {return (String)attributes.get(name);}

    /**
     * Clears the list of all previously defined attributes.
     */
    public void clear() {attributes.clear();}

    /**
     * Adds the specified attribute to this container.
     * @param name  name of the attribute to whose value should be set.
     * @param value value to which the attribute should be set.
     */
    public void add(String name, String value) {attributes.put(name, value);}

    /**
     * Returns an iterator on the attributes contained by this instance.
     * @return an iterator on the attributes contained by this instance.
     */
    public Iterator names() {return attributes.keySet().iterator();}
}
