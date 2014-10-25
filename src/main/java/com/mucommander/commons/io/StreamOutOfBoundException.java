/*
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

package com.mucommander.commons.io;

import java.io.IOException;

/**
 * This <code>IOException</code> can be used when attempting to read from a {@link BoundedInputStream} or
 * {@link BoundedReader} beyond the byte or character limit set.
 *
 * @see com.mucommander.commons.io.BoundedInputStream
 * @see com.mucommander.commons.io.BoundedOutputStream
 * @see com.mucommander.commons.io.BoundedReader
 * @author Maxence Bernard
 */
public class StreamOutOfBoundException extends IOException {
    
    public StreamOutOfBoundException(long limit) {
        super("Attempt to read out of bounds, limit="+limit);
    }
}
