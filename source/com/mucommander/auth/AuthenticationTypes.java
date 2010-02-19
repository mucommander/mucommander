/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.auth;

/**
 * Defines the different types of authentication a file protocol may use.
 *
 * @author Maxence Bernard
 */
public interface AuthenticationTypes {

    /** Indicates that the file protocol does not use any kind of authentication. */
    public final static int NO_AUTHENTICATION = 0;

    /** Indicates that the file protocol can use authentication but does not require it. */
    public final static int AUTHENTICATION_OPTIONAL = 1;

    /** Indicates that the file protocol requires authentication. */
    public final static int AUTHENTICATION_REQUIRED = 2;
}
