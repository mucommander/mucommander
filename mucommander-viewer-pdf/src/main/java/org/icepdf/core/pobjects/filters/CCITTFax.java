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
package org.icepdf.core.pobjects.filters;


import org.icepdf.core.io.BitStream;
import org.icepdf.core.io.ZeroPaddedInputStream;
import org.icepdf.core.pobjects.ImageStream;
import org.icepdf.core.pobjects.Stream;
import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Many facsimile and document imaging file formats support a form of lossless
 * data compression often described as CCITT encoding. The CCITT (International
 * Telegraph and Telephone Consultative Committee) is a standards organization
 * that has developed a series of communications protocols for the facsimile
 * transmission of black-and-white images over telephone lines and data networks.
 * These protocols are known officially as the CCITT T.4 and T.6 standards but
 * are more commonly referred to as CCITT Group 3 and Group 4 compression,
 * respectively.
 * <p>
 * The CCITT actually defines three algorithms for the encoding of bi-level image data:
 * Group 3 One-Dimensional (G31D)
 * Group 3 Two-Dimensional (G32D) - not implemented
 * Group 4 Two-Dimensional (G42D)
 */
public class CCITTFax {

    private static final Logger logger =
            Logger.getLogger(CCITTFax.class.toString());

    // white codes
    static final String[] _twcodes = {
            "00110101", "000111", "0111", "1000", "1011", "1100", "1110", "1111",
            "10011", "10100", "00111", "01000", "001000", "000011", "110100",
            "110101", "101010", "101011", "0100111", "0001100", "0001000",
            "0010111", "0000011", "0000100", "0101000", "0101011", "0010011",
            "0100100", "0011000", "00000010", "00000011", "00011010", "00011011",
            "00010010", "00010011", "00010100", "00010101", "00010110",
            "00010111", "00101000", "00101001", "00101010", "00101011",
            "00101100", "00101101", "00000100", "00000101", "00001010",
            "00001011", "01010010", "01010011", "01010100", "01010101",
            "00100100", "00100101", "01011000", "01011001", "01011010",
            "01011011", "01001010", "01001011", "00110010", "00110011",
            "00110100"
    };

    // wite codes
    static final String[] _mwcodes = {
            "11011", "10010", "010111", "0110111", "00110110", "00110111", "01100100",
            "01100101", "01101000", "01100111", "011001100", "011001101",
            "011010010", "011010011", "011010100", "011010101", "011010110",
            "011010111", "011011000", "011011001", "011011010", "011011011",
            "010011000", "010011001", "010011010", "011000", "010011011"
    };

    // black codes
    static final String[] _tbcodes = {
            "0000110111", "010", "11", "10", "011", "0011", "0010", "00011", "000101",
            "000100", "0000100", "0000101", "0000111", "00000100", "00000111",
            "000011000", "0000010111", "0000011000", "0000001000", "00001100111",
            "00001101000", "00001101100", "00000110111", "00000101000",
            "00000010111", "00000011000", "000011001010", "000011001011",
            "000011001100", "000011001101", "000001101000", "000001101001",
            "000001101010", "000001101011", "000011010010", "000011010011",
            "000011010100", "000011010101", "000011010110", "000011010111",
            "000001101100", "000001101101", "000011011010", "000011011011",
            "000001010100", "000001010101", "000001010110", "000001010111",
            "000001100100", "000001100101", "000001010010", "000001010011",
            "000000100100", "000000110111", "000000111000", "000000100111",
            "000000101000", "000001011000", "000001011001", "000000101011",
            "000000101100", "000001011010", "000001100110", "000001100111"
    };
    // black  codes
    static final String[] _mbcodes = {
            "0000001111", "000011001000", "000011001001", "000001011011", "000000110011",
            "000000110100", "000000110101", "0000001101100", "0000001101101",
            "0000001001010", "0000001001011", "0000001001100", "0000001001101",
            "0000001110010", "0000001110011", "0000001110100", "0000001110101",
            "0000001110110", "0000001110111", "0000001010010", "0000001010011",
            "0000001010100", "0000001010101", "0000001011010", "0000001011011",
            "0000001100100", "0000001100101"
    };
    static final String[] _extmcodes = {
            "00000001000", "00000001100", "00000001101", "000000010010", "000000010011",
            "000000010100", "000000010101", "000000010110", "000000010111",
            "000000011100", "000000011101", "000000011110", "000000011111"
    };

