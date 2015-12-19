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

import java.io.IOException;

/**
 * This class allows to iterate the entries of an archive. It mimics the behavior of an <code>Iterator</code>, with
 * several differences:
 *
 * <ul>
 *   <li>its methods are allowed to throw <code>IOException</code></li>
 *   <li>there is no <code>hasNext</code> method, because it wouldn't map very well onto certain formats that don't know
 * if there is a next entry until the current entry has been consumed.</li>
 *   <li>{@link #close()} needs to be called when the Iterator is not needed anymore, allowing implementations to release
 * any resources that they hold.</li>
 * </ul>
 *
 * @see com.mucommander.commons.file.SingleArchiveEntryIterator
 * @see com.mucommander.commons.file.WrapperArchiveEntryIterator
 * @author Maxence Bernard
 */
public interface ArchiveEntryIterator {

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
