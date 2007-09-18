/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.io.base64;

import java.io.IOException;
import java.io.InputStream;

/**
 * <code>Base64InputStream</code> is an <code>InputStream</code> that decodes Base64-encoded data provided by
 * an underlying <code>InputStream</code>. The underlying data must be valid base64-encoded data, if not,
 * <code>IOException</code> will be thrown when illegal data is encountered.
 *
 * @see Base64Decoder
 * @author Maxence Bernard
 */
public class Base64InputStream extends InputStream {

    /** Underlying stream data is read from */
    private InputStream in;

    /** Decoded bytes available for reading */
    private int readBuffer[] = new int[3];

    /** Index of the next byte available for reading in the buffer */
    private int readOffset;

    /** Number of bytes left for reading in the buffer */
    private int bytesLeft;

    /** Buffer used temporarily for decoding */
    private int decodeBuffer[] = new int[4];

    /** Decoding table */
    private final static int BASE64_DECODING_TABLE[];
    

    // Create the base 64 decoding table
    static {
        BASE64_DECODING_TABLE = new int[256];
        int offset;
        char c;

        for(c=0; c<256; c++)
            BASE64_DECODING_TABLE[c] = -1;

        offset = 0;
        for(c='A'; c<='Z'; c++)
            BASE64_DECODING_TABLE[c] = offset++;

        for(c='a'; c<='z'; c++)
            BASE64_DECODING_TABLE[c] = offset++;

        for(c='0'; c<='9'; c++)
            BASE64_DECODING_TABLE[c] = offset++;

        BASE64_DECODING_TABLE['+'] = 62;
        BASE64_DECODING_TABLE['/'] = 63;
    }


    /**
     * Creates a new Base64InputStream that allows to decode Base64-encoded from the provided InputStream.
     *
     * @param in underlying InputStream the Base64-encoded data is read from
     */
    public Base64InputStream(InputStream in) {
        this.in = in;
    }


    ////////////////////////////////
    // InputStream implementation //
    ////////////////////////////////

    public int read() throws IOException {
        // Read buffer empty: read and decode a new base64-encoded 4-byte group
        if(bytesLeft==0) {
            int read;
            int nbRead = 0;

            while(nbRead<4) {
                read = in.read();
                // EOF reached
                if(read==-1) {
                    if(nbRead%4 != 0) {
                        // Base64 encoded data must come in a multiple of 4 bytes, throw an IOException if the underlying stream ended prematurely
                        throw new IOException("InputStream did not end on a multiple of 4 bytes");
                    }

                    if(nbRead==0)
                        return -1;
                    else    // nbRead==4
                        break;
                }

                decodeBuffer[nbRead] = BASE64_DECODING_TABLE[read];

                // Discard any character that's not a base64 character, without throwing an IOException.
                // In particular, '\r' and '\n' characters that are usually found in email attachments are simply ignored.
                if(decodeBuffer[nbRead]==-1 && read!='=') {
                    continue;
                }

                nbRead++;
            }

            // Decode byte 0
            readBuffer[bytesLeft++] = ((decodeBuffer[0]<<2)&0xFC | ((decodeBuffer[1]>>4)&0x03));

            // Test if the character is not padding ('=')
            if(decodeBuffer[2]!=-1) {
                // Decode byte 1
                readBuffer[bytesLeft++] = (decodeBuffer[1]<<4)&0xF0 | ((decodeBuffer[2]>>2)&0x0F);

                // Test if the character is not padding ('=')
                if(decodeBuffer[3]!=-1)
                    // Decode byte 2
                    readBuffer[bytesLeft++] = ((decodeBuffer[2]<<6)&0xC0) | (decodeBuffer[3]&0x3F);
            }

            readOffset = 0;
        }

        bytesLeft--;

        return readBuffer[readOffset++];
    }
}
