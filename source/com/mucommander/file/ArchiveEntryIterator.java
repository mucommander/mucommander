/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
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

import java.io.IOException;

/**
 * This class allows to iterate the entries of an archive. It mimics the behavior of an <code>Iterator</code> and
 * provides similar hasNext/next methods.
 * <br>
 * The biggest difference over <code>Iterator</code> is that its methods are allowed to throw <code>IOException</code>.
 * This allows implementations to traverse the archive one entry after the other report errors as they occur.
 * <br>
 * It also adds a {@link #close()} method that needs to be called when the iterator is not needed anymore.
 *
 * @see com.mucommander.file.SingleArchiveEntryIterator
 * @see com.mucommander.file.WrapperArchiveEntryIterator
 * @author Maxence Bernard
 */
public interface ArchiveEntryIterator {

    /**
     * Returns <code>true</code> if this iterator has a next entry.
     *
     * @return <code>true</code> if this iterator has a next entry
     * @throws IOException if an error occurred while reading the archive, either because the archive is corrupt or
     * because of an I/O error
     */
    public boolean hasNextEntry() throws IOException;

    /**
     * Returns the next entry in this iterator, <code>null</code> if this iterator has no more entries.
     *
     * @return <code>true</code> if this iterator has a next entry
     * @throws IOException if an error occurred while reading the archive, either because the archive is corrupt or
     * because of an I/O error
     */
    public ArchiveEntry nextEntry() throws IOException;

    /**
     * Closes this iterator and releases all the resources it holds. This method must be called when this iterator
     * is not needed anymore.
     *
     * @throws IOException if an error occurred while closing the resources
     */
    public void close() throws IOException;
}