    // Mode command binary values
    static final String[] _modecodes = {
            "0001", // P  - Pass Mode
            "001", // H   - Horizontal Mode, when neither P or V Modes
            "1", // V0         - Vertical Mode
            "011", // VR1      - Vertical Mode, one pixel to the right
            "000011", // VR2   - Vertical Mode, two pixel to the right
            "0000011", // VR3  - Vertical Mode, three pixel to the right
            "010", // VL1      - Vertical Mode, one pixel to the left
            "000010", // VL2   - Vertical Mode, two pixel to the left
            "0000010", // VL3  - Vertical Mode, three pixel to the left
            "0000001111", // EXT2D    - Extension, 2D
            "000000001111", // EXT1D   - Extension, 1D
            "000000000001"          // EOL
    };

    private static class Code {
        private long value;
        private int length;
        private int tablePosition;

        public Code() {
            value = 0L;
            length = 0;
        }

        public Code(String strValue, int tablePosition) {
            value = 0L;
            length = 0;
            this.tablePosition = tablePosition;
            for (int i = 0; i < strValue.length(); i++)
                append(strValue.charAt(i) == '1');
        }

        public final void append(boolean bit) {
            // This is effectively similar to the old String code,
            // which kept the extra bits, but would then not match
            // any of the table entries
            if (bit) {
                if (length <= 63) {
                    long mask = (1L << length);
                    value |= mask;
                }
            }
            length++;
        }

        public final boolean equals(Object ob) {
            if (ob instanceof Code) {
                Code c = (Code) ob;
                return (value == c.value && length == c.length);
            }
            return false;
        }

        public final void reset() {
            value = 0L;
            length = 0;
        }

        public final int getLength() {
            return length;
        }

        public final int getTablePosition() {
            return tablePosition;
        }
    }

    static final Code[][] twcodes = convertStringArrayToCodeArray2D(_twcodes);
    static final Code[][] mwcodes = convertStringArrayToCodeArray2D(_mwcodes);
    static final Code[][] tbcodes = convertStringArrayToCodeArray2D(_tbcodes);
    static final Code[][] mbcodes = convertStringArrayToCodeArray2D(_mbcodes);
    static final Code[][] extmcodes = convertStringArrayToCodeArray2D(_extmcodes);
    static final Code[][] modecodes = convertStringArrayToCodeArray2D(_modecodes);

    private static Code[][] convertStringArrayToCodeArray2D(String[] strArray) {
        int len = strArray.length;

        // Make histogram of sizes
        int[] codeLengths = new int[64];
        for (String aStrArray : strArray) {
            int entryLength = aStrArray.length();
            codeLengths[entryLength]++;
        }

        // Make a 2d array of Code objects, where the first index is for
        //  the length of the Code, and the second index differentiates
        //  between all Code objects with that length
        // In this way, we separate all Code objects by their length,
        //  and can thus reduce the search space
        // In theory, we could then sort each sublist, and do a binary search...
        int largestLength = codeLengths.length - 1;
        while (largestLength > 0 && codeLengths[largestLength] == 0)
            largestLength--;
        Code[][] codeArray = new Code[largestLength + 1][];
        for (int i = 0; i < codeArray.length; i++)
            codeArray[i] = new Code[codeLengths[i]];

        for (int i = 0; i < len; i++) {
            int entryLength = strArray[i].length();
            Code[] entries = codeArray[entryLength];
            for (int j = 0; j < entries.length; j++) {
                if (entries[j] == null) {
                    entries[j] = new Code(strArray[i], i);
                    break;
                }
            }
        }

        return codeArray;
    }

    private static int findPositionInTable(Code lookFor, Code[][] lookIn) {
        int lookForIndex = lookFor.getLength();
        if (lookForIndex >= lookIn.length)
            return -1;
        Code[] lookInWithSameLength = lookIn[lookForIndex];
        if (lookInWithSameLength == null)
            return -1;
        for (Code potentialMatch : lookInWithSameLength) {
            if (lookFor.equals(potentialMatch))
                return potentialMatch.getTablePosition();
        }
        return -1;

    }

    // Black and white colour bit values.
    static int black = 0;
    static int white = 1;

    // Never actually used.
//    class FaxCode {
//        FaxCode zero;
//        FaxCode one;
//        boolean leaf;
//        int tipo;
//        int len;
//    }

    private static final short TIFF_COMPRESSION_NONE_default = 1;
    private static final short TIFF_COMPRESSION_GROUP3_1D = 2;
    private static final short TIFF_COMPRESSION_GROUP3_2D = 3;
    private static final short TIFF_COMPRESSION_GROUP4 = 4;

