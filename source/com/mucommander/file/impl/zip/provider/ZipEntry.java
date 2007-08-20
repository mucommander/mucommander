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

import java.util.Vector;
import java.util.zip.ZipException;

/**
 * Extension that adds better handling of extra fields and provides
 * access to the internal and external file attributes.
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 *
 * @author Apache Ant, Maxence Bernard
 */
public class ZipEntry extends java.util.zip.ZipEntry implements Cloneable {

    private static final int PLATFORM_UNIX = 3;
    private static final int PLATFORM_FAT  = 0;

    private int internalAttributes = 0;
    private int platform = PLATFORM_FAT;
    private long externalAttributes = 0;
    private Vector/*<ZipExtraField>*/ extraFields = null;
    private String name = null;

    /** Contains info about how this entry is stored in the zip file */
    private ZipEntryInfo entryInfo;

    /**
     * Creates a new zip entry with the specified name.
     * @param name the name of the entry
     */
    public ZipEntry(String name) {
        super(name);
    }

    /**
     * Creates a new zip entry with fields taken from the specified zip entry.
     * @param entry the entry to get fields from
     * @throws ZipException on error
     */
    public ZipEntry(java.util.zip.ZipEntry entry) throws ZipException {
        super(entry);
        byte[] extra = entry.getExtra();
        if (extra != null) {
            setExtraFields(ExtraFieldUtils.parse(extra));
        } else {
            // initializes extra data to an empty byte array
            setExtra();
        }
    }

    /**
     * Creates a new zip entry with fields taken from the specified zip entry.
     * @param entry the entry to get fields from
     * @throws ZipException on error
     */
    public ZipEntry(ZipEntry entry) throws ZipException {
        this((java.util.zip.ZipEntry) entry);
        setInternalAttributes(entry.getInternalAttributes());
        setExternalAttributes(entry.getExternalAttributes());
        setExtraFields(entry.getExtraFields());
    }

    protected ZipEntry() {
        super("");
    }

    /**
     * Retrieves the internal file attributes.
     *
     * @return the internal file attributes
     */
    public int getInternalAttributes() {
        return internalAttributes;
    }

    /**
     * Sets the internal file attributes.
     * @param value an <code>int</code> value
     */
    public void setInternalAttributes(int value) {
        internalAttributes = value;
    }

    /**
     * Retrieves the external file attributes.
     * @return the external file attributes
     */
    public long getExternalAttributes() {
        return externalAttributes;
    }

    /**
     * Sets the external file attributes.
     * @param value an <code>long</code> value
     */
    public void setExternalAttributes(long value) {
        externalAttributes = value;
    }

    /**
     * Sets Unix permissions in a way that is understood by Info-Zip's
     * unzip command.
     * @param mode an <code>int</code> value
     */
    public void setUnixMode(int mode) {
        setExternalAttributes((mode << 16)
                              // MS-DOS read-only attribute
                              | ((mode & 0200) == 0 ? 1 : 0)
                              // MS-DOS directory flag
                              | (isDirectory() ? 0x10 : 0));
        platform = PLATFORM_UNIX;
    }

    /**
     * Unix permission.
     * @return the unix permissions
     */
    public int getUnixMode() {
        return (int) ((getExternalAttributes() >> 16) & 0xFFFF);
    }

    /**
     * Platform specification to put into the &quot;version made
     * by&quot; part of the central file header.
     *
     * @return 0 (MS-DOS FAT) unless {@link #setUnixMode setUnixMode}
     * has been called, in which case 3 (Unix) will be returned.
     */
    public int getPlatform() {
        return platform;
    }

    /**
     * Set the platform (UNIX or FAT).
     * @param platform an <code>int</code> value - 0 is FAT, 3 is UNIX
     */
    protected void setPlatform(int platform) {
        this.platform = platform;
    }

    /**
     * Replaces all currently attached extra fields with the new array.
     * @param fields an array of extra fields
     */
    public void setExtraFields(ZipExtraField[] fields) {
        extraFields = new Vector();
        for (int i = 0; i < fields.length; i++) {
            extraFields.addElement(fields[i]);
        }
        setExtra();
    }

    /**
     * Retrieves extra fields.
     * @return an array of the extra fields
     */
    public ZipExtraField[] getExtraFields() {
        if (extraFields == null) {
            return new ZipExtraField[0];
        }
        ZipExtraField[] result = new ZipExtraField[extraFields.size()];
        extraFields.copyInto(result);
        return result;
    }

