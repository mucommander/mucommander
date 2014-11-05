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

/**
 * Super class of all file protocol implementations (by opposition to {@link AbstractArchiveFile archive file} 
 * implementations).
 *
 * @see ProtocolProvider
 * @author Maxence Bernard
 */
public abstract class ProtocolFile extends AbstractFile {

    protected ProtocolFile(FileURL url) {
        super(url);
    }
    

    /////////////////////////////////////////
    // Partial AbstractFile implementation //
    /////////////////////////////////////////

    /**
     * This implementation always returns <code>false</code>.
     *
     * @return <code>false</code>, always
     */
    @Override
    public boolean isArchive() {
        return false;
    }
}
