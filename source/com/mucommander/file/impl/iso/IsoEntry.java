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

package com.mucommander.file.impl.iso;

import com.mucommander.file.ArchiveEntry;

/**
 * IsoEntry encapsulates an ISO entry.
 *
 * @author Xavier Martin
 */
public class IsoEntry extends ArchiveEntry {

    private String path;
    private long date;
    private int size;
    private boolean isDirectory;

    private long extent;

    public IsoEntry(String path, long date, int size, boolean isDirectory, long extent) {
        super(null);
        this.path = path;
        this.date = date;
        this.size = size;
        this.isDirectory = isDirectory;
        this.extent = extent;
    }

    public long getExtent() {
        return this.extent;
    }

    /////////////////////////////////////
    // Abstract methods implementation //
    /////////////////////////////////////

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