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
 * Encapsulate a writer configuration error.
 * <p>
 * This exception is mostly meant to be used by implementations of {@link ConfigurationWriterFactory},
 * as they're the ones who will configure instances of {@link ConfigurationWriter}.
 * </p>
 * <p>
 * Since <code>WriterConfigurationException</code> subclasses {@link ConfigurationException}, it
 * inherits his capacity to wrap other exceptions.
 * </p>
 * @author Nicolas Rinaudo
 */
public class WriterConfigurationException extends ConfigurationException {
    /**
     * Creates a new writer configuration exception.
     * @param message the error message.
     */
    public WriterConfigurationException(String message) {super(message);}

    /**
     * Creates a new writer configuration exception wrapping an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, and its message will
     * become the default message for the <code>WriterConfigurationException</code>.
     * </p>
     * @param cause the exception to be wrapped in a <code>WriterConfigurationException</code>.
     */
    public WriterConfigurationException(Throwable cause) {super(cause);}

    /**
     * Creates a new writer configuration exception from an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, but the new exception will have its own message.
     * </p>
     * @param message the detail message.
     * @param cause   the exception to be wrapped in a <code>WriterConfigurationException</code>.
     */
    public WriterConfigurationException(String message, Throwable cause) {super(message, cause);}
}
