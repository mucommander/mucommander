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
 * This abstract class represents a generic archive entry. It provides getters for common archive entry attributes
 * (path, date, size, isDirectory, permissions) and allows to encapsulate the entry object of a 3rd party library.
 *
 * @author Maxence Bernard 
 */
public abstract class ArchiveEntry {
	
    /** Encapsulated entry object */
    protected Object entryObject;

    /** Depth of this entry based on the number of '/' character occurrences */
    private int depth = -1;
	
	
    /**
     * Creates a new self-contained ArchiveEntry.
     */
    protected ArchiveEntry() {
    }

    /**
     * Creates a new ArchiveEntry that encapsulates the given object providing the actual entry information.
     *
     * @param entryObject the object providing the actual entry information, <code>null</code> if this entry is self-contained
     */
    protected ArchiveEntry(Object entryObject) {
        this.entryObject = entryObject;
    }
		
    /**
     * Returns the encapsulated object providing the actual entry information (typically an object from a 3rd party
     * library), <code>null</code> if this entry is self-contained.
     *
     * @return the encapsulated object providing the actual entry information, null if this entry is self-contained
     */
    public Object getEntryObject() {
        return entryObject;
    }


    /**
     * Returns the depth of this entry based on the number of path delimiters ('/') its path contains.
     * Top-level entries have a depth of 0 (minimum depth).
     *
     * @return the depth of this entry
     */
    public int getDepth() {
        // Depth is only calculated once as it never changes (this class is immutable)
        if(depth == -1)
            depth = getDepth(getPath());

        return depth;	
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


    //////////////////////
    // Abstract methods //
    //////////////////////
		
    /**
     * Returns the entry's path. The returned path uses the '/' character as the delimiter, and is relative to the
     * archive's root, i.e. does not start with a leading '/'.
     *
     * @return the entry's path
     */
    public abstract String getPath();
	
    /**
     * Returns the entry's date.
     *
     * @return the entry's date
     */
    public abstract long getDate();

    /**
     * Returns the entry's size.
     *
     * @return the entry's size
     */
    public abstract long getSize();

    /**
     * Returns <code>true</code> if the entry is a directory.
     *
     * @return true if the entry is a directory
     */
    public abstract boolean isDirectory();

    /**
     * Returns read/write/execute permissions for owner/group/other access, in a UNIX permissions style int.
     *
     * @return read/write/execute permissions for owner/group/other access
     */
    public abstract int getPermissions();

    /**
     * Returns a bit mask specifying which permission bits are supported.
     *
     * @return a bit mask specifying which permission bits are supported
     */
    public abstract int getPermissionsMask();
}
