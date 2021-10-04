/*
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.viewer.binary;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utilities for stream data manipulations.
 */
@ParametersAreNonnullByDefault
public class StreamUtils {

    private static final int BUFFER_SIZE = 1024;

    private StreamUtils() {
    }

    /**
     * Copies all data from input stream to output stream using 1k buffer.
     *
     * @param source input stream
     * @param target output stream
     * @throws IOException if read or write fails
     */
    public static void copyInputStreamToOutputStream(InputStream source, OutputStream target) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bufferUsed = 0;

        int bytesRead;
        do {
            bytesRead = source.read(buffer, bufferUsed, BUFFER_SIZE - bufferUsed);
            if (bytesRead > 0) {
                bufferUsed += bytesRead;
                if (bufferUsed == BUFFER_SIZE) {
                    target.write(buffer, 0, BUFFER_SIZE);
                    bufferUsed = 0;
                }
            }

        } while (bytesRead > 0);

        if (bufferUsed > 0) {
            target.write(buffer, 0, bufferUsed);
        }
    }

    /**
     * Skips given amount of data from input stream.
     *
     * @param source    input stream
     * @param skipBytes number of bytes to skip
     * @throws IOException if skip fails
     */
    public static void skipInputStreamData(InputStream source, long skipBytes) throws IOException {
        while (skipBytes > 0) {
            long skipped = source.skip(skipBytes);
            if (skipped <= 0) {
                throw new IOException("Unable to skip data");
            } else {
                skipBytes -= skipped;
            }
        }
    }
}
