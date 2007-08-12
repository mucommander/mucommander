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


package com.mucommander.file.impl.tar;

import com.mucommander.file.ArchiveEntry;


/**
 * TarEntry encapsulates a <code>org.apache.tools.tar.TarEntry</code> Tar entry.
 *
 * @author Maxence Bernard
 */
public class TarEntry extends ArchiveEntry {
	
    private org.apache.tools.tar.TarEntry tarEntry;
	
    public TarEntry(org.apache.tools.tar.TarEntry tarEntry) {
        super(tarEntry);
        this.tarEntry = tarEntry;
    }


    /////////////////////////////////
    // ArchiveEntry implementation //
    /////////////////////////////////
		
    public String getPath() {
        return tarEntry.getName();
    }
	
    public long getDate() {
        return tarEntry.getModTime().getTime();
    }
	
    public long getSize() {
        return tarEntry.getSize();
    }

    public boolean isDirectory() {
        return tarEntry.isDirectory();
    }

    public int getPermissions() {
        return tarEntry.getMode();
    }

    public int getPermissionsMask() {
        return 511;     // Full UNIX permissions (777 octal)
    }
}
