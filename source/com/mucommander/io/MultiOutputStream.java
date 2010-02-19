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
import java.util.Enumeration;
import java.util.Vector;

/**
 * <code>MultiOutputStream</code> 'multiplies' a stream by forwarding the data that is written to it to several
 * registered output streams. Similarily, {@link #flush()} and {@link #close()} call the same method on all registered
 * output streams.
 * Until one or more OutputStream is registered, this stream acts as a sink: all OutputStream operations are no-ops.
 *
 * @author Maxence Bernard
 */
public class MultiOutputStream extends OutputStream {

    /** Registered OutputStreams */
    protected Vector<OutputStream> streams = new Vector<OutputStream>();

    /**
     * Creates a new MultiOutputStream that initially contains no OutputStream. * Until one or more OutputStream is
     * registered, this stream acts as a sink: all OutputStream operations are no-ops.
     */
    public MultiOutputStream() {
    }

    /**
     * Adds an <code>OutputStream</code> to the list of destination output streams. This method doesn't check whether
     * the specified stream already exists in the list, so the same output stream may be added several times.
     *
     * @param out the OutputStream to add
     */
    public synchronized void addOutputStream(OutputStream out) {
        streams.add(out);
    }

    /**
     * Removes the first occurrence of the given <code>OutputStream</code> from the list of destination output streams.
     * If the same stream was added several times, this method has to be called that many times to remove it entirely.
     *
     * @param out the OutputStream to add
     */
    public synchronized void removeOutputStream(OutputStream out) {
        streams.remove(out);
    }

    /**
     * Returns <code>true</code> if the specified stream is present in the list of destination output streams.
     *
     * @param out the OutputStream to look for
     * @return <code>true</code> if the specified stream is present in the list of destination output streams
     */
    public synchronized boolean containsOutputStream(OutputStream out) {
        return streams.contains(out);
    }

    /**
     * Returns an {@link java.util.Enumeration} of the destination output streams. This instance should be synchronized
     * externally to ensure that the list of streams is not modified while it is being enumerated.
     *
     * @return an {@link java.util.Enumeration} of the destination output streams
     */
    public synchronized Enumeration<OutputStream> enumOutputStream() {
        return streams.elements();
    }


    /////////////////////////////////
    // OutputStream implementation //
    /////////////////////////////////

    /**
     * Calls <code>write(b)</code> with the specified byte on each of the destination output streams.
     * <p>
     * Any IOException thrown by any of the destination output stream is immediately re-thrown, aborting the write
     * as a whole.
     * </p>
     */
    @Override
    public synchronized void write(int b) throws IOException {
        Enumeration<OutputStream> elements = streams.elements();
        while(elements.hasMoreElements())
            elements.nextElement().write(b);
    }

    /**
     * Calls <code>write(b,off,len)</code> with the specified parameters on each of the destination output streams.
     * <p>
     * Any IOException thrown by any of the destination output stream is immediately re-thrown, aborting the operation
     * as a whole.
     * </p>
     */
    @Override
    public synchronized void write(byte b[], int off, int len) throws IOException {
        Enumeration<OutputStream> elements = streams.elements();
        while(elements.hasMoreElements())
            elements.nextElement().write(b, off, len);
    }

    /**
     * Calls <code>write(b)</code> with the specified parameter on each of the destination output streams.
     * <p>
     * Any IOException thrown by any of the destination output stream is immediately re-thrown, aborting the operation
     * as a whole.
     * </p>
     */
    @Override
    public synchronized void write(byte b[]) throws IOException {
        Enumeration<OutputStream> elements = streams.elements();
        while(elements.hasMoreElements())
            elements.nextElement().write(b);
    }

    /**
     * Calls <code>flush</code> on each of the destination output streams.
     * <p>
     * Any IOException thrown by any of the destination output stream is immediately re-thrown, aborting the operation
     * as a whole.
     * </p>
     */
    @Override
    public synchronized void flush() throws IOException {
        Enumeration<OutputStream> elements = streams.elements();
        while(elements.hasMoreElements())
            elements.nextElement().flush();
    }

    /**
     * Calls <code>close</code> on each of the destination output streams.
     * <p>
     * Any IOException thrown by any of the destination output stream is immediately re-thrown, aborting the operation
     * as a whole.
     * </p>
     */
    @Override
    public void close() throws IOException {
        Enumeration<OutputStream> elements = streams.elements();
        while(elements.hasMoreElements())
            elements.nextElement().close();
    }
}