    private static final String[] TIFF_COMPRESSION_NAMES = new String[]{
            "",
            "TIFF_COMPRESSION_NONE_default",
            "TIFF_COMPRESSION_GROUP3_1D",
            "TIFF_COMPRESSION_GROUP3_2D",
            "TIFF_COMPRESSION_GROUP4"
    };

    private static final short TIFF_PHOTOMETRIC_INTERPRETATION_WHITE_IS_ZERO_default = 0;
    private static final short TIFF_PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO = 1;

    private static boolean USE_JAI_IMAGE_LIBRARY = false;
    private static Method jaiCreate = null;
    private static Method ssWrapInputStream = null;
    private static Method roGetAsBufferedImage = null;

    static {
        try {
            Class<?> jaiClass = Class.forName("javax.media.jai.JAI");
            jaiCreate = jaiClass.getMethod("create", String.class, ParameterBlock.class);
            Class<?> ssClass = Class.forName("com.sun.media.jai.codec.SeekableStream");
            ssWrapInputStream = ssClass.getMethod("wrapInputStream", InputStream.class, Boolean.TYPE);
            Class<?> roClass = Class.forName("javax.media.jai.RenderedOp");
            roGetAsBufferedImage = roClass.getMethod("getAsBufferedImage");
            USE_JAI_IMAGE_LIBRARY = true;
        } catch (Exception e) {
            logger.info("javax.media.jai.JAI could not bef found on the class path");
        }

        if (logger.isLoggable(Level.FINER)) {
            Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("TIFF");
            ImageReader reader;
            while (iter.hasNext()) {
                reader = iter.next();
                logger.finer("CCITTFaxDecode Image reader: " + reader);
            }
        }
    }

    /**
     * Map bitstream values to tw and mw codes.
     *
     * @param inb bit stream containing the CCITT data
     * @throws java.io.IOException
     */
    static int findWhite(BitStream inb, Code code) throws IOException {
        return findTone(inb, code, twcodes, mwcodes);
    }

    /**
     * Finds the next black occruence in the stream
     *
     * @throws java.io.IOException
     */
    static int findBlack(BitStream inb, Code code) throws IOException {
        return findTone(inb, code, tbcodes, mbcodes);
    }

    static int findTone(BitStream inb, Code code, Code[][] tCodes, Code[][] mCodes) throws IOException {
        code.reset();
        while (!inb.atEndOfFile()) {
            int i = inb.getBits(1);
            code.append(i != 0);
            int j;
            j = findPositionInTable(code, tCodes);
            if (j >= 0) {
                //System.err.println("BINGO! tb "+_tbcodes[j]+" "+j);
                return j;
            }
            j = findPositionInTable(code, mCodes);
            if (j >= 0) {
                //System.err.println("BINGO! mb "+_mbcodes[j]+" "+(j+1)*64);
                return (j + 1) * 64;
            }
            j = findPositionInTable(code, extmcodes);
            if (j >= 0) {
                //System.err.println("BINGO! extm "+_extmcodes[j]+" "+(1792+j*64));
                return (1792 + j * 64);
            }
        }
        inb.close();
        //System.err.println("CODE ERROR! " + code);
        return 0;
    }

    /**
     * @throws java.io.IOException
     */
    static void addRun(int x, G4State s, BitStream out) throws IOException {
        s.runLength += x;
        s.cur[s.curIndex++] = s.runLength;
        s.a0 += x;
        if (s.runLength > 0) {
            // black/white color switch !s.white
            out.putRunBits(s.white ? white : black, s.runLength);
        }
        out.close();
        s.runLength = 0;
    }

    /**
     * @throws java.io.IOException
     */
    static int readmode(BitStream inb, Code code) throws IOException {
        code.reset();
        while (!inb.atEndOfFile()) {
            int i = inb.getBits(1);
            code.append(i != 0);
            int j = findPositionInTable(code, modecodes);
            if (j >= 0) {
                return j;
            }
        }
        inb.close();
        return -1;
    }

    /**
     */
    static void detectB1(G4State s) {
        if (s.curIndex != 0) {
            while (s.b1 <= s.a0 && s.b1 < s.width) {
                int r = s.ref[s.refIndex] + s.ref[s.refIndex + 1];
                if (r == 0)
                    s.b1 = s.width;
                s.b1 += r;
                if (s.refIndex + 2 < s.ref.length) {
                    s.refIndex += 2;
                }
//                else {
                //System.out.println("ERROR in detectB1, refIndex=" + s.refIndex
                //        + ", ref.length=" + s.ref.length);
//                }
            }
        }
    }

