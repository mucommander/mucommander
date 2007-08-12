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
 * Exception thrown when errors are found in a configuration stream's format.
 * <p>
 * Developers can use the {@link #getColumnNumber()} and {@link #getLineNumber()} methods
 * in order to display precise error messages.
 * </p>
 * @author Nicolas Rinaudo
 */
public class ConfigurationFormatException extends Exception {
    // - Instance variables --------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /** Line at which the error occured. */
    private int line   = -1;
    /** Column at which the error occured. */
    private int column = -1;



    // - Initialisation ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    public ConfigurationFormatException() {super();}

    public ConfigurationFormatException(Throwable cause) {super(cause == null ? null : cause.getMessage(), cause);}

    public ConfigurationFormatException(String message, Throwable cause) {super(message, cause);}

    public ConfigurationFormatException(String message) {super(message);}

    public ConfigurationFormatException(int line, int column) {
        this();
        setLocationInformation(line, column);
    }

    public ConfigurationFormatException(Throwable cause, int line, int column) {
        this(cause);
        setLocationInformation(line, column);
    }

    public ConfigurationFormatException(String message, Throwable cause, int line, int column) {
        this(message, cause);
        setLocationInformation(line, column);
    }

    public ConfigurationFormatException(String message, int line, int column) {
        this(message);
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
     * @return the line at which the error occured, <code>-1</code> if the information is not
     *         avaiable or relevant.
     * @see    #getColumnNumber()
     */
    public int getLineNumber() {return line;}

    /**
     * Returns the column at which the error occured.
     * @return the column at which the error occured, <code>-1</code> if the information is not
     *         avaiable or relevant.
     * @see    #getLineNumber()
     */
    public int getColumnNumber() {return column;}
}
