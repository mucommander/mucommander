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

package com.mucommander.res;

import com.mucommander.xml.XmlAttributes;
import com.mucommander.xml.XmlWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class used to write resource list files.
 * @author Nicolas Rinaudo
 */
public class ResourceListWriter implements XmlConstants {
    // - Instance fields -------------------------------------------------
    // -------------------------------------------------------------------
    /** Where to write the content of the resource list to. */
    private XmlWriter out;



    // - Initialisation --------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Opens a new resource list writer on the specified output stream.
     * @param  out         where to write the resource list.
     * @throws IOException if an I/O error occurs.
     */
    public ResourceListWriter(OutputStream out) throws IOException {this.out = new XmlWriter(out);}

    /**
     * Opens a new resource list writer on the specified file.
     * @param  file        where to write the resource list.
     * @throws IOException if an I/O error occurs.
     */
    public ResourceListWriter(File file) throws IOException {out = new XmlWriter(file);}



    // - XML output ------------------------------------------------------
    // -------------------------------------------------------------------
    /**
     * Starts the list.
     * @throws IOException if an I/O error occurs.
     */
    public void startList() throws IOException {
        out.startElement(ROOT_ELEMENT);
        out.println();
    }

    /**
     * Adds the specified path to the list.
     * @param  path        path to add to the list.
     * @throws IOException if an I/O error occurs.
     */
    public void addFile(String path) throws IOException {
        XmlAttributes attributes;

        attributes = new XmlAttributes();
        attributes.add(PATH_ATTRIBUTE, path);
        out.writeStandAloneElement(FILE_ELEMENT, attributes);
    }

    /**
     * Ends the list.
     * @throws IOException if an I/O error occurs.
     */
    public void endList() throws IOException {out.endElement(ROOT_ELEMENT);}

    /**
     * Closes the stream used to write the list.
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {out.close();}
}
