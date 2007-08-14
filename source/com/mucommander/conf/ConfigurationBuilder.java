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
 * Interface for exploring configuration data using callbacks.
 * <p>
 * <code>ConfigurationBuilder</code> implementations can be passed to
 * {@link Configuration#build(ConfigurationBuilder)} to receive
 * information about the whole configuration tree. This mechanism is mostly meant
 * for saving the configuration, but applications that wish, for example, to display
 * it as a <code>JTree</code> can use it to initialise their UI.
 * </p>
 * @author Nicolas Rinaudo
 */
public interface ConfigurationBuilder {
    /**
     * Notifies the builder that configuration building is about to start.
     * @throws ConfigurationException if an error occurs.
     */
    public void startConfiguration() throws ConfigurationException;

    /**
     * Notifies the builder that configuration building has finished.
     * @throws ConfigurationException if an error occurs.
     */
    public void endConfiguration() throws ConfigurationException;

    /**
     * Notifies the builder that a new section is being started.
     * @param  name                   name of the new section.
     * @throws ConfigurationException if an error occurs.
     */
    public void startSection(String name) throws ConfigurationException;

    /**
     * Notifies the builder that a sections is finished.
     * @param  name                   name of the finished section.
     * @throws ConfigurationException if an error occurs. 
     */
    public void endSection(String name) throws ConfigurationException;

    /**
     * Notifies the builder that a new variable has been found for the current section.
     * @param  name                   name of the new variable.
     * @param  value                  value of the new variable.
     * @throws ConfigurationException thrown if an error occurs.
     */
    public void addVariable(String name, String value) throws ConfigurationException;
}
