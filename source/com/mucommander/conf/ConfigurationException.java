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
 * Encapsulates a general configuration error.
 * <p>
 * This class can contain basic error information from either the <code>com.mucommander.conf</code> API
 * or the application. Application writers can subclass it to provide additional functionality. Different
 * classes of the <code>com.mucommander.conf</code> API may throw this exception or any exception subclassed
 * from it.
 * </p>
 * <p>
 * If the application needs to pass through other types of exceptions, it must wrap them in a
 * <code>ConfigurationException</code> or an exception derived from it.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ConfigurationException extends Exception {
    /**
     * Creates a new configuration exception.
     * @param message the error message.
     */
    public ConfigurationException(String message) {super(message);}

    /**
     * Creates a new configuration exception wrapping an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, and its message will
     * become the default message for the <code>ConfigurationException</code>.
     * </p>
     * @param cause the exception to be wrapped in a <code>ConfigurationException</code>.
     */
    public ConfigurationException(Throwable cause) {super(cause);}

    /**
     * Creates a new configuration exception from an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, but the new exception will have its own message.
     * </p>
     * @param message the detail message.
     * @param cause   the exception to be wrapped in a <code>ConfigurationException</code>.
     */
    public ConfigurationException(String message, Throwable cause) {super(message, cause);}
}
