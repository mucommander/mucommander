/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

import java.io.InputStream;
import java.io.IOException;

/**
 * Interface for reading from a configuration source using callbacks.
 * <p>
 * Application writers that need to implement a specific configuration format need to subclass this.
 * Reader implementations have the task of parsing an input stream for configuration data and invoking the
 * relevant callback methods of {@link ConfigurationBuilder}.
 * </p>
 * <p>
 * The <code>com.mucommander.conf</code> package comes with a default implementation, {@link XmlConfigurationReader},
 * which handles the standard muCommander configuration file format.
 * </p>
 * <p>
 * In order for an implementation of <code>ConfigurationReader</code> to be useable by {@link Configuration configuration}
 * instances, it must come with an associated implementation of {@link ConfigurationReaderFactory}.
 * </p>
 * <p>
 * In addition, most readers will have an associated {@link ConfigurationWriter} used to write configuration files in a format
 * that the reader will understand.
 * </p>
 * @author Nicolas Rinaudo
 * @see    ConfigurationReaderFactory
 * @see    ConfigurationWriter
 */
public interface ConfigurationReader {
    /**
     * Reads configuration information from the specified input stream and invokes the specified builder's callback methods.
     * <p>
     * When applicable, this method is expected to throw {@link ConfigurationFormatException format} exceptions rather than
     * {@link ConfigurationException configuration} exceptions. This will allow applications to report errors in a way that is
     * useful for users.
     * </p>
     * @param in                      where to read the configuration information from.
     * @param builder                 where to send configuration messages to.
     * @throws IOException            if an I/O error occurs.
     * @throws ConfigurationException if another type of error occurs, in which case that error must be returned by <code>ConfigurationException.getCause()</code>.
     */
    public void read(InputStream in, ConfigurationBuilder builder) throws ConfigurationException, IOException;
}
