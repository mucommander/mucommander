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

import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.LongByReference;

import java.nio.CharBuffer;

/**
 * Exposes parts of the Windows Kernel32 API using the JNA (Java Native Access) library.
 * The {@link Kernel32} class should be used to retrieve an instance of this interface.
 *
 * @see Kernel32
 * @author Maxence Bernard
 */
public interface Kernel32API extends W32API {

    /** Custom alignment of structures. */
    int STRUCTURE_ALIGNMENT = Structure.ALIGN_NONE;


    /**
     * Retrieves the calling thread's last-error code value. 
     * The last-error code is maintained on a per-thread basis. Multiple threads do not overwrite each other's last-error code.
     * @return The return value is the calling thread's last-error code.
     */
    int GetLastError();
    
    
    ///////////////////////////
    // SetErrorMode Function //
    ///////////////////////////

    /** Use the system default, which is to display all error dialog boxes. */
    public int SEM_DEFAULT = 0;
    /** The system does not display the critical-error-handler message box. Instead, the system sends the error to the
     *  calling process. */
    public int SEM_FAILCRITICALERRORS = 0x0001;
    /** The system automatically fixes memory alignment faults and makes them invisible to the application. It does this
     *  for the calling process and any descendant processes. This feature is only supported by certain processor
     *  architectures. For more information, see the Remarks sections. After this value is set for a process, subsequent
     *  attempts to clear the value are ignored. */
    public int SEM_NOALIGNMENTFAULTEXCEPT = 0x0004;
    /** The system does not display the general-protection-fault message box. This flag should only be set by debugging
     *  applications that handle general protection (GP) faults themselves with an exception handler. */
    public int SEM_NOGPFAULTERRORBOX = 0x0002;
    /** The system does not display a message box when it fails to find a file. Instead, the error is returned to the
     *  calling process. */
    public int SEM_NOOPENFILEERRORBOX = 0x8000;

    /**
     * Controls whether the system will handle the specified types of serious errors or whether the process will handle
     * them.
     *
     * <p>Remarks: Each process has an associated error mode that indicates to the system how the application is going
     * to respond to serious errors. A child process inherits the error mode of its parent process. To retrieve the
     * process error mode, use the GetErrorMode function.</br>
     * Because the error mode is set for the entire process, you must ensure that multi-threaded applications do not set
     * different error-mode flags. Doing so can lead to inconsistent error handling.</br>
     * The system does not make alignment faults visible to an application on all processor architectures. Therefore,
     * specifying SEM_NOALIGNMENTFAULTEXCEPT is not an error on such architectures, but the system is free to silently
     * ignore the request.
     *
     * @param uMode The process error mode. This parameter can be one or more of the following values:
     * SEM_DEFAULT (alone), SEM_FAILCRITICALERRORS, SEM_NOALIGNMENTFAULTEXCEPT, SEM_NOGPFAULTERRORBOX and
     * SEM_NOOPENFILEERRORBOX.
     * @return the previous state of the error-mode bit flags.
     */
    int SetErrorMode(int uMode);


    /////////////////////////////////
    // GetDiskFreeSpaceEx function //
    /////////////////////////////////

    /**
     * Retrieves information about the amount of space that is available on a disk volume, which is the total amount of
     * space, the total amount of free space, and the total amount of free space available to the user that is 
     * associated with the calling thread.
     *
     * @param lpDirectoryName A directory on the disk.
     * If this parameter is NULL, the function uses the root of the current disk.
     * If this parameter is a UNC name, it must include a trailing backslash, for example, "\\MyServer\MyShare\".
     * This parameter does not have to specify the root directory on a disk. The function accepts any directory on a disk.
     * The calling application must have FILE_LIST_DIRECTORY access rights for this directory.
     * @param lpFreeBytesAvailable A pointer to a variable that receives the total number of free bytes on a disk that
     * are available to the user who is associated with the calling thread. This parameter can be NULL.
     * If per-user quotas are being used, this value may be less than the total number of free bytes on a disk.
     * @param lpTotalNumberOfBytes A pointer to a variable that receives the total number of bytes on a disk that are
     * available to the user who is associated with the calling thread. This parameter can be NULL.
     * If per-user quotas are being used, this value may be less than the total number of bytes on a disk.
     * To determine the total number of bytes on a disk or volume, use IOCTL_DISK_GET_LENGTH_INFO.
     * @param lpTotalNumberOfFreeBytes A pointer to a variable that receives the total number of free bytes on a disk.
     * This parameter can be NULL.
     * @return If the function succeeds, the return value is nonzero. If the function fails, the return value is
     * zero (0). To get extended error information, call GetLastError.
     */
    boolean GetDiskFreeSpaceEx(String lpDirectoryName,
    		LongByReference lpFreeBytesAvailable,
    		LongByReference lpTotalNumberOfBytes,
    		LongByReference lpTotalNumberOfFreeBytes);


