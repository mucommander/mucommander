/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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

package com.mucommander.file.impl.iso;

import com.mucommander.io.RandomAccessInputStream;
import com.mucommander.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * <code>IsoEntryInputStream</code> allows to reads an ISO entry.
 *
 * @author Xavier Martin
 */
// WIP rewrite it cleanly for cooked
class IsoEntryInputStream extends InputStream {

    private RandomAccessInputStream rais;
    private int pos;
    private long size;
    private boolean cooked;

    IsoEntryInputStream(RandomAccessInputStream rais, IsoArchiveEntry entry) throws IOException {
        this.rais = rais;
        this.size = entry.getSize();
        this.pos = 0;
        this.cooked = entry.isCooked();
        rais.seek(IsoParser.sector(entry.getIndex(), cooked));
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////
    
    public int read() throws IOException {
        return rais.read();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len) throws IOException {
        int available = available();
        int toRead = len;

        if (toRead > available) {
            toRead = available;
        }

        if (available == 0) {
            return -1;
        }

        int ret;
        if (cooked) {
            // atm it work because it's called for 8192 (2048 * 4)
            int full = toRead >> 11;
            int half = toRead % 2048;

            int cur = off;
            for (int i = 0; i < full; i++) {
                ret = rais.read(b, cur, 2048);
                if (ret != -1)
                    pos += ret;
                StreamUtils.skipFully(rais, 280 + 24);
                cur += 2048;
            }
            ret = rais.read(b, cur, half);
            if (ret != -1)
                pos += ret;
            ret = toRead;
        } else {
            ret = rais.read(b, off, toRead);
            if (ret != -1)
                pos += ret;
        }
        return ret;
    }

    public int available() throws IOException {
        int available = (int) (size - pos);
        return (available < 0) ? 0 : available;
    }

    public void close() throws IOException {
        rais.close();
    }
}
