package com.mucommander.file.filter;

import java.util.Iterator;
import java.util.Vector;

/**
 * @author Maxence Bernard
 */
public abstract class ChainedFileFilter extends FileFilter {

    protected Vector filters = new Vector();

    public ChainedFileFilter() {
    }

    public synchronized void addFileFilter(FileFilter filter) {
        filters.add(filter);
    }

    public synchronized void removeFileFilter(FileFilter filter) {
        filters.remove(filter);
    }

    public synchronized Iterator getFileFiltersIterator() {
        return filters.iterator();
    }
}