    ///////////////////////////
    // GetDriveType function //
    ///////////////////////////

    /** The drive type cannot be determined. */
    public final static int DRIVE_UNKNOWN = 0;

    /** The root path is invalid; for example, there is no volume is mounted at the path. */
    public final static int DRIVE_NO_ROOT_DIR = 1;

    /** The drive has removable media; for example, a floppy drive, thumb drive, or flash card reader. */
    public final static int DRIVE_REMOVABLE = 2;

    /** The drive has fixed media; for example, a hard drive or flash drive. */
    public final static int DRIVE_FIXED = 3;

    /** The drive is a remote (network) drive. */
    public final static int DRIVE_REMOTE = 4;

    /** The drive is a CD-ROM drive. */
    public final static int DRIVE_CDROM = 5;

    /** The drive is a RAM disk. */
    public final static int DRIVE_RAMDISK = 6;

    /**
     * Determines whether a disk drive is a removable, fixed, CD-ROM, RAM disk, or network drive.
     * 
     * @param lpRootPathName The root directory for the drive. A trailing backslash is required. If this parameter is
     * NULL, the function uses the root of the current directory.
     * @return The return value specifies the type of drive, which can be one of the above values.
     */
    int GetDriveType(String lpRootPathName);


    /////////////////////////
    // MoveFileEx function //
    /////////////////////////

    /** If a file named lpNewFileName exists, the function replaces its contents with the contents of the
     * lpExistingFileName file. This value cannot be used if lpNewFileName or lpExistingFileName names a directory. */
    public final static int MOVEFILE_REPLACE_EXISTING = 1;

    /** If the file is to be moved to a different volume, the function simulates the move by using the CopyFile and
     * DeleteFile functions.<br/>
     * This value cannot be used with MOVEFILE_DELAY_UNTIL_REBOOT. */
    public final static int MOVEFILE_COPY_ALLOWED = 2;

    /** The system does not move the file until the operating system is restarted. The system moves the file immediately
     * after AUTOCHK is executed, but before creating any paging files. Consequently, this parameter enables the
     * function to delete paging files from previous startups.<br/>
     * This value can be used only if the process is in the context of a user who belongs to the administrators group or
     * the LocalSystem account.<br/>
     * This value cannot be used with MOVEFILE_COPY_ALLOWED.<br/>
     * <b>Windows 2000</b>:  If you specify the MOVEFILE_DELAY_UNTIL_REBOOT flag for dwFlags, you cannot also prepend
     * the filename that is specified by lpExistingFileName with "\\?". */
    public final static int MOVEFILE_DELAY_UNTIL_REBOOT = 4;

    /** The function does not return until the file is actually moved on the disk.<br/>
     * Setting this value guarantees that a move performed as a copy and delete operation is flushed to disk before the
     * function returns. The flush occurs at the end of the copy operation.<br/>
     * This value has no effect if MOVEFILE_DELAY_UNTIL_REBOOT is set. */
    public final static int MOVEFILE_WRITE_THROUGH = 8;

    /** Reserved for future use. */
    public final static int MOVEFILE_CREATE_HARDLINK = 16;

    /** The function fails if the source file is a link source, but the file cannot be tracked after the move.
     * This situation can occur if the destination is a volume formatted with the FAT file system. */
    public final static int MOVEFILE_FAIL_IF_NOT_TRACKABLE = 32;

    /**
     * Moves an existing file or directory, including its children, with various move options.
     *
     * <p><b>Warning</b>: this method is NOT available on Windows 95, 98 and Me.</p>
     * 
     * @param lpExistingFileName The current name of the file or directory on the local computer. If dwFlags specifies
     * MOVEFILE_DELAY_UNTIL_REBOOT, the file cannot exist on a remote share, because delayed operations are performed
     * before the network is available.
     * @param lpNewFileName The new name of the file or directory on the local computer. When moving a file, the
     * destination can be on a different file system or volume. If the destination is on another drive, you must set the
     * MOVEFILE_COPY_ALLOWED flag in dwFlags. When moving a directory, the destination must be on the same drive.
     * @param dwFlags This parameter can be one or more of the 'MOVEFILE_' constant values. 
     * @return If the function succeeds, the return value is nonzero. If the function fails, the return value is
     * zero (0). To get extended error information, call GetLastError.
     */
    boolean MoveFileEx(String lpExistingFileName, String lpNewFileName, int dwFlags);


    ///////////////////////////////////
    // GetVolumeInformation function //
    ///////////////////////////////////

