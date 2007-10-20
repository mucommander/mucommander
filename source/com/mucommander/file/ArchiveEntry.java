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


package com.mucommander.file;

/**
 * This class represents a generic archive entry. It provides getters and setters for common archive entry attributes
 * and allows to encapsulate the entry object of a 3rd party library.
 *
 * @author Maxence Bernard 
 */
public class ArchiveEntry {

    /** This entry's path */
    protected String path;

    /** This entry's date */
    protected long date;

    /** This entry's size */
    protected long size;

    /** True if this entry is a directory */
    protected boolean isDirectory;

    /** This entry's permissions */
    protected int permissions = 292;        // r--r--r--

    /** This entry's permission mask */
    protected int permissionMask = 0;       // permissions should not be taken into acount

    /** Encapsulated entry object */
    protected Object entryObject;


    /**
     * Creates a new ArchiveEntry with all attributes set to their default value. 
     */
    protected ArchiveEntry() {
    }

    /**
     * Creates a new ArchiveEntry using the supplied path and isDirectory attributes, with a date set to now and
     * a size of 0.
     *
     * @param path the entry's path
     * @param isDirectory true if the entry is a directory
     */
    public ArchiveEntry(String path, boolean isDirectory) {
        this(path, isDirectory, System.currentTimeMillis(), 0);
    }

    /**
     * Creates a new ArchiveEntry using the values of the supplied attributes.
     *
     * @param path the entry's path
     * @param isDirectory true if the entry is a directory
     * @param date the entry's date
     * @param size the entry's size
     */
    public ArchiveEntry(String path, boolean isDirectory, long date, long size) {
        this.path = path;
        this.date = date;
        this.size = size;
        this.isDirectory = isDirectory;
    }


    /**
     * Returns the depth of this entry based on the number of path delimiters ('/') its path contains.
     * Top-level entries have a depth of 0 (minimum depth).
     *
     * @return the depth of this entry
     */
    public int getDepth() {
        return getDepth(getPath());
    }

    /**
     * Returns the depth of the specified entry path, based on the number of path delimiters ('/') it contains.
     * Top-level entries have a depth of 0 (minimum depth).
     *
     * @param entryPath the path for which to calculate the depth
     * @return the depth of the given entry path
     */
    public static int getDepth(String entryPath) {
        int depth = 0;
        int pos=0;

        while ((pos=entryPath.indexOf('/', pos+1))!=-1)
            depth++;

        // Directories in archives end with a '/'
        if(entryPath.charAt(entryPath.length()-1)=='/')
            depth--;

        return depth;
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
     * Returns the path to this entry. The returned path uses the '/' character as the delimiter, and is relative to the
     * archive's root, i.e. does not start with a leading '/'.
     *
     * @return this entry's path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path to this entry. The specified path must use the '/' character as the delimiter, and be relative
     * to the archive's root, i.e. must not start with a leading '/'.
     *
     * @param path this entry's path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns the date of this entry, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     *
     * @return the date of this entry, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     */
    public long getDate() {
        return date;
    }

    /**
     * Sets the date of this entry, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     *
     * @param date the date of this entry, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     */
    public void setDate(long date) {
        this.date = date;
    }

    /**
     * Returns the size of this entry, in bytes.
     *
     * @return the size of this entry, in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the size of this entry, in bytes.
     *
     * @param size the size of this entry, in bytes
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Returns <code>true</code> if the entry is a directory.
     *
     * @return true if the entry is a directory
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Specifies whether this entry is a directory or a regular file.
     *
     * @param directory true for directory, false for regular file
     */
    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    /**
     * Returns read/write/execute permissions for owner/group/other access, in a UNIX-style permission int.
     * The default decimal value is <code>292</code>: <code>r--r--r--</code>.
     *
     * @return read/write/execute permissions for owner/group/other access
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * Sets the read/write/execute permissions for owner/group/other access, in a UNIX-style permission int.
     *
     * @param permissions read/write/execute permissions for owner/group/other access
     */
    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    /**
     * Returns a bit mask specifying which permission bits are supported.
     * The default value is <code>0</code>: permissions should not be taken into acount.
     *
     * @return a bit mask specifying which permission bits are supported
     */
    public int getPermissionsMask() {
        return permissionMask;
    }

    /**
     * Sets the bit mask specifying which permission bits are supported.
     *
     * @param permissionMask a bit mask specifying which permission bits are supported
     */
    public void setPermissionMask(int permissionMask) {
        this.permissionMask = permissionMask;
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
}
