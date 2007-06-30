/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.ant.macosx;

import com.mucommander.xml.writer.XmlWriter;
import org.apache.tools.ant.BuildException;

/**
 * Wrapper for all Info.plist named elements.
 * <p>
 * Named elements are, rather logically, elements with a name. At the time of
 * writing, this include {@link com.mucommander.ant.macosx.StringKey strings},
 * {@link com.mucommander.ant.macosx.BooleanKey booleans},
 * {@link com.mucommander.ant.macosx.ArrayKey arrays} and
 * {@link com.mucommander.ant.macosx.DictKey dictionaries}. Some elements might
 * not have a name however, such as array values.
 * </p>
 * <p>
 * Named elements are composed of two items: a name and a value.<br/>
 * An element's name is always going to be a string, allowing this class.
 * to offer Ant name setting support. Any class that extends it will automatically
 * have full support of the <code>name</code> attribute, both for reading
 * (in the <code>build.xml</code> file) as for writing (in the <code>Info.plist</code>
 * file).<br/>
 * Values, however, cannot be thus generalised. NamedInfoElement tries to go as far as
 * possible in that direction by allowing subclasses to {@link #setValue(InfoElement) set}
 * and {@link #getValue() get} a generic {@link com.mucommander.ant.macosx.InfoElement}
 * value. Elements will still need to work at offering the proper Ant hooks for their
 * values, but won't have to worry about the writing bit, as it is wholly handled
 * by this class.
 * </p>
 * <p>
 * Creating a new property element using this class can be rather easy. The
 * {@link com.mucommander.ant.macosx.StringKey} implementation, for example,
 * is composed of a single method:
 * <pre>
 * public void setValue(String b) {setValue(new StringValue(b));}
 * </pre>
 * The associated {@link com.mucommander.ant.macosx.StringValue} class
 * is just slightly more complex, as it needs to know how to write its content
 * to an XML stream:
 * <pre>
 * public void setValue(String s) {value = s;}
 *
 * public void write(XmlWriter out) throws BuildException {
 *     out.startElement("string");
 *     out.writeCData(value);
 *     out.endElement("string");
 *  }
 * </pre>
 * </p>
 * <p>
 * Note that while it might seem cleaner to group those two classes together,
 * this is not actually possible. Some containers, such as
 * {@link com.mucommander.ant.macosx.ArrayKey arrays}, hold a list of
 * un-named values. Creating a global class for both the name and the value
 * of an element would necessarily result in less than perfect syntax-checking:
 * the <code>setName</code> method being public, array elements will accept
 * the <code>name</code> attribute when they shouldn't.
 * </p>
 * @author Nicolas Rinaudo
 */
public class NamedInfoElement implements InfoElement {
    private static final String ELEMENT_KEY = "key";
    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Element's name. */
    private String      name;
    /** Element's value. */
    private InfoElement value;



    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Sets this element's name.
     * @param s element's name.
     */
    public void setName(String s) {name = s;}



    // - Package tools ---------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Allows subclasses to set the element's value.
     * @param v element's value.
     */
    protected void setValue(InfoElement v) {value = v;}

    /**
     * Allows subclasses to query the element for its value.
     * @return the element's value.
     */
    protected InfoElement getValue() {return value;}

    /**
     * Allows subclasses to query the element for its name.
     * @return the element's name.
     */
    protected String getName() {return name;}



    // - Output ----------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes this element to the specified XML stream.
     * @param     out            where to write this element.
     * @exception BuildException thrown if anything goes wrong.
     */
    public void write(XmlWriter out) throws BuildException {
        // Makes sure the element has been properly initialised.
        if(name == null)
            throw new BuildException("Unnamed key - please fill in the name attribute.");
        if(value == null)
            throw new BuildException("Element value was not set.");

        // Writes the element's name.
        out.startElement(ELEMENT_KEY);
        out.writeCData(name);
        out.endElement(ELEMENT_KEY);

        // Writes the element's value.
        value.write(out);
    }
}
