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


package com.mucommander.commons.file.impl.zip.provider;

import java.util.zip.Deflater;

/**
 * Contains the various constants that are used by several classes of this package.
 *
 * @author Maxence Bernard
 */
public interface ZipConstants {

    /**
     * DEFLATED compression method
     */
    public static final int DEFLATED = java.util.zip.ZipEntry.DEFLATED;

    /**
     * STORED compression method (raw storage, no compression)
     */
    public static final int STORED = java.util.zip.ZipEntry.STORED;

    /**
     * Default compression level for DEFLATED compression
     */
    public static final int DEFAULT_DEFLATER_COMPRESSION = Deflater.DEFAULT_COMPRESSION;

    /**
     * Default size of the buffer used by Deflater.
     */
    // /!\ For some unknown reason, using a larger buffer *hurts* performance.
    public static final int DEFAULT_DEFLATER_BUFFER_SIZE = 512;

    /**
     * Maximum size of a Zip32 entry or a Zip32 file as a whole, i.e. (2^32)-1.
     * */
    public static final long MAX_ZIP32_SIZE = 4294967295l;

    /**
     * Size of write buffers
     */
    final static int WRITE_BUFFER_SIZE = 65536;

    /**
     * UTF-8 encoding String
     */
    public final static String UTF_8 = "UTF-8";

    /**
     * Local file header signature
     */
    static final byte[] LFH_SIG = ZipLong.getBytes(0X04034B50L);

    /**
     * Data descriptor signature
     */
    static final byte[] DD_SIG = ZipLong.getBytes(0X08074B50L);

    /**
     * Central file header signature
     */
    static final byte[] CFH_SIG = ZipLong.getBytes(0X02014B50L);

    /**
     * End of central dir signature
     */
    static final byte[] EOCD_SIG = ZipLong.getBytes(0X06054B50L);
}
