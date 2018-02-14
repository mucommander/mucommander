/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

/**
 * Encapsulate a source configuration error.
 * <p>
 * This exception is meant to be thrown by {@link Configuration} whenever a method that requires a
 * {@link ConfigurationSource} to have been set is called.
 * </p>
 * <p>
 * Since <code>SourceConfigurationException</code> subclasses {@link ConfigurationException}, it
 * inherits his capacity to wrap other exceptions.
 * </p>
 *
 * @author Nicolas Rinaudo
 */
public class SourceConfigurationException extends ConfigurationException {
    /**
     * Creates a new source configuration exception.
     *
     * @param message the error message.
     */
    public SourceConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a new source configuration exception wrapping an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, and its message will
     * become the default message for the <code>SourceConfigurationException</code>.
     * </p>
     *
     * @param cause the exception to be wrapped in a <code>SourceConfigurationException</code>.
     */
    public SourceConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new source configuration exception from an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, but the new exception will have its own message.
     * </p>
     *
     * @param message the detail message.
     * @param cause   the exception to be wrapped in a <code>SourceConfigurationException</code>.
     */
    public SourceConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
