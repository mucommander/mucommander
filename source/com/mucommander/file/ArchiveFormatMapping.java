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

package com.mucommander.file;

import com.mucommander.file.filter.FilenameFilter;

import java.beans.IntrospectionException;
import java.lang.reflect.Constructor;

/**
 * ArchiveFormatMapping maps a {@link FilenameFilter} characterizing the archive format onto an {@link AbstractArchiveFile}
 * class. This class can be used with {@link FileFactory} to register and unregister archive file formats at runtime.
 *
 * @see FileFactory
 * @see AbstractArchiveFile
 * @see FilenameFilter
 * @author Maxence Bernard
 */
public class ArchiveFormatMapping {

    // This fields have package access to allow FileFactory to access them directly, a little faster than using the
    // accessor methods

    /** Used to create instances of AbstractArchiveFile. */
    ArchiveFormatProvider provider;
    /** the archive filter associated with the provider class */
    FilenameFilter        filter;

    public ArchiveFormatMapping(ArchiveFormatProvider provider, FilenameFilter filter) {
        this.filter   = filter;
        this.provider = provider;
    }

    /**
     * Returns the FilenameFilter that characterizes the archive format associated with the provider class. 
     */
    public FilenameFilter getFilenameFilter() {
        return filter;
    }

    public ArchiveFormatProvider getProvider() {return provider;}

    /**
     * Returns <code>true</code> if the given Object is a ArchiveFormatMapping instance with the same FilenameFilter
     * and AbstractArchiveFile classes.
     */
    public boolean equals(Object o) {
        if(!(o instanceof ArchiveFormatMapping))
            return false;

        ArchiveFormatMapping afm = (ArchiveFormatMapping)o;
        return afm.filter.equals(filter) && afm.provider.equals(provider);
    }
}
