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
 * <code>OrFileFilter</code> is a {@link ChainedFileFilter} that matches a file if one of its registered filters 
 * matches it.
 *
 * @author Maxence Bernard
 */
public class OrFileFilter extends ChainedFileFilter {

    /**
     * Creates a new <code>OrFileFilter</code> operating in non-inverted mode and containing the specified filters,
     * if any.
     *
     * @param filters filters to add to this chained filter.
     */
    public OrFileFilter(FileFilter... filters) {
        this(false, filters);
    }

    /**
     * Creates a new <code>OrFileFilter</code> operating in the specified mode and containing the specified filters,
     * if any.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     * @param filters filters to add to this chained filter.
     */
    public OrFileFilter(boolean inverted, FileFilter... filters) {
        super(inverted, filters);
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    /**
     * Calls {@link #match(com.mucommander.commons.file.AbstractFile)} on each of the registered filters, and returns
     * <code>true</code> if one of them matched the given file, <code>false</code> if none of them did.
     *
     * <p>If this {@link ChainedFileFilter} contains no filter, this method will always return <code>true</code>.</p>
     *
     * @param file the file to test against the registered filters
     * @return if the file was matched by one filter, false if none of them did
     */
    public boolean accept(AbstractFile file) {
        int nbFilters = filters.size();

        if(nbFilters==0)
            return true;

        for(int i=0; i<nbFilters; i++)
            if((filters.elementAt(i)).match(file))
                return true;

        return false;
    }
}
