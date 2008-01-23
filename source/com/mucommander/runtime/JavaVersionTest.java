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

import junit.framework.TestCase;

/**
 * A JUnit test case for {@link JavaVersion}.
 *
 * @author Maxence Bernard
 */
public class JavaVersionTest extends TestCase implements JavaVersions {

    /**
     * Tests the order of all known Java versions.
     */
    public void testOrder() {
        assertTrue(JAVA_1_7.compareTo(JAVA_1_6)>0);
        assertTrue(JAVA_1_6.compareTo(JAVA_1_5)>0);
        assertTrue(JAVA_1_5.compareTo(JAVA_1_4)>0);
        assertTrue(JAVA_1_4.compareTo(JAVA_1_3)>0);
        assertTrue(JAVA_1_3.compareTo(JAVA_1_2)>0);
        assertTrue(JAVA_1_2.compareTo(JAVA_1_1)>0);
        assertTrue(JAVA_1_1.compareTo(JAVA_1_0)>0);
    }

    /**
     * Tests the current Java version and compares it against known Java versions.
     */
    public void testCurrent() {
        // Note: this test safely assumes that the current Java version is greater than 1.0

        assertTrue(JavaVersion.getCurrent().compareTo(JAVA_1_0)>0);

        assertFalse(JAVA_1_0.isCurrent());
        assertTrue(JAVA_1_0.isCurrentHigher());
        assertTrue(JAVA_1_0.isCurrentOrHigher());

        if(JavaVersion.getCurrent().compareTo(JAVA_1_7)<0) {
            assertFalse(JAVA_1_7.isCurrent());
            assertTrue(JAVA_1_7.isCurrentLower());
            assertTrue(JAVA_1_7.isCurrentOrLower());
        }
    }

    /**
     * Tests the parsing of known <code>java.version</code> system property values.
     */
    public void testParsing() {
        assertTrue(JavaVersion.parseSystemProperty("1.0")==JAVA_1_0);
        assertTrue(JavaVersion.parseSystemProperty("1.1")==JAVA_1_1);
        assertTrue(JavaVersion.parseSystemProperty("1.2")==JAVA_1_2);
        assertTrue(JavaVersion.parseSystemProperty("1.3")==JAVA_1_3);
        assertTrue(JavaVersion.parseSystemProperty("1.4")==JAVA_1_4);
        assertTrue(JavaVersion.parseSystemProperty("1.5")==JAVA_1_5);
        assertTrue(JavaVersion.parseSystemProperty("1.6")==JAVA_1_6);
        assertTrue(JavaVersion.parseSystemProperty("1.7")==JAVA_1_7);
    }


}
