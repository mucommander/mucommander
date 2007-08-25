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

import com.mucommander.xml.XmlWriter;
import org.apache.tools.ant.BuildException;

/**
 * Ant representation of the value of an <code>integer</code> property.
 * <h3>Description</h3>
 * <p>
 * Declares an integer value in a unnamed property list collection - that is,
 * in {@link com.mucommander.ant.macosx.ArrayKey arrays}.
 * </p>
 * <h3>Parameters</h3>
 * <p>
 * <table border="1" cellpadding="2" cellspacing="0">
 *   <tr>
 *     <td valign="top"><b>Attribute</b></td>
 *     <td valign="top"><b>Description</b></td>
 *     <td valign="top"><b>Required</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link #setValue(int) value}</td>
 *     <td valign="top">
 *       The integer property's value. This can be any signed or unsigned
 *       base 10 integer.
 *     </td>
 *     <td valign="top">Yes</td>
 *   </tr>
 * </table>
 * </p>
 * <h3>Examples</h3>
 * <blockquote>
 *   <pre>
 * &lt;array name=&quot;MuCommanderExample&quot;&gt;
 *   &lt;integer value=&quot;10&quot;/&gt;
 * &lt;/array&gt;
 *   </pre>
 * </blockquote>
 * <p>
 * creates an array containing an integer property of value 10.
 * This will generate the following entry in <code>Info.plist</code>:
 * </p>
 * <blockquote>
 *   <pre>
 * &lt;key&gt;MuCommanderExample&lt;/key&gt;
 * &lt;array&gt;
 *   &lt;integer&gt;10&lt;/integer&gt;
 * &lt;/array&gt;
 *   </pre>
 * </blockquote>
 * @author Nicolas Rinaudo
 * @see    com.mucommander.ant.macosx.IntegerKey
 */
class IntegerValue implements InfoElement {
    // - Fields ----------------------------------------------------------
    // -------------------------------------------------------------------
    /** Label of the 'integer' XML element. */
    private static final String  ELEMENT_INTEGER = "integer";
    /** Value of the integer. */
    private              Integer value;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Creates an integer property.
     */
    public IntegerValue() {}

    /**
     * Creates an integer property with the specified value.
     * @param i value of the property.
     */
    public IntegerValue(int i) {setValue(i);}



    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Sets the value of the integer property.
     * @param i value of the property.
     */
    public void setValue(int i) {value = new Integer(i);}



    // - XML output ------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the <code>Info.plist</code> representation of this integer value.
     * <p>
     * Assuming the property's value has been set, this will:<br/>
     * - open an <code>integer</code> element.<br/>
     * - write the property's value as nested text.<br/>
     * - close the <code>integer</code> element.
     * </p>
     * @param     out            where to write the property's value to.
     * @exception BuildException thrown if the value of the property has not been set.
     */
    public void write(XmlWriter out) throws BuildException {
        // Makes sure the integer's value has been set.
        if(value == null)
            throw new BuildException("Uninitialised integer property.");

        out.startElement(ELEMENT_INTEGER);
        out.writeCData(value.toString());
        out.endElement(ELEMENT_INTEGER);
    }
}
