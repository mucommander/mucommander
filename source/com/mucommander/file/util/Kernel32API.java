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

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API;

/**
 * Exposes parts of the Windows Kernel32 API using JNA (Java Native Access).
 *
 * @author Maxence Bernard
 */
public interface Kernel32API extends W32API {

    /** Custom alignment of structures. */
    int STRUCTURE_ALIGNMENT = Structure.ALIGN_NONE;

    /** An instance of the Kernel32 DLL */
    Kernel32API INSTANCE = (Kernel32API) Native.loadLibrary("kernel32", Kernel32API.class, DEFAULT_OPTIONS);

    
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
}
