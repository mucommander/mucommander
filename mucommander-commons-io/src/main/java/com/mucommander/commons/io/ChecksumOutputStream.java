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

import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

/**
 * This class extends <code>java.security.DigestOutputStream</code> and adds convenience methods that return the
 * digest/checksum expressed in various forms.
 *
 * @see com.mucommander.commons.io.ChecksumInputStream
 * @author Maxence Bernard
 */
public class ChecksumOutputStream extends DigestOutputStream {

    public ChecksumOutputStream(OutputStream stream, MessageDigest digest) {
        super(stream, digest);
    }


    /**
     * Returns this stream's digest, expressed as a byte array.
     *
     * @return this stream's digest, expressed as a byte array
     */
    public byte[] getChecksumBytes() {
        return getMessageDigest().digest();
    }

    /**
     * Returns this stream's digest, expressed as an hexadecimal string.
     *
     * @return this stream's digest, expressed as an hexadecimal string
     */
    public String getChecksumString() {
        return ByteUtils.toHexString(getChecksumBytes());
    }
}
