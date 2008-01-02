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
public interface OsFamilies {

    /** Windows 95, 98, Me */
    public static final OsFamily WINDOWS_9X =        new OsFamily("Windows 9X");

    /** Windows NT, 2000, XP and up */
    public static final OsFamily WINDOWS_NT =        new OsFamily("Windows NT");

    /** Mac OS X */
    public static final OsFamily MAC_OS_X   =        new OsFamily("Mac OS X");

    /** Linux */
    public static final OsFamily LINUX      =        new OsFamily("Linux");

    /** Solaris */
    public static final OsFamily SOLARIS    =        new OsFamily("Solaris");

    /** OS/2 */
    public static final OsFamily OS_2       =        new OsFamily("OS/2");

    /** Other OS */
    public static final OsFamily UNKNOWN_OS_FAMILY = new OsFamily("Unknown");
}
