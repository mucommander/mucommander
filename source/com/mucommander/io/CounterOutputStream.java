/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package com.mucommander.io;

import java.io.IOException;
import java.io.OutputStream;


/**
 * An OutputStream that keeps track of the number of bytes that have been written to it.
 *
 * <p>The actual number of bytes can be retrieved from the {@link ByteCounter} instance returned by {@link #getCounter()}.
 * The {@link #CounterOutputStream(OutputStream, ByteCounter)} constructor can be used to specify an existing
 * ByteCounter instance instead of creating a new one. The ByteCounter will always remain accessible, even
 * after this stream has been closed.
 *
 * @see ByteCounter
 * @author Maxence Bernard
 */
public class CounterOutputStream extends OutputStream {

    /** Underlying OutputStream */
    private OutputStream out;

    /** Byte counter */
    private ByteCounter counter;

    
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

    public void write(int b) throws IOException {
        out.write(b);
        counter.add(1);
    }

    public void write(byte b[]) throws IOException {
        out.write(b);
        counter.add(b.length);
    }
    
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        counter.add(len);
    }
    
    public void flush() throws IOException {
        out.flush();
    }

    public void close() throws IOException {
        out.close();
    }
}