    /**
     */
    static void decodePass(G4State s) {
        detectB1(s);
        s.b1 += s.ref[s.refIndex++];
        s.runLength += s.b1 - s.a0;
        s.a0 = s.b1;
        s.b1 += s.ref[s.refIndex++];
    }

    /**
     * @throws java.io.IOException
     */
    static void decodeHorizontal(BitStream in, BitStream out, G4State s, Code code) throws IOException {
        int rl;
        do {
            rl = s.white ? findWhite(in, code) : findBlack(in, code);
            if (rl >= 0) {
                if (rl < 64) {
                    addRun(rl + s.longrun, s, out);
                    s.white = !s.white;
                    s.longrun = 0;
                } else {
                    s.longrun += rl;
                }
            } else {
                addRun(rl, s, out);
            }
        } while (rl >= 64);
        out.close();
    }

    /**
     * @throws java.io.IOException
     */
    static void resetRuns(BitStream outb, G4State state) throws IOException {
        //System.err.println("EOL! "+state.a0);
        state.white = true;
        addRun(0, state, outb);
        if (state.a0 != state.width) {
            //System.out.println( (state.a0 < state.width ? "Premature EOL" : "Line length mismatch") );
            while (state.a0 > state.width)
                state.a0 -= state.cur[--state.curIndex];
            if (state.a0 < state.width) {
                if (state.a0 < 0)
                    state.a0 = 0;
                if ((state.curIndex & 0x1) != 0)
                    addRun(0, state, outb);
                addRun(state.width - state.a0, state, outb);
            } else if (state.a0 > state.width) {
                addRun(state.width, state, outb);
                addRun(0, state, outb);
            }
        }
        int tmp[] = state.ref;
        state.ref = state.cur;
        state.cur = tmp;
        //now zero out extra spots for runs
        for (int i = state.curIndex; i < state.width; i++)
            state.ref[i] = 0;
        for (int i = 0; i < state.width; i++)
            state.cur[i] = 0;
        state.runLength = 0;
        state.a0 = 0;
        state.b1 = state.ref[0];
        state.refIndex = 1;
        state.curIndex = 0;

        outb.close();
    }

    /**
     */
    public static void Group4Decode(InputStream in, OutputStream out, int width, boolean blackIs1) {
        BitStream inb = new BitStream(in);
        BitStream outb = new BitStream(out);
        // assign default colour mapping
        black = 0;
        white = 1;

        // apply blackIs1, which inverts the colour pallet
        if (blackIs1) {
            black = 1;
            white = 0;
        }

        Code code = new Code();

        try {
            G4State graphicState = new G4State(width);
            while (!inb.atEndOfFile()) {
                int mode = readmode(inb, code);
                switch (mode) {
                    case 0:                     // P
                        decodePass(graphicState);
                        continue;
                    case 1:                     // H
                        decodeHorizontal(inb, outb, graphicState, code);
                        decodeHorizontal(inb, outb, graphicState, code);
                        detectB1(graphicState);
                        break;
                    case 2:                     // V0
                        detectB1(graphicState);
                        addRun(graphicState.b1 - graphicState.a0, graphicState, outb);
                        graphicState.white = !graphicState.white;
                        graphicState.b1 += graphicState.ref[graphicState.refIndex++];
                        break;
                    case 3:                     // VR1
                        detectB1(graphicState);
                        addRun(graphicState.b1 - graphicState.a0 + 1, graphicState, outb);
                        graphicState.white = !graphicState.white;
                        graphicState.b1 += graphicState.ref[graphicState.refIndex++];
                        break;
                    case 4:                     // VR2
                        detectB1(graphicState);
                        addRun(graphicState.b1 - graphicState.a0 + 2, graphicState, outb);
                        graphicState.white = !graphicState.white;
                        graphicState.b1 += graphicState.ref[graphicState.refIndex++];
                        break;
                    case 5:                     // VR3
                        detectB1(graphicState);
                        addRun(graphicState.b1 - graphicState.a0 + 3, graphicState, outb);
                        graphicState.white = !graphicState.white;
                        graphicState.b1 += graphicState.ref[graphicState.refIndex++];
                        break;
                    case 6:                     // VL1
                        detectB1(graphicState);
                        addRun(graphicState.b1 - graphicState.a0 - 1, graphicState, outb);
                        graphicState.white = !graphicState.white;
                        if (graphicState.refIndex > 0)
                            graphicState.b1 -= graphicState.ref[--graphicState.refIndex];
                        break;
                    case 7:                     // VL2
                        detectB1(graphicState);
                        addRun(graphicState.b1 - graphicState.a0 - 2, graphicState, outb);
                        graphicState.white = !graphicState.white;
                        if (graphicState.refIndex > 0)
                            graphicState.b1 -= graphicState.ref[--graphicState.refIndex];
                        break;
                    case 8:                     // VL3
                        detectB1(graphicState);
                        addRun(graphicState.b1 - graphicState.a0 - 3, graphicState, outb);
                        graphicState.white = !graphicState.white;
                        if (graphicState.refIndex > 0)
                            graphicState.b1 -= graphicState.ref[--graphicState.refIndex];
                        break;
                    case 11:                    // EOL
                        resetRuns(outb, graphicState);
                        break;
                    default:
                        //System.err.println("UNK! "+mode);
                }
                if (graphicState.a0 >= graphicState.width) {
                    resetRuns(outb, graphicState);
                }
            }
            // do a little memory clean up.
            inb.close();
            outb.close();
            in.close();
            // out.flush(); // need this for further proccessing
            out.close();
        } catch (Exception e) {
            logger.log(Level.FINE, "Error decoding group4 CITTFax", e);
        }
    }

