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

    //////////////////////
    // Windows versions //
    //////////////////////

    /** Windows 95 */
    public static final OsVersion WINDOWS_95    = new OsVersion("Windows 95", 0);

    /** Windows 98 */
    public static final OsVersion WINDOWS_98    = new OsVersion("Windows 98", 1);

    /** Windows Me */
    public static final OsVersion WINDOWS_ME    = new OsVersion("Windows Me", 2);

    /** Windows NT */
    public static final OsVersion WINDOWS_NT    = new OsVersion("Windows NT", 3);

    /** Windows 2000 */
    public static final OsVersion WINDOWS_2000  = new OsVersion("Windows 2000", 4);

    /** Windows XP */
    public static final OsVersion WINDOWS_XP    = new OsVersion("Windows XP", 5);

    /** Windows 2003 */
    public static final OsVersion WINDOWS_2003  = new OsVersion("Windows 2003", 6);

    /** Windows Vista */
    public static final OsVersion WINDOWS_VISTA = new OsVersion("Windows Vista", 7);


    ///////////////////////
    // Mac OS X versions //
    ///////////////////////

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
