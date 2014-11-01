/**
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

package com.mucommander.commons.file.filter;

import org.testng.annotations.Test;

/**
 * Tests the {@link ExtensionFilenameFilter} class.
 * @author Nicolas Rinaudo
 */
public class ExtensionFilenameFilterTest {
    /** Filter uses for all tests. */
    private static ExtensionFilenameFilter filter = new ExtensionFilenameFilter(new String[] {".zip", ".jar", ".war", ".wal", ".wmz",
                                                                                              ".xpi", ".ear", ".sar", ".odt", ".ods",
                                                                                              ".odp", ".odg", ".odf"});

    /**
     * Runs a set of tests.
     * @param caseSensitive whether to test case-sensitive filters or not.
     */
    private void test(boolean caseSensitive) {
        filter.setCaseSensitive(caseSensitive);

        assert filter.accept("test.zip");
        assert filter.accept("test.jar");
        assert filter.accept("test.war");
        assert filter.accept("test.wal");
        assert filter.accept("test.wmz");
        assert filter.accept("test.xpi");
        assert filter.accept("test.ear");
        assert filter.accept("test.sar");
        assert filter.accept("test.odt");
        assert filter.accept("test.ods");
        assert filter.accept("test.odp");
        assert filter.accept("test.odg");
        assert filter.accept("test.odf");

        assert filter.accept("test.ZIP") != caseSensitive;
        assert filter.accept("test.JAR") != caseSensitive;
        assert filter.accept("test.WAR") != caseSensitive;
        assert filter.accept("test.WAL") != caseSensitive;
        assert filter.accept("test.WMZ") != caseSensitive;
        assert filter.accept("test.XPI") != caseSensitive;
        assert filter.accept("test.EAR") != caseSensitive;
        assert filter.accept("test.SAR") != caseSensitive;
        assert filter.accept("test.ODT") != caseSensitive;
        assert filter.accept("test.ODS") != caseSensitive;
        assert filter.accept("test.ODP") != caseSensitive;
        assert filter.accept("test.ODG") != caseSensitive;
        assert filter.accept("test.ODF") != caseSensitive;

        assert !filter.accept("test.tar");
        assert !filter.accept("test.tar.gz");
        assert !filter.accept("test.tgz");
        assert !filter.accept("test.tar.bz2");
        assert !filter.accept("test.tbz2");
        assert !filter.accept("test.gz");
        assert !filter.accept("test.bz2");
        assert !filter.accept("test.iso");
        assert !filter.accept("test.nrg");
        assert !filter.accept("test.a");
        assert !filter.accept("test.ar");
        assert !filter.accept("test.deb");
        assert !filter.accept("test.lst");

        assert !filter.accept("test");
        assert !filter.accept("");
    }

    /**
     * Tests case-sensitive filtering.
     */
    @Test
    public void testCaseSensitive() {
        test(true);
    }

    /**
     * Tests case-insensitive filtering.
     */
    @Test
    public void testCaseInsensitive() {
        test(false);
    }
}
