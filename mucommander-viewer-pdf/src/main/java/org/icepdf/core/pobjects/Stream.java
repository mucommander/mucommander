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
package org.icepdf.core.pobjects;

import org.icepdf.core.io.BitStream;
import org.icepdf.core.io.ConservativeSizingByteArrayOutputStream;
import org.icepdf.core.io.SeekableInputConstrainedWrapper;
import org.icepdf.core.pobjects.filters.*;
import org.icepdf.core.pobjects.security.SecurityManager;
import org.icepdf.core.util.Library;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Stream class is responsible for decoding stream contents and returning
 * either an images object or a byte array depending on the content.  The Stream
 * object worker method is decode which is responsible for decoding the content
 * stream, which is if the first step of the rendering process.  Once a Stream
 * is decoded it is either returned as an image object or a byte array that is
 * then processed further by the ContentParser.
 *
 * @since 1.0
 */
public class Stream extends Dictionary {

    private static final Logger logger =
            Logger.getLogger(Stream.class.toString());

    public static final Name WIDTH_KEY = new Name("Width");
    public static final Name W_KEY = new Name("W");
    public static final Name HEIGHT_KEY = new Name("Height");
    public static final Name H_KEY = new Name("H");
    public static final Name IMAGEMASK_KEY = new Name("ImageMask");
    public static final Name IM_KEY = new Name("IM");
    public static final Name COLORSPACE_KEY = new Name("ColorSpace");
    public static final Name CS_KEY = new Name("CS");
    public static final Name DECODEPARAM_KEY = new Name("DecodeParms");
    public static final Name FILTER_KEY = new Name("Filter");
    public static final Name F_KEY = new Name("F");
    public static final Name INDEXED_KEY = new Name("Indexed");
    public static final Name I_KEY = new Name("I");

    // original byte stream that has not been decoded
    protected byte[] rawBytes;

    protected HashMap decodeParams;

    // default compression state for a file loaded stream,  for re-saving
    // of form data we want to avoid re-compressing the data.
    protected boolean compressed = true;

    // reference of stream, needed for encryption support
    protected Reference pObjectReference;

    /**
     * Create a new instance of a Stream.
     *
     * @param l                  library containing a hash of all document objects
     * @param h                  HashMap of parameters specific to the Stream object.
     * @param streamInputWrapper Accessor to stream byte data
     */
    public Stream(Library l, HashMap h, SeekableInputConstrainedWrapper streamInputWrapper) {
        super(l, h);
        // capture raw bytes for later processing.
        if (streamInputWrapper != null) {
            this.rawBytes = getRawStreamBytes(streamInputWrapper);
        }
        decodeParams = library.getDictionary(entries, DECODEPARAM_KEY);
    }

    public Stream(Library l, HashMap h, byte[] rawBytes) {
        super(l, h);
        this.rawBytes = rawBytes;
        if (library != null) {
            decodeParams = library.getDictionary(entries, DECODEPARAM_KEY);
        }
    }

    /**
     * Sets the PObject referece for this stream.  The reference number and
     * generation is need by the encryption algorithm.
     */
    public void setPObjectReference(Reference reference) {
        pObjectReference = reference;
    }

    public byte[] getRawBytes() {
        return rawBytes;
    }

    public void setRawBytes(byte[] rawBytes) {
        this.rawBytes = rawBytes;
        compressed = false;
    }

    public boolean isRawBytesCompressed() {
        return compressed;
    }

    /**
     * Gets the parent PObject reference for this stream.
     *
     * @return Reference number of parent PObject.
     * @see #setPObjectReference(org.icepdf.core.pobjects.Reference)
     */
    public Reference getPObjectReference() {
        return pObjectReference;
    }

    protected boolean isImageSubtype() {
        Object subtype = library.getObject(entries, SUBTYPE_KEY);
        return subtype != null && subtype.equals("Image");
    }


