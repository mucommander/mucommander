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


package com.mucommander.commons.file.util;

import com.mucommander.commons.runtime.JavaVersion;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API;

/**
 * Exposes parts of the Windows Shell32 API using the JNA (Java Native Access) library.
 * The {@link Shell32} class should be used to retrieve an instance of this interface.
 *
 * @see Shell32
 * @author Maxence Bernard
 */
public interface Shell32API extends W32API {

    //
    // Note that the C header "shellapi.h" includes "pshpack1.h", which disables automatic alignment of structure fields.
    //

    /** Custom alignment of structures. */
    int STRUCTURE_ALIGNMENT = JavaVersion.isAmd64Architecture() ? Structure.ALIGN_DEFAULT : Structure.ALIGN_NONE;


    //////////////////////////////
    // SHFileOperation function //
    //////////////////////////////

    // Allowed wFunc values

    /** Copies the files specified in the pFrom member to the location specified in the pTo member. */
    int FO_MOVE = 1;
    /** Copies the files specified in the pFrom member to the location specified in the pTo member. */
    int FO_COPY = 2;
    /** Deletes the files specified in pFrom. */
    int FO_DELETE = 3;
    /** Renames the file specified in pFrom. You cannot use this flag to rename multiple files with a single
     * function call. Use FO_MOVE instead. */
    int FO_RENAME = 4;

    // Allowed fFlags values

    /** Not supported. */
    int FOF_MULTIDESTFILES = 1;
    /** Not supported. */
    int FOF_CONFIRMMOUSE = 2;
    /** Do not display a progress dialog box. */
    int FOF_SILENT = 4;
    /** Give the file being operated on a new name in a move, copy, or rename operation if a file with the target
     *  name already exists. */
    int FOF_RENAMEONCOLLISION = 8;
    /** Respond with "Yes to All" for any dialog box that is displayed. */
    int FOF_NOCONFIRMATION = 16;
    /** Not supported. */
    int FOF_WANTMAPPINGHANDLE = 32;
    /** Preserve Undo information, if possible. If pFrom does not contain fully qualified path and file names, this
     *  flag is ignored. */
    int FOF_ALLOWUNDO = 64;
    /** Not supported. */
    int FOF_FILESONLY = 128;
    /** Display a progress dialog box but do not show the file names. */
    int FOF_SIMPLEPROGRESS = 256;
    /** Do not confirm the creation of a new directory if the operation requires one to be created. */
    int FOF_NOCONFIRMMKDIR = 512;
    /** Do not display a user interface if an error occurs. */
    int FOF_NOERRORUI = 1024;
    /** Not supported. */
    int FOF_NOCOPYSECURITYATTRIBS = 2048;

    /**
     * This structure contains information that the SHFileOperation function uses to perform file operations.
     */
    public static class SHFILEOPSTRUCT extends Structure {

        /** Window handle to the dialog box to display information about the status of the file operation. */
        public HANDLE hwnd;
        /** Value that indicates which operation to perform. The following values are accepted:
         *  FO_COPY, FO_DELETE, FO_MOVE or FO_RENAME */
        public int wFunc;
        /** Specifies one or more source file names. These names must be fully qualified paths. Standard
         *  Microsoft MS-DOS wildcards, such as "*", are permitted in the file name position. Although this member
         *  is declared as a null-terminated string, it is used as a buffer to hold multiple file names. Each file
         *  name must be terminated by a single NULL character. An additional NULL character must be appended to the
         *  end of the final name to indicate the end of pFrom. */
        public String pFrom;
        /** Contain the name of the destination file or directory. This parameter must be set to NULL if it is not
         * used. Like pFrom, the pTo member is also a double-null terminated string and is handled in much the same
         * way. */
        public String pTo;
        /** Flags that control the file operation (see constant fields for allowed values). */
        public short fFlags;
        /** Not supported. */
        public boolean fAnyOperationsAborted;
        /** Not supported. */
        public Pointer pNameMappings;
        /** String to use as the title of a progress dialog box. This member is used only if fFlags includes the
         * FOF_SIMPLEPROGRESS flag. */
        public String lpszProgressTitle;

