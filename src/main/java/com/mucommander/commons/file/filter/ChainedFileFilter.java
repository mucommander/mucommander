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
public abstract class ChainedFileFilter extends AbstractFileFilter {

    /** List of registered FileFilter */
    protected Vector<FileFilter> filters = new Vector<FileFilter>();

    /**
     * Creates a new <code>ChainedFileFilter</code> operating in non-inverted mode and containing the specified filters,
     * if any.
     *
     * @param filters filters to add to this chained filter.
     */
    public ChainedFileFilter(FileFilter... filters) {
        this(false, filters);
    }

    /**
     * Creates a new <code>ChainedFileFilter</code> operating in the specified mode and containing the specified filters,
     * if any.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     * @param filters filters to add to this chained filter.
     */
    public ChainedFileFilter(boolean inverted, FileFilter... filters) {
        super(inverted);

        for(FileFilter filter : filters)
            addFileFilter(filter);
    }

    /**
     * Adds a new {@link FileFilter} to the list of chained filters.
     *
     * @param filter the FileFilter to add
     */
    public void addFileFilter(FileFilter filter) {
        filters.add(filter);
    }

    /**
     * Removes a {@link FileFilter} from the list of chained filters. Does nothing if the given <code>FileFilter</code>
     * is not contained by this <code>ChainedFileFilter</code>.
     *
     * @param filter the FileFilter to remove
     */
    public void removeFileFilter(FileFilter filter) {
        filters.remove(filter);
    }

    /**
     * Returns an <code>Iterator</code> that traverses all the registered filters.
     *
     * @return an <code>Iterator</code> that traverses all the registered filters. 
     */
    public Iterator<FileFilter> getFileFilterIterator() {
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
