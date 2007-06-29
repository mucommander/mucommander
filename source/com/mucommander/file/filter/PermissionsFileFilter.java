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

package com.mucommander.file.filter;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FilePermissions;

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
        if((permission == EXECUTE_PERMISSION) && (PlatformManager.JAVA_VERSION <= PlatformManager.JAVA_1_5))
            return true;

        return filter ? file.getPermission(USER_ACCESS, permission) : !file.getPermission(USER_ACCESS, permission);
    }

    public int getPermission() {return permission;}
    public boolean getFilter() {return filter;}
}

