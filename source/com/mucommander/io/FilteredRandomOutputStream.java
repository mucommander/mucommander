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

/**
 * FilteredRandomOutputStream is a filtered output stream for {@link RandomAccessOutputStream} subclasses, allowing
 * to easily extend the functionality provided by the stream by overriding only a few methods.
 * In a similar way to <code>java.io.FilteredOutputStream</code>, this class delegates all method calls to the
 * specified <code>RandomAccessOutputStream</code>.
 *
 * @author Maxence Bernard
 */
public class FilteredRandomOutputStream extends RandomAccessOutputStream {

    /** The underlying RandomAccessOutputStream */
    protected RandomAccessOutputStream raos;

    
    /**
     * Creates a new FilteredRandomOutputStream that delegates all method calls to the specified
     * <code>RandomAccessOutputStream</code>.
     *
     * @param raos the RandomAccessOutputStream to delegate method calls to
     */
    public FilteredRandomOutputStream(RandomAccessOutputStream raos) {
        this.raos = raos;
    }


    /////////////////////////////////////////////
    // RandomAccessOutputStream implementation //
    /////////////////////////////////////////////
    
    public void write(int b) throws IOException {
        raos.write(b);
    }

    public void write(byte b[]) throws IOException {
        raos.write(b);
    }

    public void write(byte b[], int off, int len) throws IOException {
        raos.write(b, off, len);
    }

    public void setLength(long newLength) throws IOException {
        raos.setLength(newLength);
    }

    public long getOffset() throws IOException {
        return raos.getOffset();
    }

    public long getLength() throws IOException {
        return raos.getLength();
    }

    public void seek(long offset) throws IOException {
        raos.seek(offset);
    }

    public void flush() throws IOException {
        raos.flush();
    }

    public void close() throws IOException {
        try {
            flush();
        }
        catch(IOException e) {
            // Try closing the stream anyway
        }

        raos.close();
    }
}
