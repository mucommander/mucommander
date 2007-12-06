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

package com.mucommander.bookmark.file.bookmark;

import com.mucommander.bookmark.Bookmark;
import com.mucommander.bookmark.BookmarkManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileURL;
import com.mucommander.file.ProtocolProvider;

import java.io.IOException;

/**
 * Used to create instances of {@link com.mucommander.file.AbstractFile} on the <code>bookmarks://</code> file system.
 * @author Nicolas Rinaudo
 */
public class BookmarkProtocolProvider implements ProtocolProvider {
    public AbstractFile getFile(FileURL url) throws IOException {
        // If the URL contains a path but no host, it's illegal.
        // If it contains neither host nor path, we're browsing bookmarks://
        if(url.getHost() == null) {
            if(url.getPath().equals("/"))
                return new BookmarkRoot(url);
            throw new IOException();
        }

        // If the URL contains a host, look it up in the bookmark list and use that
        // as the root of the returned path.
        else {
            Bookmark bookmark;
            // If the bookmark doesn't exist, but a path is specified, throws an exception.
            // Otherwise, returns the requested bookmark.
            if((bookmark = BookmarkManager.getBookmark(url.getHost())) == null) {
                if(!url.getPath().equals("/"))
                    throw new IOException();
                return new BookmarkFile(new Bookmark(url.getHost(), url.getPath()));
            }

            // If the bookmark exists, and a path is specified, creates a new path
            // from the bookmark's location and the specified path.
            if(!url.getPath().equals("/"))
                return FileFactory.getFile(bookmark.getLocation() + url.getPath());

            // Otherwise, creates a new bookmark file.
            return new BookmarkFile(bookmark);
        }
    }
}
