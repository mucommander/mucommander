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

package com.mucommander.bookmark;

/**
 * Defines bookmarks XML file structure.
 * @author Nicolas Rinaudo
 */
interface BookmarkConstants {

    /** Root element */
    static final String ELEMENT_ROOT     = "bookmarks";

    /** Element containing the last muCommander version that was used to create the file */
    static final String ELEMENT_VERSION  = "version";

    /** Element describing one of the bookmarks in the list */
    static final String ELEMENT_BOOKMARK = "bookmark";

    /** Bookmark name */
    static final String ELEMENT_NAME     = "name";

    /** Bookmark location */
    static final String ELEMENT_LOCATION      = "location";

    /** Bookmark URL: was used up until 0.8 beta3 nightly builds and replaced by 'location' element. Kept
     * for upward compatibility */
    static final String ELEMENT_URL      = "url";
}