    public static BufferedImage attemptDeriveBufferedImageFromBytes(
            ImageStream stream, Library library, HashMap streamDictionary, Color fill) throws InvocationTargetException, IllegalAccessException {
        if (!USE_JAI_IMAGE_LIBRARY)
            return null;

        boolean imageMask = stream.isImageMask();
        List decodeArray = (List) library.getObject(streamDictionary, ImageStream.DECODE_KEY);
        // get decode parameters from stream properties
        HashMap decodeParmsDictionary = library.getDictionary(streamDictionary, ImageStream.DECODEPARMS_KEY);
        boolean blackIs1 = stream.getBlackIs1(library, decodeParmsDictionary);
        // double check for blackIs1 in the main dictionary.
        if (!blackIs1 && ImageStream.CHECK_PARENT_BLACK_IS_1) {
            blackIs1 = stream.getBlackIs1(library, streamDictionary);
        }
        float k = library.getFloat(decodeParmsDictionary, ImageStream.K_KEY);

        short compression = TIFF_COMPRESSION_NONE_default;
        if (k < 0) compression = TIFF_COMPRESSION_GROUP4;
        else if (k > 0) compression = TIFF_COMPRESSION_GROUP3_2D;
        else if (k == 0) compression = TIFF_COMPRESSION_GROUP3_1D;
        boolean hasHeader;

        InputStream input = stream.getDecodedByteArrayInputStream();
        if (input == null)
            return null;
        input = new ZeroPaddedInputStream(input);
        BufferedInputStream bufferedInput = new BufferedInputStream(input, 1024);
        bufferedInput.mark(4);
        try {
            int hb1 = bufferedInput.read();
            int hb2 = bufferedInput.read();
            bufferedInput.reset();
            if (hb1 < 0 || hb2 < 0) {
                input.close();
                return null;
            }
            hasHeader = ((hb1 == 0x4d && hb2 == 0x4d) || (hb1 == 0x49 && hb2 == 0x49));
        } catch (IOException e) {
            try {
                input.close();
            } catch (IOException ioe) {
                // keep quiet
            }
            return null;
        }
        input = bufferedInput;

        BufferedImage img;

        byte[] fakeHeaderBytes;
        if (!hasHeader) {
            // Apparently if the stream dictionary contains all the necessary info about
            //   the TIFF data in the stream, then some encoders omit the standard
            //   TIFF header in the stream, which confuses some image decoders, like JAI,
            //   in which case we inject a TIFF header which is derived from the stream
            //   dictionary.
            fakeHeaderBytes = new byte[]{
                    // TIFF Header
                    0x4d, 0x4d,                                        // 00 : Big (sane) endian
                    0x00, 0x2a,                                        // 02 : Magic 42
                    0x00, 0x00, 0x00, 0x08,                            // 04 : Offset to first IFD

                    // First IFD
                    0x00, 0x0c,                                        // 08 : Num Directory Entries
                    // Directory Entries: ushort tag, ushort type, uint count, uint valueOrOffset
                    0x00, (byte) 0xfe, 0x00, 0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,  // 0a : NewSubfileType
                    0x01, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,         // 16 : ImageWidth
                    0x01, 0x01, 0x00, 0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,         // 22 : ImageLength
                    0x01, 0x02, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,         // 2E : BitsPerSample
                    0x01, 0x03, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00,         // 3A : Compression
                    0x01, 0x06, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,         // 46 : PhotometricInterpretation
                    0x01, 0x11, 0x00, 0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, (byte) 0xAE,  // 52 : StripOffsets
                    0x01, 0x16, 0x00, 0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,         // 5E : RowsPerStrip
                    0x01, 0x17, 0x00, 0x04, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,         // 6A : StripByteCounts
                    0x01, 0x1A, 0x00, 0x05, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, (byte) 0x9E,  // 76 : XResolution
                    0x01, 0x1B, 0x00, 0x05, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, (byte) 0xA6,  // 82 : YResolution
                    0x01, 0x28, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,         // 8E : ResolutionUnit
                    0x00, 0x00, 0x00, 0x00,                            // 9A : Next IFD
                    // Values from IFD, which don't fit in value field
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,   // 9E : XResolution RATIONAL value
                    0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01}; // A6 : YResolution RATIONAL value
            // AE : Begin data

            // Have to fill in values for: ImageWidth, ImageLength, BitsPerSample, Compression,
            //   PhotometricIntrerpretation, RowsPerStrip, StripByteCounts

            boolean pdfStatesBlackAndWhite = false;
            if (blackIs1) {
                pdfStatesBlackAndWhite = true;
            }
            int width = library.getInt(streamDictionary, ImageStream.WIDTH_KEY);
            int height = library.getInt(streamDictionary, ImageStream.HEIGHT_KEY);

            Object columnsObj = library.getObject(decodeParmsDictionary, ImageStream.COLUMNS_KEY);
            if (columnsObj != null && columnsObj instanceof Number) {
                int columns = ((Number) columnsObj).intValue();
                if (columns > width)
                    width = columns;
            }

            Utils.setIntIntoByteArrayBE(width, fakeHeaderBytes, 0x1E);       // ImageWidth
            Utils.setIntIntoByteArrayBE(height, fakeHeaderBytes, 0x2A);      // ImageLength
            Object bitsPerComponent =                                          // BitsPerSample
                    library.getObject(streamDictionary, ImageStream.BITSPERCOMPONENT_KEY);
            if (bitsPerComponent != null && bitsPerComponent instanceof Number) {
                Utils.setShortIntoByteArrayBE(((Number) bitsPerComponent).shortValue(), fakeHeaderBytes, 0x36);
            }

            Utils.setShortIntoByteArrayBE(compression, fakeHeaderBytes, 0x42);
            short photometricInterpretation = TIFF_PHOTOMETRIC_INTERPRETATION_WHITE_IS_ZERO_default;
            // PDF has default BlackIs1=false               ==> White=1, Black=0
            // TIFF has default PhotometricInterpretation=0 ==> White=0, Black=1
            // So, if PDF doesn't state what black and white are, then use TIFF's default
            if (pdfStatesBlackAndWhite) {
                if (!blackIs1)
                    photometricInterpretation = TIFF_PHOTOMETRIC_INTERPRETATION_BLACK_IS_ZERO;
            }
            Utils.setShortIntoByteArrayBE(                                     // PhotometricInterpretation
                    photometricInterpretation, fakeHeaderBytes, 0x4E);
            Utils.setIntIntoByteArrayBE(height, fakeHeaderBytes, 0x66);      // RowsPerStrip
            int lengthOfCompressedData = Integer.MAX_VALUE - 1;                // StripByteCounts
            Object lengthValue = library.getObject(streamDictionary, Stream.LENGTH_KEY);
            if (lengthValue != null && lengthValue instanceof Number)
                lengthOfCompressedData = ((Number) lengthValue).intValue();
            else {
                // JAI's SeekableStream pukes if we give a number too large
                int approxLen = width * height;
                if (approxLen > 0)
                    lengthOfCompressedData = approxLen;
            }
            Utils.setIntIntoByteArrayBE(lengthOfCompressedData, fakeHeaderBytes, 0x72);

            ByteArrayInputStream fakeHeaderBytesIn = new ByteArrayInputStream(fakeHeaderBytes);
            org.icepdf.core.io.SequenceInputStream sin = new org.icepdf.core.io.SequenceInputStream(fakeHeaderBytesIn, input);

            img = deriveBufferedImageFromTIFFBytes(sin, library, lengthOfCompressedData, width, height, compression);
            if (img == null) {
                for (int i = 1; i <= 4; i++) { // Try the three other types of compression (1, 2, 3, 4)
                    compression++;
                    // We don't try the default uncompressed format, because it sometimes
                    //  returns a blank image, which we don't want.  If JAI fails, we
                    //  want it to return null, so that the fallback code can have a try
                    if (compression > TIFF_COMPRESSION_GROUP4)
                        compression = TIFF_COMPRESSION_GROUP3_1D;

                    Utils.setShortIntoByteArrayBE(compression, fakeHeaderBytes, 0x42);
                    input = stream.getDecodedByteArrayInputStream();
                    if (input == null)
                        return null;
                    input = new ZeroPaddedInputStream(input);
                    fakeHeaderBytesIn = new ByteArrayInputStream(fakeHeaderBytes);
                    sin = new org.icepdf.core.io.SequenceInputStream(fakeHeaderBytesIn, input);
                    img = deriveBufferedImageFromTIFFBytes(sin, library, lengthOfCompressedData, width, height, compression);
                    if (img != null) {
                        break;
                    }
                }
            }
        } else {
            int width = library.getInt(streamDictionary, ImageStream.WIDTH_KEY);
            int height = library.getInt(streamDictionary, ImageStream.HEIGHT_KEY);
            int approxLen = width * height;
            img = deriveBufferedImageFromTIFFBytes(input, library, approxLen, width, height, compression);
        }

        if (img != null) {
            img = applyImageMaskAndDecodeArray(img, imageMask, blackIs1, decodeArray, fill);
        }

        return img;
    }

