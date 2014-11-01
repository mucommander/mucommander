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

/**
 * ZipBuffer is a C struct-like class that holds byte buffers that are used to convert Java values to Big Endian byte
 * arrays. It allows to reuse the same byte buffers instead of instanciating new ones for each conversion.
 *
 * @see ZipShort#getBytes(int, byte[], int)
 * @see ZipLong#getBytes(long, byte[], int)
 * @author Maxence Bernard
 */
public class ZipBuffer {

    /**  2-byte buffer that can hold a Zip short value */
    byte[] shortBuffer = new byte[2];

    /**  2-byte buffer that can hold a Zip long value */
    byte[] longBuffer = new byte[4];
}
