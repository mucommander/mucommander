/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
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

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class allows to guess at an encoding in which an array of bytes is encoded. Detecting an encoding is by no means
 * an accurate operation, as it relies on heuristics that are imprecise by nature. However, accuracy improves with the
 * quantity of bytes that is supplied: a small amount of data (say 10 bytes) has little chance of being guessed
 * correctly, whereas a larger amount of data (say 1000 bytes) is likely to provide a good result. On the other hand,
 * providing a very large amount of data will only marginally improve the accuracy, and is not worth the extra effort
 * considering that encoding detection is a costly operation which involves many comparisons per byte.
 * The {@link #MAX_RECOMMENDED_BYTE_SIZE} field controls that threshold: if a supplied byte array is larger than this
 * value, the additional bytes will not be processed by the <code>detectEncoding</code> methods. Therefore, this value
 * should be taken into account if bytes are to be fetched specifically for the purpose of detecting the encoding.
 * <p>
 * <p>
 * EncodingDetector uses <i>ICU4J</i> under the hood. Here's a list of encodings that can currently be detected:
 * <pre>
 * UTF-8
 * UTF-16BE
 * UTF-16LE
 * UTF-32BE
 * UTF-32LE
 * Shift_JIS
 * ISO-2022-JP
 * ISO-2022-CN
 * ISO-2022-KR
 * GB18030
 * EUC-JP
 * EUC-KR
 * Big5
 * ISO-8859-1
 * ISO-8859-2
 * ISO-8859-5
 * ISO-8859-6
 * ISO-8859-7
 * ISO-8859-8
 * windows-1251
 * windows-1256
 * KOI8-R
 * ISO-8859-9
 * </pre>
 * </p>
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 * @see <a href="http://philip.html5.org/data/charsets.html">ICU charset detection accuracy</a>
 */
public class EncodingDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncodingDetector.class);

    /**
     * Maximum number of bytes that the detectEncoding methods will process.
     * <p>
     * See http://philip.html5.org/data/charsets.html and http://philip.html5.org/data/encoding-detection.svg
     * for why 4096 is the recommended size.
     * </p>
     */
    public final static int MAX_RECOMMENDED_BYTE_SIZE = 4096;


    /**
     * This method is a shorthand for {@link #detectEncoding(byte[], int, int) detectEncoding(b, 0, b.length)}.
     *
     * @param bytes the bytes for which to detect the encoding
     * @return the best guess at the character encoding, null if there is none (not enough data or confidence)
     */
    public static String detectEncoding(byte bytes[]) {
        return detectEncoding(bytes, 0, bytes.length);
    }

    /**
     * Try and detect the character encoding in which the given bytes are encoded, and returns the best guess or
     * <code>null</code> if there is none (not enough data or confidence).
     * Note that the returned character encoding may not be available on the Java runtime -- use
     * <code>java.nio.Charset#isSupported(String)</code> to determine if it is available.
     * <p>
     * <p>A maximum of {@link #MAX_RECOMMENDED_BYTE_SIZE} will be read from the array. If the array is larger than this
     * value, all further bytes will be ignored.</p>
     *
     * @param bytes the bytes for which to detect the encoding
     * @param off   the array offset at which the data to process starts
     * @param len   length of the data in the array
     * @return the best guess at the encoding, null if there is none (not enough data or confidence)
     */
    public static String detectEncoding(byte bytes[], int off, int len) {
        // The current ICU CharsetDetector class will throw an ArrayIndexOutOfBoundsException exception if the
        // supplied array is less than 4 bytes long. In that case, return null.
        if (len < 4)
            return null;

        // Trim the array if it is too long, detecting the charset is an expensive operation and past a certain point,
        // having more bytes won't help any further        
        if (len > MAX_RECOMMENDED_BYTE_SIZE)
            len = MAX_RECOMMENDED_BYTE_SIZE;

        // CharsetDetector will process the array fully, so if the data does not start at 0 or ends before the array's
        // length, create a new array that fits the data exactly
        if (off > 0 || len < bytes.length) {
            byte tmp[] = new byte[len];
            System.arraycopy(bytes, off, tmp, 0, len);
            bytes = tmp;
        }

        CharsetDetector cd = new CharsetDetector();
        cd.setText(bytes);

        CharsetMatch cm = cd.detect();

        // Debug info
        LOGGER.trace("bestMatch getName()={}, getConfidence()={}", (cm == null ? "null" : cm.getName()),
                (cm == null ? "null" : Integer.toString(cm.getConfidence())));
//            CharsetMatch cms[] = cd.detectAll();
//            for(int i=0; i<cms.length; i++)
//                CommonsLogger.finest("getName()="+cms[i].getName()+" getConfidence()="+cms[i].getConfidence());

        return cm == null ? null : cm.getName();
    }


    /**
     * Try and detect the character encoding in which the bytes contained by the given <code>InputStream</code> are
     * encoded, and returns the best guess or <code>null</code> if there is none (not enough data or confidence).
     * Note that the returned character encoding may or may not be available on the Java runtime -- use
     * <code>java.nio.Charset#isSupported(String)</code> to determine if it is available.
     * <p>
     * <p>A maximum of {@link #MAX_RECOMMENDED_BYTE_SIZE} will be read from the <code>InputStream</code>. The
     * stream will not be closed and will not be repositionned after the bytes have been read. It is up to the calling
     * method to use the <code>InputStream#mark()</code> and <code>InputStream#reset()</code> methods (if supported)
     * or reopen the stream if needed.
     * </p>
     *
     * @param in the InputStream that supplies the bytes
     * @return the best guess at the character encoding, null if there is none (not enough data or confidence)
     * @throws IOException if an error occurred while reading the stream
     */
    public static String detectEncoding(InputStream in) throws IOException {
        byte buf[] = BufferPool.getByteArray(MAX_RECOMMENDED_BYTE_SIZE);

        try {
            return detectEncoding(buf, 0, StreamUtils.readUpTo(in, buf));
        } finally {
            BufferPool.releaseByteArray(buf);
        }
    }

    /**
     * Returns an array of encodings that can be detected by the <code>detectEncoding</code> methods.
     * Note that some of the returned character encodings may not be available on the Java runtime.
     *
     * @return an array of encodings that can be detected by the <code>detectEncoding</code> methods.
     */
    public static String[] getDetectableEncodings() {
        return CharsetDetector.getAllDetectableCharsets();
    }

    /**
     * Lists all detectable encodings as returned by {@link #getDetectableEncodings()} to the standard output.
     *
     * @param args command line arguments.
     */
    public static void main(String args[]) {
        String encodings[] = getDetectableEncodings();

        for (String encoding : encodings)
            System.out.println(encoding);
    }
}
