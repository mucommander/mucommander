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

import com.mucommander.commons.file.AbstractFile;

/**
 * @author Xavier Martin
 */
class IsoUtil {
    static final int MODE1_2048 = 2048;
    static final int MODE2_2352 = 2352;
    static final int MODE2_2336 = 2336;

    static final int MODE2_2352_skip = 24;
    static final int MODE2_2336_skip = 8;

    static final int MODE2_EC_skip = 280;

    static final int WAV_header = 44;

    public static int guessSectorSize(AbstractFile file) {
        int sectSize = MODE1_2048;

        // cooked images are usually 2352 bytes/sector, so it's 1st check here
        if (file.getSize() % MODE2_2352 == 0)
            sectSize = MODE2_2352;
        else if (file.getSize() % MODE2_2336 == 0)
            sectSize = MODE2_2336;

        return sectSize;
    }

    public static int toDwordBE(byte p[], int offset) {
        return ((p[offset] & 0xff)
                | ((p[1 + offset] & 0xff) << 8)
                | ((p[2 + offset] & 0xff) << 16)
                | ((p[3 + offset] & 0xff) << 24));
    }

    public static int toDword(byte p[], int offset) {
        return ((p[3 + offset] & 0xff)
                | ((p[2 + offset] & 0xff) << 8)
                | ((p[1 + offset] & 0xff) << 16)
                | ((p[offset] & 0xff) << 24));
    }

    public static void toArray(int value, byte b[], int offset) {
        b[offset] = (byte) (value & 0xff);
        b[offset + 1] = (byte) ((value & 0xff00) >> 8);
        b[offset + 2] = (byte) ((value & 0xff0000) >> 16);
        b[offset + 3] = (byte) ((value & 0xff000000) >> 24);
    }

    public static long offsetInSector(long index, int sectSize, boolean audio) {
        switch (sectSize) {
            case MODE2_2352:
                return (index * 2352) + (audio ? 0 : 24);
            case MODE2_2336:
                return (index * 2336) + 8;
        }
        return index << 11;
    }
}