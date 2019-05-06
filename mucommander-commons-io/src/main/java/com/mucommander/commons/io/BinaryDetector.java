/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2018 Maxence Bernard
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

import com.mucommander.commons.io.bom.BOM;
import com.mucommander.commons.io.bom.BOMConstants;
import com.mucommander.commons.io.bom.BOMInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class provides methods to determine whether some data is binary data or text data.
 * As there is no formal characterization of what binary data really is, this method is an approximation at best
 * and should not be trusted for anything critical.
 *
 * <p>The {@link #RECOMMENDED_BYTE_SIZE} field indicates how many bytes should be provided for the detector to be
 * confident enough.</p>
 *
 * @see com.mucommander.commons.io.EncodingDetector
 * @author Maxence Bernard
 */
public class BinaryDetector {

    /** Provides an indication as to the number of bytes that should fed to the detector for it to have enough
     * confidence. */
    public final static int RECOMMENDED_BYTE_SIZE = 1024;


    /**
     * This method is a shorthand for {@link #guessBinary(byte[], int, int) guessBinary(b, 0, b.length)}.
     *
     * @param b the data to analyze
     * @return true if BinaryDetector thinks that the specified data is binary
     */
    public static boolean guessBinary(byte b[]) {
        return guessBinary(b, 0, b.length);
    }

    /**
     * Tries and detect whether the given bytes correspond to binary or text data. The specified bytes can typically
     * be the beginning of a file.</br>
     * This method returns <code>true</code> if it thinks that the bytes correspond to binary data.
     *
     * @param b the data to analyze
     * @param off specifies where to start reading the array
     * @param len specifies where to stop reading the array
     * @return true if BinaryDetector thinks that the specified data is binary
     */
    public static boolean guessBinary(byte b[], int off, int len) {
            // Returns true if any of the bytes are the NUL character. The NUL character is usually not found in a text
            // file, except for UTF-16 and UTF-32 streams.
            // So first, we try and look for a BOM (byte-order mark) to see if the stream is UTF-16 or UTF-32 encoded.
        try (BOMInputStream bin = new BOMInputStream(new ByteArrayInputStream(b, off, len))) {
            BOM bom = bin.getBOM();

            if(bom!=null) {
                if(bom.equals(BOMConstants.UTF16_BE_BOM) || bom.equals(BOMConstants.UTF16_LE_BOM)
                || bom.equals(BOMConstants.UTF32_BE_BOM) || bom.equals(BOMConstants.UTF32_LE_BOM)) {
                    return false;
                }
            }
            // No BOM, start looking for zeros

            int i;
            while((i=bin.read())!=-1)
                if(i==0x00)
                    return true;
        }
        catch(IOException e) {
            // Can never happen in practice with a ByteArrayInputStream.
        }

        return false;
    }

    /**
     * Tries and detect whether the given stream contains binary or text data.</br>
     * This method returns <code>true</code> if it thinks that the bytes correspond to binary data.
     *
     * <p>A maximum of {@link #RECOMMENDED_BYTE_SIZE} will be read from the <code>InputStream</code>. The
     * stream will not be closed and will not be repositionned after the bytes have been read. It is up to the calling
     * method to use the <code>InputStream#mark()</code> and <code>InputStream#reset()</code> methods (if supported)
     * or reopen the stream if needed.
     * </p>
     *
     * @param in the stream to analyze
     * @return true if BinaryDetector thinks that the specified data is binary
     * @throws IOException if an error occurred while reading the InputStream.
     */
    public static boolean guessBinary(InputStream in) throws IOException {
        byte[] bytes = new byte[RECOMMENDED_BYTE_SIZE];
        return guessBinary(bytes, 0, StreamUtils.readUpTo(in, bytes));
    }
}
