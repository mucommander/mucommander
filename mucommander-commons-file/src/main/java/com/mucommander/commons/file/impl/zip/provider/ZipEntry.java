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
public class ZipEntry implements Cloneable {

    /** Name/path of this entry */
    protected String name = null;

    /** Uncompressed size of the entry data */
    protected long size = -1;

    /** Compressed size of the entry data */
    protected long compressedSize = -1;

    /** CRC-32 checksum of the uncompressed entry data */
    protected long crc = -1;

    /** Data/time of this entry, in the DOS time format */
    protected long dosTime = -1;

    /** Data/time of this entry, in the Java time format */
    protected long javaTime = -1;

    /** Compression method that was used for the entry data */
    protected int method = -1;

    /** An optional comment for this entry */
    protected String comment;

    /** Platform, part of the 'version made by' central directory field */
    protected int platform = PLATFORM_FAT;

    /** Internal attributes (2 bytes) */
    protected int internalAttributes = 0;

    /** External attributes (4 bytes) */
    protected long externalAttributes = 0;

    /** List of extra fields, as ZipEntraField instances */
    protected Vector<ZipExtraField> extraFields = null;

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
     * Creates a new Zip entry with an empty name.
     */
    public ZipEntry() {
        this("");
    }

    /**
     * Creates a new Zip entry with the specified name.
     *
     * @param name the name of the entry
     */
    public ZipEntry(String name) {
        this.name = name;
    }