    /** The file system preserves the case of file names when it places a name on disk. */
    public final static int FILE_CASE_PRESERVED_NAMES = 0x00000002;

    /** The file system supports case-sensitive file names. */
    public final static int FILE_CASE_SENSITIVE_SEARCH = 0x00000001;

    /** The file system supports file-based compression. */
    public final static int FILE_FILE_COMPRESSION = 0x00000010;

    /** The file system supports named streams. */
    public final static int FILE_NAMED_STREAMS = 0x00040000;

    /** The file system preserves and enforces access control lists (ACL). For example, the NTFS file system preserves
     * and enforces ACLs, and the FAT file system does not. */
    public final static int FILE_PERSISTENT_ACLS = 0x00000008;

    /** The specified volume is read-only. Windows 2000:  This value is not supported.*/
    public final static int FILE_READ_ONLY_VOLUME = 0x00080000;

    /** The volume supports a single sequential write. */
    public final static int FILE_SEQUENTIAL_WRITE_ONCE = 0x00100000;

    /** The file system supports the Encrypted File System (EFS). */
    public final static int FILE_SUPPORTS_ENCRYPTION = 0x00020000;

    /** The file system supports object identifiers. */
    public final static int FILE_SUPPORTS_OBJECT_IDS = 0x00010000;

    /** The file system supports re-parse points. */
    public final static int FILE_SUPPORTS_REPARSE_POINTS = 0x00000080;

    /** The file system supports sparse files. */
    public final static int FILE_SUPPORTS_SPARSE_FILES = 0x00000040;

    /** The volume supports transactions. */
    public final static int FILE_SUPPORTS_TRANSACTIONS = 0x00200000;

    /** The file system supports Unicode in file names as they appear on disk. */
    public final static int FILE_UNICODE_ON_DISK = 0x00000004;

    /** The specified volume is a compressed volume, for example, a DoubleSpace volume. */
    public final static int FILE_VOLUME_IS_COMPRESSED = 0x00008000;

    /** The file system supports disk quotas. */
    public final static int FILE_VOLUME_QUOTAS = 0x00000020;

    /**
     * Retrieves information about the file system and volume associated with the specified root directory.
     *
     * @param lpRootPathName A pointer to a string that contains the root directory of the volume to be described.
     * If this parameter is NULL, the root of the current directory is used. A trailing backslash is required.
     * For example, you specify \\MyServer\MyShare as "\\MyServer\MyShare\", or the C drive as "C:\".
     * @param lpVolumeNameBuffer A pointer to a buffer that receives the name of a specified volume. The maximum buffer
     * size is MAX_PATH+1.
     * @param nVolumeNameSize The length of a volume name buffer, in TCHARs. The maximum buffer size is MAX_PATH+1.
     * This parameter is ignored if the volume name buffer is not supplied.
     * @param lpVolumeSerialNumber A pointer to a variable that receives the volume serial number.
     * This parameter can be NULL if the serial number is not required.
     * @param lpMaximumComponentLength A pointer to a variable that receives the maximum length, in TCHARs, of a file
     * name component that a specified file system supports. A file name component is the portion of a file name between
     * backslashes. The value that is stored in the variable that *lpMaximumComponentLength points to is used to
     * indicate that a specified file system supports long names. For example, for a FAT file system that supports long
     * names, the function stores the value 255, rather than the previous 8.3 indicator. Long names can also be
     * supported on systems that use the NTFS file system.
     * @param lpFileSystemFlags A pointer to a variable that receives flags associated with the specified file system.
     * This parameter can be one or more of the following flags ; FS_FILE_COMPRESSION and FS_VOL_IS_COMPRESSED
     * are mutually exclusive:
     * FILE_CASE_PRESERVED_NAMES, FILE_CASE_SENSITIVE_SEARCH, FILE_FILE_COMPRESSION, FILE_NAMED_STREAMS,
     * FILE_PERSISTENT_ACLS, FILE_READ_ONLY_VOLUME, FILE_SEQUENTIAL_WRITE_ONCE, FILE_SUPPORTS_ENCRYPTION,
     * FILE_SUPPORTS_OBJECT_IDS, FILE_SUPPORTS_REPARSE_POINTS, FILE_SUPPORTS_SPARSE_FILES, FILE_SUPPORTS_TRANSACTIONS,
     * FILE_UNICODE_ON_DISK, FILE_VOLUME_IS_COMPRESSED, FILE_VOLUME_QUOTAS.
     * @param lpFileSystemNameBuffer A pointer to a buffer that receives the name of the file system, for example, the
     * FAT file system or the NTFS file system. The maximum buffer size is MAX_PATH+1.
     * @param nFileSystemNameSize The length of the file system name buffer, in TCHARs. The maximum buffer size is
     * MAX_PATH+1. This parameter is ignored if the file system name buffer is not supplied.
     * @return If all the requested information is retrieved, the return value is true. If not all the requested
     * information is retrieved, the return value is false. To get extended error information, call GetLastError.
     */
    boolean GetVolumeInformation(
            char[] lpRootPathName,
            CharBuffer lpVolumeNameBuffer,
            int nVolumeNameSize,
            LongByReference lpVolumeSerialNumber,
            LongByReference lpMaximumComponentLength,
            LongByReference lpFileSystemFlags,
            CharBuffer lpFileSystemNameBuffer,
            int nFileSystemNameSize
    		);

