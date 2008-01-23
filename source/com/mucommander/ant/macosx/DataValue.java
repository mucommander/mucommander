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

class DataValue implements InfoElement {
    private static final String DATA_ELEMENT = "data";

    private StringBuffer data;

    public DataValue() {data = new StringBuffer();}

    public void addText(String txt) {data.append(txt);}

    public void write(XmlWriter out) throws BuildException {
        try {
            out.startElement(DATA_ELEMENT);
            out.writeCData(data.toString());
            out.endElement(DATA_ELEMENT);
        }
        catch(IOException e) {throw new BuildException(e);}
    }
}
