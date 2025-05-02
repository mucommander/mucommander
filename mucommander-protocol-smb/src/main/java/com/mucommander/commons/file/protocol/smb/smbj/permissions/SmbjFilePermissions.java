/**
 * This file is part of muCommander, http://www.mucommander.com
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



package com.mucommander.commons.file.protocol.smb.smbj.permissions;

import com.mucommander.commons.file.*;

public class SmbjFilePermissions extends IndividualPermissionBits implements FilePermissions {

    // Permissions bit are octal
    public final static PermissionBits EMPTY_MASK = new GroupedPermissionBits(0000);       // ---------
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
