/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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
import java.io.OutputStream;

/**
 * An OutputStream that keeps track of the number of bytes that have been written to it.
 * <p>
 * <p>The actual number of bytes can be retrieved from the {@link ByteCounter} instance returned by {@link #getCounter()}.
 * The {@link #CounterOutputStream(OutputStream, ByteCounter)} constructor can be used to specify an existing
 * ByteCounter instance instead of creating a new one. The ByteCounter will always remain accessible, even
 * after this stream has been closed.
 *
 * @author Maxence Bernard
 * @see ByteCounter
 */
public class CounterOutputStream extends OutputStream {

    /**
     * Underlying OutputStream
     */
    private final OutputStream out;

    /**
     * Byte counter
     */
    private final ByteCounter counter;


    /**
     * Creates a new CounterOutputStream using the specified OutputStream. A new {@link ByteCounter} will be created.
     *
     * @param out the underlying OutputStream the data will be written to
     */
    public CounterOutputStream(OutputStream out) {
        this.out = out;
        this.counter = new ByteCounter();
    }

    /**
     * Creates a new CounterOutputStream using the specified OutputStream and {@link ByteCounter}.
     * The provided <code>ByteCounter</code> will NOT be reset, whatever value it contains will be kept.
     *
     * @param out the underlying OutputStream the data will be written to
     */
    public CounterOutputStream(OutputStream out, ByteCounter counter) {
        this.out = out;
        this.counter = counter;
    }


    /**
     * Returns the ByteCounter that holds the number of bytes that have been written to this OutputStream.
     */
    public ByteCounter getCounter() {
        return this.counter;
    }


    /////////////////////////////////
    // OutputStream implementation //
    /////////////////////////////////

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        counter.add(1);
    }

    @Override
    public void write(byte b[]) throws IOException {
        out.write(b);
        counter.add(b.length);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        counter.add(len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
