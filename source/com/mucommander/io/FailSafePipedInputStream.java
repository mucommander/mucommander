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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * SafePipedInputStream is a <code>PipedInputStream</code> that can be safely interrupted when the thread that
 * pushes data to this stream has encountered an error. This can be done by calling
 * {@link #setExternalFailure(java.io.IOException)} with an <code>IOException</code> that will be thrown by
 * read/skip/available methods thereafter. This method ensures that the <code>IOException</code> will be propagated
 * to any thread that is currently executing a read/skip/available method of this class.
 *
 * <p>This class overcomes a limitation of <code>PipedInputStream</code> whose read/skip/available methods do not
 * throw an <code>IOException</code> when the stream is closed from another thread in the midst of their execution.
 * </p>
 *
 * @author Maxence Bernard
 */
public class FailSafePipedInputStream extends PipedInputStream {

    /** An IOException to be thrown by read/skip/available methods */
    private volatile IOException failure;


    public FailSafePipedInputStream() {
        super();
    }

    public FailSafePipedInputStream(int pipeSize) {
        super(pipeSize);
    }

    public FailSafePipedInputStream(PipedOutputStream src) throws IOException {
        super(src);
    }

    public FailSafePipedInputStream(PipedOutputStream src, int pipeSize) throws IOException {
        super(src, pipeSize);
    }

    /**
     * Sets an IOException to be subsequently thrown by <code>read</code>, <code>skip</code> and
     * <code>available</code> methods. This method calls {@link #close()} to have any other thread blocked in a
     * read/skip/available return immediately and throw the specified exception.
     *
     * @param failure the IOException to be thrown by read, skip and available methods, <code>null</code> for none
     */
    public void setExternalFailure(IOException failure) {
        this.failure = failure;

        if(failure!=null) {
            // Close the PipedInputStream to have any other thread blocked in a read/skip/available return immediately
            try {
                super.close();
            }
            catch(IOException e) {
                // Swallow the exception
            }
        }
    }

    /**
     * Checks whether an external failure (IOException) has been registered and if has, throws it.
     *
     * @throws java.io.IOException if an external failure has been registered
     */
    protected void checkExternalFailure() throws IOException {
        if(failure!=null)
            throw failure;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public synchronized int read() throws IOException {
        int ret = super.read();

        checkExternalFailure();

        return ret;
    }

    @Override
    public synchronized int read(byte b[], int off, int len) throws IOException {
        int ret = super.read(b, off, len);

        checkExternalFailure();

        return ret;
    }

    @Override
    public synchronized int read(byte b[]) throws IOException {
        int ret = super.read(b);

        checkExternalFailure();

        return ret;
    }


    @Override
    public long skip(long n) throws IOException {
        long ret = super.skip(n);

        checkExternalFailure();

        return ret;
    }

    @Override
    public synchronized int available() throws IOException {
        int ret = super.available();

        checkExternalFailure();

        return ret;
    }

    @Override
    public void close() throws IOException {
        super.close();

        checkExternalFailure();
    }
}
