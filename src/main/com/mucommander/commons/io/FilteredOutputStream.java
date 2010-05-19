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
import java.io.OutputStream;

/**
 * This class provides a proper implementation of an OutputStream filter.
 *
 * <p>Unlike <code>java.io.FilterOutputStream</code>, this method delegates all methods to an underlying OutputStream
 * and nothing more. In particular, {@link #write(byte[])} and {@link #write(byte[], int, int)} do <b>not</b>
 * call {@link #write(int)} repeatedly (very unefficient) but delegate to the corresponding OutputStream methods. This
 * makes this class much safer to use from a performance perspective than <code>java.io.FilteredOutputStream</code>.
 * </p>
 *
 * @author Maxence Bernard
 */
public class FilteredOutputStream extends OutputStream {

    /** The underlying OutputStream to filter */
    protected OutputStream out;

    /**
     * Creates a new FilteredOutputStream that delegates all methods to the provided OutputStream.
     *
     * @param out the underlying OutputStream to filter
     */
    public FilteredOutputStream(OutputStream out) {
        this.out = out;
    }


    /////////////////////////////////
    // OutputStream implementation //
    /////////////////////////////////

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public void write(byte b[]) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
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
