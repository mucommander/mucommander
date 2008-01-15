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
 * This class represents a non-versioned family of operating system, like <code>Windows</code> or <code>Linux</code>. 
 * The current runtime instance is determined using the value of the <code>os.name</code> system property.
 *
 * @see OsFamilies
 * @see OsVersion
 * @author Maxence Bernard
 */
public class OsFamily extends RuntimeProperty implements OsFamilies {

    /** Holds the OsFamily of the current runtime environment  */
    private static OsFamily currentValue;


    protected OsFamily(String stringRepresentation) {
        super(stringRepresentation);
    }


    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Determines the current value by parsing the corresponding system property. This method is called automatically
     * by this class the first time the current value is accessed. However, this method has been made public to allow
     * to force the initialization if it needs to happen at a predictable time.
     */
    public static void init() {
        // Note: performing the initialization outside of the class static block avoids cyclic dependency problems.
        if(currentValue==null) {
            currentValue = parseSystemProperty(getRawSystemProperty());
            if(Debug.ON) Debug.trace("Current OS family: "+ currentValue);
        }
    }

    /**
     * Returns the OS family of the current runtime environment.
     *
     * @return the OS family of the current runtime environment
     */
    public static OsFamily getCurrent() {
        if(currentValue==null) {
            // init() is called only once
            init();
        }

        return currentValue;
    }

    /**
     * Returns <code>true</code> if this OS family is UNIX-based. The following OS families are considered UNIX-based:
     * <ul>
     *  <li>{@link #LINUX}</li>
     *  <li>{@link #MAC_OS_X}</li>
     *  <li>{@link #SOLARIS}</li>
     *  <li>{@link #FREEBSD}</li>
     *  <li>{@link #AIX}</li>
     *  <li>{@link #HP_UX}</li>
     *  <li>{@link #OPENVMS}</li>
     *  <li>{@link #UNKNOWN_OS_FAMILY}: the reasonning for this being that most alternative OSes are Unix-based.</li>
     * </ul>
     *
     * @return <code>true</code> if the current OS is UNIX-based
     */
    public boolean isUnixBased() {
        return this==MAC_OS_X
                || this==LINUX
                || this==SOLARIS
                || this==FREEBSD
                || this==AIX
                || this==HP_UX
                || this==OPENVMS
                || this== UNKNOWN_OS_FAMILY;

        // Not UNIX-based: WINDOWS and OS/2
    }

    /**
     * Returns the value of the system property which serves to detect the OS family at runtime.
     *
     * @return the value of the system property which serves to detect the OS family at runtime.
     */
    public static String getRawSystemProperty() {
        return System.getProperty("os.name");
    }

    /**
     * Returns an <code>OsFamily</code> instance corresponding to the specified system property's value.
     *
     * @param osNameProp the value of the "os.name" system property
     * @return an OsFamily instance corresponding to the specified system property's value
     */
    static OsFamily parseSystemProperty(String osNameProp) {
        OsFamily osFamily;

        // This website holds a collection of system property values under many OSes:
        // http://lopica.sourceforge.net/os.html

        // Windows family
        if(osNameProp.startsWith("Windows")) {
            osFamily = WINDOWS;
        }
        // Mac OS X family
        else if(osNameProp.startsWith("Mac OS X")) {
            osFamily = MAC_OS_X;
        }
        // OS/2 family
        else if(osNameProp.startsWith("OS/2")) {
            osFamily = OS_2;
        }
        // Linux family
        else if(osNameProp.startsWith("Linux")) {
            osFamily = LINUX;
        }
        // Solaris family
        else if(osNameProp.startsWith("Solaris") || osNameProp.startsWith("SunOS")) {
            osFamily = SOLARIS;
        }
        else if(osNameProp.startsWith("FreeBSD")) {
            osFamily = FREEBSD;
        }
        else if(osNameProp.startsWith("AIX")) {
            osFamily = AIX;
        }
        else if(osNameProp.startsWith("HP-UX")) {
            osFamily = HP_UX;
        }
        else if(osNameProp.startsWith("OpenVMS")) {
            osFamily = OPENVMS;
        }
        // Any other OS
        else {
            osFamily = UNKNOWN_OS_FAMILY;
        }

        return osFamily;
    }


    ////////////////////////////////////
    // RuntimeProperty implementation //
    ////////////////////////////////////

    protected RuntimeProperty getCurrentValue() {
        return getCurrent();
    }
}
