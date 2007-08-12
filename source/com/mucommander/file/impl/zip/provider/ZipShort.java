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

package com.mucommander.file.impl.zip.provider;

/**
 * Utility class that represents a two byte integer with conversion
 * rules for the big endian byte order of ZIP files.
 *
 * ------------------------------------------------------------------------------------------------------------------
 *
 * <p>This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 *
 * @author Apache Ant, Maxence Bernard
 */
public final class ZipShort implements Cloneable {

    private int value;

    /**
     * Create instance from a number.
     * @param value the int to store as a ZipShort
     */
    public ZipShort (int value) {
        this.value = value;
    }

    /**
     * Create instance from bytes.
     * @param bytes the bytes to store as a ZipShort
     */
    public ZipShort (byte[] bytes) {
        this(bytes, 0);
    }

    /**
     * Create instance from the two bytes starting at offset.
     * @param bytes the bytes to store as a ZipShort
     * @param offset the offset to start
     */
    public ZipShort (byte[] bytes, int offset) {
        value = ZipShort.getValue(bytes, offset);
    }

    /**
     * Get value as two bytes in big endian byte order.
     * @return the value as a a two byte array in big endian byte order
     */
    public byte[] getBytes() {
        byte[] result = new byte[2];
        result[0] = (byte) (value & 0xFF);
        result[1] = (byte) ((value & 0xFF00) >> 8);
        return result;
    }

    /**
     * Get value as Java int.
     * @return value as a Java int
     */
    public int getValue() {
        return value;
    }

    /**
     * Get value as two bytes in big endian byte order.
     * @param value the Java int to convert to bytes
     * @return the converted int as a byte array in big endian byte order
     */
    public static byte[] getBytes(int value) {
        byte[] result = new byte[2];
        result[0] = (byte) (value & 0xFF);
        result[1] = (byte) ((value & 0xFF00) >> 8);
        return result;
    }

    /**
     * Helper method to get the value as a java int from two bytes starting at given array offset
     * @param bytes the array of bytes
     * @param offset the offset to start
     * @return the correspondanding java int value
     */
    public static int getValue(byte[] bytes, int offset) {
        int value = (bytes[offset + 1] << 8) & 0xFF00;
        value += (bytes[offset] & 0xFF);
        return value;
    }

    /**
     * Helper method to get the value as a java int from a two-byte array
     * @param bytes the array of bytes
     * @return the correspondanding java int value
     */
    public static int getValue(byte[] bytes) {
        return getValue(bytes, 0);
    }

    /**
     * Override to make two instances with same value equal.
     * @param o an object to compare
     * @return true if the objects are equal
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ZipShort)) {
            return false;
        }
        return value == ((ZipShort) o).getValue();
    }

    /**
     * Override to make two instances with same value equal.
     * @return the value stored in the ZipShort
     */
    public int hashCode() {
        return value;
    }
}
