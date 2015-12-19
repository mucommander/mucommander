/*
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

package com.mucommander.commons.runtime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a major version of an operating system, like <code>Mac OS X 10.5</code> or
 * <code>Windows XP</code>. The current runtime value is determined using the value of the <code>os.version</code>
 * system property and the current {@link OsFamily} instance.
 * Being a {@link com.mucommander.commons.runtime.ComparableRuntimeProperty}, OS versions are ordered and can be compared
 * against each other.
 *
 * @see OsVersions
 * @see OsFamily
 * @author Maxence Bernard, Arik Hadas
 */
public enum OsVersion implements ComparableRuntimeProperty {
	/** Unknown OS version */
	UNKNOWN_VERSION("Unknown"),

	//////////////////////
	// Windows versions //
	//////////////////////

	// Windows 9X subfamily

	/** Windows 95 */
	WINDOWS_95("Windows 95"),
	/** Windows 98 */
	WINDOWS_98("Windows 98"),
	/** Windows Me */
	WINDOWS_ME("Windows Me"),

	// Windows NT subfamily

	/** Windows NT */
	WINDOWS_NT("Windows NT"),
	/** Windows 2000 */
	WINDOWS_2000("Windows 2000"),
	/** Windows XP */
	WINDOWS_XP("Windows XP"),
	/** Windows 2003 */
	WINDOWS_2003("Windows 2003"),
	/** Windows Vista */
	WINDOWS_VISTA("Windows Vista"),
	/** Windows 7 */
	WINDOWS_7("Windows 7"),
	/** Windows 8 */
	WINDOWS_8("Windows 8"),


	///////////////////////
	// Mac OS X versions //
	///////////////////////

	/** Mac OS X 10.0 */
	MAC_OS_X_10_0("10.0"),
	/** Mac OS X 10.1 */
	MAC_OS_X_10_1("10.1"),
	/** Mac OS X 10.2 */
	MAC_OS_X_10_2("10.2"),
	/** Mac OS X 10.3 */
	MAC_OS_X_10_3("10.3"),
	/** Mac OS X 10.4 */
	MAC_OS_X_10_4("10.4"),
	/** Mac OS X 10.5 */
	MAC_OS_X_10_5("10.5"),
	/** Mac OS X 10.6 */
	MAC_OS_X_10_6("10.6"),
	/** Mac OS X 10.7 */
	MAC_OS_X_10_7("10.7"),
	/** Mac OS X 10.8 */
	MAC_OS_X_10_8("10.8");
	

    /** Logger used by this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OsVersion.class);

    /** The String representation of this RuntimeProperty, set at creation time */
    protected final String stringRepresentation;

    /** Holds the OsVersion of the current runtime environment  */
    private static OsVersion currentValue;

    /**
     * Determines the current value by parsing the corresponding system property. This method is called automatically
     * by this class the first time the current value is accessed. However, this method has been made public to allow
     * to force the initialization if it needs to happen at a predictable time.
     */
    static {
    	currentValue = parseSystemProperty(getRawSystemProperty(), OsFamily.getRawSystemProperty(), OsFamily.getCurrent());
    	LOGGER.info("Current OS version: {}", currentValue);
    }


    OsVersion(String stringRepresentation) {
    	this.stringRepresentation = stringRepresentation;
    }

    
    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Returns the OS version of the current runtime environment.
     *
     * @return the OS version of the current runtime environment
     */
    public static OsVersion getCurrent() {
        return currentValue;
    }

    /**
     * Returns the value of the system property which serves to detect the OS version at runtime.
     *
     * @return the value of the system property which serves to detect the OS version at runtime.
     */
    public static String getRawSystemProperty() {
        return System.getProperty("os.version");
    }

    /**
     * Returns an <code>OsVersion</code> instance corresponding to the specified system property's value.
     *
     * @param osVersionProp the value of the "os.version" system property
     * @param osNameProp the value of the "os.name" system property
     * @param osFamily the current OS family
     * @return an OsVersion instance corresponding to the specified system property's value
     */
    static OsVersion parseSystemProperty(String osVersionProp, String osNameProp, OsFamily osFamily) {
        // This website holds a collection of system property values under many OSes:
        // http://lopica.sourceforge.net/os.html

        if(osFamily==OsFamily.WINDOWS) {
            if (osNameProp.equals("Windows 95"))
                return WINDOWS_95;

            if (osNameProp.equals("Windows 98"))
                return WINDOWS_98;

            if (osNameProp.equals("Windows Me"))
                return WINDOWS_ME;

            if (osNameProp.equals("Windows NT"))
                return WINDOWS_NT;

            if (osNameProp.equals("Windows 2000"))
                return WINDOWS_2000;

            if (osNameProp.equals("Windows XP"))
                return WINDOWS_XP;

            if (osNameProp.equals("Windows 2003"))
                return WINDOWS_2003;

            if (osNameProp.equals("Windows Vista"))
                return WINDOWS_VISTA;

            if (osNameProp.equals("Windows 7"))
                return WINDOWS_7;

            if (osNameProp.equals("Windows 8"))
                return WINDOWS_8;

            // Newer version we don't know of yet, assume latest supported OS version
            return WINDOWS_8;
        }
        // Mac OS X versions
        if (osFamily==OsFamily.MAC_OS_X) {
            if(osVersionProp.startsWith("10.8"))
                return MAC_OS_X_10_8;

            if(osVersionProp.startsWith("10.7"))
                return MAC_OS_X_10_7;

            if (osVersionProp.startsWith("10.6"))
                return MAC_OS_X_10_6;

            if (osVersionProp.startsWith("10.5"))
                return MAC_OS_X_10_5;

            if (osVersionProp.startsWith("10.4"))
                return MAC_OS_X_10_4;

            if (osVersionProp.startsWith("10.3"))
                return MAC_OS_X_10_3;

            if (osVersionProp.startsWith("10.2"))
                return MAC_OS_X_10_2;

            if (osVersionProp.startsWith("10.1"))
                return MAC_OS_X_10_1;

            if (osVersionProp.startsWith("10.0"))
                return MAC_OS_X_10_0;

            // Newer version we don't know of yet, assume latest supported OS version
            return MAC_OS_X_10_8;
        }

        return OsVersion.UNKNOWN_VERSION;
    }

    /**
     * Returns <code>true</code> if this instance is the same instance as the one returned by {@link #getCurrent()}.
     *
     * @return true if this instance is the same as the current runtime's value
     */
    public boolean isCurrent() {
        return this==currentValue;
    }

    //////////////////////////////////////////////
    // ComparableRuntimeProperty implementation //
    //////////////////////////////////////////////

    public boolean isCurrentOrLower() {
		return currentValue.compareTo(this)<=0;
	}

	public boolean isCurrentLower() {
		return currentValue.compareTo(this)<0;
	}

	public boolean isCurrentOrHigher() {
		return currentValue.compareTo(this)>=0;
	}

	public boolean isCurrentHigher() {
		return currentValue.compareTo(this)>0;
	}

    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    public String toString() {
        return stringRepresentation;
    }
}
