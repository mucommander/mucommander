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
 * This interface defines getters for the following file attributes:
 * <dl>
 *   <dt>path</dt>
 *   <dd>the file's path, <code>null</code> by default. The type of path (relative or absolute) separator character
 * are unspecified and context-dependant.</dd>
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
 *   <dd>represents the file permissions as a {@link com.mucommander.commons.file.FilePermissions} object, <code>null</code> if
 * undefined</dd>
 *
 *   <dt>owner</dt>
 *   <dd>the file's owner, <code>null</code> by default</dd>
 *
 *   <dt>group</dt>
 *   <dd>the file's group, <code>null</code> by default</dd>
 * </dl>
 *
 * <p>See the {@link MutableFileAttributes} for an extended interface that include file attribute setters.</p>
 *
 * @see MutableFileAttributes
 * @see SimpleFileAttributes
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
     * Returns <code>true</code> if the file exists physically on the underlying filesystem, <code>false</code>
     * by default.
     *
     * @return <code>true</code> if the file exists physically on the underlying filesystem, <code>false</code> by default
     */
    public boolean exists();

    /**
     * Returns the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970), <code>0</code> by default
     *
     * @return the file's date in milliseconds since the epoch (00:00:00 GMT, January 1, 1970), <code>0</code> by default
     */
    public long getDate();

    /**
     * Returns the file's size in bytes.
     *
     * @return the file's size in bytes
     */
    public long getSize();

    /**
     * Returns <code>true</code> if the file is a directory, <code>false</code> if it is a regular file
     * (defaults to <code>false</code>).
     *
     * @return <code>true</code> if the file is a directory, <code>false</code> if it is a regular file or undefined
     */
    public boolean isDirectory();

    /**
     * Returns the file's permissions, <code>null</code> by default.
     *
     * @return the file's permissions, <code>null</code> by default
     */
    public FilePermissions getPermissions();

    /**
     * Returns the file's owner, <code>null</code> by default.
     *
     * @return the file's owner, <code>null</code> by default
     */
    public String getOwner();

    /**
     * Returns the file's group, <code>null</code> by default.
     *
     * @return the file's group, <code>null</code> by default
     */
    public String getGroup();
}
