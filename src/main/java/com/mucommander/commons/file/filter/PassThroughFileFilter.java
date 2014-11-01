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
 * <code>PassThroughFileFilter</code> is a filter that {@link #accept(com.mucommander.commons.file.AbstractFile) accepts} all
 * files. Depending on the {@link #isInverted() inverted} mode, this filter will match all files or no file at all.
 *
 * @author Maxence Bernard
 */
public class PassThroughFileFilter extends AbstractFileFilter {

    /**
     * Creates a new <code>PassThroughFileFilter</code> operating in non-inverted mode.
     */
    public PassThroughFileFilter() {
        this(false);
    }

    /**
     * Creates a new <code>PassThroughFileFilter</code> operating in the specified mode.
     *
     * @param inverted if true, this filter will operate in inverted mode.
     */
    public PassThroughFileFilter(boolean inverted) {
        super(inverted);
    }


    ///////////////////////////////
    // FileFilter implementation //
    ///////////////////////////////

    public boolean accept(AbstractFile file) {
        return true;
    }
}
