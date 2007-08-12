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
 * Exception thrown when configuration related errors happen.
 * @author Nicolas Rinaudo
 */
public class ConfigurationException extends Exception {
    /**
     * Builds a new exception.
     */
    public ConfigurationException() {super();}

    /**
     * Builds a new exception with the specified message.
     * @param message exception's message.
     */
    public ConfigurationException(String message) {super(message);}

    /**
     * Builds a new exception with the specified cause.
     * @param cause exception's cause.
     */
    public ConfigurationException(Throwable cause) {super(cause);}

    /**
     * Builds a new exception with the specified message and cause.
     * @param message exception's message.
     * @param cause   exception's cause.
     */
    public ConfigurationException(String message, Throwable cause) {super(message, cause);}
}