    /**
     * Creates a new Zip entry with fields taken from the specified zip entry.
     *
     * @param entry the entry to get fields from
     * @throws ZipException on error
     */
    public ZipEntry(java.util.zip.ZipEntry entry) throws ZipException {
        this.name = entry.getName();
        this.crc = entry.getCrc();
        this.size = entry.getSize();
        this.compressedSize = entry.getCompressedSize();
        this.method = entry.getMethod();
        this.comment = entry.getComment();

        setExtra(entry.getExtra());

        // ZipEntry.getTime() has to do a DOS time to Java time conversion, and we have to do the opposite.
        // This is inefficient but there is unfortunately no way to retrieve the DOS time field as it is private.
        setTime(entry.getTime());
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
     * Replaces all current extra fields with the specified ones.
     *
     * @param fields an array of extra fields
     */
    public void setExtraFields(ZipExtraField[] fields) {
        extraFields = new Vector<ZipExtraField>();
        for (ZipExtraField field : fields)
            extraFields.addElement(field);
    }

    /**
     * Returns the extra fields of this entry.
     *
     * @return the extra fields of this entry
     */
    public ZipExtraField[] getExtraFields() {
        if (extraFields == null)
            return new ZipExtraField[0];

        ZipExtraField[] result = new ZipExtraField[extraFields.size()];
        extraFields.copyInto(result);
        return result;
    }

    /**
     * Adds an extra fields, replacing any extra field of the same type previously added.
     *
     * @param ze the extra field to add
     */
    public void addExtraField(ZipExtraField ze) {
        if (extraFields == null)
            extraFields = new Vector<ZipExtraField>();

        ZipShort type = ze.getHeaderId();
        for (int i=0, nbFields=extraFields.size(); i<nbFields; i++) {
            if (extraFields.elementAt(i).getHeaderId().equals(type)) {
                extraFields.setElementAt(ze, i);
                return;
            }
        }

        extraFields.addElement(ze);
    }

    /**
     * Removes the first extra field corresponding to the given type.
     *
     * @param type the type of extra field to remove
     * @return <code>true</code> if an extra field corresponding to given type was removed, <code>false</code> if no
     * matching field was found
     */
    public boolean removeExtraField(ZipShort type) {
        if (extraFields == null)
            return false;

        for (int i=0, nbFields=extraFields.size(); i<nbFields; i++) {
            if (extraFields.elementAt(i).getHeaderId().equals(type)) {
                extraFields.removeElementAt(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the data of the local file extra fields. The returned byte array may be empty but never
     * <code>null</code>.
     *
     * @return the data of the local file extra fields
     */
    public byte[] getLocalFileDataExtra() {
        return ExtraFieldUtils.mergeLocalExtraFields(getExtraFields());
    }

    /**
     * Returns the data of the central directory extra fields. The returned byte array may be empty but never
     * <code>null</code>.
     *
     * @return the data of the central directory extra fields
     */
    public byte[] getCentralDirectoryExtra() {
        return ExtraFieldUtils.mergeCentralExtraFields(getExtraFields());
    }

    /**
     * Returns the name of this entry.
     *
     * @return the name of this entry
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this entry.
     *
     * @param name the new name for this entry
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Returns <code>true</code> if the entry is a directory. Directory entries are characterized by their name
     * ending with a '/' character.
     *
     * @return <code>true</code> if the entry is a directory
     */
    public boolean isDirectory() {
        return getName().endsWith("/");
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

    /**
     * Returns the uncompressed size of the entry data, or <code>-1</code> if not known.
     *
     * @return the uncompressed size of the entry data, or <code>-1</code> if not known
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the uncompressed size of the entry data.
     *
     * @param size the uncompressed size in bytes
     * @throws IllegalArgumentException if the specified size is less than 0 or greater than 0xFFFFFFFF bytes
     */
    public void setSize(long size) {
        if(!isValidUnsignedInt(size))
	        throw new IllegalArgumentException("Invalid entry size");

	    this.size = size;
    }

    /**
     * Returns the size of the compressed entry data, or <code>-1</code> if not known. In the case of a stored entry,
     * the compressed size will be the same as the uncompressed size of the entry.
     *
     * @return the size of the compressed entry data, or <code>-1</code> if not known
     */
    public long getCompressedSize() {
        return compressedSize;
    }

    /**
     * Sets the size of the compressed entry data.
     *
     * @param csize the compressed size to set to
     */
    public void setCompressedSize(long csize) {
        if(!isValidUnsignedInt(csize))
	        throw new IllegalArgumentException("Invalid entry size");

        this.compressedSize = csize;
    }

    /**
     * Returns the CRC-32 checksum of the uncompressed entry data, or <code>-1</code> if not known.
     *
     * @return the CRC-32 checksum of the uncompressed entry data, or <code>-1</code> if not known
     */
    public long getCrc() {
        return crc;
    }

    /**
     * Sets the CRC-32 checksum of the uncompressed entry data.
     *
     * @param crc the new CRC-32 value
     * @throws IllegalArgumentException if the specified CRC-32 value is less than 0 or greater than 0xFFFFFFFF
     */
    public void setCrc(long crc) {
        if(!isValidUnsignedInt(crc))
            throw new IllegalArgumentException("invalid entry crc-32");

        this.crc = crc;
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
     * Sets this entry's date/time to the specified one. The time must be expressed in the Java time format,
     * i.e. as a number of milliseconds since the Epoch.
     *
     * @param javaTime the new time of this entry, expressed in the Java time format
     */
    public void setTime(long javaTime) {
        this.javaTime = javaTime;
        this.dosTime = javaTime==-1?-1:javaToDosTime(javaTime);
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
    }

    /**
     * Returns the compression method of the entry, or <code>-1</code> if not specified.
     *
     * @return the compression method of the entry, or <code>-1</code> if not specified
     */
    public int getMethod() {
    	return method;
    }

    /**
     * Sets the compression method for the entry.
     *
     * @param method the compression method, either {@link ZipConstants#STORED} or {@link ZipConstants#DEFLATED}
     * @throws IllegalArgumentException if the specified compression method is invalid
     */
    public void setMethod(int method) {
        if (method != ZipConstants.STORED && method != ZipConstants.DEFLATED)
            throw new IllegalArgumentException("Invalid compression method");

        this.method = method;
    }

    /**
     * Returns the comment string for the entry, or <code>null</code> if there is none.
     *
     * @return the comment string for the entry, or <code>null</code> if there is none
     */
    public String getComment() {
    	return comment;
    }

    /**
     * Sets the optional comment string for the entry.
     *
     * @param comment the comment string
     * @throws IllegalArgumentException if the length of the specified comment string is greater than 0xFFFF bytes
     */
    public void setComment(String comment) {
        if (comment != null && comment.length() > 0xffff/3 && getUTF8Length(comment) > 0xffff)
            throw new IllegalArgumentException("invalid entry comment length");

        this.comment = comment;
    }

    /**
     * Throws an <code>IllegalArgumentException</code> if byte array cannot be parsed into extra fields.
     *
     * @param extra an array of bytes to be parsed into extra fields
     * @throws IllegalArgumentException if the byte array cannot be parsed into extra fields
     */
    public void setExtra(byte[] extra) throws IllegalArgumentException {
        if(extra==null || extra.length==0) {
            extraFields = null;
        }
        else {
            try {
                setExtraFields(ExtraFieldUtils.parse(extra));
            }
            catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }


    ////////////////////
    // Helper methods //
    ////////////////////

    /*
     * Converts DOS time (Epoch=1980) to Java time (Epoch=1970).
     *
     * @param dosTime time expressed in the convoluted DOS time format
     * @return time expressed as the number of milliseconds since the epoch
     */
    protected static long dosToJavaTime(long dosTime) {
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

    /*
     * Returns the length of the given String's <code>UTF-8</code> representation.
     */
    protected static int getUTF8Length(String s) {
        // This method is a dup from java.util.ZipOutputStream
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch <= 0x7f)
                count++;
            else if (ch <= 0x7ff)
                count += 2;
            else
                count += 3;
        }
        return count;
    }

    /**
     * Returns <code>true</code> if the given long is a valid unsigned int value, i.e. comprised between 0 and 2^32-1.
     *
     * @param l the long value to test
     * @return <code>true</code> if the given long is a valid unsigned int value, i.e. comprised between 0 and 2^32-1
     */
    protected boolean isValidUnsignedInt(long l) {
        return l>=0 && l<=0xFFFFFFFFL;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns a cloned instance of this entry.
     *
     * @return a cloned instance of this entry
     * @throws CloneNotSupportedException should never happen
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ZipEntry ze = (ZipEntry)super.clone();

        if(extraFields!=null)
            ze.extraFields = (Vector<ZipExtraField>)extraFields.clone();

        return ze;
    }

    /**
     * Returns a hash of this entry's name.
     *
     * @return a hash of this entry's name
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Returns <code>true</code> if the given object is a <code>ZipEntry</code> that has the same name as this one.
     *
     * @param o the object to test for equality
     * @return <code>true</code> if the given object is a <code>ZipEntry</code> that has the same name as this one
     */
    public boolean equals(Object o) {
        return (o instanceof ZipEntry) && ((ZipEntry) o).getName().equals(getName());
    }
}
