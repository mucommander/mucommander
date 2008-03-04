/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.util;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

/**
 * Exposes parts of the POSIX standard library using JNA (Java Native Access).
 *
 * @author Maxence Bernard
 */
public interface POSIX extends Library {

    /** Singleton instance */
    public POSIX INSTANCE = (POSIX) Native.loadLibrary("c", POSIX.class);


    //////////////////////
    // statvfs function //
    //////////////////////
    
    /**
     * Structure that holds the information returned by {@link POSIX#statvfs(String, com.mucommander.file.util.POSIX.STATVFSSTRUCT)}.
     */
    public static class STATVFSSTRUCT extends Structure {
        /* file system block size */
        public int f_bsize;
        /* fragment size */
        public int f_frsize;
        /* size of fs in f_frsize units */
        public int f_blocks;
        /* # free blocks */
        public int f_bfree;
        /* # free blocks for non-root */
        public int f_bavail;
        /* # inodes */
        public int f_files;
        /* # free inodes */
        public int f_ffree;
        /* # free inodes for non-root */
        public int f_favail;
        /* file system ID */
        public long f_fsid;
        /* mount flags */
        public int f_flag;
        /* maximum filename length */
        public int f_namemax;
    }

    /**
     * Returns information about the filesystem on which the specified file resides.
     *
     * @param path pathname of any file within the mounted filesystem
     * @param struct a {@link STATVFSSTRUCT} object
     * @return 0 on success, -1 on error
     */
    int statvfs(String path, STATVFSSTRUCT struct);
}
