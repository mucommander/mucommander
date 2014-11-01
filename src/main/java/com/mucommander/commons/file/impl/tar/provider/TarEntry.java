/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.mucommander.commons.file.impl.tar.provider;

import java.util.Date;

/**
 * This class represents an entry in a Tar archive. It consists
 * of the entry's header, as well as the entry's File. Entries
 * can be instantiated in one of three ways, depending on how
 * they are to be used.
 * <p>
 * TarEntries that are created from the header bytes read from
 * an archive are instantiated with the TarEntry( byte[] )
 * constructor. These entries will be used when extracting from
 * or listing the contents of an archive. These entries have their
 * header filled in using the header bytes. They also set the File
 * to null, since they reference an archive entry not a file.
 * <p>
 * TarEntries that are created from Files that are to be written
 * into an archive are instantiated with the TarEntry( File )
 * constructor. These entries have their header filled in using
 * the File's information. They also keep a reference to the File
 * for convenience when writing entries.
 * <p>
 * Finally, TarEntries can be constructed from nothing but a name.
 * This allows the programmer to construct the entry by hand, for
 * instance when only an InputStream is available for writing to
 * the archive, and the header information is constructed from
 * other information. In this case the header fields are set to
 * defaults and the File is set to null.
 *
 * <p>
 * The C structure for a Tar Entry's header is:
 * <pre>
 * struct header {
 * char name[NAMSIZ];
 * char mode[8];
 * char uid[8];
 * char gid[8];
 * char size[12];
 * char mtime[12];
 * char chksum[8];
 * char linkflag;
 * char linkname[NAMSIZ];
 * char magic[8];
 * char uname[TUNMLEN];
 * char gname[TGNMLEN];
 * char devmajor[8];
 * char devminor[8];
 * } header;
 * </pre>
 *
 * <p>-----------------------------------</p>
 * <p>This class is based off the <code>org.apache.tools.tar</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.1 of Ant.</p>
 * 
 * @author Apache Ant, Maxence Bernard
 */
public class TarEntry implements TarConstants {
    /** The entry's name. */
    private StringBuffer name;

    /** The entry's permission mode. */
    private int mode;

    /** The entry's user id. */
    private int userId;

    /** The entry's group id. */
    private int groupId;

    /** The entry's size. */
    private long size;

    /** The entry's modification time. */
    private long modTime;

    /** The entry's link flag. */
    private byte linkFlag;

    /** The entry's link name. */
    private StringBuffer linkName;

    /** The entry's magic tag. */
    private StringBuffer magic;

    /** The entry's user name. */
    private StringBuffer userName;

    /** The entry's group name. */
    private StringBuffer groupName;

    /** The entry's major device number. */
    private int devMajor;

    /** The entry's minor device number. */
    private int devMinor;

    /** The entry's offset from the start of the archive */
    private long offset;

    /** Maximum length of a user's name in the tar file */
    public static final int MAX_NAMELEN = 31;

    /** Default permissions bits for directories */
    public static final int DEFAULT_DIR_MODE = 040755;

    /** Default permissions bits for files */
    public static final int DEFAULT_FILE_MODE = 0100644;

    /** Convert millis to seconds */
    public static final int MILLIS_PER_SECOND = 1000;

    /**
     * Construct an empty entry and prepares the header values.
     */
    private TarEntry () {
        this.magic = new StringBuffer(TMAGIC);
        this.name = new StringBuffer();
        this.linkName = new StringBuffer();

        String user = System.getProperty("user.name", "");

        if (user.length() > MAX_NAMELEN) {
            user = user.substring(0, MAX_NAMELEN);
        }

        this.userId = 0;
        this.groupId = 0;
        this.userName = new StringBuffer(user);
        this.groupName = new StringBuffer("");
    }

    /**
     * Construct an entry with only a name. This allows the programmer
     * to construct the entry's header "by hand". File is set to null.
     *
     * @param name the entry name
     */
    public TarEntry(String name) {
        this();

        boolean isDir = name.endsWith("/");

        this.devMajor = 0;
        this.devMinor = 0;
        this.name = new StringBuffer(name);
        this.mode = isDir ? DEFAULT_DIR_MODE : DEFAULT_FILE_MODE;
        this.linkFlag = isDir ? LF_DIR : LF_NORMAL;
        this.userId = 0;
        this.groupId = 0;
        this.size = 0;
        this.modTime = (new Date()).getTime() / MILLIS_PER_SECOND;
        this.linkName = new StringBuffer("");
        this.userName = new StringBuffer("");
        this.groupName = new StringBuffer("");
        this.devMajor = 0;
        this.devMinor = 0;

    }

