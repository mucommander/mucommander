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
 * FilePermissions is an interface that represents the permissions of an {@link com.mucommander.commons.file.AbstractFile}.
 * The actual permission values can be retrieved by the methods inherited from the
 * {@link com.mucommander.commons.file.PermissionBits} interface. The permissions mask returned by {@link #getMask()} allows
 * to determine which permission bits are significant, i.e. should be taken into account. That way, certain
 * {@link AbstractFile} implementations that have limited permissions support can set those supported permission bits
 * while making it clear that other bits should be ignored, and not simply be considered as being disabled.
 * For instance, a file implementation with support for the sole 'user' permissions (read/write/execute) will return a
 * mask whose int value is 448 (700 octal).
 *
 * <p>This interface also defines constants for commonly used file permissions.</p>
 *
 * @see com.mucommander.commons.file.AbstractFile#getPermissions()
 * @author Maxence Bernard
 */
public abstract interface FilePermissions extends PermissionBits {

    /** Empty file permissions: read/write/execute permissions cleared for user/group/other (0), none of the permission
     * bits are supported (mask is 0) */
    public final static FilePermissions EMPTY_FILE_PERMISSIONS = new SimpleFilePermissions(0, 0);

    /** Default file permissions used by {@link AbstractFile#importPermissions(AbstractFile)} for permission bits that
     * are not available in the source: rw-r--r-- (644 octal). All of the permission bits are marked as supported. */
    public final static FilePermissions DEFAULT_FILE_PERMISSIONS = new SimpleFilePermissions(420, FULL_PERMISSION_BITS);

    /** Default directory permissions used by {@link AbstractFile#importPermissions(AbstractFile)} for permission bits that
     * are not available in the source: rwxr-xr-x (755 octal). All of the permission bits are marked as supported. */
    public final static FilePermissions DEFAULT_DIRECTORY_PERMISSIONS = new SimpleFilePermissions(493, FULL_PERMISSION_BITS);


    /**
     * Returns the mask that indicates which permission bits are significant and should be taken into account.
     * Permission bits that are unsupported have no meaning and their value should simply be ignored.
     *
     * @return the mask that indicates which permission bits are significant and should be taken into account.
     */
    public PermissionBits getMask();
}
