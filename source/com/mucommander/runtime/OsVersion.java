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
public class OsVersion extends ComparableRuntimeProperty {

    private final static OsVersion currentValue = parseSystemProperty(getRawSystemProperty());

    static {
        if(Debug.ON) Debug.trace("Current OS version: "+ currentValue);
    }

    protected OsVersion(String osVersionString, int osVersionInt) {
        super(osVersionString, osVersionInt);
    }

    public static OsVersion getCurrent() {
        return currentValue;
    }

    public static String getRawSystemProperty() {
        return System.getProperty("os.version");
    }

    static OsVersion parseSystemProperty(String osVersionProp) {
        OsFamily osFamily = OsFamily.getCurrent();
        OsVersion osVersion;

        // Mac OS X versions
        if(osFamily==OsFamilies.MAC_OS_X) {
            if(osVersionProp.startsWith("10.5")) {
                osVersion = OsVersions.MAC_OS_X_10_5;
            }
            else if(osVersionProp.startsWith("10.4")) {
                osVersion = OsVersions.MAC_OS_X_10_4;
            }
            else if(osVersionProp.startsWith("10.3")) {
                osVersion = OsVersions.MAC_OS_X_10_3;
            }
            else if(osVersionProp.startsWith("10.2")) {
                osVersion = OsVersions.MAC_OS_X_10_2;
            }
            else if(osVersionProp.startsWith("10.1")) {
                osVersion = OsVersions.MAC_OS_X_10_1;
            }
            else if(osVersionProp.startsWith("10.0")) {
                osVersion = OsVersions.MAC_OS_X_10_0;
            }
            else {
                // Newer version we don't know of yet, assume latest supported OS version
                osVersion = OsVersions.MAC_OS_X_10_5;
            }
        }
        else {
            osVersion = OsVersions.UNKNOWN_VERSION;
        }

        return osVersion;
    }

    
    ///////////////////////////////////////////////
    // ComparableRuntimeProperty implementation //
    ///////////////////////////////////////////////

    protected RuntimeProperty getCurrentValue() {
        return currentValue;
    }
}
