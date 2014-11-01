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

import java.io.IOException;

/**
 * This interface allows {@link FileFactory} to instantiate {@link AbstractFile} implementations.
 * <p>
 * For {@link AbstractFile} implementations to be automatically instantiated by {@link FileFactory}, this interface
 * needs to be implemented and an instance registered with {@link FileFactory} and binded to a protocol identifier.
 * </p>
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see FileFactory
 * @see AbstractFile
 */
public interface ProtocolProvider {
    /**
     * Creates a new instance of <code>AbstractFile</code> that matches the specified URL.
     * @param url URL to map as an <code>AbstractFile</code>.
     * @param instantiationParams file implementation-specific parameters used for instantiating the
     * {@link AbstractFile} implementation. Those parameters are used when creating file instances within
     * the AbstractFile implementation.
     * @return a new instance of <code>AbstractFile</code> that matches the specified URL.
     * @throws IOException if an error occurs.
     */
    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException;
}
