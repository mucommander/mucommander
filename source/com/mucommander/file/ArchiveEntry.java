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
 * This abstract class encapsulates 3rd-party archive entries.
 *
 * @author Maxence Bernard 
 */
public abstract class ArchiveEntry {
	
    /** Underlying entry object */
    protected Object entry;

    /** Depth of this entry based on the number of '/' character occurrences */
    private int depth = -1;
	
	
    /**
     * Creates a new ArchiveEntry that encapsulates the given 3rd party entry.
     */
    public ArchiveEntry(Object entry) {
        this.entry = entry;
    }
		
    /**
     * Returns the encapsulated entry.
     */
    public Object getEntry() {
        return entry;
    }


    /**
     * Returns the depth of this entry based on the number of slash character ('/') occurrences its path contains.
     * Minimum depth is 0.
     */
    public int getDepth() {
        // Depth is only calculated once as it never changes (this class is immutable)
        if(depth == -1)
            depth = getDepth(getPath());

        return depth;	
    }


    /**
     * Returns the depth of the specified entry path, based on the number of slash character ('/') occurrences
     * the path contains. Minimum depth is 0.
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
     * Returns the encapsulated entry's path.
     */
    public abstract String getPath();
	
    /**
     * Returns the encapsulated entry's date.
     */
    public abstract long getDate();

    /**
     * Returns the encapsulated entry's size.
     */
    public abstract long getSize();

    /**
     * Returns <code>true</code> if the encapsulated entry is a directory.
     */
    public abstract boolean isDirectory();

    /**
     * Returns read/write/execute permissions for owner/group/other access as an int, UNIX permissions style.
     */
    public abstract int getPermissions();

    /**
     * Returns a bit mask for the support permission bits.
     */
    public abstract int getPermissionsMask();
}
