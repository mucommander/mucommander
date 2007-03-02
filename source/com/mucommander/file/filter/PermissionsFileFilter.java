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