    /**
     * Construct an entry with a name and a link flag.
     *
     * @param name the entry name
     * @param linkFlag the entry link flag.
     */
    public TarEntry(String name, byte linkFlag) {
        this(name);
        this.linkFlag = linkFlag;
    }

    /**
     * Construct an entry from an archive's header bytes. File is set
     * to null.
     *
     * @param headerBuf The header bytes from a tar archive entry.
     */
    public TarEntry(byte[] headerBuf) {
        this();
        parseTarHeader(headerBuf);
    }

    /**
     * Determine if the two entries are equal. Equality is determined
     * by the header names being equal.
     *
     * @param it Entry to be checked for equality.
     * @return True if the entries are equal.
     */
    public boolean equals(TarEntry it) {
        return getName().equals(it.getName());
    }

    /**
     * Determine if the two entries are equal. Equality is determined
     * by the header names being equal.
     *
     * @param it Entry to be checked for equality.
     * @return True if the entries are equal.
     */
    public boolean equals(Object it) {
        if (it == null || getClass() != it.getClass()) {
            return false;
        }
        return equals((TarEntry) it);
    }

    /**
     * Hashcodes are based on entry names.
     *
     * @return the entry hashcode
     */
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * Determine if the given entry is a descendant of this entry.
     * Descendancy is determined by the name of the descendant
     * starting with this entry's name.
     *
     * @param desc Entry to be checked as a descendent of this.
     * @return True if entry is a descendant of this.
     */
    public boolean isDescendent(TarEntry desc) {
        return desc.getName().startsWith(getName());
    }

    /**
     * Get this entry's name.
     *
     * @return This entry's name.
     */
    public String getName() {
        return name.toString();
    }

    /**
     * Set this entry's name.
     *
     * @param name This entry's new name.
     */
    public void setName(String name) {
        this.name = new StringBuffer(name);
    }

    /**
     * Set the mode for this entry
     *
     * @param mode the mode for this entry
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Get this entry's link name.
     *
     * @return This entry's link name.
     */
    public String getLinkName() {
        return linkName.toString();
    }

    /**
     * Get this entry's user id.
     *
     * @return This entry's user id.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Set this entry's user id.
     *
     * @param userId This entry's new user id.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Get this entry's group id.
     *
     * @return This entry's group id.
     */
    public int getGroupId() {
        return groupId;
    }

    /**
     * Set this entry's group id.
     *
     * @param groupId This entry's new group id.
     */
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    /**
     * Get this entry's user name.
     *
     * @return This entry's user name.
     */
    public String getUserName() {
        return userName.toString();
    }

    /**
     * Set this entry's user name.
     *
     * @param userName This entry's new user name.
     */
    public void setUserName(String userName) {
        this.userName = new StringBuffer(userName);
    }

    /**
     * Get this entry's group name.
     *
     * @return This entry's group name.
     */
    public String getGroupName() {
        return groupName.toString();
    }

    /**
     * Set this entry's group name.
     *
     * @param groupName This entry's new group name.
     */
    public void setGroupName(String groupName) {
        this.groupName = new StringBuffer(groupName);
    }

    /**
     * Convenience method to set this entry's group and user ids.
     *
     * @param userId This entry's new user id.
     * @param groupId This entry's new group id.
     */
    public void setIds(int userId, int groupId) {
        setUserId(userId);
        setGroupId(groupId);
    }

    /**
     * Convenience method to set this entry's group and user names.
     *
     * @param userName This entry's new user name.
     * @param groupName This entry's new group name.
     */
    public void setNames(String userName, String groupName) {
        setUserName(userName);
        setGroupName(groupName);
    }

    /**
     * Set this entry's modification time. The parameter passed
     * to this method is in "Java time".
     *
     * @param time This entry's new modification time.
     */
    public void setModTime(long time) {
        modTime = time / MILLIS_PER_SECOND;
    }

    /**
     * Set this entry's modification time.
     *
     * @param time This entry's new modification time.
     */
    public void setModTime(Date time) {
        modTime = time.getTime() / MILLIS_PER_SECOND;
    }

    /**
     * Set this entry's modification time.
     *
     * @return time This entry's new modification time.
     */
    public Date getModTime() {
        return new Date(modTime * MILLIS_PER_SECOND);
    }

