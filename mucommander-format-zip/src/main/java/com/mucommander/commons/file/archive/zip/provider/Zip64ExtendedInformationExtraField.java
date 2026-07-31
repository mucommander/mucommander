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

import java.util.zip.ZipException;

/**
 * Holds size and other extended information for entries that use Zip64 features.
 *
 * <p>See <a href="https://www.pkware.com/documents/casestudies/APPNOTE.TXT">PKWARE's
 * APPNOTE.TXT, section 4.5.3</a>.</p>
 *
 * <p>The implementation relies on data being read from the local file header and assumes that
 * both size values are always present.</p>
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file.</p>
 *
 * @author Apache Ant
 */
public class Zip64ExtendedInformationExtraField implements CentralDirectoryParsingZipExtraField {

    /** Size in bytes of a four byte (32 bit) field */
    private static final int WORD = 4;

    /** Size in bytes of an eight byte (64 bit) field */
    private static final int DWORD = 8;

    static final ZipShort HEADER_ID = new ZipShort(0x0001);

    private static final String LFH_MUST_HAVE_BOTH_SIZES_MSG =
        "Zip64 extended information must contain"
        + " both size values in the local file header.";
    private static final byte[] EMPTY = new byte[0];

    private ZipEightByteInteger size, compressedSize, relativeHeaderOffset;
    private ZipLong diskStart;

    /**
     * Stored in {@link #parseFromCentralDirectoryData parseFromCentralDirectoryData} so it can be
     * reused when {@link #reparseCentralDirectoryData reparseCentralDirectoryData} is called.
     *
     * <p>Not used for anything else</p>
     */
    private byte[] rawCentralDirectoryData;

    /**
     * This constructor should only be used by the code that reads archives.
     */
    public Zip64ExtendedInformationExtraField() {
    }

    /**
     * Creates an extra field based on the original and compressed size.
     *
     * @param size the entry's original size
     * @param compressedSize the entry's compressed size
     * @throws IllegalArgumentException if size or compressedSize is null
     */
    public Zip64ExtendedInformationExtraField(ZipEightByteInteger size, ZipEightByteInteger compressedSize) {
        this(size, compressedSize, null, null);
    }

    /**
     * Creates an extra field based on all four possible values.
     *
     * @param size the entry's original size
     * @param compressedSize the entry's compressed size
     * @param relativeHeaderOffset the entry's offset
     * @param diskStart the disk on which the entry starts
     * @throws IllegalArgumentException if size or compressedSize is null
     */
    public Zip64ExtendedInformationExtraField(ZipEightByteInteger size, ZipEightByteInteger compressedSize,
            ZipEightByteInteger relativeHeaderOffset, ZipLong diskStart) {
        this.size = size;
        this.compressedSize = compressedSize;
        this.relativeHeaderOffset = relativeHeaderOffset;
        this.diskStart = diskStart;
    }

    @Override
    public ZipShort getHeaderId() {
        return HEADER_ID;
    }

    @Override
    public ZipShort getLocalFileDataLength() {
        return new ZipShort(size != null ? 2 * DWORD : 0);
    }

    @Override
    public ZipShort getCentralDirectoryLength() {
        return new ZipShort((size != null ? DWORD : 0)
                + (compressedSize != null ? DWORD : 0)
                + (relativeHeaderOffset != null ? DWORD : 0)
                + (diskStart != null ? WORD : 0));
    }

    @Override
    public byte[] getLocalFileDataData() {
        if (size != null || compressedSize != null) {
            if (size == null || compressedSize == null) {
                throw new IllegalArgumentException(LFH_MUST_HAVE_BOTH_SIZES_MSG);
            }
            byte[] data = new byte[2 * DWORD];
            addSizes(data);
            return data;
        }
        return EMPTY;
    }

    @Override
    public byte[] getCentralDirectoryData() {
        byte[] data = new byte[getCentralDirectoryLength().getValue()];
        int off = addSizes(data);
        if (relativeHeaderOffset != null) {
            System.arraycopy(relativeHeaderOffset.getBytes(), 0, data, off, DWORD);
            off += DWORD;
        }
        if (diskStart != null) {
            System.arraycopy(diskStart.getBytes(), 0, data, off, WORD);
            off += WORD;
        }
        return data;
    }

    @Override
    public void parseFromLocalFileData(byte[] buffer, int offset, int length) throws ZipException {
        if (length == 0) {
            // no local file data at all, may happen if an archive only holds a Zip64 extended
            // information extra field inside the central directory but not inside the local file header
            return;
        }
        if (length < 2 * DWORD) {
            throw new ZipException(LFH_MUST_HAVE_BOTH_SIZES_MSG);
        }
        size = new ZipEightByteInteger(buffer, offset);
        offset += DWORD;
        compressedSize = new ZipEightByteInteger(buffer, offset);
        offset += DWORD;
        int remaining = length - 2 * DWORD;
        if (remaining >= DWORD) {
            relativeHeaderOffset = new ZipEightByteInteger(buffer, offset);
            offset += DWORD;
            remaining -= DWORD;
        }
        if (remaining >= WORD) {
            diskStart = new ZipLong(buffer, offset);
        }
    }

