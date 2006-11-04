package com.mucommander.bookmark;

/**
 * Defines bookmark files structure.
 * @author Nicolas Rinaudo
 */
interface BookmarkConstants {
    /** Root element of the bookmark XML file. */
    static final String ELEMENT_ROOT     = "bookmarks";
    /** Element describing the file's version. */
    static final String ELEMENT_VERSION  = "version";
    /** Element describing one of the bookmarks in the list. */
    static final String ELEMENT_BOOKMARK = "bookmark";
    /** Bookmark name. */
    static final String ELEMENT_NAME     = "name";
    /** Bookmark URL. */
    static final String ELEMENT_URL      = "url";
    /** Bookmark password */
    static final String ELEMENT_PASSWORD = "password";

    /** Root element's attribute containing the encryption method used for passwords */
    static final String ATTRIBUTE_ENCRYPTION = "encryption";

    /** Weak password encryption method */
    public static final String WEAK_ENCRYPTION_METHOD = "weak";
}
