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

package com.mucommander.runtime;

/**
 * @author Maxence Bernard
 */
public interface OsVersions {

    /** Mac OS X 10.0 */
    public static final OsVersion MAC_OS_X_10_0   = new OsVersion("10.0", 0);

    /** Mac OS X 10.1 */
    public static final OsVersion MAC_OS_X_10_1   = new OsVersion("10.1", 1);

    /** Mac OS X 10.2 */
    public static final OsVersion MAC_OS_X_10_2   = new OsVersion("10.2", 2);

    /** Mac OS X 10.3 */
    public static final OsVersion MAC_OS_X_10_3   = new OsVersion("10.3", 3);

    /** Mac OS X 10.4 */
    public static final OsVersion MAC_OS_X_10_4   = new OsVersion("10.4", 4);

    /** Mac OS X 10.5 */
    public static final OsVersion MAC_OS_X_10_5   = new OsVersion("10.5", 5);

    /** Unknown OS version */
    public static final OsVersion UNKNOWN_VERSION = new OsVersion("Unknown", -1);
}
