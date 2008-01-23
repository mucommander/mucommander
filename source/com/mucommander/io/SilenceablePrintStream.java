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
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * SilenceablePrintStream is as the name implies a proxy PrintStream that can be silenced.
 * The {@link #setSilenced(boolean)} method allows to control whether the data written to the stream should go through
 * (be written to the underlying stream) or be ignored.
 *
 * @author Maxence Bernard
 */
public class SilenceablePrintStream extends PrintStream {

    /** When true, write and print methods are no-op */
    private boolean silenced;

    /**
     * Creates a new SilenceablePrintStream that forwards written data to the specified <code>OutputStream</code>
     * when not silenced. By default, this SilenceablePrintStream is not silenced.
     *
     * @param out the OutputStream to forward the data written to when not silenced  
     */
    public SilenceablePrintStream(OutputStream out) {
        this(out, false);
    }

    /**
     * Creates a new SilenceablePrintStream that forwards written data to the specified <code>OutputStream</code>
     * when not silenced.
     *
     * @param out the OutputStream to forward the data written to when not silenced
     * @param silenced initial silenced state
     */
    public SilenceablePrintStream(OutputStream out, boolean silenced) {
        super(out);
        this.silenced = silenced;
    }

    /**
     * Controls whether the data written to the stream goes through (be written to the underlying stream) or is ignored.
     * If called with <code>false</code>, any further call to <code>write</code> or <code>print</code> methods will be
     * ignored (they become no-op), until this method is called again with <code>false</code>. Note that un-silencing
     * this stream will not print messages that were previously written while the stream was silenced.
     *
     * @param silenced if <code>true</code>, <code>write</code> and <code>print</code> methods will become no-op, if
     * <code>false</code> data written
     */
    public void setSilenced(boolean silenced) {
        this.silenced = silenced;
    }

    /**
     * Returns <code>true</code> if this <code>SilenceablePrintStream</code> is currently ignoring calls to
     * <code>write</code> and <code>print</code> methods, <code>false</code> if it is forwarding written data to the
     * underlying <code>OutputStream</code>.
     */
    public boolean isSilenced() {
        return silenced;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public void write(int i) {
        if(silenced)
            return;

        super.write(i);
    }

    public void write(byte[] bytes) throws IOException {
        if(silenced)
            return;

        super.write(bytes);
    }

    public void write(byte[] bytes, int i, int i1) {
        if(silenced)
            return;

        super.write(bytes, i, i1);
    }

    public void print(boolean b) {
        if(silenced)
            return;

        super.print(b);
    }

    public void print(char c) {
        if(silenced)
            return;

        super.print(c);
    }

    public void print(int i) {
        if(silenced)
            return;

        super.print(i);
    }

    public void print(long l) {
        if(silenced)
            return;

        super.print(l);
    }

    public void print(float v) {
        if(silenced)
            return;

        super.print(v);
    }

    public void print(double v) {
        if(silenced)
            return;

        super.print(v);
    }

    public void print(char[] chars) {
        if(silenced)
            return;

        super.print(chars);
    }

    public void print(String string) {
        if(silenced)
            return;

        super.print(string);
    }

    public void print(Object object) {
        if(silenced)
            return;

        super.print(object);
    }

    public void println() {
        if(silenced)
            return;

        super.println();
    }

    public void println(boolean b) {
        if(silenced)
            return;

        super.println(b);
    }

    public void println(char c) {
        if(silenced)
            return;

        super.println(c);
    }

    public void println(int i) {
        if(silenced)
            return;

        super.println(i);
    }

    public void println(long l) {
        if(silenced)
            return;

        super.println(l);
    }

    public void println(float v) {
        if(silenced)
            return;

        super.println(v);
    }

    public void println(double v) {
        if(silenced)
            return;

        super.println(v);
    }

    public void println(char[] chars) {
        if(silenced)
            return;

        super.println(chars);
    }

    public void println(String string) {
        if(silenced)
            return;

        super.println(string);
    }

    public void println(Object object) {
        if(silenced)
            return;

        super.println(object);
    }
}
