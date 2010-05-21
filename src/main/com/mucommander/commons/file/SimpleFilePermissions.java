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
 * SimpleFilePermissions is a FilePermissions implementation that takes int values for the permission values and mask.
 * Additionally, this class defines <code>padPermission</code> static methods that allows to pad unsupported permission
 * bits with default values.
 *
 * @author Maxence Bernard
 */
public class SimpleFilePermissions extends GroupedPermissionBits implements FilePermissions {

    /** The permissions mask */
    protected PermissionBits mask;

    /**
     * Creates a new SimpleFilePermissions using the specified UNIX-style permission int for permission values and
     * {@link #FULL_PERMISSION_BITS full permissions mask}.
     *
     * @param permissions a UNIX-style permission int that holds permission values.
     */
    public SimpleFilePermissions(int permissions) {
        this(permissions, FULL_PERMISSION_BITS);
    }

    /**
     * Creates a new SimpleFilePermissions using the specified UNIX-style permission int values for permission values
     * and mask.
     *
     * @param permissions a UNIX-style permission int that holds permission values.
     * @param mask a UNIX-style permission int which defines which permission bits are supported.
     */
    public SimpleFilePermissions(int permissions, int mask) {
        this(permissions, new GroupedPermissionBits(mask));
    }

    /**
     * Creates a new SimpleFilePermissions using the specified UNIX-style permission int and permission mask.
     *
     * @param permissions a UNIX-style permission int that holds permission values.
     * @param mask a permission mask which defines which permission bits are supported.
     */
    public SimpleFilePermissions(int permissions, PermissionBits mask) {
        super(permissions);

        this.mask = mask;
    }


    /**
     * Pads the given permissions with the specified ones: the permission bits that are not supported
     * (as reported by the supplied permissions mask} are replaced by those of the default permissions.
     * That means:<br/>
     *  - if the mask indicates that all permission bits are supported (mask = 777 octal), the supplied permissions will
     * simply be returned, without using any of the default permissions<br/>
     *  - if the mask indicates that none of the permission bits are supported (mask = 0), the default permissions will
     * be returned, without using any of the supplied permissions<br/>
     *
     * @param permissions the permissions to pad with default permissions for the bits that are not supported
     * @param defaultPermissions permissions to use for the bits that are not supported
     * @return the permissions padded with the default permissions
     */
    public static FilePermissions padPermissions(FilePermissions permissions, FilePermissions defaultPermissions) {
        int permissionMask = permissions.getMask().getIntValue();

        return new SimpleFilePermissions((
                permissions.getIntValue() & permissionMask) | (~permissionMask & defaultPermissions.getIntValue()),
                defaultPermissions.getMask());
    }

    /**
     * Pads the given permissions with the specified ones: the permission bits that are not supported
     * (as reported by the supplied permissions mask} are replaced by those of the default permissions.
     * That means:<br/>
     *  - if the mask indicates that all permission bits are supported (mask = 777 octal), the supplied permissions will
     * simply be returned, without using any of the default permissions<br/>
     *  - if the mask indicates that none of the permission bits are supported (mask = 0), the default permissions will
     * be returned, without using any of the supplied permissions<br/>
     *
     * @param permissions the permissions to pad with default permissions for the bits that are not supported
     * @param supportedPermissionsMask the bit mask that indicates which bits of the given permissions are supported
     * @param defaultPermissions permissions to use for the bits that are not supported
     * @return the given permissions padded with the default permissions
     */
    public static int padPermissions(int permissions, int supportedPermissionsMask, int defaultPermissions) {
        return (permissions & supportedPermissionsMask) | (~supportedPermissionsMask & defaultPermissions);
    }


    ////////////////////////////////////
    // FilePermissions implementation //
    ////////////////////////////////////

    public PermissionBits getMask() {
        return mask;
    }
}
