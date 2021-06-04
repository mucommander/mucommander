/*
 * This file is part of muCommander, http://www.mucommander.com
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

	/** Windows 7 */
	WINDOWS_7("Windows 7"),
	/** Windows 8 */
	WINDOWS_8("Windows 8"),
	/** Windows 8.1 */
	WINDOWS_8_1("Windows 8.1"),
	/** Windows 10 */
	WINDOWS_10("Windows 10"),


	///////////////////////
	// Mac OS X versions //
	///////////////////////

	/** Lion */
	MAC_OS_10_7("10.7"),
	/** Mountain Lion */
	MAC_OS_10_8("10.8"),
	/** Mavericks */
	MAC_OS_10_9("10.9"),
	/** Yosemite */
	MAC_OS_10_10("10.10"),
	/** El Capitan */
	MAC_OS_10_11("10.11"),
	/** Sierra */
	MAC_OS_10_12("10.12"),
	/** High Sierra */
	MAC_OS_10_13("10.13"),
	/** Mojave */
	MAC_OS_10_14("10.14"),
	/** Catalina */
	MAC_OS_10_15("10.15"),
	/** Big Sur */
	MAC_OS_10_16("10.16");
	

    /** Logger used by this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(OsVersion.class);

    /** The String representation of this RuntimeProperty, set at creation time */
    protected final String stringRepresentation;

    /** Holds the OsVersion of the current runtime environment  */
    private static OsVersion currentValue;

    /*
     * Determines the current value by parsing the corresponding system property. This method is called automatically
     * by this class the first time the current value is accessed. However, this method has been made public to allow
     * to force the initialization if it needs to happen at a predictable time.
     */
    static {
    	currentValue = parseSystemProperty(getRawSystemProperty(), OsFamily.getRawSystemProperty(), OsFamily.getCurrent());
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
            if (osNameProp.equals("Windows 7"))
                return WINDOWS_7;

            if (osNameProp.equals("Windows 8"))
                return WINDOWS_8;

            if (osNameProp.equals("Windows 8.1"))
                return WINDOWS_8_1;

            if (osNameProp.equals("Windows 10"))
                return WINDOWS_10;

            // Newer version we don't know of yet, assume latest supported OS version
            return WINDOWS_10;
        }
        // Mac OS X versions
        if (osFamily==OsFamily.MAC_OS) {
            if(osVersionProp.startsWith("10.16"))
                return MAC_OS_10_16;

            if(osVersionProp.startsWith("10.15"))
                return MAC_OS_10_15;

            if(osVersionProp.startsWith("10.14"))
                return MAC_OS_10_14;

            if(osVersionProp.startsWith("10.13"))
                return MAC_OS_10_13;

            if(osVersionProp.startsWith("10.12"))
                return MAC_OS_10_12;

        	if(osVersionProp.startsWith("10.11"))
                return MAC_OS_10_11;

        	if(osVersionProp.startsWith("10.10"))
                return MAC_OS_10_10;
        	
        	if(osVersionProp.startsWith("10.9"))
                return MAC_OS_10_9;

            if(osVersionProp.startsWith("10.8"))
                return MAC_OS_10_8;

            if(osVersionProp.startsWith("10.7"))
                return MAC_OS_10_7;

            // Newer version we don't know of yet, assume latest supported OS version
            return MAC_OS_10_16;
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
