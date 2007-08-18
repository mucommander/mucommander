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
 * Receive notification of the logical structure of a {@link Configuration configuration} instance.
 * <p>
 * If a class needs to be informed of the logical structure of a configuration instance,
 * it implements this interface and registers an instance with the {@link Configuration} using
 * its {@link Configuration#build(ConfigurationBuilder) build} method. The {@link Configuration}
 * uses the instance to report configuration related events such as the start of sections and
 * variable declarations.
 * </p>
 * <p>
 * The <code>com.mucommander.conf</code> API comes with a default <i>no-op</i> implementation,
 * {@link DefaultConfigurationBuilder}. This can be used instead of <code>ConfigurationBuilder</code>
 * when only a subset of the possible events are of interest.
 * </p>
 * @author Nicolas Rinaudo
 * @see    DefaultConfigurationBuilder
 */
public interface ConfigurationBuilder {
    /**
     * Receives notification at the begining of the configuration.
     * <p>
     * This method will only be invoked once, before any other method in this interface.
     * </p>
     * @throws ConfigurationException any Configuration error, possibly wrapping another exception.
     */
    public void startConfiguration() throws ConfigurationException;

    /**
     * Receives notification at the end of the configuration.
     * <p>
     * This method will be invoked at most once, and if it is, it will be the last one. If an
     * unrecoverable error happens, this method might never be called.
     * </p>
     * @throws ConfigurationException any Configuration error, possibly wrapping another exception.
     */
    public void endConfiguration() throws ConfigurationException;

    /**
     * Receives notification at the beginning of a section.
     * <p>
     * This method will be invoked once at the beginning of every configuration section. Unless an
     * unrecoverable error happens, there will be an {@link #endSection(String) endSection} event for every
     * <code>startSection</code> event, even if the section is empty. All of the section's content will be
     * reported, in order, before the corresponding {@link #endSection(String) endSection} event.
     * </p>
     * @param  name                   name of the new section.
     * @throws ConfigurationException any Configuration error, possibly wrapping another exception.
     */
    public void startSection(String name) throws ConfigurationException;

    /**
     * Receives notification at the end of a section.
     * <p>
     * This method will be invoked once at the end of every configuration section. There will be a
     * corresponding {@link #startSection(String) startSection} event for every <code>endSection</code>
     * event, even if the section is empty.
     * </p>
     * @param  name                   name of the finished section.
     * @throws ConfigurationException any Configuration error, possibly wrapping another exception.
     */
    public void endSection(String name) throws ConfigurationException;

    /**
     * Receives notification of variable definition.
     * <p>
     * This method will be invoked once per variable found in a section. The declared variable
     * will always belong to the section defined in the last {@link #startSection(String) startSection}
     * event which hasn't yet been closed by an {@link #endSection(String) endSection} event. If there is
     * no such section, the variable belongs to the unnamed root section.
     * </p>
     * @param  name                   name of the new variable.
     * @param  value                  value of the new variable.
     * @throws ConfigurationException any Configuration error, possibly wrapping another exception.
     */
    public void addVariable(String name, String value) throws ConfigurationException;
}
