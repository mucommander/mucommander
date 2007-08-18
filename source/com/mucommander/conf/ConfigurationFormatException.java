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
 * Encapsulate a configuration format error.
 * <p>
 * Within the scope of the <code>com.mucommander.conf</code> API, format errors
 * are syntax errors in a configuration source.
 * </p>
 * <p>
 * This exception is mostly meant to be used by implementations of {@link ConfigurationReader},
 * as they're the ones who will analyse the syntax of a configuration stream.
 * </p>
 * <p>
 * When applicable, instances of <code>ConfigurationFormatException</code> might provide information
 * about the position in the source at which the error occured. See the documentation of
 * {@link #getLineNumber() getLineNumber} and {@link #getColumnNumber() getColumnNumber} for more
 * information on location conventions.
 * </p>
 * <p>
 * Since <code>ConfigurationFormatException</code> subclasses {@link ConfigurationException}, it
 * inherits his capacity to wrap other exceptions.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ConfigurationFormatException extends ConfigurationException {
    // - Class constants -----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Describes an unknown {@link #getLineNumber() line} or {@link #getColumnNumber() column} value.*/
    public static final int UNKNOWN_LOCATION = -1;



    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Line at which the error occured. */
    private int line   = UNKNOWN_LOCATION;
    /** Column at which the error occured. */
    private int column = UNKNOWN_LOCATION;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Creates a new configuration format exception.
     * @param message the error message.
     */
    public ConfigurationFormatException(String message) {super(message);}

    /**
     * Creates a new configuration format exception.
     * <p>
     * {@link #UNKNOWN_LOCATION} is a legal value for both <code>line</code> and <code>column</code>.
     * See the documentation of {@link #getLineNumber()  getLineNumber} and
     * {@link #getColumnNumber() getColumnNumber} for more information on location conventions.
     * </p>
     * @param message the error message.
     * @param line    line at which the error occured.
     * @param column  column at which the error occured.
     */
    public ConfigurationFormatException(String message, int line, int column) {
        this(message);
        setLocationInformation(line, column);
    }

    /**
     * Creates a new configuration format exception wrapping an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, and its message will
     * become the default message for the <code>ConfigurationFormatException</code>.
     * </p>
     * @param cause the exception to be wrapped in a <code>ConfigurationFormatException</code>.
     */
    public ConfigurationFormatException(Throwable cause) {super(cause == null ? null : cause.getMessage(), cause);}

    /**
     * Creates a new configuration format exception wrapping an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, and its message will
     * become the default message for the <code>ConfigurationFormatException</code>.
     * </p>
     * <p>
     * {@link #UNKNOWN_LOCATION} is a legal value for both <code>line</code> and <code>column</code>.
     * See the documentation of {@link #getLineNumber() getLineNumber} and
     * {@link #getColumnNumber() getColumnNumber} for more information on location conventions.
     * </p>
     * @param cause  the exception to be wrapped in a <code>ConfigurationFormatException</code>.
     * @param line   line at which the error occured.
     * @param column column at which the error occured.
     */
    public ConfigurationFormatException(Throwable cause, int line, int column) {
        this(cause);
        setLocationInformation(line, column);
    }

    /**
     * Creates a new configuration format exception from an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, but the new exception will have its own message.
     * </p>
     * @param message the detail message.
     * @param cause   the exception to be wrapped in a <code>ConfigurationFormatException</code>.
     */
    public ConfigurationFormatException(String message, Throwable cause) {super(message, cause);}

    /**
     * Creates a new configuration format exception from an existing exception.
     * <p>
     * The existing exception will be embedded in the new one, but the new exception will have its own message.
     * </p>
     * <p>
     * {@link #UNKNOWN_LOCATION} is a legal value for both <code>line</code> and <code>column</code>.
     * See the documentation of {@link #getLineNumber() getLineNumber} and
     * {@link #getColumnNumber() getColumnNumber} for more information on location conventions.
     * </p>
     * @param message the detail message.
     * @param cause   the exception to be wrapped in a <code>ConfigurationFormatException</code>.
     * @param line    line at which the error occured.
     * @param column  column at which the error occured.
     */
    public ConfigurationFormatException(String message, Throwable cause, int line, int column) {
        this(message, cause);
        setLocationInformation(line, column);
    }


    // - Misc. ---------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the position in the stream at which the error occured.
     * @param line   line at which the error occured.
     * @param column column at which the error occured.
     */
    private void setLocationInformation(int line, int column) {
        this.line   = line;
        this.column = column;
    }

    /**
     * Returns the line at which the error occured.
     * <p>
     * By convention, the line at which an error occurs is equal to the number of line breaks encountered
     * before the problem, plus one. This means that a line number of <code>1</code> will describe the
     * first line in the configuration source.
     * </p>
     * @return the line at which the error occured, {@link #UNKNOWN_LOCATION} if the information is not
     *         avaiable or relevant.
     * @see    #getColumnNumber()
     */
    public int getLineNumber() {return line;}

    /**
     * Returns the column at which the error occured.
     * <p>
     * By convention, the column at which an error occurs is equal to the number of character encountered
     * after the last line break and before the problem, plus one. This means that a column number of
     * <code>1</code> will describe the first character in the current line.
     * </p>
     * @return the column at which the error occured, {@link #UNKNOWN_LOCATION} if the information is not
     *         avaiable or relevant.
     * @see    #getLineNumber()
     */
    public int getColumnNumber() {return column;}
}
