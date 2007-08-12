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
 * <code>SimpleArchiveEntry</code> is a self-contained {@link ArchiveEntry} implementation, where all the information
 * about the entry is passed to the constructor.
 *
 * @author Maxence Bernard
 */
public class SimpleArchiveEntry extends ArchiveEntry {

    private String path;
    private long date;
    private long size;
    private boolean isDirectory;

    /**
     * Creates a new SimpleArchiveEntry using the supplied path and isDirectory attributes, with a date set to now and
     * a size of 0.
     *
     * @param path the entry's path
     * @param isDirectory true if the entry is a directory
     */
    public SimpleArchiveEntry(String path, boolean isDirectory) {
        this(path, System.currentTimeMillis(), 0, isDirectory);
    }

    /**
     * Creates a new SimpleArchiveEntry using the supplied attributes.
     *
     * @param path the entry's path
     * @param date the entry's date
     * @param size the entry's size
     * @param isDirectory true if the entry is a directory
     */
    public SimpleArchiveEntry(String path, long date, long size, boolean isDirectory) {
        this.path = path;
        this.date = date;
        this.size = size;
        this.isDirectory = isDirectory;
    }

    
    /////////////////////////////////
    // ArchiveEntry implementation //
    /////////////////////////////////
		
    public String getPath() {
        return path;
    }
	
    public long getDate() {
        return date;
    }
	
    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public int getPermissions() {
        return 292;     // r--r--r--
    }

    public int getPermissionsMask() {
        return 0;       // permissions should not be taken into acount
    }
}
