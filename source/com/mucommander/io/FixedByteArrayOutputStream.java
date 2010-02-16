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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>FixedByteArrayOutputStream</code> writes data to a pre-allocated byte array passed to the constructor.
 * <p>
 * This class is similar to {@link ByteArrayOutputStream} except that the byte array is pre-allocated and does not
 * grow when its capacity is reached. Attempts to write more bytes than the array's length will result in
 * {@link ArrayIndexOutOfBoundsException}. To prevent {@link ArrayIndexOutOfBoundsException} from being thrown,
 * a <code>FixedByteArrayOutputStream</code> can be wrapped around a {@link BoundedOutputStream} with a limit set to
 * the byte array's length.
 * </p>
 *
 * @author Maxence Bernard
 */
public class FixedByteArrayOutputStream extends OutputStream {

    private byte[] bytes;
    private int offset;

    public FixedByteArrayOutputStream(byte bytes[]) {
        this(bytes, 0);
    }

    public FixedByteArrayOutputStream(byte bytes[], int offset) {
        this.bytes = bytes;
        this.offset = offset;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public synchronized void write(int b) throws IOException {
        bytes[offset++] = (byte)b;
    }

    @Override
    public synchronized void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        System.arraycopy(b, off, bytes, offset, len);
        offset += len;
    }
}
