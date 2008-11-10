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

import java.util.Calendar;
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

    /** Name/path of this entry */
    protected String name = null;

    /** Data/time of this entry, in the DOS time format */
    protected long dosTime = -1;

    /** Data/time of this entry, in the Java time format */
    protected long javaTime = -1;

    /** Platform, part of the 'version made by' central directory field */
    protected int platform = PLATFORM_FAT;

    /** Internal attributes (2 bytes) */
    protected int internalAttributes = 0;

    /** External attributes (4 bytes) */
    protected long externalAttributes = 0;

    /** List of extra fields, as ZipEntraField instances */
    protected Vector/*<ZipExtraField>*/ extraFields = null;

    /** Contains info about how this entry is stored in the zip file */
    protected ZipEntryInfo entryInfo;

    /** An instance of Calendar shared through all instances of this class and used for Java<->DOS time conversion */
    protected final static Calendar CALENDAR = Calendar.getInstance();

    /** Smallest DOS time (Epoch 1980) */
    protected final static long MIN_DOS_TIME = 0x00002100L;

    /** Value of the bit flag that denotes a Unix directory in the external attributes */
    protected final static int UNIX_DIRECTORY_FLAG = 16384;
    /** Value of the bit flag that denotes a Unix file in the external attributes */
    protected final static int UNIX_FILE_FLAG = 32768;

    /** Value of the bit flag that denotes an MS-DOS directory in the external attributes */
    protected final static int MSDOS_DIRECTORY_FLAG = 0x10;
    /** Value of the bit flag that denotes a read-only MS-DOS file in the external attributes */
    protected final static int MSDOS_READ_ONLY_FLAG = 1;

    /** Value of the user write permission bit */
    protected final static int USER_WRITE_PERMISSION_BIT = 128;

    /** Value of the Unix platform used in the 'version made by' central directory field */
    protected static final int PLATFORM_UNIX = 3;
    /** Value of the MSDOS/OS-2 platform (FAT filesystem) used in the 'version made by' central directory field */
    protected static final int PLATFORM_FAT  = 0;


    /**
     * Creates a new zip entry with the specified name.
     *
     * @param name the name of the entry
     */
    public ZipEntry(String name) {
        super(name);
    }

    /**
     * Creates a new zip entry with fields taken from the specified zip entry.
     *
     * @param entry the entry to get fields from
     * @throws ZipException on error
     */
    public ZipEntry(java.util.zip.ZipEntry entry) throws ZipException {
        super(entry);

        byte[] extra = entry.getExtra();
        if (extra != null) {
            setExtraFields(ExtraFieldUtils.parse(extra));
        }
        else {
            // initializes extra data to an empty byte array
            setExtra();
        }
    }

    /**
     * Creates a new zip entry with fields taken from the specified zip entry.
     *
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
     *
     * @param value an <code>int</code> value
     */
    public void setInternalAttributes(int value) {
        internalAttributes = value;
    }

    /**
     * Retrieves the external file attributes.
     *
     * @return the external file attributes
     */
    public long getExternalAttributes() {
        return externalAttributes;
    }

    /**
     * Sets the external file attributes.
     *
     * @param value an <code>long</code> value
     */
    public void setExternalAttributes(long value) {
        externalAttributes = value;
    }

    /**
     * Sets Unix permissions in a way that is understood by Info-Zip's unzip command.
     *
     * @param mode an <code>int</code> value
     */
    public void setUnixMode(int mode) {
        boolean isDirectory = isDirectory();

        setExternalAttributes(
              // Unix directory flag
              ((isDirectory ? UNIX_DIRECTORY_FLAG : UNIX_FILE_FLAG) << 16)
              // Unix file permissions
              | (mode << 16)
              // MS-DOS read-only attribute
              | ((mode & USER_WRITE_PERMISSION_BIT) == 0 ? MSDOS_READ_ONLY_FLAG : 0)
              // MS-DOS directory flag
              | (isDirectory ? MSDOS_DIRECTORY_FLAG : 0));

        platform = PLATFORM_UNIX;
    }

    /**
     * Unix permission.
     *
     * @return the unix permissions
     */
    public int getUnixMode() {
        return (int) ((getExternalAttributes() >> 16) & 0xFFFF);
    }

    /**
     * Returns <code>true</code> if this ZipEntry has Unix mode/permissions.
     * If that's not the case, the value returned by {@link #getUnixMode()} has no meaning.
     *
     * @return <code>true</code> if this ZipEntry has Unix mode/permissions
     */
    public boolean hasUnixMode() {
        return getPlatform()==PLATFORM_UNIX;
    }

    /**
     * Returns the platform specification to put into the 'version made by' part of the central file header.
     *
     * @return {@link #PLATFORM_FAT} unless {@link #setUnixMode setUnixMode} has been called,
     * in which case {@link #PLATFORM_UNIX} will be returned.
     */
    public int getPlatform() {
        return platform;
    }

    /**
     * Sets the platform: {@link #PLATFORM_FAT} or {@link #PLATFORM_UNIX}.
     *
     * @param platform {@link #PLATFORM_FAT} or {@link #PLATFORM_UNIX}
     */
    protected void setPlatform(int platform) {
        this.platform = platform;
    }

    /**
     * Replaces all currently attached extra fields with the new array.
     *
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
     *
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
     * Adds an extra fields - replacing an already present extra field of the same type.
     *
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
     *
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
     * Unfortunately {@link java.util.zip.ZipOutputStream} seems to access the extra data directly, so overriding
     * getExtra doesn't help - we need to modify super's data directly.
     */
    protected void setExtra() {
        super.setExtra(ExtraFieldUtils.mergeLocalFileDataData(getExtraFields()));
    }

    /**
     * Retrieves the extra data for the local file data.
     *
     * @return the extra data for local file
     */
    public byte[] getLocalFileDataExtra() {
        byte[] extra = getExtra();
        return extra != null ? extra : new byte[0];
    }

    /**
     * Retrieves the extra data for the central directory.
     *
     * @return the central directory extra data
     */
    public byte[] getCentralDirectoryExtra() {
        return ExtraFieldUtils.mergeCentralDirectoryData(getExtraFields());
    }

    /**
     * Set the name of the entry.
     *
     * @param name the name to use
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Returns this entry's date/time expressed in the DOS time format.
     *
     * @return this entry's date/time expressed in the DOS time format
     */
    protected long getDosTime() {
        return dosTime;
    }

    /**
     * Sets this entry's date/time to the specified one. The time must be expressed in the DOS time format.
     *
     * @param dosTime the new time of this entry, expressed in the DOS time format
     */
    protected void setDosTime(long dosTime) {
        this.dosTime = dosTime;
        this.javaTime = dosTime==-1?-1:dosToJavaTime(dosTime);

        // Unfortunately, the 'time' field is private in java.util.zip.ZipEntry, so we must call the setter which
        // converts the time to the DOS format again => unefficient
        super.setTime(this.javaTime);
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

    /*
     * Converts DOS time (Epoch=1980) to Java time (Epoch=1970).
     *
     * @param dosTime time expressed in the convoluted DOS time format
     * @return time expressed as the number of milliseconds since the epoch
     */
    private static long dosToJavaTime(long dosTime) {
        synchronized(CALENDAR) {
            CALENDAR.set(Calendar.YEAR, (int) ((dosTime >> 25) & 0x7f) + 1980);
            CALENDAR.set(Calendar.MONTH, (int) ((dosTime >> 21) & 0x0f) - 1);
            CALENDAR.set(Calendar.DATE, (int) (dosTime >> 16) & 0x1f);
            CALENDAR.set(Calendar.HOUR_OF_DAY, (int) (dosTime >> 11) & 0x1f);
            CALENDAR.set(Calendar.MINUTE, (int) (dosTime >> 5) & 0x3f);
            CALENDAR.set(Calendar.SECOND, (int) (dosTime << 1) & 0x3e);

            return CALENDAR.getTimeInMillis();
        }
    }

    /**
     * Converts Java time (Epoch=1970) to DOS time (Epoch=1980).
     *
     * @param javaTime number of milliseconds since the epoch
     * @return time expressed in the convoluted DOS time format
     */
    protected static long javaToDosTime(long javaTime) {
        synchronized(CALENDAR) {
            CALENDAR.setTimeInMillis(javaTime);

            int year = CALENDAR.get(Calendar.YEAR);
            if (year < 1980) {
                return MIN_DOS_TIME;
            }

            return ((year - 1980) << 25)
                |  ((CALENDAR.get(Calendar.MONTH)+1) << 21)
                |  (CALENDAR.get(Calendar.DAY_OF_MONTH) << 16)
                |  (CALENDAR.get(Calendar.HOUR_OF_DAY) << 11)
                |  (CALENDAR.get(Calendar.MINUTE) << 5)
                |  (CALENDAR.get(Calendar.SECOND) >> 1);
        }
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Get the name of the entry.
     *
     * @return the entry name
     */
    public String getName() {
        return name == null ? super.getName() : name;
    }

    /**
     * Is this entry a directory?
     *
     * @return true if the entry is a directory
     */
    public boolean isDirectory() {
        return getName().endsWith("/");
    }

    /**
     * Sets this entry's date/time to the specified one. The time must be expressed in the Java time format,
     * i.e. as a number of milliseconds since the Epoch.
     *
     * @param javaTime the new time of this entry, expressed in the Java time format
     */
    public void setTime(long javaTime) {
        this.javaTime = javaTime;
        this.dosTime = javaTime==-1?-1:javaToDosTime(javaTime);

        // Unfortunately, the 'time' field is private in java.util.zip.ZipEntry, so we must call the setter which
        // converts the time to the DOS format again => unefficient
        super.setTime(this.javaTime);
    }

    /**
     * Returns this entry's date/time expressed in the Java time format, i.e. as a number of milliseconds since
     * the Epoch.
     *  
     * @return this entry's date/time expressed in the Java time format
     */
    public long getTime() {
        return javaTime;
    }

    /**
     * Throws an Exception if extra data cannot be parsed into extra fields.
     *
     * @param extra an array of bytes to be parsed into extra fields
     * @throws RuntimeException if the bytes cannot be parsed
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
     *
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
     * Get the hashCode of the entry. This uses the name as the hashcode.
     *
     * @return a hashcode.
     */
    public int hashCode() {
        return getName().hashCode();
    }

    public boolean equals(Object o) {
        if(!(o instanceof java.util.zip.ZipEntry))
            return false;

        return ((java.util.zip.ZipEntry)o).getName().equals(getName());
    }

}
