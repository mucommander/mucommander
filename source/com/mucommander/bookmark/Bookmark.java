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
 * This class represents a bookmark, which is a simple name/location pair:
 * <ul>
 * <li>The name is a String describing the bookmark.</li>
 * <li>The location should designate a path or file URL. The designated location may not exist or may not even be
 * a valid path or URL, so it is up to classes that call {@link #getLocation()} to deal with it appropriately.</li>
 * </ul>
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
        return new Bookmark(name, location);
    }


    /**
     * Returns the bookmark's name.
     */
    public String toString() {
        return name;
    }

    public boolean equals(Object object) {
        if(!(object instanceof Bookmark))
            return false;

        Bookmark bookmark;
        bookmark = (Bookmark)object;
        return bookmark.getName().equals(name);
    }
}
