/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

package com.mucommander.file;

/**
 * This interface defines JavaBean-compliant getter and setter methods for common file attributes:
 * <dl>
 *   <dt>path</dt>
 *   <dd>the file's path, <code>null</code> by default. The format and separator character of the path are filesystem-dependent.</dd>
 *
 *   <dt>exists</dt>
 *   <dd>specifies whether the file exists physically on the underlying filesystem, <code>false</code> by default</dd>
 *
 *   <dt>date</dt>
 *   <dd>the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970),
 * <code>0 (00:00:00 GMT, January 1, 1970)</code> by default</dd>
 *
 *   <dt>size</dt>
 *   <dd>the file's size in bytes, <code>0</code> by default</dd>
 *
 *   <dt>isDirectory</dt>
 *   <dd>specifies whether the file is a directory or a regular file, <code>false</code> by default</dd>
 *
 *   <dt>permissions</dt>
 *   <dd>represents the file permissions as a {@link com.mucommander.file.FilePermissions} object, <code>null</code> if
 * undefined</dd>
 *
 *   <dt>owner</dt>
 *   <dd>the file's owner, <code>null</code> by default</dd>
 *
 *   <dt>group</dt>
 *   <dd>the file's group, <code>null</code> by default</dd>
 * </dl>
 *
 * <p>See the {@link com.mucommander.file.SimpleFileAttributes} class for a basic implementation of this interface.</p>
 *
 * @see com.mucommander.file.SimpleFileAttributes
 * @author Maxence Bernard
 */
public interface FileAttributes {

    /**
     * Returns the file's path, <code>null</code> by default.
     *
     * <p>The format and separator character of the path are filesystem-dependent.</p>
     *
     * @return the file's path, <code>null</code> by default
     */
    public String getPath();

    /**
     * Sets the file's path.
     *
     * <p>The format and separator character of the path are filesystem-dependent.</p>
     *
     * @param path the file's path
     */
    public void setPath(String path);


    /**
     * Returns <code>true</code> if the file exists physically on the underlying filesystem, <code>false</code>
     * by default.
     *
     * @return <code>true</code> if the file exists physically on the underlying filesystem, <code>false</code> by default
     */
    public boolean getExists();

    /**
     * Sets whether the file exists physically on the underlying filesystem.
     *
     * @param exists <code>true</code> if the file exists physically on the underlying filesystem
     */
    public void setExists(boolean exists);


    /**
     * Returns the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970), <code>0</code> by default
     *
     * @return the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970), <code>0</code> by default
     */
    public long getDate();

    /**
     * Sets the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     *
     * @param date the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970)
     */
    public void setDate(long date);


    /**
     * Returns the file's size in bytes.
     *
     * @return the file's size in bytes
     */
    public long getSize();

    /**
     * Sets the file's size in bytes.
     *
     * @param size the file's size in bytes
     */
    public void setSize(long size);


    /**
     * Returns <code>true</code> if the file is a directory, <code>false</code> if it is a regular file
     * (defaults to <code>false</code>).
     *
     * @return <code>true</code> if the file is a directory, <code>false</code> if it is a regular file or undefined
     */
    public boolean isDirectory();

    /**
     * Specifies whether the file is a directory or a regular file.
     *
     * @param directory <code>true</code> for directory, <code>false</code> for regular file
     */
    public void setDirectory(boolean directory);


    /**
     * Returns the file's permissions, <code>null</code> by default.
     *
     * @return the file's permissions, <code>null</code> by default
     */
    public FilePermissions getPermissions();

    /**
     * Sets the file's permissions.
     *
     * @param permissions the file's permissions
     */
    public void setPermissions(FilePermissions permissions);


    /**
     * Returns the file's owner, <code>null</code> by default.
     *
     * @return the file's owner, <code>null</code> by default
     */
    public String getOwner();

    /**
     * Sets the file's owner.
     *
     * @param owner the file's owner
     */
    public void setOwner(String owner);


    /**
     * Returns the file's group, <code>null</code> by default.
     *
     * @return the file's group, <code>null</code> by default
     */
    public String getGroup();

    /**
     * Sets the file's group.
     *
     * @param group the file's owner
     */
    public void setGroup(String group);
}