        /**
         * Encodes <code>pFrom/pTo</code> paths, terminating them with NUL characters as required.
         *
         * @param paths a list of paths to encode
         * @return the encoded path
         */
        public String encodePaths(String[] paths) {
            StringBuffer encodedPaths = new StringBuffer();
            for (String path : paths) {
                encodedPaths.append(path);
                encodedPaths.append('\0');
            }
            encodedPaths.append('\0');

            return encodedPaths.toString();
        }
    }

    /**
     * This function can be used to copy, move, rename, or delete a file system object.
     *
     * <p>Remarks: You should use fully qualified path names with this function. Using it with relative path names
     * is not thread-safe.</br>
     * When used to delete a file, SHFileOperation attempts to place the deleted file in the Recycle Bin. If you
     * wish to delete a file and guarantee that it is not placed in the Recycle Bin, use the DeleteFile function.
     * </p>
     *
     * @param lpFileOp a SHFILEOPSTRUCT structure that contains information this function needs to carry out the
     * specified operation.
     * @return Returns zero if successful, or nonzero otherwise.
     */
    int SHFileOperation(SHFILEOPSTRUCT lpFileOp);


    ////////////////////////////////
    // SHEmptyRecycleBin function //
    ////////////////////////////////

    /** No dialog box confirming the deletion of the objects will be displayed. */
    int SHERB_NOCONFIRMATION = 0x00000001;
    /** No dialog box indicating the progress will be displayed. */
    int SHERB_NOPROGRESSUI = 0x00000002;
    /** No sound will be played when the operation is complete. */
    int SHERB_NOSOUND = 0x00000004;

    /**
     * Empties the Recycle Bin on the specified drive.
     *
     * @param hwnd A handle to the parent window of any dialog boxes that might be displayed during the operation.
     * This parameter can be NULL.
     * @param pszRootPath a null-terminated string of maximum length MAX_PATH that contains the path of the root
     * drive on which the Recycle Bin is located. This parameter can contain a string formatted with the drive,
     * folder, and subfolder names, for example c:\windows\system\, etc. It can also contain an empty string or
     * NULL. If this value is an empty string or NULL, all Recycle Bins on all drives will be emptied.
     * @param dwFlags a bitwise combination of SHERB_NOCONFIRMATION, SHERB_NOPROGRESSUI and SHERB_NOSOUND.
     * @return Returns S_OK (0) if successful, or a COM-defined error value otherwise.
     */
    int SHEmptyRecycleBin(HANDLE hwnd, String pszRootPath, int dwFlags);


    ////////////////////////////////
    // SHQueryRecycleBin function //
    ////////////////////////////////

    /**
     * Contains the size and item count information retrieved by the SHQueryRecycleBin function.
     */
    public static class SHQUERYRBINFO extends Structure {

        /** The size of the structure, in bytes. This member must be filled in prior to calling SHQueryRecycleBin. */
        public int cbSize = 20;     // 1 DWORD + 2 DWORDLONG = 4 + 2*8 = 20 bytes
        /** The total size of all the objects in the specified Recycle Bin, in bytes. */
        public long i64Size;
        /** The total number of items in the specified Recycle Bin. */
        public long i64NumItems;
    }

    /**
     * Retrieves the size of the Recycle Bin and the number of items in it, for a specified drive.
     *
     * <p>Remarks: With Microsoft Windows 2000, if NULL is passed in the pszRootPath parameter, the function fails
     * and returns an E_INVALIDARG error code. In earlier versions of the operating system, you can pass an empty
     * string or NULL. If pszRootPath contains an empty string or NULL, information is retrieved for all
     * Recycle Bins on all drives.</p>
     *
     * @param pszRootPath a null-terminated string of maximum length MAX_PATH to contain the path of the root drive
     * on which the Recycle Bin is located. This parameter can contain a string formatted with the drive, folder,
     * and subfolder names (C:\Windows\System...).
     * @param pSHQueryRBInfo a SHQUERYRBINFO structure that receives the Recycle Bin information. The cbSize member
     * of the structure must be set to the size of the structure before calling this API.
     * @return Returns S_OK (0) if successful, or a COM-defined error value otherwise.
     */
    int SHQueryRecycleBin(String pszRootPath, SHQUERYRBINFO pSHQueryRBInfo);
}
