/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.file.connection;

import com.mucommander.file.FileURL;

/**
 * This interface should be implemented by classes that are able to create ConnectionHandler instances for a given
 * server location, typically {@link com.mucommander.file.AbstractFile} implementations.
 *
 * <p>This interface allows to take advantage of {@link ConnectionPool} to share connections across
 * {@link com.mucommander.file.AbstractFile} instances.  
 *
 * @author Maxence Bernard
 */
public interface ConnectionHandlerFactory {

    /**
     * Creates and returns a {@link ConnectionHandler} instance for the given location.
     */
    public ConnectionHandler createConnectionHandler(FileURL location);
}
