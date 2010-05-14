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

package com.mucommander.commons.file.impl.zip.provider;

import java.util.zip.CRC32;
import java.util.zip.ZipException;

/**
 * Adds Unix file permission and UID/GID fields as well as symbolic
 * link handling.
 *
 * <p>This class uses the ASi extra field in the format:
 * <pre>
 *         Value         Size            Description
 *         -----         ----            -----------
 * (Unix3) 0x756e        Short           tag for this extra block type
 *         TSize         Short           total data size for this block
 *         CRC           Long            CRC-32 of the remaining data
 *         Mode          Short           file permissions
 *         SizDev        Long            symlink'd size OR major/minor dev num
 *         UID           Short           user ID
 *         GID           Short           group ID
 *         (var.)        variable        symbolic link filename
 * </pre>
 * taken from appnote.iz (Info-ZIP note, 981119) found at <a
 * href="ftp://ftp.uu.net/pub/archiving/zip/doc/">ftp://ftp.uu.net/pub/archiving/zip/doc/</a></p>
 *
 * <p>Short is two bytes and Long is four bytes in big endian byte and
 * word order, device numbers are currently not supported.</p>
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 * 
 * @author Apache Ant, Maxence Bernard
 */
public class AsiExtraField implements ZipExtraField, UnixStat, Cloneable {

    private static final ZipShort HEADER_ID = new ZipShort(0x756E);

    /**
     * Standard Unix stat(2) file mode.
     */
    private int mode = 0;
    /**
     * User ID.
     */
    private int uid = 0;
    /**
     * Group ID.
     */
    private int gid = 0;
    /**
     * File this entry points to, if it is a symbolic link.
     *
     * <p>empty string - if entry is not a symbolic link.</p>
     */
    private String link = "";
    /**
     * Is this an entry for a directory?
     */
    private boolean dirFlag = false;

    /**
     * Instance used to calculate checksums.
     */
    private CRC32 crc = new CRC32();

    /** Constructor for AsiExtraField. */
    public AsiExtraField() {
    }

    /**
     * The Header-ID.
     * @return the value for the header id for this extrafield
     */
    public ZipShort getHeaderId() {
        return HEADER_ID;
    }

    /**
     * Length of the extra field in the local file data - without
     * Header-ID or length specifier.
     * @return a <code>ZipShort</code> for the length of the data of this extra field
     */
    public ZipShort getLocalFileDataLength() {
        return new ZipShort(4         // CRC
                          + 2         // Mode
                          + 4         // SizDev
                          + 2         // UID
                          + 2         // GID
                          + getLinkedFile().getBytes().length);
    }

    /**
     * Delegate to local file data.
     * @return the centralDirectory length
     */
    public ZipShort getCentralDirectoryLength() {
        return getLocalFileDataLength();
    }

    /**
     * The actual data to put into local file data - without Header-ID
     * or length specifier.
     * @return get the data
     */
    public byte[] getLocalFileDataData() {
        // CRC will be added later
        byte[] data = new byte[getLocalFileDataLength().getValue() - 4];
        ZipShort.getBytes(getMode(), data, 0);

        byte[] linkArray = getLinkedFile().getBytes();
        ZipLong.getBytes(linkArray.length, data, 2);
        ZipShort.getBytes(getUserId(), data, 6);
        ZipShort.getBytes(getGroupId(), data, 8);
        System.arraycopy(linkArray, 0, data, 10, linkArray.length);

        crc.reset();
        crc.update(data);
        long checksum = crc.getValue();

        byte[] result = new byte[data.length + 4];
        ZipLong.getBytes(checksum, result, 0);
        System.arraycopy(data, 0, result, 4, data.length);

        return result;
    }

    /**
     * Delegate to local file data.
     * @return the local file data
     */
    public byte[] getCentralDirectoryData() {
        return getLocalFileDataData();
    }

    /**
     * Set the user id.
     * @param uid the user id
     */
    public void setUserId(int uid) {
        this.uid = uid;
    }

    /**
     * Get the user id.
     * @return the user id
     */
    public int getUserId() {
        return uid;
    }

    /**
     * Set the group id.
     * @param gid the group id
     */
    public void setGroupId(int gid) {
        this.gid = gid;
    }

    /**
     * Get the group id.
     * @return the group id
     */
    public int getGroupId() {
        return gid;
    }

    /**
     * Indicate that this entry is a symbolic link to the given filename.
     *
     * @param name Name of the file this entry links to, empty String
     *             if it is not a symbolic link.
     */
    public void setLinkedFile(String name) {
        link = name;
        mode = getMode(mode);
    }

    /**
     * Name of linked file
     *
     * @return name of the file this entry links to if it is a
     *         symbolic link, the empty string otherwise.
     */
    public String getLinkedFile() {
        return link;
    }

    /**
     * Is this entry a symbolic link?
     * @return true if this is a symbolic link
     */
    public boolean isLink() {
        return getLinkedFile().length() != 0;
    }

    /**
     * File mode of this file.
     * @param mode the file mode
     */
    public void setMode(int mode) {
        this.mode = getMode(mode);
    }

    /**
     * File mode of this file.
     * @return the file mode
     */
    public int getMode() {
        return mode;
    }

    /**
     * Indicate whether this entry is a directory.
     * @param dirFlag if true, this entry is a directory
     */
    public void setDirectory(boolean dirFlag) {
        this.dirFlag = dirFlag;
        mode = getMode(mode);
    }

    /**
     * Is this entry a directory?
     * @return true if this entry is a directory
     */
    public boolean isDirectory() {
        return dirFlag && !isLink();
    }

    /**
     * Populate data from this array as if it was in local file data.
     * @param data an array of bytes
     * @param offset the start offset
     * @param length the number of bytes in the array from offset
     * @throws ZipException on error
     */
    public void parseFromLocalFileData(byte[] data, int offset, int length)
        throws ZipException {

        long givenChecksum = ZipLong.getValue(data, offset);
        byte[] tmp = new byte[length - 4];
        System.arraycopy(data, offset + 4, tmp, 0, length - 4);
        crc.reset();
        crc.update(tmp);
        long realChecksum = crc.getValue();
        if (givenChecksum != realChecksum) {
            throw new ZipException("bad CRC checksum "
                                   + Long.toHexString(givenChecksum)
                                   + " instead of "
                                   + Long.toHexString(realChecksum));
        }

        int newMode = ZipShort.getValue(tmp, 0);
        byte[] linkArray = new byte[(int) ZipLong.getValue(tmp, 2)];
        uid = ZipShort.getValue(tmp, 6);
        gid = ZipShort.getValue(tmp, 8);

        if (linkArray.length == 0) {
            link = "";
        } else {
            System.arraycopy(tmp, 10, linkArray, 0, linkArray.length);
            link = new String(linkArray);
        }
        setDirectory((newMode & DIR_FLAG) != 0);
        setMode(newMode);
    }

    /**
     * Get the file mode for given permissions with the correct file type.
     * @param mode the mode
     * @return the type with the mode
     */
    protected int getMode(int mode) {
        int type = FILE_FLAG;
        if (isLink()) {
            type = LINK_FLAG;
        } else if (isDirectory()) {
            type = DIR_FLAG;
        }
        return type | (mode & PERM_MASK);
    }

}
