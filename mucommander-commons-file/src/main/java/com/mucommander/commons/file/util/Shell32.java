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

import com.mucommander.commons.runtime.OsFamily;
import com.sun.jna.Native;

/**
 * This class provides access to a static instance of the {@link com.mucommander.commons.file.util.Shell32API} interface,
 * allowing to invoke selected Shell32 Windows DLL functions.
 *
 * <p>The Kernel32 DLL and the JNA library (which is used to access native libraries) may not be available on
 * all OS/CPU architectures: {@link #isAvailable()} can be used to determine that at runtime.</p>
 *
 * @see Shell32API
 * @author Maxence Bernard
 */
public class Shell32 {

    /** An instance of the Shell32 DLL */
    private static Shell32API INSTANCE;

    static {
        if(OsFamily.WINDOWS.isCurrent()) {        // Don't even bother if we're not running Windows
            try {
                INSTANCE = (Shell32API)Native.loadLibrary("shell32", Shell32API.class, Shell32API.DEFAULT_OPTIONS);
            }
            catch(Throwable e) {
                // java.lang.UnsatisfiedLinkError is thrown if the CPU architecture is not supported by JNA.
                INSTANCE = null;
            }
        }
    }

    /**
     * Returns <code>true</code> if the Shell32 API can be accessed on the current OS/CPU architecture.
     *
     * @return <code>true</code> if the Shell32 API can be accessed on the current OS/CPU architecture
     */
    public static boolean isAvailable() {
        return INSTANCE!=null;
    }

    /**
     * Returns a static instance of the {@link com.mucommander.commons.file.util.Shell32API} interface, allowing to invoke
     * some Shell32 Windows DLL functions. <code>null</code> will be returned if {@link #isAvailable()} returned
     * <code>false</code>.
     *
     * @return a static instance of the {@link com.mucommander.commons.file.util.Shell32API} interface, <code>null</code> if it
     * is not available on the current OS/CPU architecture
     */
    public static Shell32API getInstance() {
        return INSTANCE;
    }
}
