/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.bookmark.file;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkBuilder;
import com.mucommander.bookmark.BookmarkManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Class used to provide an output stream on a bookmark.
 * @author Nicolas Rinaudo
 */
class BookmarkOutputStream extends ByteArrayOutputStream implements BookmarkBuilder {
    // - Stream methods --------------------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Parses the content that has been written.
     * @throws IOException if an error occurs.
     */
    public void close() throws IOException {
        super.close();

        try {BookmarkManager.readBookmarks(new ByteArrayInputStream(toByteArray()), this);}
        catch(IOException e) {throw e;}
        catch(Exception e) {throw new IOException(e);}
    }



    // - Bookmark builder methods ----------------------------------------------
    // -------------------------------------------------------------------------
    /**
     * Ignored.
     */
    public void startBookmarks() {}

    /**
     * Ignored.
     */
    public void endBookmarks() {}

    /**
     * Adds the specified bookmark to the bookmark manager
     * <p>
     * Note that this method will remove any previous bookmark of the same name.
     * </p>
     * @param name     name of the new bookmark.
     * @Param location location of the new bookmark.
     */
    public void addBookmark(String name, String location) {
        Bookmark oldBookmark; // Old bookmark of the same name, if any.
        Bookmark newBookmark; // Bookmark to create.

        // Creates the new bookmark and checks for conflicts.
        newBookmark = new Bookmark(name, location);
        if((oldBookmark = BookmarkManager.getBookmark(name)) != null)
            BookmarkManager.removeBookmark(oldBookmark);

        // Adds the new bookmark.
        BookmarkManager.addBookmark(newBookmark);
    }
}
