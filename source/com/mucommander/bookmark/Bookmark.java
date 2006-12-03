
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
	private String location;


    /**
     * Creates a new Bookmark using the given name and location.
     *
     * @param name name given to this bookmark
     * @param location location (path or URL) this bookmark points to
     */
    public Bookmark(String name, String location) {
        // Use setters to checks for null values
        setName(name);
        setLocation(location);
    }


    /**
     * Returns this bookmark's name.
     */
    public String getName() {
        return name;
    }


    /**
     * Changes this bookmark's name to the given one and fires an event to registered {@link BookmarkListener}
     * instances.
     */
    public void setName(String newName) {
        // Replace null values by empty strings
        if(newName==null)
            newName = "";

        if(!newName.equals(this.name)) {
            this.name = newName;

            // Notify registered listeners of the change
            BookmarkManager.fireBookmarksChanged();
        }
    }


    /**
     * Returns this bookmark's location which should normally designate a path or file URL, but which isn't
     * necessarily valid nor exists.
     */
    public String getLocation() {
        return location;
    }


    /**
     * Changes this bookmark's location to the given one and fires an event to registered {@link BookmarkListener}
     * instances.
     */
    public void setLocation(String newLocation) {
        // Replace null values by empty strings
        if(newLocation==null)
            newLocation = "";

        if(!newLocation.equals(this.location)) {
            this.location = newLocation;

            // Notify registered listeners of the change
            BookmarkManager.fireBookmarksChanged();
        }
    }


    /**
     * Returns a clone of this bookmark.
     */
    public Object clone() throws CloneNotSupportedException {
        return new Bookmark(new String(name), new String(location));
    }


    /**
     * Returns the bookmark's name.
     */
    public String toString() {
        return name;
    }
}
