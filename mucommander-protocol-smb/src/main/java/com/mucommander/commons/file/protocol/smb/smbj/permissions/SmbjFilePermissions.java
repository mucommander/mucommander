package com.mucommander.commons.file.protocol.smb.smbj.permissions;

import com.mucommander.commons.file.*;

public class SmbjFilePermissions extends IndividualPermissionBits implements FilePermissions {

    // Permissions bit are octal
    private final static PermissionBits EMPTY_MASK = new GroupedPermissionBits(0000);       // ---------
    private final static PermissionBits READ_MASK = new GroupedPermissionBits(0400);        // r--------
    private final static PermissionBits WRITE_MASK = new GroupedPermissionBits(0200);       // -w-------
    private final static PermissionBits READ_WRITE_MASK = new GroupedPermissionBits(0600);  // rw-------

    private final boolean canRead;

    private final boolean canWrite;

    public SmbjFilePermissions(boolean canRead, boolean canWrite) {
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    @Override
    public PermissionBits getMask() {
        if (this.canRead && this.canWrite) {
            return READ_WRITE_MASK;
        } else if (this.canRead) {
            return READ_MASK;
        } else if (this.canWrite) {
            return WRITE_MASK;
        } else {
            return EMPTY_MASK;
        }
    }

    @Override
    public boolean getBitValue(PermissionAccess access, PermissionType type) {
        return switch (type) {
            case READ -> this.canRead;
            case WRITE -> this.canWrite;
            case EXECUTE -> false;
        };
    }

}
