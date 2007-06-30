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


package com.mucommander.file.impl.zip;

import com.mucommander.file.ArchiveEntry;


/**
 * ZipEntry encapsulates a <code>java.util.zip</code> Zip entry.
 *
 * @author Maxence Bernard
 */
public class ZipEntry extends ArchiveEntry {
	
    private java.util.zip.ZipEntry zipEntry;
	
    public ZipEntry(java.util.zip.ZipEntry zipEntry) {
        super(zipEntry);
        this.zipEntry = zipEntry;
    }
	
    /////////////////////////////////////
    // Abstract methods implementation //
    /////////////////////////////////////
		
    public String getPath() {
        return zipEntry.getName();
    }
	
    public long getDate() {
        return zipEntry.getTime();
    }
	
    public long getSize() {
        return zipEntry.getSize();
    }

    public boolean isDirectory() {
        return zipEntry.isDirectory();
    }

    public int getPermissions() {
        return 292;     // r--r--r--
    }

    public int getPermissionsMask() {
        return 0;       // permissions should not be taken into acount
    }
}