    private byte[] getRawStreamBytes(SeekableInputConstrainedWrapper streamInputWrapper) {
        // copy the raw bytes out to internal storage for later decoding.
        int length = (int) streamInputWrapper.getLength();
        byte[] rawBytes = new byte[length];
        try {
            streamInputWrapper.read(rawBytes, 0, length);
        } catch (IOException e) {
            logger.warning("IO Error getting stream bytes");
        }
        return rawBytes;
    }

    /**
     * Gets the decoded Byte stream of the Stream object.
     *
     * @return decoded Byte stream
     */
    public ByteArrayInputStream getDecodedByteArrayInputStream() {
        return new ByteArrayInputStream(getDecodedStreamBytes(0));
    }

    public byte[] getDecodedStreamBytes() {
        return getDecodedStreamBytes(8192);
    }

    /**
     * This is similar to getDecodedStreamByteArray(), except that the returned byte[]
     * is not necessarily exactly sized, and may be larger. Therefore the returned
     * Integer gives the actual valid size
     *
     * @param presize potential size to associate with byte array.
     * @return Object[] { byte[] data, Integer sizeActualData }
     */
    public byte[] getDecodedStreamBytes(int presize) {
        // decompress the stream
        if (compressed) {
            try {
                ByteArrayInputStream streamInput = new ByteArrayInputStream(rawBytes);
                long rawStreamLength = rawBytes.length;
                InputStream input = getDecodedInputStream(streamInput, rawStreamLength);
                if (input == null) return null;
                int outLength;
                if (presize > 0) {
                    outLength = presize;
                } else {
                    outLength = Math.max(4096, (int) rawStreamLength);
                }
                ConservativeSizingByteArrayOutputStream out = new
                        ConservativeSizingByteArrayOutputStream(outLength);
                byte[] buffer = new byte[(outLength > 4096) ? 4096 : 8192];
                while (true) {
                    int read = input.read(buffer);
                    if (read <= 0)
                        break;
                    out.write(buffer, 0, read);
                }
                input.close();
                out.flush();
                out.close();
                out.trim();
                return out.relinquishByteArray();
            } catch (IOException e) {
                logger.log(Level.FINE, "Problem decoding stream bytes: ", e);
            }
        }
        // we have an edited stream which isn't compressed yet, so just return
        // the raw bytes.
        else {
            return rawBytes;
        }
        return null;
    }

    /**
     * Utility method for decoding the byte stream using the decode algorithem
     * specified by the filter parameter
     * <p/>
     * The memory manger is called every time a stream is being decoded with an
     * estimated size of the decoded stream.  Because many of the Filter
     * algorithms use compression,  further research must be done to try and
     * find the average amount of memory used by each of the algorithms.
     *
     * @return inputstream that has been decoded as defined by the streams filters.
     */
    private InputStream getDecodedInputStream(InputStream streamInput, long streamLength) {
        // Make sure that the stream actually has data to decode, if it doesn't
        // make it null and return.
        if (streamInput == null || streamLength < 1) {
            return null;
        }

        InputStream input = streamInput;

        int bufferSize = Math.min(Math.max((int) streamLength, 64), 16 * 1024);
        input = new java.io.BufferedInputStream(input, bufferSize);

        // Search for crypt dictionary entry and decode params so that
        // named filters can be assigned correctly.
        SecurityManager securityManager = library.getSecurityManager();
//        System.out.println("Thread " + Thread.currentThread() + " " + pObjectReference);
        if (securityManager != null) {
            // check see of there is a decodeParams for a crypt filter.
            input = securityManager.decryptInputStream(
                    pObjectReference, securityManager.getDecryptionKey(),
                    decodeParams, input, true);
        }

        // Get the filter name for the encoding type, which can be either
        // a Name or Vector.
        List filterNames = getFilterNames();
        if (filterNames == null)
            return input;

        // Decode the stream data based on the filter names.
        // Loop through the filterNames and apply the filters in the order
        // in which they where found.
        for (Object filterName1 : filterNames) {
            // grab the name of the filter
            String filterName = filterName1.toString();
            //System.out.println("  Decoding: " + filterName);

            if (filterName.equals("FlateDecode")
                    || filterName.equals("/Fl")
                    || filterName.equals("Fl")) {
                input = new FlateDecode(library, entries, input);
            } else if (
                    filterName.equals("LZWDecode")
                            || filterName.equals("/LZW")
                            || filterName.equals("LZW")) {
                input = new LZWDecode(new BitStream(input), library, entries);
            } else if (
                    filterName.equals("ASCII85Decode")
                            || filterName.equals("/A85")
                            || filterName.equals("A85")) {
                input = new ASCII85Decode(input);
            } else if (
                    filterName.equals("ASCIIHexDecode")
                            || filterName.equals("/AHx")
                            || filterName.equals("AHx")) {
                input = new ASCIIHexDecode(input);
            } else if (
                    filterName.equals("RunLengthDecode")
                            || filterName.equals("/RL")
                            || filterName.equals("RL")) {
                input = new RunLengthDecode(input);
            } else if (
                    filterName.equals("CCITTFaxDecode")
                            || filterName.equals("/CCF")
                            || filterName.equals("CCF")) {
                // Leave empty so our else clause works
            } else if (
                    filterName.equals("DCTDecode")
                            || filterName.equals("/DCT")
                            || filterName.equals("DCT")) {
                // Leave empty so our else clause works
            } else if ( // No short name, since no JBIG2 for inline images
                    filterName.equals("JBIG2Decode")) {
                // Leave empty so our else clause works
            } else if ( // No short name, since no JPX for inline images
                    filterName.equals("JPXDecode")) {
                // Leave empty so our else clause works
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("UNSUPPORTED:" + filterName + " " + entries);
                }
            }
        }
        // Apply  Predictor Filter logic fo LZW or Flate streams.
        if (PredictorDecode.isPredictor(library, entries)) {
            input = new PredictorDecode(input, library, entries);
        }

