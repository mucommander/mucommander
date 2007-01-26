package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;

/**
 * AndFileFilter is a {@link ChainedFileFilter} that must satisfy all of the registered filters'
 * {@link FileFilter#accept(AbstractFile)} methods. If any of those methods returns false, the file will not be accepted.
 *
 * If this {@link ChainedFileFilter} contains no filter, {@link #accept(AbstractFile)} will always return true.
 *
 * @author Maxence Bernard
 */
public class AndFileFilter extends ChainedFileFilter {

    /**
     * Creates a new AndFileFilter that initially contains no {@link FileFilter}.
     */
    public AndFileFilter() {
    }

    
    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    /**
     * Calls the registered filters' {@link FileFilter#accept(AbstractFile)} methods, and returns true if all of them
     * accepted the given AbstractFile (i.e. returned true).  Returns false if one of them rejected the file.
     *
     * <p>If this {@link ChainedFileFilter} contains no filter, true will always be returned.
     *
     * @param file the file to test against the registered filters
     * @return if the file was accepted by all filters, false if it was rejected by one filter 
     */
    public synchronized boolean accept(AbstractFile file) {
        int nbFilters = filters.size();

        for(int i=0; i<nbFilters; i++)
            if(!((FileFilter)filters.elementAt(i)).accept(file))
                return false;

        return true;
    }
}
