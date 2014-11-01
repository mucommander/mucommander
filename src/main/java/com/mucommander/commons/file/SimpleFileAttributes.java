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
 * This class is a simple implementation of the {@link com.mucommander.commons.file.FileAttributes} interface, where all
 * the attributes are stored as protected members of the class.
 *
 * @author Maxence Bernard
 */
public class SimpleFileAttributes implements MutableFileAttributes {

    /** Path attribute */
    private String path;

    /** Exists attribute */
    private boolean exists;

    /** Date attribute */
    private long date;

    /** Size attribute */
    private long size;

    /** Directory attribute */
    private boolean directory;

    /** Permissions attribute */
    private FilePermissions permissions;

    /** Owner attribute */
    private String owner;

    /** Group attribute */
    private String group;

    /**
     * Creates a new SimpleFileAttributes instance with unspecified/null attribute values.
     */
    public SimpleFileAttributes() {
    }


    /**
     * Creates a new SimpleFileAttributes instance whose attributes are set to those of the given AbstractFile.
     * Note that the path attribute is set to the file's {@link com.mucommander.commons.file.AbstractFile#getAbsolutePath() absolute path}.
     *
     * @param file the file from which to fetch the attribute values
     */
    public SimpleFileAttributes(AbstractFile file) {
        setPath(file.getAbsolutePath());
        setExists(file.exists());
        setDate(file.getDate());
        setSize(file.getSize());
        setDirectory(file.isDirectory());
        setPermissions(file.getPermissions());
        setOwner(file.getOwner());
        setGroup(file.getGroup());
    }


    //////////////////////////////////////////
    // MutableFileAttributes implementation //
    //////////////////////////////////////////

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean exists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public FilePermissions getPermissions() {
        return permissions;
    }

    public void setPermissions(FilePermissions permissions) {
        this.permissions = permissions;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
