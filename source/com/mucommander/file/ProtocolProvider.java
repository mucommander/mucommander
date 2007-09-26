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

import java.io.IOException;

/**
 * Interface used to provide {@link FileFactory} with a way of creating instances of {@link AbstractFile} for a given protocol.
 * <p>
 * Implementation of {@link AbstractFile} that implement a file protocol must create
 * an associated provider and register it to {@link FileFactory} in order to be recognised by the system.
 * </p>
 * @author Nicolas Rinaudo
 * @see    FileFactory
 */
public interface ProtocolProvider {
    /**
     * Creates a new instance of <code>AbstractFile</code> that matches the specified URL.
     * @param  url         URL to map as an <code>AbstractFile</code>.
     * @return             a new instance of <code>AbstractFile</code> that matches the specified URL.
     * @throws IOException if an error occurs.
     */
    public AbstractFile getFile(FileURL url) throws IOException;
}
