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

package com.mucommander.file.filter;

import com.mucommander.file.AbstractFile;

import junit.framework.TestCase;

/**
 * Tests the {@link ExtensionFilenameFilter} class.
 * @author Nicolas Rinaudo
 */
public class ExtensionFilenameFilterTest extends TestCase {
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

        assertTrue(filter.accept("test.zip"));
        assertTrue(filter.accept("test.jar"));
        assertTrue(filter.accept("test.war"));
        assertTrue(filter.accept("test.wal"));
        assertTrue(filter.accept("test.wmz"));
        assertTrue(filter.accept("test.xpi"));
        assertTrue(filter.accept("test.ear"));
        assertTrue(filter.accept("test.sar"));
        assertTrue(filter.accept("test.odt"));
        assertTrue(filter.accept("test.ods"));
        assertTrue(filter.accept("test.odp"));
        assertTrue(filter.accept("test.odg"));
        assertTrue(filter.accept("test.odf"));

        assertTrue(filter.accept("test.ZIP") != caseSensitive);
        assertTrue(filter.accept("test.JAR") != caseSensitive);
        assertTrue(filter.accept("test.WAR") != caseSensitive);
        assertTrue(filter.accept("test.WAL") != caseSensitive);
        assertTrue(filter.accept("test.WMZ") != caseSensitive);
        assertTrue(filter.accept("test.XPI") != caseSensitive);
        assertTrue(filter.accept("test.EAR") != caseSensitive);
        assertTrue(filter.accept("test.SAR") != caseSensitive);
        assertTrue(filter.accept("test.ODT") != caseSensitive);
        assertTrue(filter.accept("test.ODS") != caseSensitive);
        assertTrue(filter.accept("test.ODP") != caseSensitive);
        assertTrue(filter.accept("test.ODG") != caseSensitive);
        assertTrue(filter.accept("test.ODF") != caseSensitive);

        assertFalse(filter.accept("test.tar"));
        assertFalse(filter.accept("test.tar.gz"));
        assertFalse(filter.accept("test.tgz"));
        assertFalse(filter.accept("test.tar.bz2"));
        assertFalse(filter.accept("test.tbz2"));
        assertFalse(filter.accept("test.gz"));
        assertFalse(filter.accept("test.bz2"));
        assertFalse(filter.accept("test.iso"));
        assertFalse(filter.accept("test.nrg"));
        assertFalse(filter.accept("test.a"));
        assertFalse(filter.accept("test.ar"));
        assertFalse(filter.accept("test.deb"));
        assertFalse(filter.accept("test.lst"));

        assertFalse(filter.accept("test"));
        assertFalse(filter.accept(""));
    }

    /**
     * Tests case-sensitive filtering.
     */
    public void testCaseSensitive() {test(true);}

    /**
     * Tests case-insensitive filtering.
     */
    public void testCaseInsensitive() {test(false);}
}
