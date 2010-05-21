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
 * GroupedPermissionBits is an implementation of {@link com.mucommander.commons.file.PermissionBits} using a given UNIX-style
 * permission int: {@link #getIntValue()} returns the specified int, and {@link #getBitValue(int, int)} isolates a
 * specified value.
 *
 * @see com.mucommander.commons.file.IndividualPermissionBits
 * @author Maxence Bernard
 */
public class GroupedPermissionBits implements PermissionBits {

    /** UNIX-style permission int */
    protected int permissions;

    /**
     * Creates a new GroupedPermissionBits using the specified UNIX-style permission int. The int can be created
     * by combining (binary OR and shift) values defined in {@link com.mucommander.commons.file.PermissionTypes} and
     * {@link com.mucommander.commons.file.PermissionAccesses}.
     *
     * @param permissions a UNIX-style permission int.
     */
    public GroupedPermissionBits(int permissions) {
        this.permissions = permissions;
    }


    ///////////////////////////////////
    // PermissionBits implementation //
    ///////////////////////////////////

    public int getIntValue() {
        return permissions;
    }

    public boolean getBitValue(int access, int type) {
        return (permissions & (type << (access*3))) != 0;
    }
}
