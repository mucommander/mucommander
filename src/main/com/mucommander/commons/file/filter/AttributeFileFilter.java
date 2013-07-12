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

package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

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
 * {@link com.mucommander.commons.file.filter.ChainedFileFilter}.</p>
 *
 * @author Maxence Bernard
 */
public class AttributeFileFilter extends AbstractFileFilter {

	public enum FileAttribute {
		/** Tests if the file is a {@link com.mucommander.commons.file.AbstractFile#isDirectory() directory}. */
		DIRECTORY,
		/** Tests if the file is a regular file, i.e. not a directory. This is equivalent to negating {@link #DIRECTORY}. */
		FILE,
		/** Tests if the file is {@link com.mucommander.commons.file.AbstractFile#isBrowsable() browsable}. */
		BROWSABLE,
		/** Tests if the file is an {@link com.mucommander.commons.file.AbstractFile#isArchive() archive}. */
		ARCHIVE,
		/** Tests if the file is a {@link com.mucommander.commons.file.AbstractFile#isSymlink() symlink}. */
		SYMLINK,
		/** Tests if the file is {@link com.mucommander.commons.file.AbstractFile#isHidden() hidden}. */
		HIDDEN,
		/** Tests if the file is a {@link com.mucommander.commons.file.AbstractFile#isRoot() root folder}. */
		ROOT,
		/** Tests if the file is a {@link com.mucommander.commons.file.AbstractFile#isSystem() system file}. */
		SYSTEM;
	}

    /** The attribute to test files against */
    private FileAttribute attribute;


    /**
     * Creates a new <code>AttributeFileFilter</code> matching files that have the specified attribute set and operating
     * in non-inverted mode.
     *
     * @param attribute the attribute to test files against
     */
    public AttributeFileFilter(FileAttribute attribute) {
        this(attribute, false);
    }

    /**
     * Creates a new <code>AttributeFileFilter</code> matching files that have the specified attribute set and operating
     * in the specified mode.
     *
     * @param attribute the attribute to test files against
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public AttributeFileFilter(FileAttribute attribute, boolean inverted) {
        super(inverted);
        this.attribute = attribute;
    }


    /**
     * Returns the attribute which files are tested against.
     *
     * @return the attribute which files are tested against.
     */
    public FileAttribute getAttribute() {
        return attribute;
    }

    /**
     * Sets the attribute which files are tested against.
     *
     * @param attribute the attribute which files are tested against.
     */
    public void setAttribute(FileAttribute attribute) {
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
                return file.isArchive();
            case SYMLINK:
                return file.isSymlink();
            case HIDDEN:
                return file.isHidden();
            case ROOT:
                return file.isRoot();
		    case SYSTEM:
			    return file.isSystem();
		    default:
		    	return true;
        }
    }
}

