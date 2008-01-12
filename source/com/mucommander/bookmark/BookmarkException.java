/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.bookmark;

/**
 * Exception thrown when bookmark related errors occur.
 * @author Nicolas Rinaudo
 */
public class BookmarkException extends Exception {
    /**
     * Creates a new exception with the specified message.
     * @param message exception's message.
     */
    public BookmarkException(String message) {super(message);}

    /**
     * Creates a new exception wrapping the specified error.
     * @param cause root cause of the new exception.
     */
    public BookmarkException(Throwable cause) {super(cause);}

    /**
     * Creates a new exception with the specified message and cause.
     * @param message exception's message.
     * @param cause   root cause of the new exception.
     */
    public BookmarkException(String message, Throwable cause) {super(message, cause);}
}
