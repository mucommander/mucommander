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

/**
 * Implementations of this interface are used to build bookmark sets.
 * @author Nicolas Rinaudo
 */
public interface BookmarkBuilder {
    /**
     * Notifies the builder that the bookmark list is starting.
     * @throws BookmarkException if an error occurs.
     */
    public void startBookmarks() throws BookmarkException;

    /**
     * Notifies the builder of a new bookmark in the list.
     * @param  name              bookmark's name.
     * @param  location          bookmark's location.
     * @throws BookmarkException if an error occurs.
     */
    public void addBookmark(String name, String location) throws BookmarkException;

    /**
     * Notifies the builder that the bookmark list is finished.
     * @throws BookmarkException if an error occurs.
     */
    public void endBookmarks() throws BookmarkException;
}
