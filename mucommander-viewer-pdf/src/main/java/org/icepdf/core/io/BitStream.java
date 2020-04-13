/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The class <code>BitStream</code> is designed to convert an input stream of
 * bytes into bits.  A <code>BitStream</code> is primarily used when bit
 * manipulation of stream needed, such as when bit data is needed for encoding
 * a image.
 *
 * @since 1.0
 */
public class BitStream {

    // Input stream
    InputStream in;
    // Output stream
    OutputStream out;
    // bits left in stream
    int bits;
    // number of bits left in a byte
    int bits_left;
    boolean readEOF;

    // making value
    private static final int masks[] = new int[32];

    static {
        for (int i = 0; i < 32; i++) {
            masks[i] = ((1 << i) - 1);
        }
    }

    /**
     * Create a new instance of a <code>BitStream</code> from the given
     * input stream.
     *
     * @param i input stream to create a bit stream from.
     */
    public BitStream(InputStream i) {
        in = i;
        bits = 0;
        bits_left = 0;
        readEOF = false;
    }

    /**
     * Create a new instance of a <code>BitStream</code> from the given
     * output stream.
     *
     * @param o output stream to create a bit stream from.
     */
    public BitStream(OutputStream o) {
        out = o;
        bits = 0;
        bits_left = 0;
        readEOF = false;
    }

    /**
     * Close all streams data associated with this object
     *
     * @throws IOException error closing stream
     */
    public void close() throws IOException {

        // clean up the streams.
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.flush();
            out.close();
        }
    }

    /**
     * @param count
     * @return
     * @throws java.io.IOException
     */
    public int getBits(int count) throws IOException {
        if (count < 32) {
            while (bits_left < count) {
                int r = in.read();
                if (r < 0) {
                    readEOF = true;
                    break;
                }
                bits <<= 8;
                bits |= (r & 0xFF);
                bits_left += 8;
            }
            bits_left -= count;
            return (bits >> bits_left) & masks[count];
        } else if (count == 32) {
            return (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | in.read();
        }
        return 0;
    }

    public boolean atEndOfFile() {
        return readEOF && (bits_left <= 0);
    }

    /**
     * @param i
     * @throws java.io.IOException
     */
    public void putBit(int i) throws IOException {
        bits <<= 1;
        bits |= i;
        bits_left++;
        if (bits_left == 8) {
            out.write(bits);
            bits = 0;
            bits_left = 0;
        }
    }

    /**
     * @param i
     * @param len
     * @throws java.io.IOException
     */
    public void putRunBits(int i, int len) throws IOException {
        for (int j = len - 1; j >= 0; ) {
            if (bits_left != 0 || j < 8) {
                putBit(i);
                j--;
            } else {
                if (i == 0)
                    out.write(0x00);
                else
                    out.write(0xFF);
                j -= 8;
            }
        }
    }

    /**
     * @return
     * @throws java.io.IOException
     */
    public int available() throws IOException {
        if (bits_left == 0 && in.available() <= 0)
            return 0;
        return 1;
    }

    /**
     * @throws java.io.IOException
     */
    public void skipByte() throws IOException {
        bits_left = 0;
        bits = 0;
    }
}



