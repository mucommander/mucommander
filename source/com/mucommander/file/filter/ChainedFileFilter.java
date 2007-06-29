/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.file.filter;

import java.util.Iterator;
import java.util.Vector;

/**
 * ChainedFileFilter combines several {@link FileFilter} and acts as just one. The {@link AndFileFilter} and
 * {@link OrFileFilter} implementations allow to accept files that respectively match all of the registered
 * <code>FileFilter</code>, or any one of them.
 *
 * <p>Use the {@link #addFileFilter(FileFilter)} and {@link #removeFileFilter(FileFilter)} to respectively add or
 * remove a <code>FileFilter</code>, and {@link #getFileFilterIterator()} to iterate through all the registered filters.
 *
 * @see AndFileFilter
 * @see OrFileFilter
 * @author Maxence Bernard
 */
public abstract class ChainedFileFilter extends FileFilter {

    /** List of registered FileFilter */
    protected Vector filters = new Vector();

    /**
     * Creates a new ChainedFileFilter that initially contains no {@link FileFilter}.
     */
    public ChainedFileFilter() {
    }

    /**
     * Adds a new {@link FileFilter} to the list of chained filters.
     *
     * @param filter the FileFilter to add
     */
    public synchronized void addFileFilter(FileFilter filter) {
        filters.add(filter);
    }

    /**
     * Removes a {@link FileFilter} to the list of chained filters. Does nothing if the given <code>FileFilter</code>
     * is not contained by this <code>ChainedFileFilter</code>.
     *
     * @param filter the FileFilter to remove
     */
    public synchronized void removeFileFilter(FileFilter filter) {
        filters.remove(filter);
    }

    /**
     * Returns an <code>Iterator</code> that traverses all the registered filters. 
     */
    public synchronized Iterator getFileFilterIterator() {
        return filters.iterator();
    }

    public synchronized boolean isEmpty() {return filters.isEmpty();}
}
