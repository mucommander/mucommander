/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.runtime;

import com.mucommander.Debug;

/**
 * @author Maxence Bernard
*/
public class OsFamily extends RuntimeProperty {

    private final static OsFamily currentValue;

    static {
        String osName = getRawSystemProperty();
        
        // Windows family
        if(osName.startsWith("Windows")) {
            // Windows 95, 98, Me
            if (osName.startsWith("Windows 95") || osName.startsWith("Windows 98") || osName.startsWith("Windows Me")) {
                currentValue = OsFamilies.WINDOWS_9X;
            }
            // Windows NT, 2000, XP and up
            else {
                currentValue = OsFamilies.WINDOWS_NT;
            }
        }
        // Mac OS X family
        else if(osName.startsWith("Mac OS X")) {
            currentValue = OsFamilies.MAC_OS_X;
        }
        // OS/2 family
        else if(osName.startsWith("OS/2")) {
            currentValue = OsFamilies.OS_2;
        }
        // Linux family
        else if(osName.startsWith("Linux")) {
            currentValue = OsFamilies.LINUX;
        }
        // Solaris family
        else if(osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
            currentValue = OsFamilies.SOLARIS;
        }
        // Any other OS
        else {
            currentValue = OsFamilies.UNKNOWN_OS_FAMILY;
        }

        if(Debug.ON) Debug.trace("Detected OS family: "+ currentValue);
    }

    protected OsFamily(String osFamilyString) {
        super(osFamilyString);
    }

    public static String getRawSystemProperty() {
        return System.getProperty("os.name");
    }

    public static OsFamily getCurrent() {
        return currentValue;
    }

    /////////////////////////////////////
    // RuntimeProperty implementation //
    /////////////////////////////////////

    protected RuntimeProperty getCurrentValue() {
        return currentValue;
    }
}
