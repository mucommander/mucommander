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

/**
 * This interface contains all the definitions used in the package.
 *
 * <p>-----------------------------------</p>
 * <p>This class is based off the <code>org.apache.tools.tar</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.1 of Ant.</p>
 * 
 * @author Apache Ant
 */
public interface TarConstants {

    /**
     * The length of the name field in a header buffer.
     */
    int    NAMELEN = 100;

    /**
     * The length of the mode field in a header buffer.
     */
    int    MODELEN = 8;

    /**
     * The length of the user id field in a header buffer.
     */
    int    UIDLEN = 8;

    /**
     * The length of the group id field in a header buffer.
     */
    int    GIDLEN = 8;

    /**
     * The length of the checksum field in a header buffer.
     */
    int    CHKSUMLEN = 8;

    /**
     * The length of the size field in a header buffer.
     */
    int    SIZELEN = 12;

    /**
     * The maximum size of a file in a tar archive (That's 11 sevens, octal).
     */
    long   MAXSIZE = 077777777777L;

    /**
     * The length of the magic field in a header buffer.
     */
    int    MAGICLEN = 8;

    /**
     * The length of the modification time field in a header buffer.
     */
    int    MODTIMELEN = 12;

    /**
     * The length of the user name field in a header buffer.
     */
    int    UNAMELEN = 32;

    /**
     * The length of the group name field in a header buffer.
     */
    int    GNAMELEN = 32;

    /**
     * The length of the devices field in a header buffer.
     */
    int    DEVLEN = 8;

    /**
     * LF_ constants represent the "link flag" of an entry, or more commonly,
     * the "entry type". This is the "old way" of indicating a normal file.
     */
    byte   LF_OLDNORM = 0;

    /**
     * Normal file type.
     */
    byte   LF_NORMAL = (byte) '0';

    /**
     * Link file type.
     */
    byte   LF_LINK = (byte) '1';

    /**
     * Symbolic link file type.
     */
    byte   LF_SYMLINK = (byte) '2';

    /**
     * Character device file type.
     */
    byte   LF_CHR = (byte) '3';

    /**
     * Block device file type.
     */
    byte   LF_BLK = (byte) '4';

    /**
     * Directory file type.
     */
    byte   LF_DIR = (byte) '5';

    /**
     * FIFO (pipe) file type.
     */
    byte   LF_FIFO = (byte) '6';

    /**
     * Contiguous file type.
     */
    byte   LF_CONTIG = (byte) '7';

    /**
     * The magic tag representing a POSIX tar archive.
     */
    String TMAGIC = "ustar";

    /**
     * The magic tag representing a GNU tar archive.
     */
    String GNU_TMAGIC = "ustar  ";

    /**
     * The namr of the GNU tar entry which contains a long name.
     */
    String GNU_LONGLINK = "././@LongLink";

    /**
     * Identifies the *next* file on the tape as having a long name.
     */
    byte LF_GNUTYPE_LONGNAME = (byte) 'L';
}
