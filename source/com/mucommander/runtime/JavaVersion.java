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

package com.mucommander.runtime;

import com.mucommander.Debug;

/**
 * This class represents a major version of Java, like <code>Java 1.5</code> for instance. The current runtime instance
 * is determined using the value of the <code>java.version</code> system property.
 * Being a {@link com.mucommander.runtime.ComparableRuntimeProperty}, versions of Java are ordered and can be compared
 * against each other.
 *
 * @see JavaVersions
 * @author Maxence Bernard
*/
public class JavaVersion extends ComparableRuntimeProperty implements JavaVersions {

    /** Holds the JavaVersion of the current runtime environment  */
    private static JavaVersion currentValue;

    protected JavaVersion(String stringRepresentation, int intValue) {
        super(stringRepresentation, intValue);
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
            if(Debug.ON) Debug.trace("Current Java version: "+ currentValue);
        }
    }

    /**
     * Returns the Java version of the current runtime environment.
     *
     * @return the Java version of the current runtime environment
     */
    public static JavaVersion getCurrent() {
        if(currentValue==null) {
            // init() is called only once
            init();
        }

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
     * Returns a <code>JavaVersion</code> instance corresponding to the specified system property's value.
     *
     * @param javaVersionProp the value of the "java.version" system property
     * @return a JavaVersion instance corresponding to the specified system property's value
     */
    static JavaVersion parseSystemProperty(String javaVersionProp) {
        JavaVersion javaVersion;

        // Java version property should never be null or empty, but better be safe than sorry ...
        if(javaVersionProp==null || (javaVersionProp=javaVersionProp.trim()).equals(""))
            // Assume java 1.4 (first supported Java version)
            javaVersion = JavaVersions.JAVA_1_4;
        // Java 1.7
        else if(javaVersionProp.startsWith("1.7"))
            javaVersion = JavaVersions.JAVA_1_7;
        // Java 1.6
        else if(javaVersionProp.startsWith("1.6"))
            javaVersion = JavaVersions.JAVA_1_6;
        // Java 1.5
        else if(javaVersionProp.startsWith("1.5"))
            javaVersion = JavaVersions.JAVA_1_5;
        // Java 1.4
        else if(javaVersionProp.startsWith("1.4"))
            javaVersion = JavaVersions.JAVA_1_4;
        // Java 1.3
        else if(javaVersionProp.startsWith("1.3"))
            javaVersion = JavaVersions.JAVA_1_3;
        // Java 1.2
        else if(javaVersionProp.startsWith("1.2"))
            javaVersion = JavaVersions.JAVA_1_2;
        // Java 1.1
        else if(javaVersionProp.startsWith("1.1"))
            javaVersion = JavaVersions.JAVA_1_1;
        // Java 1.0
        else if(javaVersionProp.startsWith("1.0"))
            javaVersion = JavaVersions.JAVA_1_0;
        // Newer version we don't know of yet, assume latest supported Java version
        else
            javaVersion = JavaVersions.JAVA_1_6;

        return javaVersion;
    }


    //////////////////////////////////////////////
    // ComparableRuntimeProperty implementation //
    //////////////////////////////////////////////

    protected RuntimeProperty getCurrentValue() {
        return getCurrent();
    }
}
