package com.mucommander.xml;

import java.util.*;

/**
 * Container for XML attributes.
 * <p>
 * This class is meant for use with {@link com.mucommander.ant.util.XmlWriter}.
 * It's used to hold a list of XML attributes that will be passed to one of
 * the {@link com.mucommander.ant.util.XmlWriter#openTag(String,XmlAttributes) tag opening}
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
