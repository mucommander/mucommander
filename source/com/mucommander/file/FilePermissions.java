/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.file;

/**
 * Contains file permissions and access types.
 *
 * @author Maxence Bernard
 */
public interface FilePermissions {

    /** Bit mask for 'execute' permission */
    public final static int EXECUTE_PERMISSION = 1;
    /** Bit mask for 'write' permission */
    public final static int WRITE_PERMISSION = 2;
    /** Bit mask for 'read' permission */
    public final static int READ_PERMISSION = 4;

    /** Bit mask for 'other' permissions */
    public final static int OTHER_ACCESS = 0;
    /** Bit mask for 'group' permissions */
    public final static int GROUP_ACCESS = 1;
    /** Bit mask for 'owner' permissions */
    public final static int USER_ACCESS = 2;
}
