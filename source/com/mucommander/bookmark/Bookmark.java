
package com.mucommander.bookmark;

import com.mucommander.file.FileURL;


/**
 * This class represents a bookmark, which is a simple name/url pair.
 *
 * @author Maxence Bernard
 */
public class Bookmark {

    private String name;
    private FileURL fileURL;
	

    /**
     * Creates a new Bookmark.
     *
     * @param name Name given to this bookmark
     * @param fileURL URL of the file this bookmark points to.
     */
    public Bookmark(String name, FileURL fileURL) {
        this.name = name;
        this.fileURL = fileURL;
    }


    /**
     * Returns this bookmark's name.
     */
    public String getName() {
        return name;
    }


//    /**
//     * Returns this bookmark's name, appended with the protocol if this bookmark's URL doesn't refer to a local file.
//     */
//    public String getNameWithProtocol() {
//        String displayableName = getName();
//
//        String protocol = fileURL.getProtocol();
//        if(!protocol.equals("file"))
//            displayableName += " ["+protocol.toUpperCase()+"]";
//
//        return displayableName;
//    }


    /**
     * Changes this bookmark's name to the given one.
     */
    public void setName(String newName) {
        this.name = newName;

        // Notify registered listeners of the change
        BookmarkManager.fireBookmarkChanged(this);
    }


    /**
     * Returns this bookmark's file URL.
     */
    public FileURL getURL() {
        return fileURL;
    }

    /**
     * Changes this bookmark's URL to the given one.
     */
    public void setURL(FileURL newFileURL) {
        this.fileURL = newFileURL;

        // Notify registered listeners of the change
        BookmarkManager.fireBookmarkChanged(this);
    }


    /**
     * Returns the bookmark's name.
     */
    public String toString() {
        return name;
    }
}
