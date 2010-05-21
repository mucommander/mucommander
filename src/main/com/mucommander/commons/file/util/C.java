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

import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides access to a static instance of the {@link CLibrary} interface, allowing to invoke selected
 * functions of the C standard library.
 *
 * <p>The C standard library and the JNA library (which is used to access native libraries) may not be available on
 * all OS/CPU architectures: {@link #isAvailable()} can be used to determine that at runtime.</p>
 *
 * @see CLibrary
 * @author Maxence Bernard
 */
public class C {
    private static final Logger LOGGER = LoggerFactory.getLogger(C.class);

    /** Singleton instance */
    private static CLibrary INSTANCE;

    static {
        try {
            INSTANCE = (CLibrary)Native.loadLibrary("c", CLibrary.class);
        }
        catch(Throwable e) {
            LOGGER.info("Unable to load C library", e);

            // java.lang.UnsatisfiedLinkError is thrown if the CPU architecture is not supported by JNA.
            INSTANCE = null;
        }
    }

    /**
     * Returns <code>true</code> if the C standard library can be accessed on the current OS/CPU architecture.
     *
     * @return <code>true</code> if the C standard library can be accessed on the current OS/CPU architecture
     */
    public static boolean isAvailable() {
        return INSTANCE!=null;
    }

    /**
     * Returns a static instance of the {@link CLibrary} interface, allowing to invoke selected functions of the C
     * standard library. <code>null</code> will be returned if {@link #isAvailable()} returned <code>false</code>.
     *
     * @return a static instance of the {@link CLibrary} interface, <code>null</code> if it is not available on the
     * current OS/CPU architecture
     */
    public static CLibrary getInstance() {
        return INSTANCE;
    }
}