    ////////////////////////////////
    // GetFileAttributes function //
    ////////////////////////////////

    /** Failed to retrieve file attributes  */
    public final static int INVALID_FILE_ATTRIBUTES = -1;

    /** A file or directory that the operating system uses a part of, or uses exclusively. */
    public final static int FILE_ATTRIBUTE_SYSTEM =  0x00000004;

    /**
     * Retrieves file system attributes for a specified file or directory.
     * 
     * @param fileName The name of the file or directory.
     * @return If the function succeeds, the return value contains the attributes of the specified file or directory.
     * If the function fails, the return value is INVALID_FILE_ATTRIBUTES. To get extended error information, call GetLastError.
     */
    int GetFileAttributes(String fileName);

    ////////////////////////////
    // FindFirstFile function //
    ////////////////////////////
    /** Alias class for W32API.HANDLE. */
    public final class FindFileHandle extends W32API.HANDLE {
    	public boolean isValid() {
    		return this != W32API.INVALID_HANDLE_VALUE;
    	}
    }

    /** Contains information about the file that is found by the FindFirstFile, FindFirstFileEx, or FindNextFile function. */
    public class WIN32_FIND_DATA extends Structure {
    	/** The file attributes of a file. */
    	public int dwFileAttributes;
    	/** A FILETIME structure that specifies when a file or directory was created. */
    	public long ftCreationTime;
    	/** For a file, the structure specifies when the file was last read from, written to, or for executable files, run.
    	 *  For a directory, the structure specifies when the directory is created. If the underlying file system does not support last access time, this member is zero. */
    	public long ftLastAccessTime;
    	/** For a file, the structure specifies when the file was last written to, truncated, or overwritten, for example, when WriteFile or SetEndOfFile are used. The date and time are not updated when file attributes or security descriptors are changed.
    	 *  For a directory, the structure specifies when the directory is created. If the underlying file system does not support last write time, this member is zero. */
    	public long ftLastWriteTime;
    	/** The high-order DWORD value of the file size, in bytes.
    	 *  This value is zero unless the file size is greater than MAXDWORD.
    	 *  The size of the file is equal to (nFileSizeHigh * (MAXDWORD+1)) + nFileSizeLow. */
    	public int nFileSizeHigh;
    	/** The low-order DWORD value of the file size, in bytes. */
    	public int nFileSizeLow;
    	/** If the dwFileAttributes member includes the FILE_ATTRIBUTE_REPARSE_POINT attribute, this member specifies the reparse point tag.
    	 *  Otherwise, this value is undefined and should not be used. */
    	public int dwReserved0;
    	/** Reserved for future use. */
    	public int dwReserved1;
    	/** The name of the file. */
    	public char[] cFileName = new char[250];
    	/** An alternative name for the file.
    	 * This name is in the classic 8.3 file name format. */
    	public char[] cAlternateFileName = new char[14];
    }

    /**
     * Searches a directory for a file or subdirectory with a name that matches a specific name (or partial name if wildcards are used).
     * 
     * @param fileName The directory or path, and the file name, which can include wildcard characters, for example, an asterisk (*) or a question mark (?).
     * @param findFileData A pointer to the WIN32_FIND_DATA structure that receives information about a found file or directory.
     * @return If the function succeeds, the return value is a search handle used in a subsequent call to FindNextFile or FindClose, and the lpFindFileData parameter contains information about the first file or directory found.
     */
    FindFileHandle FindFirstFile(String fileName, WIN32_FIND_DATA findFileData);

    /////////////////////////
    // FindClose function //
    ////////////////////////
    /**
     * Closes a file search handle opened by the FindFirstFile, FindFirstFileEx, FindFirstFileNameW, FindFirstFileNameTransactedW, FindFirstFileTransacted, FindFirstStreamTransactedW, or FindFirstStreamW functions.
     * 
     * @param hFindFile The file search handle.
     * @return If the function succeeds, the return value is nonzero.
     * If the function fails, the return value is zero. To get extended error information, call GetLastError.
     */
    boolean FindClose(FindFileHandle hFindFile);
}
