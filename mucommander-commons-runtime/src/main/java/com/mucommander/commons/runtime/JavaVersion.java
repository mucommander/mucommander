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
 * This class represents a major version of Java, like <code>Java 1.5</code> for instance. The current runtime instance
 * is determined using the value of the <code>java.version</code> system property.
 * Being a {@link com.mucommander.commons.runtime.ComparableRuntimeProperty}, versions of Java are ordered and can be compared
 * against each other.
 *
 * @author Maxence Bernard, Arik Hadas
*/
public enum JavaVersion implements ComparableRuntimeProperty {
    /** Java 1.8.x */
    JAVA_8("1.8"),
    /** Java 9.x */
    JAVA_9("9"),
    /** Java 10.x */
    JAVA_10("10"),
    /** Java 11.x */
    JAVA_11("11"),
    /** Java 12.x */
    JAVA_12("12"),
    /** Java 13.x */
    JAVA_13("13"),
    /** Java 14.x */
    JAVA_14("14"),
    /** Java 15.x */
    JAVA_15("15"),
    /** Java 16.x */
    JAVA_16("16"),
    /** Java 17.x */
    JAVA_17("17"),
    ;

    /** Logger used by this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaVersion.class);

    /** Holds the JavaVersion of the current runtime environment  */
    private static JavaVersion currentValue;

    /** Holds the String representation of the current JVM architecture  */
    private static String currentArchitecture;

    /** The String representation of this RuntimeProperty, set at creation time */
    protected final String stringRepresentation;

    /*
     * Determines the current value by parsing the corresponding system property. This method is called automatically
     * by this class the first time the current value is accessed. However, this method has been made public to allow
     * to force the initialization if it needs to happen at a predictable time.
     */
    static {
    	currentValue = parseSystemProperty(getRawSystemProperty());
    	currentArchitecture = System.getProperty("os.arch");
    	LOGGER.info("Current Java version: {}", currentValue);
    	LOGGER.info("Current JVM architecture: {}", currentArchitecture);
    }


    JavaVersion(String stringRepresentation) {
    	this.stringRepresentation = stringRepresentation;
    }

    ////////////////////
    // Static methods //
    ////////////////////

    /**
     * Returns <code>true</code> if the JVM architecture is amd64
     *
     * @return <code>true</code> if the JVM architecture is amd64, and <code>false</code> otherwise.
     */
    public static boolean isAmd64Architecture() {
    	return "amd64".equals(currentArchitecture);
    }

    /**
     * Returns the Java version of the current runtime environment.
     *
     * @return the Java version of the current runtime environment
     */
    public static JavaVersion getCurrent() {
        return currentValue;
    }

    /**
     * Returns the value of the system property which serves to detect the Java version at runtime.
     *
     * @return the value of the system property which serves to detect the Java version at runtime.
     */
    public static String getRawSystemProperty() {
        return System.getProperty("java.version");
    }

	/**
	 * Returns a <code>JavaVersion</code> instance corresponding to the
	 * specified system property's value.
	 *
	 * @param javaVersionProp
	 *            the value of the "java.version" system property
	 * @return a JavaVersion instance corresponding to the specified system
	 *         property's value
	 */
	static JavaVersion parseSystemProperty(String javaVersionProp) {
		// Java version property should never be null or empty, but better be
		// safe than sorry ...
		if (javaVersionProp == null
				|| (javaVersionProp = javaVersionProp.trim()).equals(""))
			// Assume java 1.8 (first supported Java version)
			return JavaVersion.JAVA_8;
		// Java 17
		if (javaVersionProp.startsWith("17"))
			return JavaVersion.JAVA_17;
		// Java 16
		if (javaVersionProp.startsWith("16"))
			return JavaVersion.JAVA_16;
		// Java 15
		if (javaVersionProp.startsWith("15"))
			return JavaVersion.JAVA_15;
		// Java 14
		if (javaVersionProp.startsWith("14"))
			return JavaVersion.JAVA_14;
		// Java 13
		if (javaVersionProp.startsWith("13"))
			return JavaVersion.JAVA_13;
		// Java 12
		if (javaVersionProp.startsWith("12"))
			return JavaVersion.JAVA_12;
		// Java 11
		if (javaVersionProp.startsWith("11"))
			return JavaVersion.JAVA_11;
		// Java 10
		if (javaVersionProp.startsWith("10"))
			return JavaVersion.JAVA_10;
		// Java 9
		if (javaVersionProp.startsWith("9"))
			return JavaVersion.JAVA_9;
		// Java 1.8
		if (javaVersionProp.startsWith("1.8"))
			return JavaVersion.JAVA_8;

		// Newer version we don't know of yet, assume latest supported Java version
		return JavaVersion.JAVA_16;
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
