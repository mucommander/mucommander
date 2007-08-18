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

/**
 * Reader factory implementation used to create instances of {@link XmlConfigurationReader}.
 * <p>
 * {@link ConfigurationReader Reader} instances created by this factory are meant to read the standard muCommander
 * configuration file format described in the documentation of {@link XmlConfigurationReader}.
 * </p>
 * @author Nicolas Rinaudo
 * @see    XmlConfigurationReader
 * @see    XmlConfigurationWriter
 */
public class XmlConfigurationReaderFactory implements ConfigurationReaderFactory {
    /**
     * Creates a new XML configuration reader factory.
     */
    public XmlConfigurationReaderFactory() {}

    /**
     * Returns an instance of {@link XmlConfigurationReader}.
     * @return an instance of {@link XmlConfigurationReader}.
     */
    public ConfigurationReader getReaderInstance() {return new XmlConfigurationReader();}
}
