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

package com.mucommander.commons.file.impl.lst;

import com.mucommander.commons.file.ArchiveEntry;

/**
 * An LST archive entry. In addition to the common attributes found in {@link ArchiveEntry}, it contains a base
 * folder which, when concatenated with this entry's path, gives the absolute path to the file referenced by the
 * LST entry. 
 *
 * @author Maxence Bernard
 */
public class LstArchiveEntry extends ArchiveEntry {

    /** The base folder that when concatenated to this entry's path gives the absolute path to the file referenced
     * by this entry */
    protected String baseFolder;

    public LstArchiveEntry(String path, boolean directory, long date, long size, String baseFolder) {
        super(path, directory, date, size, true);

        this.baseFolder = baseFolder;
    }

    /**
     * Returns the base folder which, when concatenated with this entry's path, gives the absolute path to the file
     * referenced by the LST entry. The returned path should always end with a trailing separator character.
     *
     * @return the base folder of this entry
     */
    protected String getBaseFolder() {
        return baseFolder;
    }
}
