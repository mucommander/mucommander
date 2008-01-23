/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.conf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides instances of {@link Configuration} with streams to configuration data.
 * <p>
 * Application writers that need to retrieve configuration data from a non-standard source
 * (over the network, from a database, ...) need to subclass this.
 * </p>
 * <p>
 * Implementations of this interface can be registered through {@link Configuration}'s
 * {@link Configuration#setSource(ConfigurationSource) setSource} method. Their purpose is
 * to provide the system with streams to a configuration source. This system allows applications
 * to retrieve their configuration information from non-standard sources, such as over the network,
 * in a database, ...
 * </p>
 * <p>
 * The <code>com.mucommander.conf</code> package comes with a default implementation, {@link FileConfigurationSource},
 * which will open input and output streams on a local file.
 * </p>
 * @author Nicolas Rinaudo
 * @see    FileConfigurationSource
 */
public interface ConfigurationSource {
    /**
     * Returns an input stream on the configuration source.
     * @return             an input stream on the configuration source.
     * @throws IOException if any I/O error occurs.
     */
    public InputStream getInputStream() throws IOException;

    /**
     * Returns an output stream on the configuration source.
     * @return             an output stream on the configuration source.
     * @throws IOException if any I/O error occurs.
     */
    public OutputStream getOutputStream() throws IOException;
}
