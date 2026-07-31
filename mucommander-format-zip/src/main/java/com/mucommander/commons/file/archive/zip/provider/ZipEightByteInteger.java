/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mucommander.commons.file.archive.zip.provider;

/**
 * Utility class that represents an eight byte integer with conversion
 * rules for the big endian byte order of ZIP files.
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file.</p>
 *
 * @author Apache Ant
 */
public final class ZipEightByteInteger implements Cloneable {

    /** A {@code ZipEightByteInteger} instance representing the value 0 */
    public static final ZipEightByteInteger ZERO = new ZipEightByteInteger(0);

    private long value;

    /**
     * Create instance from a number.
     * @param value the long to store as a ZipEightByteInteger
     */
    public ZipEightByteInteger(long value) {
        this.value = value;
    }

    /**
     * Create instance from bytes.
     * @param bytes the bytes to store as a ZipEightByteInteger
     */
    public ZipEightByteInteger(byte[] bytes) {
        this(bytes, 0);
    }

    /**
     * Create instance from the eight bytes starting at offset.
     * @param bytes the bytes to store as a ZipEightByteInteger
     * @param offset the offset to start
     */
    public ZipEightByteInteger(byte[] bytes, int offset) {
        value = ZipEightByteInteger.getLongValue(bytes, offset);
    }

    /**
     * Get value as eight bytes in big endian byte order.
     * @return value as eight bytes in big endian order
     */
    public byte[] getBytes() {
        return ZipEightByteInteger.getBytes(value);
    }

    /**
     * Get value as Java long.
     * @return value as a long
     */
    public long getLongValue() {
        return value;
    }

    /**
     * Converts the given long value as eight bytes in big endian byte order.
     * @param value the long value to convert
     * @return the converted value as a byte array in big endian byte order
     */
    public static byte[] getBytes(long value) {
        return getBytes(value, new byte[8], 0);
    }

    /**
     * Converts the given long value as eight bytes in big endian byte order. The specified byte array is used to
     * store the result, starting at the given offset. The returned byte array is the same as the given one.
     * @param value the long value to convert
     * @param result the byte array in which to store the value in big endian byte order
     * @param off offset at which to start writing the result in the array
     * @return the converted value as a byte array in big endian byte order
     */
    public static byte[] getBytes(long value, byte[] result, int off) {
        result[off]   = (byte) ((value & 0xFFL));
        result[off+1] = (byte) ((value & 0xFF00L) >> 8);
        result[off+2] = (byte) ((value & 0xFF0000L) >> 16);
        result[off+3] = (byte) ((value & 0xFF000000L) >> 24);
        result[off+4] = (byte) ((value & 0xFF00000000L) >> 32);
        result[off+5] = (byte) ((value & 0xFF0000000000L) >> 40);
        result[off+6] = (byte) ((value & 0xFF000000000000L) >> 48);
        result[off+7] = (byte) ((value & 0xFF00000000000000L) >> 56);
        return result;
    }

    /**
     * Helper method to get the value as a Java long from eight bytes starting at given array offset
     * @param bytes the array of bytes
     * @param offset the offset to start
     * @return the corresponding Java long value
     */
    public static long getLongValue(byte[] bytes, int offset) {
        long value = (bytes[offset+7] << 56) & 0xFF00000000000000L;
        value += ((long)bytes[offset+6] << 48) & 0xFF000000000000L;
        value += ((long)bytes[offset+5] << 40) & 0xFF0000000000L;
        value += ((long)bytes[offset+4] << 32) & 0xFF00000000L;
        value += ((long)bytes[offset+3] << 24) & 0xFF000000L;
        value += ((long)bytes[offset+2] << 16) & 0xFF0000L;
        value += ((long)bytes[offset+1] << 8) & 0xFF00L;
        value += ((long)bytes[offset] & 0xFFL);
        return value;
    }

    /**
     * Helper method to get the value as a Java long from an eight-byte array
     * @param bytes the array of bytes
     * @return the corresponding Java long value
     */
    public static long getLongValue(byte[] bytes) {
        return getLongValue(bytes, 0);
    }

    /**
     * Override to make two instances with same value equal.
     * @param o an object to compare
     * @return true if the objects are equal
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ZipEightByteInteger)) {
            return false;
        }
        return value == ((ZipEightByteInteger) o).getLongValue();
    }

    /**
     * Override to make two instances with same value equal.
     * @return the value stored in the ZipEightByteInteger
     */
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }
}
