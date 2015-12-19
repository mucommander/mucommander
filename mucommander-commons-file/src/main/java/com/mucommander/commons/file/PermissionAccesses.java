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
 * This interface defines constants fields used for designating the three different permission accesses:
 * {@link #USER_ACCESS}, {@link #GROUP_ACCESS} and {@link #OTHER_ACCESS}. Their actual int values represent the number
 * of 3-bit left shifts (<< operator) needed to represent a particular
 * {@link com.mucommander.commons.file.PermissionTypes permission type} in a UNIX-style permission int. To illustrate,
 * the 'read' permission (value = 4) for the 'user' access (value = 2) is represented in a UNIX-style permission int as:
 * <code>4 << 3*2 = 256 (400 octal)</code>.
 *
 * @see com.mucommander.commons.file.PermissionTypes
 * @author Maxence Bernard
 */
public interface PermissionAccesses {

    /** Designates the 'other' permission access. */
    public int OTHER_ACCESS = 0;

    /** Designates the 'group' permission access. */
    public int GROUP_ACCESS = 1;

    /** Designates the 'user' permission access. */
    public int USER_ACCESS = 2;
}
