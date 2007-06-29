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
 * Filter on a file's type.
 * @author Nicolas Rinaudo
 */
public class TypeFileFilter extends FileFilter {
    public static final int DIRECTORY = 0;
    public static final int SYMLINK   = 1;
    public static final int HIDDEN    = 2;

    private int     type;
    private boolean filter;

    public TypeFileFilter(int type, boolean filter) {
        this.type   = type;
        this.filter = filter;
    }

    public boolean accept(AbstractFile file) {
        switch(type) {
        case DIRECTORY:
            return filter ? file.isDirectory() : !file.isDirectory();
        case SYMLINK:
            return filter ? file.isSymlink() : !file.isSymlink();
        case HIDDEN:
            return filter ? file.isHidden() : !file.isHidden();
        }
        return true;
    }

    public int getType() {return type;}
    public boolean getFilter() {return filter;}
}

