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

import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>BoundedOutputStream</code> is an <code>OutputStream</code> that has a set limit to the number of bytes that can
 * be written to it. When that limit is reached, <code>write</code> methods throw a {@link StreamOutOfBoundException}.
 *
 * @author Maxence Bernard
 * @see BoundedInputStream
 * @see StreamOutOfBoundException
 */
public class BoundedOutputStream extends FilteredOutputStream implements Bounded {

    protected long totalWritten;
    protected long allowedBytes;

    /**
     * Creates a new <code>BoundedInputStream</code> over the specified stream, allowing a maximum of
     * <code>allowedBytes</code> to be written to it. If <code>allowedBytes</code> is equal to <code>-1</code>, this
     * stream is not bounded and acts as a normal stream.
     *
     * @param out the stream to be bounded
     * @param allowedBytes the total number of bytes that are allowed to written, <code>-1</code> for no limit
     */
    public BoundedOutputStream(OutputStream out, long allowedBytes) {
        super(out);

        this.allowedBytes = allowedBytes;
    }


    ////////////////////////////
    // Bounded implementation //
    ////////////////////////////

    public synchronized long getAllowedBytes() {
        return allowedBytes;
    }

    public synchronized long getProcessedBytes() {
        return totalWritten;
    }

    public synchronized long getRemainingBytes() {
        return allowedBytes<=-1?Long.MAX_VALUE:allowedBytes-totalWritten;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public synchronized void write(int b) throws IOException {
        if(getRemainingBytes()==0)
            throw new StreamOutOfBoundException(allowedBytes);

        out.write(b);
        totalWritten++;
    }

    @Override
    public synchronized void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        int canWrite = (int)Math.min(getRemainingBytes(), len);
        if(canWrite==0)
            throw new StreamOutOfBoundException(allowedBytes);

        out.write(b, off, canWrite);
        totalWritten += canWrite;
    }
}
