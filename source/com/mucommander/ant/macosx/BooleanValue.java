/*
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

package com.mucommander.ant.macosx;

import com.mucommander.xml.XmlWriter;
import org.apache.tools.ant.BuildException;

import java.io.IOException;

/**
 *
 */
class BooleanValue implements InfoElement {
    public static final String ELEMENT_TRUE      = "true";
    public static final String ELEMENT_FALSE     = "false";
    private boolean value;

    public BooleanValue() {}
    public BooleanValue(boolean b) {setValue(b);}
    public void setValue(boolean b) {value = b;}

    public void write(XmlWriter out) throws BuildException {
        try {
            if(value)
                out.writeStandAloneElement(ELEMENT_TRUE);
            else
                out.writeStandAloneElement(ELEMENT_FALSE);
        }
        catch(IOException e) {throw new BuildException(e);}
    }
}
