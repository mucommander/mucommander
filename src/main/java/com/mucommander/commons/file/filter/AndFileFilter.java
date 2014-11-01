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
 * AndFileFilter is a {@link ChainedFileFilter} that matches a file if all of its registered filters match it.
 *
 * @author Maxence Bernard
 */
public class AndFileFilter extends ChainedFileFilter {

    /**
     * Creates a new <code>AndFileFilter</code> operating in non-inverted mode and containing the specified filters,
     * if any.
     *
     * @param filters filters to add to this chained filter.
     */
    public AndFileFilter(FileFilter... filters) {
        this(false, filters);
    }

    /**
     * Creates a new <code>AndFileFilter</code> operating in the specified mode and containing the specified filters,
     * if any.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     * @param filters filters to add to this chained filter.
     */
    public AndFileFilter(boolean inverted, FileFilter... filters) {
        super(inverted, filters);
    }


    
    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    /**
     * Calls {@link #match(com.mucommander.commons.file.AbstractFile)} on each of the registered filters, and returns
     * <code>true</code> if all of them matched the given file, <code>false</code> if one of them didn't.
     *
     * <p>If this {@link ChainedFileFilter} contains no filter, this method will always return <code>true</code>.</p>
     *
     * @param file the file to test against the registered filters
     * @return if the file was matched by all filters, false if one of them didn't 
     */
    public boolean accept(AbstractFile file) {
        int nbFilters = filters.size();

        for(int i=0; i<nbFilters; i++)
            if(!filters.elementAt(i).match(file))
                return false;
        return true;
    }
}
