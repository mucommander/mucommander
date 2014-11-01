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
 * This interface extends <code>FileAttributes</code> to add attribute getters. Refer to {@link FileAttributes}'s
 * documentation for more information about attributes.
 *
 * <p>See the {@link SimpleFileAttributes} class for an implementation of this interface.</p>
 *
 * @author Maxence Bernard
 * @see SimpleFileAttributes
 */
public interface MutableFileAttributes extends FileAttributes {

    /**
     * Sets the file's path.
     *
     * <p>The format and separator character of the path are filesystem-dependent.</p>
     *
     * @param path the file's path
     */
    public void setPath(String path);

    /**
     * Sets whether the file exists physically on the underlying filesystem.
     *
     * @param exists <code>true</code> if the file exists physically on the underlying filesystem
     */
    public void setExists(boolean exists);

    /**
     * Sets the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     *
     * @param date the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     */
    public void setDate(long date);

    /**
     * Sets the file's size in bytes.
     *
     * @param size the file's size in bytes
     */
    public void setSize(long size);

    /**
     * Specifies whether the file is a directory or a regular file.
     *
     * @param directory <code>true</code> for directory, <code>false</code> for regular file
     */
    public void setDirectory(boolean directory);

    /**
     * Sets the file's permissions.
     *
     * @param permissions the file's permissions
     */
    public void setPermissions(FilePermissions permissions);

    /**
     * Sets the file's owner.
     *
     * @param owner the file's owner
     */
    public void setOwner(String owner);

    /**
     * Sets the file's group.
     *
     * @param group the file's owner
     */
    public void setGroup(String group);
}
