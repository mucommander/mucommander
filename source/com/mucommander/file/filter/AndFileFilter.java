package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;

/**
 * @author Maxence Bernard
 */
public class AndFileFilter extends ChainedFileFilter {

    public AndFileFilter() {
    }

    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public synchronized boolean accept(AbstractFile file) {
        int nbFilters = filters.size();

        for(int i=0; i<nbFilters; i++)
            if(!((FileFilter)filters.elementAt(i)).accept(file))
                return false;

        return true;
    }
}
