/*
 * This file is part of muCommander, http://www.mucommander.com
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

package com.mucommander.command;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.PermissionAccess;
import com.mucommander.commons.file.PermissionType;
import com.mucommander.commons.file.filter.AbstractFileFilter;

/**
 * Filter on a file's permissions.
 * @author Nicolas Rinaudo
 */
public class PermissionsFileFilter extends AbstractFileFilter {
    private PermissionType permission;
    private boolean filter;

    /**
     * Creates a new <code>PermissionsFileFilter</code>.
     * @param permission permission that will be checked against as defined in {@link com.mucommander.commons.file.FilePermissions}.
     * @param filter     whether or not the specified permission flag must be set for a file to be accepted.
     */
    public PermissionsFileFilter(PermissionType permission, boolean filter) {
        this.permission = permission;
        this.filter     = filter;
    }

    public boolean accept(AbstractFile file) {
        return filter ? file.getPermissions().getBitValue(PermissionAccess.USER, permission) : !file.getPermissions().getBitValue(PermissionAccess.USER, permission);
    }

    /**
     * Returns the permission that this filter will check.
     * @return the permission that this filter will check.
     */
    public PermissionType getPermission() {return permission;}

    /**
     * Returns <code>true</code> if files must have the filter's permission flag set in order to be accepted.
     * @return <code>true</code> if files must have the filter's permission flag set in order to be accepted, <code>false</code> otherwise.
     */
    public boolean getFilter() {return filter;}
}