    /**
     * Adds an extra fields - replacing an already present extra field
     * of the same type.
     * @param ze an extra field
     */
    public void addExtraField(ZipExtraField ze) {
        if (extraFields == null) {
            extraFields = new Vector();
        }
        ZipShort type = ze.getHeaderId();
        boolean done = false;
        for (int i = 0, fieldsSize = extraFields.size(); !done && i < fieldsSize; i++) {
            if (((ZipExtraField) extraFields.elementAt(i)).getHeaderId().equals(type)) {
                extraFields.setElementAt(ze, i);
                done = true;
            }
        }
        if (!done) {
            extraFields.addElement(ze);
        }
        setExtra();
    }

    /**
     * Remove an extra fields.
     * @param type the type of extra field to remove
     */
    public void removeExtraField(ZipShort type) {
        if (extraFields == null) {
            extraFields = new Vector();
        }
        boolean done = false;
        for (int i = 0, fieldsSize = extraFields.size(); !done && i < fieldsSize; i++) {
            if (((ZipExtraField) extraFields.elementAt(i)).getHeaderId().equals(type)) {
                extraFields.removeElementAt(i);
                done = true;
            }
        }
        if (!done) {
            throw new java.util.NoSuchElementException();
        }
        setExtra();
    }

    /**
     * Unfortunately {@link java.util.zip.ZipOutputStream
     * java.util.zip.ZipOutputStream} seems to access the extra data
     * directly, so overriding getExtra doesn't help - we need to
     * modify super's data directly.
     */
    protected void setExtra() {
        super.setExtra(ExtraFieldUtils.mergeLocalFileDataData(getExtraFields()));
    }

    /**
     * Retrieves the extra data for the local file data.
     * @return the extra data for local file
     */
    public byte[] getLocalFileDataExtra() {
        byte[] extra = getExtra();
        return extra != null ? extra : new byte[0];
    }

    /**
     * Retrieves the extra data for the central directory.
     * @return the central directory extra data
     */
    public byte[] getCentralDirectoryExtra() {
        return ExtraFieldUtils.mergeCentralDirectoryData(getExtraFields());
    }

    /**
     * Set the name of the entry.
     * @param name the name to use
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the {@link ZipEntryInfo} instance that contains info about how this entry is stored in the zip file.
     *
     * @return the {@link ZipEntryInfo} instance that contains info about how this entry is stored in the zip file
     */
    protected ZipEntryInfo getEntryInfo() {
        return entryInfo;
    }

    /**
     * Sets the {@link ZipEntryInfo} instance that contains info about how this entry is stored in the zip file.
     *
     * @param entryInfo the {@link ZipEntryInfo} instance that contains info about how this entry is stored in the zip file
     */
    protected void setEntryInfo(ZipEntryInfo entryInfo) {
        this.entryInfo = entryInfo;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Get the name of the entry.
     * @return the entry name
     */
    public String getName() {
        return name == null ? super.getName() : name;
    }

    /**
     * Is this entry a directory?
     * @return true if the entry is a directory
     */
    public boolean isDirectory() {
        return getName().endsWith("/");
    }

    /**
     * Throws an Exception if extra data cannot be parsed into extra fields.
     * @param extra an array of bytes to be parsed into extra fields
     * @throws RuntimeException if the bytes cannot be parsed
     * @throws RuntimeException on error
     */
    public void setExtra(byte[] extra) throws RuntimeException {
        try {
            setExtraFields(ExtraFieldUtils.parse(extra));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Overwrite clone.
     * @return a cloned copy of this ZipEntry
     */
    public Object clone() {
        ZipEntry e = (ZipEntry) super.clone();

        e.extraFields = extraFields != null ? (Vector) extraFields.clone() : null;
        e.setInternalAttributes(getInternalAttributes());
        e.setExternalAttributes(getExternalAttributes());
        e.setExtraFields(getExtraFields());
        return e;
    }

    /**
     * Get the hashCode of the entry.
     * This uses the name as the hashcode.
     * @return a hashcode.
     */
    public int hashCode() {
        // this method has severe consequences on performance. We cannot rely
        // on the super.hashCode() method since super.getName() always return
        // the empty string in the current implemention (there's no setter)
        // so it is basically draining the performance of a hashmap lookup
        return getName().hashCode();
    }

    public boolean equals(Object o) {
        if(!(o instanceof java.util.zip.ZipEntry))
            return false;

        return ((java.util.zip.ZipEntry)o).getName().equals(getName());
    }

}
