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

package com.mucommander.command;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FilePermissions;
import com.mucommander.file.filter.FileFilter;

/**
 * Filter on a file's permissions.
 * @author Nicolas Rinaudo
 */
public class PermissionsFileFilter extends FileFilter implements FilePermissions {
    private int     permission;
    private boolean filter;

    public PermissionsFileFilter(int permission, boolean filter) {
        this.permission = permission;
        this.filter     = filter;
    }

    public boolean accept(AbstractFile file) {
        if(permission==EXECUTE_PERMISSION && PlatformManager.JAVA_1_5.isCurrentOrLower())
            return true;

        return filter ? file.getPermission(USER_ACCESS, permission) : !file.getPermission(USER_ACCESS, permission);
    }

    public int getPermission() {return permission;}
    public boolean getFilter() {return filter;}
}

