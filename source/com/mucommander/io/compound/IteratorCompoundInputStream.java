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

import java.io.InputStream;
import java.util.Iterator;

/**
 * A <code>CompoundInputStream</code> implementation using an {@link Iterator} to implement {@link #getNextInputStream()}.
 *
 * @author Maxence Bernard
 */
public class IteratorCompoundInputStream extends CompoundInputStream {

    /** Iterator containing the InputStreams to be concatenated */
    private Iterator<? extends InputStream> inputStreamIterator;

    /**
     * Creates a new compound input stream using the {@link InputStream} instances contained by the given
     * {@link Iterator} and the specified mode.
     *
     * @param inputStreamIterator an Iterator that contains the {@link InputStream} instances to be used
     * by this <code>CompoundInputStream</code>.
     * @param merged <code>true</code> if the streams should be merged, acting as a single stream, or considered
     * as separate streams that have to be {@link #advanceInputStream() advanced manually}.
     */
    public IteratorCompoundInputStream(Iterator<? extends InputStream> inputStreamIterator, boolean merged) {
        super(merged);

        this.inputStreamIterator = inputStreamIterator;
    }


    ////////////////////////////////////////
    // CompoundInputStream implementation //
    ////////////////////////////////////////

    @Override
    public InputStream getNextInputStream() {
        return inputStreamIterator.hasNext()?inputStreamIterator.next():null;
    }
}