    /**
     * Calling code assumes that this method will trap all exceptions,
     * so that null shows it didn't work
     *
     * @param in InputStream to TIFF byte data
     * @return RenderedImage if could derive one, else null
     */
    private static BufferedImage deriveBufferedImageFromTIFFBytes(
            InputStream in, Library library, int compressedBytes, int width, int height, int compression) throws InvocationTargetException, IllegalAccessException {
        BufferedImage img = null;
        try {
            /*
            com.sun.media.jai.codec.SeekableStream s = com.sun.media.jai.codec.SeekableStream.wrapInputStream( in, true );
            ParameterBlock pb = new ParameterBlock();
            pb.add( s );
            javax.media.jai.RenderedOp op = javax.media.jai.JAI.create( "tiff", pb );
            */
            Object com_sun_media_jai_codec_SeekableStream_s = ssWrapInputStream.invoke(null, in, Boolean.TRUE);
            ParameterBlock pb = new ParameterBlock();
            pb.add(com_sun_media_jai_codec_SeekableStream_s);
            Object javax_media_jai_RenderedOp_op = jaiCreate.invoke(null, "tiff", pb);

            /*
             * This was another approach:

             TIFFDecodeParam tiffDecodeParam = new TIFFDecodeParam();
             // tiffDecodeParam.setDecodePaletteAsShorts(true);

             ImageDecoder dec = ImageCodec.createImageDecoder("TIFF", s, tiffDecodeParam );

             NullOpImage op = new NullOpImage( dec.decodeAsRenderedImage(0), null, null, OpImage.OP_IO_BOUND );

             // RenderedImage img = dec.decodeAsRenderedImage();
             // RenderedImageAdapter ria = new RenderedImageAdapter(img);
             // BufferedImage bi = ria.getAsBufferedImage();

             */

            if (javax_media_jai_RenderedOp_op != null) {
                if (logger.isLoggable(Level.FINER)) {
                    logger.fine("Decoding TIFF: " + TIFF_COMPRESSION_NAMES[compression]);
                }
                // This forces the image to decode, so we can see if that fails,
                //   and then potentially try a different compression setting
                /* op.getTile( 0, 0 ); */
                RenderedImage ri = (RenderedImage) javax_media_jai_RenderedOp_op;
                Raster r = ri.getTile(0, 0);

                // Calling op.getAsBufferedImage() causes a spike in memory usage
                // For example, for RenderedOp that's 100KB in size, we spike 18MB,
                //   with 1MB remaining and 17MB getting gc'ed
                // So, we try to build it piecemeal instead
                //System.out.println("Memory free: " + Runtime.getRuntime().freeMemory() + ", total:" + Runtime.getRuntime().totalMemory() + ", used: " + (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory()));
                if (r instanceof WritableRaster) {
                    ColorModel cm = ri.getColorModel();
                    img = new BufferedImage(cm, (WritableRaster) r, false, null);
                } else {
                    /* img = op.getAsBufferedImage(); */
                    img = (BufferedImage) roGetAsBufferedImage.invoke(javax_media_jai_RenderedOp_op);
                }
            }
        } catch (Throwable e) {
            // catch and return a null image so we can try again using a different compression method.
            logger.finer("Decoding TIFF: " + TIFF_COMPRESSION_NAMES[compression] + " failed trying alternative");
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                // keep quiet
            }
        }
        return img;
    }

    private static BufferedImage applyImageMaskAndDecodeArray(
            BufferedImage img, boolean imageMask, Boolean blackIs1, List decode, Color fill) {
        // If the image we actually have is monochrome, and so is useful as an image mask
        ColorModel cm = img.getColorModel();
        if (cm instanceof IndexColorModel && cm.getPixelSize() == 1) {
            // From PDF 1.6 spec, concerning ImageMask and Decode array:
            // [0 1] (the default for an image mask), a sample value of 0 marks
            //       the page with the current color, and a 1 leaves the previous
            //       contents unchanged.
            // [1 0] Is the reverse
            // In case alpha transparency doesn't work, it'll paint white opaquely

            boolean defaultDecode =
                    (decode == null) ||
                            (0.0f == ((Number) decode.get(0)).floatValue());
            // From empirically testing 6 of the 9 possible combinations of
            //  BlackIs1 {true, false, not given} and Decode {[0 1], [1 0], not given}
            //  this is the rule. Unknown combinations:
            //    BlackIs1=false, Decode=[0 1] 
            //    BlackIs1=false, Decode=[1 0] 
            //    BlackIs1=true,  Decode=[0 1] 
            boolean flag = ((blackIs1 == null) && (!defaultDecode)) ||
                    ((blackIs1 != null) && blackIs1 && (decode == null));
            if (imageMask) {
                int a = 0x00FFFFFF; // Clear if alpha supported, else white
                int[] cmap = new int[]{
                        (flag ? fill.getRGB() : a),
                        (flag ? a : fill.getRGB())
                };
                int transparentIndex = (flag ? 1 : 0);
                IndexColorModel icm = new IndexColorModel(
                        cm.getPixelSize(),      // the number of bits each pixel occupies
                        cmap.length,            // the size of the color component arrays
                        cmap,                   // the array of color components
                        0,                      // the starting offset of the first color component
                        true,                   // indicates whether alpha values are contained in the cmap array
                        transparentIndex,       // the index of the fully transparent pixel
                        cm.getTransferType());  // the data type of the array used to represent pixel values. The data type must be either DataBuffer.TYPE_BYTE or DataBuffer.TYPE_USHORT
                img = new BufferedImage(
                        icm, img.getRaster(), img.isAlphaPremultiplied(), null);
            } else {
                int[] cmap = new int[]{
                        (flag ? 0xFF000000 : 0xFFFFFFFF),
                        (flag ? 0xFFFFFFFF : 0xFF000000)
                };
                IndexColorModel icm = new IndexColorModel(
                        cm.getPixelSize(),      // the number of bits each pixel occupies
                        cmap.length,            // the size of the color component arrays
                        cmap,                   // the array of color components
                        0,                      // the starting offset of the first color component
                        false,                  // indicates whether alpha values are contained in the cmap array
                        -1,                     // the index of the fully transparent pixel
                        cm.getTransferType());  // the data type of the array used to represent pixel values. The data type must be either DataBuffer.TYPE_BYTE or DataBuffer.TYPE_USHORT
                img = new BufferedImage(
                        icm, img.getRaster(), img.isAlphaPremultiplied(), null);
            }
        }
        return img;
    }

    /*
    public static void showRenderedImage(java.awt.image.RenderedImage ri, String frameTitle) {
        System.out.println("showRenderedImage() \"" + frameTitle + "\"");

        // java.awt.Component djai = new com.sun.media.jai.widget.DisplayJAI( ri );
        java.awt.Component djai = null;
        try {
            Class displayClass = Class.forName("com.sun.media.jai.widget.DisplayJAI");
            if( displayClass != null ) {
                java.lang.reflect.Constructor ctor = displayClass.getConstructor(
                    new Class[] { java.awt.image.RenderedImage.class } );
                djai = (java.awt.Component) ctor.newInstance( new Object[] { ri } );
            }
        }
        catch(Exception e) {
            System.out.println("showRenderedImage()  problem with JAI: " + e);
            return;
        }

        javax.swing.JFrame testFrame = new javax.swing.JFrame( frameTitle );
        testFrame.getContentPane().add( new javax.swing.JScrollPane(djai) );
        testFrame.pack();
        testFrame.setSize( new java.awt.Dimension(900,800) );
        testFrame.setVisible( true );
        System.out.println("showRenderedImage() shown");
    }
    */
}
