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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>SilenceableOutStream</code> is a {@link FilterOutputStream} that forwards data to an underlying
 * {@link OutputStream} and that can be silenced on demand.
 * The {@link #setSilenced(boolean)} method allows to control whether the data written to the stream should go through
 * (be written to the underlying stream) or be discarded.
 *
 * @author Maxence Bernard
 */
public class SilenceableOutputStream extends FilterOutputStream {

    /** When true, write methods are no-op */
    private boolean silenced;


    /**
     * Creates a new <code>SilenceableOutputStream</code> that forwards written data to the specified
     * <code>OutputStream</code> when not silenced. By default, this <code>SilenceableOutputStream</code> is not
     * silenced.
     *
     * @param out the OutputStream to forward the data written to when not silenced  
     */
    public SilenceableOutputStream(OutputStream out) {
        super(out);
    }

    /**
     * Creates a new <code>SilenceableOutputStream</code> that forwards written data to the specified
     * <code>OutputStream</code> when not silenced.
     *
     * @param out the OutputStream to forward the data written to when not silenced
     * @param silenced initial silenced state
     */
    public SilenceableOutputStream(OutputStream out, boolean silenced) {
        super(out);
        this.silenced = silenced;
    }

    /**
     * Controls whether the data written to the stream goes through (be written to the underlying stream) or
     * discarded. If called with <code>false</code>, any subsequent call to <code>write</code> methods will be
     * ignored (they become no-op), until this method is called again with <code>false</code>. Note that un-silencing
     * this stream will not print messages that were previously written while the stream was silenced.
     *
     * @param silenced <code>true</code> to have <code>write</code> methods become no-ops, <code>false</code> to have
     * them forward the data to the underlying <code>OutputStream</code>
     */
    public void setSilenced(boolean silenced) {
        this.silenced = silenced;
    }

    /**
     * Returns <code>true</code> if this <code>SilenceableOutStream</code> is currently ignoring calls to
     * <code>write</code> methods, <code>false</code> if it is forwarding written data to the
     * underlying <code>OutputStream</code>.
     *
     * @return <code>true</code> if this <code>SilenceableOutStream</code> is currently ignoring calls to
     * <code>write</code>, <code>false</code> if it is forwarding written data to the
     * underlying <code>OutputStream</code>.
     */
    public boolean isSilenced() {
        return silenced;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void write(int b) throws IOException {
        if(silenced)
            return;

        out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if(silenced)
            return;

        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if(silenced)
            return;

        out.write(b, off, len);
    }
}
