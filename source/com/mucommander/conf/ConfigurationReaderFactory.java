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

/**
 * Interface used to provide instances of {@link Configuration} with a way of creating {@link ConfigurationReader reader} instances.
 * <p>
 * A <code>ConfigurationReaderFactory</code>'s sole purpose is to create instances of {@link ConfigurationReader}. In most cases, a
 * factory class will be associated with a reader class, and its code will look something like:
 * <pre>
 * public class MyReaderFactory implements ConfigurationReaderFactory {
 *    public ConfigurationReader getReaderInstance() {return new MyReader();}
 * }
 * </pre>
 * </p>
 * <p>
 * The <code>com.mucommander.conf</code> package comes with a default implementation, {@link XmlConfigurationReaderFactory},
 * which creates readers meant to handle the standard muCommander configuration file format.
 * </p>
 * @author Nicolas Rinaudo
 * @see    ConfigurationReader
 */
public interface ConfigurationReaderFactory {
    /**
     * Creates an instance of {@link ConfigurationReader}.
     * @return                              an instance of {@link ConfigurationReader}.
     * @throws ReaderConfigurationException if the factory wasn't properly configured.
     */
    public ConfigurationReader getReaderInstance() throws ReaderConfigurationException;
}
