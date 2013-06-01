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

import org.testng.annotations.Test;

/**
 * A JUnit test case for {@link JavaVersion}.
 *
 * @author Maxence Bernard
 */
public class JavaVersionTest {

    /**
     * Tests the order of all known Java versions.
     */
    @Test
    public void testOrder() {
        assert JavaVersion.JAVA_1_7.compareTo(JavaVersion.JAVA_1_6)>0;
        assert JavaVersion.JAVA_1_6.compareTo(JavaVersion.JAVA_1_5)>0;
        assert JavaVersion.JAVA_1_5.compareTo(JavaVersion.JAVA_1_4)>0;
        assert JavaVersion.JAVA_1_4.compareTo(JavaVersion.JAVA_1_3)>0;
        assert JavaVersion.JAVA_1_3.compareTo(JavaVersion.JAVA_1_2)>0;
        assert JavaVersion.JAVA_1_2.compareTo(JavaVersion.JAVA_1_1)>0;
        assert JavaVersion.JAVA_1_1.compareTo(JavaVersion.JAVA_1_0)>0;
    }

    /**
     * Tests the current Java version and compares it against known Java versions.
     */
    @Test
    public void testCurrent() {
        // Note: this test safely assumes that the current Java version is greater than 1.0

        assert JavaVersion.getCurrent().compareTo(JavaVersion.JAVA_1_0)>0;

        assert !JavaVersion.JAVA_1_0.isCurrent();
        assert JavaVersion.JAVA_1_0.isCurrentHigher();
        assert JavaVersion.JAVA_1_0.isCurrentOrHigher();

        if(JavaVersion.getCurrent().compareTo(JavaVersion.JAVA_1_7)<0) {
            assert !JavaVersion.JAVA_1_7.isCurrent();
            assert JavaVersion.JAVA_1_7.isCurrentLower();
            assert JavaVersion.JAVA_1_7.isCurrentOrLower();
        }
    }

    /**
     * Tests the parsing of known <code>java.version</code> system property values.
     */
    @Test
    public void testParsing() {
        assert JavaVersion.parseSystemProperty("1.0")==JavaVersion.JAVA_1_0;
        assert JavaVersion.parseSystemProperty("1.1")==JavaVersion.JAVA_1_1;
        assert JavaVersion.parseSystemProperty("1.2")==JavaVersion.JAVA_1_2;
        assert JavaVersion.parseSystemProperty("1.3")==JavaVersion.JAVA_1_3;
        assert JavaVersion.parseSystemProperty("1.4")==JavaVersion.JAVA_1_4;
        assert JavaVersion.parseSystemProperty("1.5")==JavaVersion.JAVA_1_5;
        assert JavaVersion.parseSystemProperty("1.6")==JavaVersion.JAVA_1_6;
        assert JavaVersion.parseSystemProperty("1.7")==JavaVersion.JAVA_1_7;
    }


}
