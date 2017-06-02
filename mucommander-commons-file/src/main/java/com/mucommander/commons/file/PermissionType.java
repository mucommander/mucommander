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
 * This interface defines constants fields used for designating the three different permission types:
 * {@link #READ_PERMISSION}, {@link #WRITE_PERMISSION} and {@link #EXECUTE_PERMISSION}. Their actual value represent
 * the bit to be set and left-shifted with the desired {@link com.mucommander.commons.file.PermissionAccess permission access}
 * in a UNIX-style permission int.
 *
 * @see PermissionAccess
 * @author Maxence Bernard
 */
public enum PermissionType {

    /** Designates the 'execute' permission. */
    EXECUTE(1),
    /** Designates the 'write' permission. */
    WRITE(2),
    /** Designates the 'read' permission. */
    READ(4);

    private int intValue;

    private PermissionType(int val) {
		this.intValue = val;
	}

    public int toInt() {
    	return intValue;
    }

    public static List<PermissionType> reverseValues() {
        List<PermissionType> values = Arrays.asList(PermissionType.values());
        Collections.reverse(values);
        return values;
    }
}
