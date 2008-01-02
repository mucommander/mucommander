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

    private final static JavaVersion currentValue = parseSystemProperty(getRawSystemProperty());

    static {
        if(Debug.ON) Debug.trace("Current Java version: "+ currentValue);
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

    static JavaVersion parseSystemProperty(String javaVersionString) {
        JavaVersion javaVersion;

        // Java version property should never be null or empty, but better be safe than sorry ...
        if(javaVersionString==null || (javaVersionString=javaVersionString.trim()).equals(""))
            // Assume java 1.4 (first supported Java version)
            javaVersion = JavaVersions.JAVA_1_4;
        // Java 1.7
        else if(javaVersionString.startsWith("1.7"))
            javaVersion = JavaVersions.JAVA_1_7;
        // Java 1.6
        else if(javaVersionString.startsWith("1.6"))
            javaVersion = JavaVersions.JAVA_1_6;
        // Java 1.5
        else if(javaVersionString.startsWith("1.5"))
            javaVersion = JavaVersions.JAVA_1_5;
        // Java 1.4
        else if(javaVersionString.startsWith("1.4"))
            javaVersion = JavaVersions.JAVA_1_4;
        // Java 1.3
        else if(javaVersionString.startsWith("1.3"))
            javaVersion = JavaVersions.JAVA_1_3;
        // Java 1.2
        else if(javaVersionString.startsWith("1.2"))
            javaVersion = JavaVersions.JAVA_1_2;
        // Java 1.1
        else if(javaVersionString.startsWith("1.1"))
            javaVersion = JavaVersions.JAVA_1_1;
        // Java 1.0
        else if(javaVersionString.startsWith("1.0"))
            javaVersion = JavaVersions.JAVA_1_0;
        // Newer version we don't know of yet, assume latest supported Java version
        else
            javaVersion = JavaVersions.JAVA_1_6;

        return javaVersion;
    }

    ///////////////////////////////////////////////
    // ComparableRuntimeProperty implementation //
    ///////////////////////////////////////////////

    protected RuntimeProperty getCurrentValue() {
        return currentValue;
    }
}
