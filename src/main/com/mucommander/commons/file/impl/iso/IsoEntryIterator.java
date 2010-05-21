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


package com.mucommander.commons.file.impl.iso;

import com.mucommander.commons.file.ArchiveEntry;
import com.mucommander.commons.file.WrapperArchiveEntryIterator;
import com.mucommander.commons.io.RandomAccessInputStream;

import java.io.IOException;
import java.util.Iterator;

/**
 * This class iterates through the entries of an ISO file, and keeps the ISO file's
 * {@link #getRandomAccessInputStream RandomAccessInputStream} so that it doesn't have to be opened each time a
 * new entry is read. {@link #close} closes the stream.
 *
 * @author Maxence Bernard
 */
class IsoEntryIterator extends WrapperArchiveEntryIterator {

    /**
     * The ISO file's InputStream
     */
    private RandomAccessInputStream rais;

    public IsoEntryIterator(Iterator<? extends ArchiveEntry> iterator, RandomAccessInputStream rais) {
        super(iterator);

        this.rais = rais;
    }

    /**
     * Returns the ISO file's {@link RandomAccessInputStream} that was passed to the constructor.
     *
     * @return the ISO file's {@link RandomAccessInputStream} that was passed to the constructor.
     */
    RandomAccessInputStream getRandomAccessInputStream() {
        return rais;
    }

    /**
     * Closes the ISO file's {@link RandomAccessInputStream} that was passed to the constructor.
     *
     * @throws IOException if an I/O error occurs while closing the stream
     */
    @Override
    public void close() throws IOException {
        rais.close();
    }
}
