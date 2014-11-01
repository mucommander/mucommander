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



package com.mucommander.commons.file.util;

import com.mucommander.commons.file.AbstractFile;

import java.util.Vector;


/**
 * FileSet is a <code>java.util.Vector</code> of {@link AbstractFile} instances, and an optional base folder which is
 * the folder containing the files.
 *
 * @author Maxence Bernard
 */
public class FileSet extends Vector<AbstractFile> {

    /** The base/parent folder of the files this Vector contains */
    private AbstractFile baseFolder;

    /**
     * Creates a new empty FileSet.
     */
    public FileSet() {
    }

    /**
     * Creates a new empty FileSet with the specified base folder.
     *
     * @param baseFolder the folder containing the files
     */
    public FileSet(AbstractFile baseFolder) {
        this.baseFolder = baseFolder;
    }

    /**
     * Creates a new empty FileSet with the specified base folder.
     *
     * @param baseFolder the folder containing the files
     * @param initialCapacity initial capacity of the vector
     */
    public FileSet(AbstractFile baseFolder, int initialCapacity) {
        super(initialCapacity);

        this.baseFolder = baseFolder;
    }

    /**
     * Creates a new empty FileSet with the specified base folder, and adds the given file.
     *
     * @param baseFolder the folder containing the files
     * @param file the file to add
     */
    public FileSet(AbstractFile baseFolder, AbstractFile file) {
        this.baseFolder = baseFolder;
        add(file);
    }

	
    /**
     * Returns the base folder associated with this FileSet, <code>null</code> if there isn't any.
     *
     * @return the base folder associated with this FileSet, null if there isn't any.
     */
    public AbstractFile getBaseFolder() {
        return baseFolder;
    }
	

    /**
     * Adds all the files in the given array to this FileSet. Does nothing if the specified array is <code>null</code.
     *
     * @param files the files to add to this FileSet.
     */
    public void addAll(AbstractFile[] files) {
        if(files==null)
            return;

        for (AbstractFile file : files)
            add(file);
    }
}
