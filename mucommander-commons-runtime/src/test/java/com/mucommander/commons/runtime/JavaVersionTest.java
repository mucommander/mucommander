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

import org.testng.annotations.Test;

/**
 * A JUnit test case for {@link JavaVersion}.
 *
 * @author Maxence Bernard, Arik Hadas
 */
public class JavaVersionTest {

    /**
     * Tests the order of all known Java versions.
     */
    @Test
    public void testOrder() {
        assert JavaVersion.JAVA_17.compareTo(JavaVersion.JAVA_16)>0;
        assert JavaVersion.JAVA_16.compareTo(JavaVersion.JAVA_15)>0;
        assert JavaVersion.JAVA_15.compareTo(JavaVersion.JAVA_14)>0;
        assert JavaVersion.JAVA_14.compareTo(JavaVersion.JAVA_13)>0;
        assert JavaVersion.JAVA_13.compareTo(JavaVersion.JAVA_12)>0;
        assert JavaVersion.JAVA_12.compareTo(JavaVersion.JAVA_11)>0;
        assert JavaVersion.JAVA_11.compareTo(JavaVersion.JAVA_10)>0;
        assert JavaVersion.JAVA_10.compareTo(JavaVersion.JAVA_9)>0;
        assert JavaVersion.JAVA_9.compareTo(JavaVersion.JAVA_8)>0;
    }

    /**
     * Tests the current Java version and compares it against known Java versions.
     */
    @Test
    public void testCurrent() {
        if(JavaVersion.getCurrent().compareTo(JavaVersion.JAVA_17)<0) {
            assert !JavaVersion.JAVA_17.isCurrent();
            assert JavaVersion.JAVA_17.isCurrentLower();
            assert JavaVersion.JAVA_17.isCurrentOrLower();
        }
    }

    /**
     * Tests the parsing of known <code>java.version</code> system property values.
     */
    @Test
    public void testParsing() {
        assert JavaVersion.parseSystemProperty("1.8")==JavaVersion.JAVA_8;
        assert JavaVersion.parseSystemProperty("9")==JavaVersion.JAVA_9;
        assert JavaVersion.parseSystemProperty("10")==JavaVersion.JAVA_10;
        assert JavaVersion.parseSystemProperty("11")==JavaVersion.JAVA_11;
        assert JavaVersion.parseSystemProperty("12")==JavaVersion.JAVA_12;
        assert JavaVersion.parseSystemProperty("13")==JavaVersion.JAVA_13;
        assert JavaVersion.parseSystemProperty("14")==JavaVersion.JAVA_14;
        assert JavaVersion.parseSystemProperty("15")==JavaVersion.JAVA_15;
        assert JavaVersion.parseSystemProperty("16")==JavaVersion.JAVA_16;
        assert JavaVersion.parseSystemProperty("17")==JavaVersion.JAVA_17;
    }


}