    /**
     * Get this entry's mode.
     *
     * @return This entry's mode.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Get this entry's file size.
     *
     * @return This entry's file size.
     */
    public long getSize() {
        return size;
    }

    /**
     * Set this entry's file size.
     *
     * @param size This entry's new file size.
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Returns this entry's offset from the start of the archive.
     *
     * @return this entry's offset from the start of the archive
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Sets this entry's offset from the start of the archive.
     *
     * @param offset this entry's offset from the start of the archive
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * Indicate if this entry is a GNU long name block
     *
     * @return true if this is a long name extension provided by GNU tar
     */
    public boolean isGNULongNameEntry() {
        return linkFlag == LF_GNUTYPE_LONGNAME
                           && name.toString().equals(GNU_LONGLINK);
    }

    /**
     * Return whether or not this entry represents a directory.
     *
     * @return True if this entry is a directory.
     */
    public boolean isDirectory() {
        if (linkFlag == LF_DIR) {
            return true;
        }

        return getName().endsWith("/");
    }

    /**
     * Write an entry's header information to a header buffer.
     *
     * @param outbuf The tar entry header buffer to fill in.
     */
    public void writeEntryHeader(byte[] outbuf) {
        int offset = 0;

        offset = TarUtils.getNameBytes(name, outbuf, offset, NAMELEN);
        offset = TarUtils.getOctalBytes(mode, outbuf, offset, MODELEN);
        offset = TarUtils.getOctalBytes(userId, outbuf, offset, UIDLEN);
        offset = TarUtils.getOctalBytes(groupId, outbuf, offset, GIDLEN);
        offset = TarUtils.getLongOctalBytes(size, outbuf, offset, SIZELEN);
        offset = TarUtils.getLongOctalBytes(modTime, outbuf, offset, MODTIMELEN);

        int csOffset = offset;

        for (int c = 0; c < CHKSUMLEN; ++c) {
            outbuf[offset++] = (byte) ' ';
        }

        outbuf[offset++] = linkFlag;
        offset = TarUtils.getNameBytes(linkName, outbuf, offset, NAMELEN);
        offset = TarUtils.getNameBytes(magic, outbuf, offset, MAGICLEN);
        offset = TarUtils.getNameBytes(userName, outbuf, offset, UNAMELEN);
        offset = TarUtils.getNameBytes(groupName, outbuf, offset, GNAMELEN);
        offset = TarUtils.getOctalBytes(devMajor, outbuf, offset, DEVLEN);
        offset = TarUtils.getOctalBytes(devMinor, outbuf, offset, DEVLEN);

        while (offset < outbuf.length) {
            outbuf[offset++] = 0;
        }

        long chk = TarUtils.computeCheckSum(outbuf);

        TarUtils.getCheckSumOctalBytes(chk, outbuf, csOffset, CHKSUMLEN);
    }

    /**
     * Parse an entry's header information from a header buffer.
     *
     * @param header The tar entry header buffer to get information from.
     */
    public void parseTarHeader(byte[] header) {
        int offset = 0;

        name = TarUtils.parseName(header, offset, NAMELEN);
        offset += NAMELEN;
        mode = (int) TarUtils.parseOctal(header, offset, MODELEN);
        offset += MODELEN;
        userId = (int) TarUtils.parseOctal(header, offset, UIDLEN);
        offset += UIDLEN;
        groupId = (int) TarUtils.parseOctal(header, offset, GIDLEN);
        offset += GIDLEN;
        size = TarUtils.parseOctal(header, offset, SIZELEN);
        offset += SIZELEN;
        modTime = TarUtils.parseOctal(header, offset, MODTIMELEN);
        offset += MODTIMELEN;
        offset += CHKSUMLEN;
        linkFlag = header[offset++];
        linkName = TarUtils.parseName(header, offset, NAMELEN);
        offset += NAMELEN;
        magic = TarUtils.parseName(header, offset, MAGICLEN);
        offset += MAGICLEN;
        userName = TarUtils.parseName(header, offset, UNAMELEN);
        offset += UNAMELEN;
        groupName = TarUtils.parseName(header, offset, GNAMELEN);
        offset += GNAMELEN;
        devMajor = (int) TarUtils.parseOctal(header, offset, DEVLEN);
        offset += DEVLEN;
        devMinor = (int) TarUtils.parseOctal(header, offset, DEVLEN);
    }
}
