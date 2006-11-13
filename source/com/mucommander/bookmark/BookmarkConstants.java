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
