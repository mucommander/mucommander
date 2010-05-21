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


package com.mucommander.commons.file;

import com.mucommander.commons.file.filter.FilenameFilter;

import java.io.IOException;

/**
 * This interface allows {@link FileFactory} to instantiate {@link AbstractArchiveFile} implementations and associate
 * them with the filenames matched by a {@link FilenameFilter}.
 * <p>
 * For {@link AbstractArchiveFile} implementations to be automatically instantiated by {@link FileFactory},
 * this interface needs to be implemented and an instance registered with {@link FileFactory}.
 * </p>
 *
 * @author Nicolas Rinaudo, Maxence Bernard
 * @see AbstractArchiveFile
 * @see FileFactory
 */
public interface ArchiveFormatProvider {

    /**
     * Creates a new instance of <code>AbstractArchiveFile</code> .
     *
     * @param  file        file to map as an <code>AbstractArchiveFile</code>.
     * @return             a new instance of <code>AbstractArchiveFile</code> that matches the specified URL.
     * @throws IOException if an error occurs.
     */
    public AbstractArchiveFile getFile(AbstractFile file) throws IOException;


    /**
     * Returns the <code>FilenameFilter</code> that matches filenames to be associated with this archive format.
     *
     * @return the <code>FilenameFilter</code> that matches filenames to be associated with this archive format
     */
    public FilenameFilter getFilenameFilter();
}
