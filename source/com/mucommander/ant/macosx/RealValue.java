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
import java.io.IOException;

/**
 * Ant representation of the value of an <code>real</code> property.
 * <h3>Description</h3>
 * <p>
 * Declares an real value in a unnamed property list collection - that is,
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
 *     <td valign="top">{@link #setValue(float) value}</td>
 *     <td valign="top">
 *       The property's value. This can be any signed or unsigned floating
 *       point number.
 *     </td>
 *     <td valign="top">Yes</td>
 *   </tr>
 * </table>
 * </p>
 * <h3>Examples</h3>
 * <blockquote>
 *   <pre>
 * &lt;array name=&quot;MuCommanderExample&quot;&gt;
 *   &lt;real value=&quot;4.2&quot;/&gt;
 * &lt;/array&gt;
 *   </pre>
 * </blockquote>
 * <p>
 * creates an array containing a real property of value 4.2.
 * This will generate the following entry in <code>Info.plist</code>:
 * </p>
 * <blockquote>
 *   <pre>
 * &lt;key&gt;MuCommanderExample&lt;/key&gt;
 * &lt;array&gt;
 *   &lt;real&gt;4.2&lt;/real&gt;
 * &lt;/array&gt;
 *   </pre>
 * </blockquote>
 * @author Nicolas Rinaudo
 * @see    com.mucommander.ant.macosx.RealKey
 */
class RealValue implements InfoElement {
    // - Fields ----------------------------------------------------------
    // -------------------------------------------------------------------
    /** Label of the 'real' XML element. */
    private static final String ELEMENT_REAL = "real";
    /** Value of the property. */
    private Float value;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Creates a new real property.
     */
    public RealValue() {}

    /**
     * Creates a new real property with the specified value.
     * @param f value of the property.
     */
    public RealValue(float f) {setValue(f);}



    // - Ant interaction -------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Sets the value of the real property.
     * @param f value of the property.
     */
    public void setValue(float f) {value = new Float(f);}



    // - XML output ------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Writes the <code>Info.plist</code> representation of this real value.
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
        // Makes sure the property has been initialised.
        if(value == null)
            throw new BuildException("Uninitialised real property.");

        try {
            // Writes its XML representation.
            out.startElement(ELEMENT_REAL);
            out.writeCData(value.toString());
            out.endElement(ELEMENT_REAL);
        }
        catch(IOException e) {throw new BuildException(e);}
    }
}
