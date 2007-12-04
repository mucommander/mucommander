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

import java.util.Iterator;
import java.util.Vector;

/**
 * ChainedFileFilter combines one or several {@link FileFilter} to act as just one.
 *{@link #addFileFilter(FileFilter)} and {@link #removeFileFilter(FileFilter)} allow to add or remove a
 * <code>FileFilter</code>, {@link #getFileFilterIterator()} to iterate through all the registered filters.
 *
 * <p>The {@link AndFileFilter} and {@link OrFileFilter} implementations match files that respectively match all of
 * the registered filters, or any of them</p>.
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
     *
     * @return an <code>Iterator</code> that traverses all the registered filters. 
     */
    public synchronized Iterator getFileFilterIterator() {
        return filters.iterator();
    }

    /**
     * Returns <code>true</code> if this chained filter doesn't contain any file filter.
     *
     * @return <code>true</code> if this chained filter doesn't contain any file filter.
     */
    public boolean isEmpty() {
        return filters.isEmpty();
    }
}
