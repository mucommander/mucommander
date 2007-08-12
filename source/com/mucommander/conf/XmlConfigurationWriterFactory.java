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
 * {@link ConfigurationWriterFactory} implementation used to create instances of {@link XmlConfigurationWriter}.
 * <p>
 * {@link ConfigurationWriter Writer} instances created by this factory are meant to create the standard muCommander
 * configuration file format described in the documentation of {@link XmlConfigurationWriter}.
 * </p>
 * @author Nicolas Rinaudo
 * @see    XmlConfigurationReader
 * @see    XmlConfigurationWriter
 */
public class XmlConfigurationWriterFactory implements ConfigurationWriterFactory {
    /**
     * Returns an instance of {@link XmlConfigurationWriter}.
     * @return an instance of {@link XmlConfigurationWriter}.
     */
    public ConfigurationWriter getWriterInstance() {return new XmlConfigurationWriter();}
}
