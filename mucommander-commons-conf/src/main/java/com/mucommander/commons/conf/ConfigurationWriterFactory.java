/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.commons.conf;

import java.io.Writer;

/**
 * Interface used to provide interfaces of {@link Configuration} with a way of creating writer instances.
 * <p>
 * A <code>ConfigurationWriterFactory</code>'s sole purpose is to create instances of writer. In most cases, a
 * factory class will be associated with a writer class, and its code will look something like:
 * <pre>
 * public class MyWriterFactory implements ConfigurationWriterFactory {
 *    public ConfigurationWriter getWriterInstance() {return new MyWriter();}
 * }
 * </pre>
 * </p>
 * @author Nicolas Rinaudo
 */
@FunctionalInterface
public interface ConfigurationWriterFactory {
	
    /**
     * Creates an instance of {@link ConfigurationBuilder}.
     * <p>
     * The returned builder instance will serialize configuration events to the specified writer.
     * </p>
     * @param  out                          where to write the configuration data.
     * @return                              an instance of {@link ConfigurationBuilder}.
     * @throws WriterConfigurationException if the factory wasn't properly configured.
     */
    ConfigurationBuilder getWriterInstance(Writer out) throws WriterConfigurationException;
}