        return input;
    }

    @SuppressWarnings("unchecked")
    protected List<String> getFilterNames() {
        List<String> filterNames = null;
        Object o = library.getObject(entries, FILTER_KEY);
        if (o instanceof Name) {
            filterNames = new ArrayList<String>(1);
            filterNames.add(o.toString());
        } else if (o instanceof List) {
            filterNames = (List) o;
        }
        return filterNames;
    }

    protected List<String> getNormalisedFilterNames() {
        List<String> filterNames = getFilterNames();
        if (filterNames == null)
            return null;

        String filterName;
        for (int i = 0; i < filterNames.size(); i++) {
            filterName = filterNames.get(i);

            if (filterName.equals("FlateDecode")
                    || filterName.equals("/Fl")
                    || filterName.equals("Fl")) {
                filterName = "FlateDecode";
            } else if (
                    filterName.equals("LZWDecode")
                            || filterName.equals("/LZW")
                            || filterName.equals("LZW")) {
                filterName = "LZWDecode";
            } else if (
                    filterName.equals("ASCII85Decode")
                            || filterName.equals("/A85")
                            || filterName.equals("A85")) {
                filterName = "ASCII85Decode";
            } else if (
                    filterName.equals("ASCIIHexDecode")
                            || filterName.equals("/AHx")
                            || filterName.equals("AHx")) {
                filterName = "ASCIIHexDecode";
            } else if (
                    filterName.equals("RunLengthDecode")
                            || filterName.equals("/RL")
                            || filterName.equals("RL")) {
                filterName = "RunLengthDecode";
            } else if (
                    filterName.equals("CCITTFaxDecode")
                            || filterName.equals("/CCF")
                            || filterName.equals("CCF")) {
                filterName = "CCITTFaxDecode";
            } else if (
                    filterName.equals("DCTDecode")
                            || filterName.equals("/DCT")
                            || filterName.equals("DCT")) {
                filterName = "DCTDecode";
            }
            // There aren't short names for JBIG2Decode or JPXDecode
            filterNames.set(i, filterName);
        }
        return filterNames;
    }

    /**
     * Return a string description of the object.  Primarly used for debugging.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append("STREAM= ");
        sb.append(entries);
        if (getPObjectReference() != null) {
            sb.append("  ");
            sb.append(getPObjectReference());
        }
        return sb.toString();
    }

}
