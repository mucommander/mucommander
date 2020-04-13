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
package org.icepdf.core.util;

import java.io.*;

/**
 * Utility class for formatting byte[] data in the nicely organized hex dump format.  In a hex dump,
 * each byte is represented as a two-digit hexadecimal number.  Each row consists of 16 bytes separated
 * by white space.  Each row is also pre appended with the memory address and post appended with the ASCII
 * text for bytes.
 */
public class HexDumper {

    public static final int BYTE_PER_ROW = 16;

    private PrintStream printStream;
    private int currentLineLength;
    private int currentByte;
    private int offset;
    private byte[] thisLine = new byte[BYTE_PER_ROW];

    /**
     * Dump the hex bytes for the given input.
     *
     * @param inputBytes bytes to format at a hex dump.
     * @return hex dump formatted byte data.
     */
    public String dump(byte[] inputBytes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inputBytes);
        try {
            this.dump(inputStream, outputStream);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read input stream. ");
        }
        return outputStream.toString();
    }

    /**
     * Dump the hex bytes for the given input to the specified out stream.
     *
     * @param inputBytes   bytes to convert to hex dump format.
     * @param outputStream output bytes where hex dump is written to.
     * @throws IOException
     */
    public void dump(InputStream inputBytes, OutputStream outputStream) throws IOException {
        byte[] lineBytes = new byte[BYTE_PER_ROW];
        reset(outputStream);
        int lineLength;
        do {
            lineLength = readFully(inputBytes, lineBytes);
            if (lineLength == 0) {
                break;
            }
            writeMemoryOffset(lineLength);
            // write out the bytes.
            for (int i = 0; i < lineLength; i++) {
                writeHexBytes(lineBytes, i);
            }
            writeASCIISummary();
        } while (lineLength >= BYTE_PER_ROW);
    }

    /**
     * Reads a line of byte data, ~16 bytes.
     *
     * @param inputBytes input array > 16 bytes.
     * @param lineBytes  output data.
     * @return length of bytes copied.
     * @throws IOException
     */
    private int readFully(InputStream inputBytes, byte[] lineBytes) throws IOException {
        for (int i = 0; i < lineBytes.length; ++i) {
            int j = inputBytes.read();
            if (j == -1) {
                return i;
            }
            lineBytes[i] = (byte) j;
        }
        return lineBytes.length;
    }

    private void reset(OutputStream var1) throws IOException {
        offset = 0;
        printStream = new PrintStream(var1);
    }

    /**
     * Write the memory offset to the start of each line.
     *
     * @param offsetLength line length.
     * @throws IOException
     */
    protected void writeMemoryOffset(int offsetLength) throws IOException {
        convertByteToHex(printStream, (byte) (offset >>> 8 & 255));
        convertByteToHex(printStream, (byte) (offset & 255));
        printStream.print(": ");
        currentByte = 0;
        currentLineLength = offsetLength;
    }

    /**
     * Writes out the byte[] one byte at a time converted to hex.
     *
     * @param byteArray array of data
     * @param length    length of bytes of convert.
     * @throws IOException
     */
    private void writeHexBytes(byte[] byteArray, int length) throws IOException {
        thisLine[this.currentByte] = byteArray[length];
        convertByteToHex(this.printStream, byteArray[length]);
        printStream.print(" ");
        currentByte++;
        if (currentByte == 8) {
            printStream.print("  ");
        }
    }

    /**
     * Write ASCII data out to end of pritn stream.
     *
     * @throws IOException
     */
    private void writeASCIISummary() throws IOException {
        // add some column padding
        if (currentLineLength < BYTE_PER_ROW) {
            for (int i = currentLineLength; i < BYTE_PER_ROW; i++) {
                printStream.print("   ");
                if (i == 7) {
                    printStream.print("  ");
                }
            }
        }
        printStream.print(" ");
        // write out he ASCII version of the bytes which may or may not having meaning to end user.
        for (int i = 0; i < currentLineLength; i++) {
            if (this.thisLine[i] >= 32 && this.thisLine[i] <= 122) {
                printStream.write(this.thisLine[i]);
            } else {
                printStream.print(".");
            }
        }
        printStream.println();
        offset += currentLineLength;
    }

    /**
     * Convert byteValue to a two char hex number.
     *
     * @param printStream output stream.
     * @param byteValue   byte value to convert.
     */
    private void convertByteToHex(PrintStream printStream, byte byteValue) {
        // convert the first 4 bites to hex
        char hexChar = (char) (byteValue >> 4 & 15);
        if (hexChar > 9) {
            hexChar = (char) (hexChar - 10 + 65);
        } else {
            hexChar = (char) (hexChar + 48);
        }
        printStream.write(hexChar);
        // covert next 4 bits to hex.
        hexChar = (char) (byteValue & 15);
        if (hexChar > 9) {
            hexChar = (char) (hexChar - 10 + 65);
        } else {
            hexChar = (char) (hexChar + 48);
        }
        printStream.write(hexChar);
    }
}
