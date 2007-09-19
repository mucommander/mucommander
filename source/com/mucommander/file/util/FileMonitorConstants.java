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

package com.mucommander.file.util;

/**
 * Contains constants used by {@link com.mucommander.file.util.FileMonitor}.
 *
 * @author Maxence Bernard
 */
public interface FileMonitorConstants {

    /** File date attribute, as returned by {@link com.mucommander.file.AbstractFile#getDate()} */
    public final static int DATE_ATTRIBUTE = 1;

    /** File size attribute, as returned by {@link com.mucommander.file.AbstractFile#getSize()} */
    public final static int SIZE_ATTRIBUTE = 2;

    /** File permissions attribute, as returned by {@link com.mucommander.file.AbstractFile#getPermissions()} */
    public final static int PERMISSIONS_ATTRIBUTE = 4;

    /** File 'is directory' attribute, as returned by {@link com.mucommander.file.AbstractFile#isDirectory()} */
    public final static int IS_DIRECTORY_ATTRIBUTE = 8;

    /** File 'exists' attribute, as returned by {@link com.mucommander.file.AbstractFile#exists()} */
    public final static int EXISTS_ATTRIBUTE = 16;

    /** Default attribute set: {@link #DATE_ATTRIBUTE} */
    public final static int DEFAULT_ATTRIBUTES = DATE_ATTRIBUTE;

    /** Designates all attributes */
    public final static int ALL_ATTRIBUTES = DATE_ATTRIBUTE|SIZE_ATTRIBUTE|PERMISSIONS_ATTRIBUTE|IS_DIRECTORY_ATTRIBUTE|EXISTS_ATTRIBUTE;

    /** Default poll period in milliseconds */
    public final static long DEFAULT_POLL_PERIOD = 10000;
}
