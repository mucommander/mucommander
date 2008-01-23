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

package com.mucommander.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that has a set limit to the number of bytes that can be read from it before the EOF is reached.
 * The limit will have no effect if it is higher than the number of remaining bytes in the underlying stream.
 *
 * <p>This class is particularly useful for reading archive formats which contain concatenated files.
 *
 * @author Maxence Bernard
 * @see com.mucommander.file.impl.ar.ArArchiveFile
 */
public class ByteLimitInputStream extends InputStream {

    private InputStream in;
    private long bytesRemaining;

    public ByteLimitInputStream(InputStream in, long size) {
        this.in = in;
        this.bytesRemaining = size;
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    public int read() throws IOException {
        if(bytesRemaining<=0)
            return -1;  // EOF reached

        int i = in.read();
        if(i>0)
            this.bytesRemaining--;

        return i;
    }

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        if(bytesRemaining<=0)
            return -1;  // EOF reached

        int nbRead = in.read(b, off, Math.min(len, (int)this.bytesRemaining));
        if(nbRead>0)
            this.bytesRemaining -= nbRead;

        return nbRead;
    }


    public long skip(long n) throws IOException {
        if(bytesRemaining<=0)
            return -1;  // EOF reached

        long nbSkipped = in.skip(Math.min(n, (int)this.bytesRemaining));
        if(nbSkipped>0)
            this.bytesRemaining -= nbSkipped;

        return nbSkipped;
    }


    public int available() throws IOException {
        return (int)this.bytesRemaining;
    }


    public void close() throws IOException {
        in.close();
    }


    public void mark(int readLimit) {
        in.mark(readLimit);
    }


    public boolean markSupported() {
        return in.markSupported();
    }


    public void reset() throws IOException  {
        in.reset();
    }
}
