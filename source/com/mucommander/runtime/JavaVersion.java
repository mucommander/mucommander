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
public class JavaVersion extends ComparableRuntimeProperty {

    private final static JavaVersion currentValue;

    static {
        // - Java version ----------------------------
        // -------------------------------------------
        // Java version detection //
        String versionString = getRawSystemProperty();

        // Java version property should never be null or empty, but better be safe than sorry ...
        if(versionString==null || (versionString=versionString.trim()).equals(""))
            // Assume java 1.4 (first supported Java version)
            currentValue = JavaVersions.JAVA_1_4;
        // Java 1.7
        else if(versionString.startsWith("1.7"))
            currentValue = JavaVersions.JAVA_1_7;
        // Java 1.6
        else if(versionString.startsWith("1.6"))
            currentValue = JavaVersions.JAVA_1_6;
        // Java 1.5
        else if(versionString.startsWith("1.5"))
            currentValue = JavaVersions.JAVA_1_5;
        // Java 1.4
        else if(versionString.startsWith("1.4"))
            currentValue = JavaVersions.JAVA_1_4;
        // Java 1.3
        else if(versionString.startsWith("1.3"))
            currentValue = JavaVersions.JAVA_1_3;
        // Java 1.2
        else if(versionString.startsWith("1.2"))
            currentValue = JavaVersions.JAVA_1_2;
        // Java 1.1
        else if(versionString.startsWith("1.1"))
            currentValue = JavaVersions.JAVA_1_1;
        // Java 1.0
        else if(versionString.startsWith("1.0"))
            currentValue = JavaVersions.JAVA_1_0;
        // Newer version we don't know of yet, assume latest supported Java version
        else
            currentValue = JavaVersions.JAVA_1_6;

        if(Debug.ON) Debug.trace("Detected Java version: "+ currentValue);
    }

    protected JavaVersion(String javaVersionString, int javaVersionInt) {
        super(javaVersionString, javaVersionInt);
    }

    public static JavaVersion getCurrent() {
        return currentValue;
    }

    public static String getRawSystemProperty() {
        return System.getProperty("java.version");
    }


    ///////////////////////////////////////////////
    // ComparableRuntimeProperty implementation //
    ///////////////////////////////////////////////

    protected RuntimeProperty getCurrentValue() {
        return currentValue;
    }
}
