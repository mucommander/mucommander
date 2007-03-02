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

