
package com.mucommander.bookmark;

import com.mucommander.file.FileURL;

import java.net.MalformedURLException;


/**
 * This class represents a bookmark, which is a simple name/location pair:
 * <ul>
 * <li>The name is just a String describing the bookmark
 * <li>The location should designate a path or file URL. The designated location may not exist or may not even be
 * a valid path or URL, so it is up to classes that call {@link #getLocation()} to deal with it appropriately.
 * </ul>
 * 
 * @author Maxence Bernard
 */
public class Bookmark implements Cloneable {

    private String name;
//    private FileURL fileURL;
	private String location;


//    /**
//     * Creates a new Bookmark.
//     *
//     * @param name Name given to this bookmark
//     * @param fileURL URL of the file this bookmark points to.
//     */
//    public Bookmark(String name, FileURL fileURL) {
//        this.name = name;
//        this.fileURL = fileURL;
//    }

    /**
     * Creates a new Bookmark using the given name and location.
     *
     * @param name name given to this bookmark
     * @param location location (path or URL) this bookmark points to
     */
    public Bookmark(String name, String location) {
        this.name = name;
        this.location = location;
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
     * Changes this bookmark's name to the given one and fires an event to registered {@link BookmarkListener}
     * instances.
     */
    public void setName(String newName) {
        this.name = newName;

        // Notify registered listeners of the change
        BookmarkManager.fireBookmarksChanged();
    }


    /**
     * Returns this bookmark's location which should normally designate a path or file URL, but which isn't
     * necessarily valid nor exists.
     */
    public String getLocation() {
        return location;
    }


    /**
     * Tries to create a {@link FileURL} out of this bookmark's location and returns it.
     *
     * <b>Important: </b> The returned FileURL may very well be <code>null</code> if the location cannot be resolved
     * as a FileURL. That is the case if the location does not designate a URL but a local path.
     *
     * @return this bookmark's location as a FileURL, <code>null</code> if the location is not a valid URL
     */
    public FileURL getLocationAsURL() {
        try {
            return new FileURL(location);
        }
        catch(MalformedURLException e) {
            return null;
        }
    }


    /**
     * Changes this bookmark's location to the given one and fires an event to registered {@link BookmarkListener}
     * instances.
     */
    public void setLocation(String location) {
        this.location = location;

        // Notify registered listeners of the change
        BookmarkManager.fireBookmarksChanged();
    }

//    /**
//     * Returns this bookmark's file URL.
//     */
//    public FileURL getURL() {
//        return fileURL;
//    }
//
//    /**
//     * Changes this bookmark's URL to the given one.
//     */
//    public void setURL(FileURL newFileURL) {
//        this.fileURL = newFileURL;
//
//        // Notify registered listeners of the change
//        BookmarkManager.fireBookmarkChanged(this);
//    }

    /**
     * Returns a clone of this bookmark.
     */
    public Object clone() throws CloneNotSupportedException {
        return new Bookmark(new String(name), new String(location));
    }

//    /**
//     * Returns a clone of this bookmark.
//     */
//    public Object clone() throws CloneNotSupportedException {
//        return new Bookmark(new String(name), (FileURL)fileURL.clone());
//    }

    /**
     * Returns the bookmark's name.
     */
    public String toString() {
        return name;
    }
}
