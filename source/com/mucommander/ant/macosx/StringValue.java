/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.ant.macosx;

import com.mucommander.xml.writer.XmlWriter;
import org.apache.tools.ant.BuildException;

/**
 *
 */
class StringValue implements InfoElement {
    private static final String ELEMENT_STRING    = "string";
    private String value;

    public StringValue() {}
    public StringValue(String s) {setValue(s);}

    public void setValue(String s) {value = s;}

    public void write(XmlWriter out) throws BuildException {
        if(value == null)
            throw new BuildException("Uninitialised string key.");
        out.startElement(ELEMENT_STRING);
        out.writeCData(value);
        out.endElement(ELEMENT_STRING);
    }
}