    @Override
    public void parseFromCentralDirectoryData(byte[] buffer, int offset, int length) throws ZipException {
        // store for processing in reparseCentralDirectoryData
        rawCentralDirectoryData = new byte[length];
        System.arraycopy(buffer, offset, rawCentralDirectoryData, 0, length);

        // if there is no size information in here, we can only hope things will get resolved
        // by local file header data later. But there are some cases that can be detected:
        // * all data is there
        // * length == 24 -> both sizes and offset
        // * length % 8 == 4 -> at least we can identify the diskStart field
        if (length >= 3 * DWORD + WORD) {
            parseFromLocalFileData(buffer, offset, length);
        } else if (length == 3 * DWORD) {
            size = new ZipEightByteInteger(buffer, offset);
            offset += DWORD;
            compressedSize = new ZipEightByteInteger(buffer, offset);
            offset += DWORD;
            relativeHeaderOffset = new ZipEightByteInteger(buffer, offset);
        } else if (length % DWORD == WORD) {
            diskStart = new ZipLong(buffer, offset + length - WORD);
        }
    }

    /**
     * Parses the raw bytes read from the central directory extra field with knowledge which fields
     * are expected to be there.
     *
     * <p>All four fields inside the Zip64 extended information extra field are optional and must
     * only be present if their corresponding entry inside the central directory contains the
     * correct magic value.</p>
     *
     * @param hasUncompressedSize whether the central directory's uncompressed size field is set to 0xFFFFFFFF
     * @param hasCompressedSize whether the central directory's compressed size field is set to 0xFFFFFFFF
     * @param hasRelativeHeaderOffset whether the central directory's relative header offset field is set to 0xFFFFFFFF
     * @param hasDiskStart whether the central directory's disk start field is set to 0xFFFF
     * @throws ZipException if the expected length of the central directory data is incorrect
     */
    public void reparseCentralDirectoryData(boolean hasUncompressedSize, boolean hasCompressedSize,
            boolean hasRelativeHeaderOffset, boolean hasDiskStart) throws ZipException {
        if (rawCentralDirectoryData != null) {
            int expectedLength = (hasUncompressedSize ? DWORD : 0)
                    + (hasCompressedSize ? DWORD : 0)
                    + (hasRelativeHeaderOffset ? DWORD : 0)
                    + (hasDiskStart ? WORD : 0);
            if (rawCentralDirectoryData.length < expectedLength) {
                throw new ZipException("central directory zip64 extended information extra field's length"
                        + " doesn't match central directory data.  Expected length "
                        + expectedLength + " but is " + rawCentralDirectoryData.length);
            }
            int offset = 0;
            if (hasUncompressedSize) {
                size = new ZipEightByteInteger(rawCentralDirectoryData, offset);
                offset += DWORD;
            }
            if (hasCompressedSize) {
                compressedSize = new ZipEightByteInteger(rawCentralDirectoryData, offset);
                offset += DWORD;
            }
            if (hasRelativeHeaderOffset) {
                relativeHeaderOffset = new ZipEightByteInteger(rawCentralDirectoryData, offset);
                offset += DWORD;
            }
            if (hasDiskStart) {
                diskStart = new ZipLong(rawCentralDirectoryData, offset);
            }
        }
    }

    /**
     * The uncompressed size stored in this extra field.
     * @return the uncompressed size
     */
    public ZipEightByteInteger getSize() {
        return size;
    }

    /**
     * Sets the uncompressed size stored in this extra field.
     * @param size the uncompressed size
     */
    public void setSize(ZipEightByteInteger size) {
        this.size = size;
    }

    /**
     * The compressed size stored in this extra field.
     * @return the compressed size
     */
    public ZipEightByteInteger getCompressedSize() {
        return compressedSize;
    }

    /**
     * Sets the compressed size stored in this extra field.
     * @param compressedSize the compressed size
     */
    public void setCompressedSize(ZipEightByteInteger compressedSize) {
        this.compressedSize = compressedSize;
    }

    /**
     * The relative header offset stored in this extra field.
     * @return the relative header offset
     */
    public ZipEightByteInteger getRelativeHeaderOffset() {
        return relativeHeaderOffset;
    }

    /**
     * Sets the relative header offset stored in this extra field.
     * @param relativeHeaderOffset the relative header offset
     */
    public void setRelativeHeaderOffset(ZipEightByteInteger relativeHeaderOffset) {
        this.relativeHeaderOffset = relativeHeaderOffset;
    }

    /**
     * The disk start number stored in this extra field.
     * @return the disk start number
     */
    public ZipLong getDiskStartNumber() {
        return diskStart;
    }

    /**
     * Sets the disk start number stored in this extra field.
     * @param diskStart the disk start number
     */
    public void setDiskStartNumber(ZipLong diskStart) {
        this.diskStart = diskStart;
    }

    private int addSizes(byte[] data) {
        int off = 0;
        if (size != null) {
            System.arraycopy(size.getBytes(), 0, data, 0, DWORD);
            off += DWORD;
        }
        if (compressedSize != null) {
            System.arraycopy(compressedSize.getBytes(), 0, data, off, DWORD);
            off += DWORD;
        }
        return off;
    }
}
