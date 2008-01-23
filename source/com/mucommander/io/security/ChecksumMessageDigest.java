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

package com.mucommander.io.security;

import java.security.MessageDigest;
import java.util.zip.Checksum;

/**
 * This class turns a <code>java.util.zip.Checksum</code> into a <code>java.security.MessageDigest</code>, allowing
 * <code>Checksum</code> implementations to be used with the Java Cryptography Extension.
 *
 * @author Maxence Bernard
 */
public class ChecksumMessageDigest extends MessageDigest {

    /** The Checksum instance that performs all of the checksumming work */
    private Checksum checksum;

    /**
     * Creates a new <code>ChecksumMessageDigest</code> that delegates all the checksumming work to the given
     * <code>Checksum</code> instance. 
     *
     * @param checksum the Checksum responsible for calculating the checksum
     * @param algorithm the name of the checksum algorithm implemented by the Checksum
     */
    public ChecksumMessageDigest(Checksum checksum, String algorithm) {
        super(algorithm);

        this.checksum = checksum;
    }


    //////////////////////////////////
    // MessageDigest implementation //
    //////////////////////////////////

    /**
     * This method delegates to the underlying <code>java.util.zip.Checksum</code> instance.
     */
    protected void engineReset() {
        checksum.reset();
    }

    /**
     * This method delegates to the underlying <code>java.util.zip.Checksum</code> instance.
     */
    protected void engineUpdate(byte input) {
        checksum.update(input);
    }

    /**
     * This method delegates to the underlying <code>java.util.zip.Checksum</code> instance.
     */
    protected void engineUpdate(byte[] input, int offset, int len) {
        checksum.update(input, offset, len);
    }

    /**
     * This method delegates to the underlying <code>java.util.zip.Checksum</code> instance.
     */
    protected byte[] engineDigest() {
        long crcLong = checksum.getValue();

        byte[] crcBytes = new byte[4];
        crcBytes[0] = (byte)((crcLong>>24) & 0xFF);
        crcBytes[1] = (byte)((crcLong>>16) & 0xFF);
        crcBytes[2] = (byte)((crcLong>>8) & 0xFF);
        crcBytes[3] = (byte)(crcLong & 0xFF);

        return crcBytes;
    }
}
