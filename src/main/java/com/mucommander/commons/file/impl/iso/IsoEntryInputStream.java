/**
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


package com.mucommander.commons.file.impl.iso;

import com.mucommander.commons.io.RandomAccessInputStream;
import com.mucommander.commons.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * <code>IsoEntryInputStream</code> allows to reads an ISO entry.
 *
 * @author Xavier Martin
 */
class IsoEntryInputStream extends InputStream {

    private RandomAccessInputStream rais;
    private int pos;
    private long size;
    private int sectSize;
    private boolean audio;

    IsoEntryInputStream(RandomAccessInputStream rais, IsoArchiveEntry entry) throws IOException {
        this.rais = rais;
        this.size = entry.getSize();
        this.pos = 0;
        this.sectSize = entry.getSectSize();
        this.audio = entry.getAudio();
        rais.seek(IsoUtil.offsetInSector(entry.getIndex(), sectSize, audio) + entry.getShiftOffset());
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    @Override
    public int read() throws IOException {
        return rais.read();
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
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
        int cur = off;
        int before = pos;
        int skip = 0;

        // on the 1st run : generate a valid wav header
        if (audio && pos == 0) {
            IsoUtil.toArray(0x46464952, b, 0);                         // "RIFF"
            IsoUtil.toArray((int) size - 8, b, 4);                     // size of file - 8
            IsoUtil.toArray(0x45564157, b, 8);                         // "WAVE"
            IsoUtil.toArray(0x20746D66, b, 12);                        // "fmt "
            b[16] = 0x10;                                              // Chunk Data Size
            IsoUtil.toArray(0x00020001, b, 20);                        // WAVE type format : PCM header 0100, stereo 0200
            IsoUtil.toArray(0x0000AC44, b, 24);                        // sample rate : 44100hz
            IsoUtil.toArray(0x0002B110, b, 28);                        // bytes/sec : 176400
            IsoUtil.toArray(0x00100004, b, 32);                        // Block alignment 0400  + Bits/sample 1000
            IsoUtil.toArray(0x61746164, b, 36);                        // "data"
            IsoUtil.toArray((int) size - IsoUtil.WAV_header, b, 40);   // size of 'real' data
            cur = IsoUtil.WAV_header;
            toRead -= IsoUtil.WAV_header;
            pos += IsoUtil.WAV_header;
        }

        switch (sectSize) {
            case IsoUtil.MODE2_2336:
                skip = IsoUtil.MODE2_2336_skip;
                break;
            case IsoUtil.MODE2_2352:
                skip = IsoUtil.MODE2_2352_skip;
                break;
            case IsoUtil.MODE1_2048:
                // shortcut
                ret = rais.read(b, off, toRead);
                if (ret != -1)
                    pos += ret;
                return ret;
        }

        // atm it work because it's called for 8192 (2048 * 4)
        int full = toRead >> 11;
        int half = toRead % IsoUtil.MODE1_2048;
        if (full >= 1) {
            for (int i = 0; i < full; i++) {
                ret = rais.read(b, cur, IsoUtil.MODE1_2048);
                if (ret != -1)
                    pos += ret;
                if (!audio)
                    StreamUtils.skipFully(rais, IsoUtil.MODE2_EC_skip + skip);
                cur += IsoUtil.MODE1_2048;
            }
            ret = rais.read(b, cur, half);
            if (ret != -1)
                pos += ret;
        } else {
            // in fact it doesn't work for internal viewer, because it's called by chunk of 1024
            ret = rais.read(b, cur, half);
            if (ret != -1)
                pos += ret;
            if (((pos % 2048) == 0) && !audio)
                StreamUtils.skipFully(rais, IsoUtil.MODE2_EC_skip + skip);
        }

        return pos - before;
    }

    @Override
    public int available() throws IOException {
        int available = (int) (size - pos);
        return (available < 0) ? 0 : available;
    }

    @Override
    public void close() throws IOException {
        rais.close();
    }
}