/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2017 Maxence Bernard
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This interface defines constants fields used for designating the three different permission accesses:
 * {@link #USER_ACCESS}, {@link #GROUP_ACCESS} and {@link #OTHER_ACCESS}. Their actual int values represent the number
 * of 3-bit left shifts (<< operator) needed to represent a particular
 * {@link com.mucommander.commons.file.PermissionType permission type} in a UNIX-style permission int. To illustrate,
 * the 'read' permission (value = 4) for the 'user' access (value = 2) is represented in a UNIX-style permission int as:
 * <code>4 << 3*2 = 256 (400 octal)</code>.
 *
 * @see com.mucommander.commons.file.PermissionType
 * @author Maxence Bernard
 */
public enum PermissionAccess {

    /** Designates the 'other' permission access. */
    OTHER(0),
    /** Designates the 'group' permission access. */
    GROUP(1),
    /** Designates the 'user' permission access. */
    USER(2);

    private int intVal;

    private PermissionAccess(int intVal) {
    	this.intVal = intVal;
	}

    public int toInt() {
    	return intVal;
    }

    public static List<PermissionAccess> reverseValues() {
    	List<PermissionAccess> values = Arrays.asList(PermissionAccess.values());
        Collections.reverse(values);
        return values;
    }
}
