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
}
