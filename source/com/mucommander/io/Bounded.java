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

package com.mucommander.io;

/**
 * This interface defines methods that are common to bounded streams, whether they be input streams or output streams.
 *
 * @author Maxence Bernard
 * @see BoundedInputStream
 * @see BoundedOutputStream
 */
public interface Bounded {

    /**
     * Returns the total number of bytes that are allowed to be processed (read or written) by the stream,
     * <code>-1</code> if the stream is not bounded.
     *
     * @return the total number of bytes that are allowed to be processed (read or written) by the stream,
     * <code>-1</code> if the stream is not bounded.
     */
    public abstract long getAllowedBytes();

    /**
     * Returns the total number of bytes that have been processed (read or written) by the stream thus far.
     *
     * @return the total number of bytes that have been processed (read or written) by the stream thus far.
     */
    public abstract long getProcessedBytes();

    /**
     * Returns the remaining number of bytes that are allowed to be processed (read or written) by the stream,
     * {@link Long#MAX_VALUE} if this stream is not bounded.
     *
     * @return the remaining number of bytes that are allowed to be processed (read or written) by the stream,
     * {@link Long#MAX_VALUE} if this stream is not bounded.
     */
    public long getRemainingBytes();
}
