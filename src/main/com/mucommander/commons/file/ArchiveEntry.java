/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



package com.mucommander.commons.file;

import com.mucommander.commons.file.util.PathUtils;

/**
 * This class represents a generic archive entry. It provides getters and setters for common archive entry attributes
 * and allows to encapsulate the entry object of a 3rd party library.
 *
 * <p><b>Important</b>: the path of archive entries must use the '/' character as a path delimiter, and be relative
 * to the archive's root, i.e. must not start with a leading '/'.</p>
 *
 * @author Maxence Bernard 
 */
public class ArchiveEntry extends SimpleFileAttributes {

    /** Encapsulated entry object */
    protected Object entryObject;

    /** Caches the computed hashcode */
    private int hashCode;


    /**
     * Creates a new ArchiveEntry with all attributes set to their default value.
     */
    public ArchiveEntry() {
    }

    /**
     * Creates a new ArchiveEntry using the values of the supplied attributes.
     *
     * @param path the entry's path
     * @param directory true if the entry is a directory
     * @param date the entry's date
     * @param size the entry's size
     * @param exists <code>true</code> if the entry exists in the archive
     */
    public ArchiveEntry(String path, boolean directory, long date, long size, boolean exists) {
        setPath(path);
        setDate(date);
        setSize(size);
        setDirectory(directory);
        setExists(exists);
    }


    /**
     * Returns the depth of this entry based on the number of path delimiters ('/') its path contains.
     * Top-level entries have a depth of 1.
     *
     * @return the depth of this entry
     */
    public int getDepth() {
        return getDepth(getPath());
    }

    /**
     * Returns the depth of the specified entry path, based on the number of path delimiters ('/') it contains.
     * Top-level entries have a depth of 1.
     *
     * @param entryPath the path for which to calculate the depth
     * @return the depth of the given entry path
     */
    public static int getDepth(String entryPath) {
        return PathUtils.getDepth(entryPath, "/");
    }

    /**
     * Extracts this entry's filename from its path and returns it.
     *
     * @return this entry's filename
     */
    public String getName() {
        String path = getPath();
        int len = path.length();
        // Remove trailing '/' if any
        if(path.charAt(len-1)=='/')
            path = path.substring(0, --len);

        int lastSlash = path.lastIndexOf('/');
        return lastSlash==-1?
          path:
          path.substring(lastSlash+1, len);
    }

    /**
     * Returns an archive format-dependent object providing extra information about this entry, typically an object from
     * a 3rd party library ; <code>null</code> if this entry has none.
     *
     * @return an object providing extra information about this entry, null if this entry has none
     */
    public Object getEntryObject() {
        return entryObject;
    }

    /**
     * Sets an archive format-dependent object providing extra information about this entry, typically an object from
     * a 3rd party library ; <code>null</code> for none.
     *
     * @param entryObject an object providing extra information about this entry, null for none
     */
    public void setEntryObject(Object entryObject) {
        this.entryObject = entryObject;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns the file permissions of this entry. This method is overridden to return default permissions
     * ({@link FilePermissions#DEFAULT_DIRECTORY_PERMISSIONS} for directories, {@link FilePermissions#DEFAULT_FILE_PERMISSIONS}
     * for regular files), when none have been set.
     *
     * @return the file permissions of this entry
     */
    @Override
    public FilePermissions getPermissions() {
        FilePermissions permissions = super.getPermissions();
        if(permissions==null)
            return isDirectory()?FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS:FilePermissions.DEFAULT_FILE_PERMISSIONS;

        return permissions;
    }

    /**
     * Overriden to invalidates any previously computed hash code.
     *
     * @param path new path to set
     */
    @Override
    public void setPath(String path) {
        super.setPath(path);

        // Invalidate any previously
        hashCode = 0;
    }

    /**
     * Returns <code>true</code> if the given object is an <code>ArchiveEntry</code> whose path is equal to this one,
     * according to {@link PathUtils#pathEquals(String, String, String)} (trailing slash-insensitive comparison).
     *
     * @param o the object to test
     * @return <code>true</code> if the given object is an <code>ArchiveEntry</code> whose path is equal to this one
     * @see PathUtils#pathEquals(String, String, String)
     */
    public boolean equals(Object o) {
        if(!(o instanceof ArchiveEntry))
            return false;

        return PathUtils.pathEquals(getPath(), ((ArchiveEntry)o).getPath(), "/");
    }

    /**
     * This method is overridden to return a hash code that is consistent with {@link #equals(Object)},
     * so that <code>url1.equals(url2)</code> implies <code>url1.hashCode()==url2.hashCode()</code>.
     */
    public int hashCode() {
        if(hashCode!=0)         // Return any previously computed hashCode. Note that setPath invalidates the hashCode.
            return hashCode;

        String path = getPath();

        // #equals(Object) is trailing separator insensitive, so the hashCode must be trailing separator invariant
        hashCode = path.endsWith("/")
                ?path.substring(0, path.length()-1).hashCode()
                :path.hashCode();

        return hashCode;
    }
}
