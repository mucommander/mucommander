/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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


package com.mucommander.commons.file;

/**
 * Defines the different types of authentication a file protocol may use.
 *
 * @see FileURL#getAuthenticationType()
 * @author Maxence Bernard
 */
public enum AuthenticationType {

    /** Indicates that the file protocol does not use any kind of authentication. */
    NO_AUTHENTICATION,

    /** Indicates that the file protocol can use authentication but does not require it. */
    AUTHENTICATION_OPTIONAL,

    /** Indicates that the file protocol requires authentication. */
    AUTHENTICATION_REQUIRED
}
