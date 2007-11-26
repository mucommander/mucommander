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

package com.mucommander.file.filter;

import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.AbstractFile;

/**
 * <code>AttributeFileFilter</code> matches files which have a specific attribute set.
 * Here's a list of supported file attributes:
 * <ul>
 *   <li>{@link #DIRECTORY}</li>
 *   <li>{@link #FILE}</li>
 *   <li>{@link #BROWSABLE}</li>
 *   <li>{@link #ARCHIVE}</li>
 *   <li>{@link #SYMLINK}</li>
 *   <li>{@link #HIDDEN}</li>
 *   <li>{@link #ROOT}</li>
 * </ul>
 *
 * <p>Only one attribute can be matched at a time. To match several attributes, combine them using a
 * {@link com.mucommander.file.filter.ChainedFileFilter}.</p>
 *
 * @author Maxence Bernard
 */
public class AttributeFileFilter extends FileFilter {

    /** Tests if the file is a directory, cf {@link com.mucommander.file.AbstractFile#isDirectory()}. */
    public static final int DIRECTORY = 0;

    /** Tests if the file is a regular file, i.e. not a directory. Note that this is equivalent to inverting {@link #DIRECTORY}. */
    public static final int FILE      = 1;

    /** Tests if the file is browsable, cf {@link com.mucommander.file.AbstractFile#isBrowsable()}. */
    public static final int BROWSABLE = 2;

    /** Tests if the file is an archive. */
    public static final int ARCHIVE   = 3;

    /** Tests if the file is a symlink, cf {@link com.mucommander.file.AbstractFile#isSymlink()}. */
    public static final int SYMLINK   = 4;

    /** Tests if the file is hidden, cf {@link com.mucommander.file.AbstractFile#isHidden()}. */
    public static final int HIDDEN    = 5;

    /** Tests if the file is a root folder, cf {@link com.mucommander.file.AbstractFile#isRoot()}. */
    public static final int ROOT      = 6;


    /** The attribute to test files against */
    private int attribute;


    /**
     * Creates a new <code>AttributeFileFilter</code> that matches files which have the specified attribute set.
     * This filter will operate in normal, non-inverted mode.
     *
     * @param attribute the attribute to test files against
     */
    public AttributeFileFilter(int attribute) {
        this(attribute, false);
    }

    /**
     * Creates a new <code>AttributeFileFilter</code> that matches files which have the specified attribute set.
     * This filter operates in the specified mode.
     *
     * @param attribute the attribute to test files against
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public AttributeFileFilter(int attribute, boolean inverted) {
        super(inverted);
        this.attribute = attribute;
    }


    /**
     * Returns the attribute which files are tested against.
     *
     * @return the attribute which files are tested against.
     */
    public int getAttribute() {
        return attribute;
    }

    /**
     * Sets the attribute which files are tested against.
     *
     * @param attribute the attribute which files are tested against.
     */
    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean accept(AbstractFile file) {
        switch(attribute) {
            case DIRECTORY:
                return file.isDirectory();
            case FILE:
                return !file.isDirectory();
            case BROWSABLE:
                return file.isBrowsable();
            case ARCHIVE:
                return file.hasAncestor(AbstractArchiveFile.class);
            case SYMLINK:
                return file.isSymlink();
            case HIDDEN:
                return file.isHidden();
            case ROOT:
                return file.isRoot();
        }
        return true;
    }
}

