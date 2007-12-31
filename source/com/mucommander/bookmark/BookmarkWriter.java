/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.bookmark;

import com.mucommander.xml.XmlWriter;

import java.io.IOException;
import java.io.OutputStream;


/**
 * This class provides a method to write bookmarks to an XML file.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
class BookmarkWriter implements BookmarkConstants, BookmarkBuilder {
    private XmlWriter out;

    public BookmarkWriter(OutputStream stream) throws IOException {out = new XmlWriter(stream);}

    public void startBookmarks() throws BookmarkException {
        // Root element
        try {
            out.startElement(ELEMENT_ROOT);
            out.println();

            // Add muCommander version
            out.startElement(ELEMENT_VERSION);
            out.writeCData(com.mucommander.RuntimeConstants.VERSION);
            out.endElement(ELEMENT_VERSION);
        }
        catch(IOException e) {throw new BookmarkException(e);}
    }

    public void endBookmarks() throws BookmarkException {
        try {out.endElement(ELEMENT_ROOT);}
        catch(IOException e) {throw new BookmarkException(e);}
    }

    public void addBookmark(String name, String location) throws BookmarkException {
        try {
            // Start bookmark element
            out.startElement(ELEMENT_BOOKMARK);
            out.println();

            // Write the bookmark's name
            out.startElement(ELEMENT_NAME);
            out.writeCData(name);
            out.endElement(ELEMENT_NAME);

            // Write the bookmark's location
            out.startElement(ELEMENT_LOCATION);
            out.writeCData(location);
            out.endElement(ELEMENT_LOCATION);

            // End bookmark element
            out.endElement(ELEMENT_BOOKMARK);
        }
        catch(IOException e) {throw new BookmarkException(e);}
    }
}
