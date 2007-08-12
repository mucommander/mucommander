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

import java.io.OutputStream;
import java.io.IOException;

/**
 * Interface for writing to a configuration source using callbacks.
 * <p>
 * <code>ConfigurationWriter</code> is a specialised version of
 * {@link ConfigurationBuilder}.<br/>
 * The <code>com.mucommander.conf</code> packages comes with a default implementation, {@link XmlConfigurationWriter},
 * which handles the standard muCommander configuration file format.
 * </p>
 * <p>
 * In order for an implementation of <code>ConfigurationWriter</code> to be useable by the {@link ConfigurationManager},
 * it must come with an associated implementation of {@link ConfigurationWriterFactory}.<br/>
 * </p>
 * <p>
 * In addition, most writers will have an associated {@link ConfigurationReader}, the later being used to read configuration
 * data written by the former.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface ConfigurationWriter extends ConfigurationBuilder {
    /**
     * Sets the output stream in which the writer should write its data.
     * <p>
     * <code>ConfigurationWriter</code> implementations can rely on this method
     * always being called before configuration building has started.
     * </p>
     * @param out output stream in which to write the configuration data.
     */
    public void setOutputStream(OutputStream out);
}
