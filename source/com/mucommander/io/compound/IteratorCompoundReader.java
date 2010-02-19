/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.io.compound;

import java.io.Reader;
import java.util.Iterator;

/**
 * A <code>CompoundReader</code> implementation using an {@link Iterator} to implement {@link #getNextReader()}.
 *
 * @author Maxence Bernard
 */
public class IteratorCompoundReader extends CompoundReader {

    /** Iterator containing the readers to be concatenated */
    private Iterator<? extends Reader> readerIterator;

    /**
     * Creates a new compound reader using the {@link Reader} instances contained by the given
     * {@link Iterator} and the specified mode.
     *
     * @param readerIterator an Iterator that contains the {@link Reader} instances to be used
     * by this <code>CompoundReader</code>.
     * @param merged <code>true</code> if the reader should be merged, acting as a single reader, or considered
     * as separate readers that have to be {@link #advanceReader() advanced manually}.
     */
    public IteratorCompoundReader(Iterator<? extends Reader> readerIterator, boolean merged) {
        super(merged);

        this.readerIterator = readerIterator;
    }


    ///////////////////////////////////
    // CompoundReader implementation //
    ///////////////////////////////////

    @Override
    public Reader getNextReader() {
        return readerIterator.hasNext()?readerIterator.next():null;
    }
}