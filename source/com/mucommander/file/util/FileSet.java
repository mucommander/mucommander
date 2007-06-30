/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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


package com.mucommander.file.util;

import com.mucommander.file.AbstractFile;

import java.util.Vector;


/**
 * FileSet is a file vector, with an optional base folder attached which is the parent
 * folder of all contained files.
 *
 * @author Maxence Bernard
 */
public class FileSet extends Vector {

    /** Parent folder of all contained files */
    private AbstractFile baseFolder;
	
	
    /**
     * Creates a new empty FileSet.
     */
    public FileSet() {
    }


    /**
     * Creates a new empty FileSet with the specified base folder.
     */
    public FileSet(AbstractFile baseFolder) {
        this.baseFolder = baseFolder;
    }

	
    /**
     * Creates a new empty FileSet with the specified base folder, and adds the given file.
     */
    public FileSet(AbstractFile baseFolder, AbstractFile file) {
        this.baseFolder = baseFolder;
        add(file);
    }

	
    /**
     * Returns the base folder associated with this FileSet, null if there isn't any.
     */
    public AbstractFile getBaseFolder() {
        return baseFolder;
    }
	

    /**
     * Convenience method to avoid casts to AbstractFile.
     */
    public AbstractFile fileAt(int i) {
        return (AbstractFile)super.elementAt(i);
    }
	
	
    /**
     * 
     */
    //	public Object clone() {
    //		return super.clone();
    //	}
}
