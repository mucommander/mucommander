/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
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

package com.mucommander.commons.file;

import com.mucommander.commons.file.impl.local.LocalFileTest;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * A test case for {@link com.mucommander.commons.file.SimpleFileAttributes}.
 *
 * @see com.mucommander.commons.file.SimpleFileAttributes
 * @author Maxence Bernard
 */
public class SimpleFileAttributesTest extends TestCase {

    /**
     * Creates a SimpleFileAttributes instance from an AbstractFile and ensures that the values returned by
     * SimpleFileAttributes' getters match those of AbstractFile.
     *
     * @throws IOException should not happen
     */
    public void testAccessors() throws IOException {
        LocalFileTest lft = new LocalFileTest();

        // File doesn't exist
        AbstractFile tempFile = lft.getTemporaryFile();
        assertAttributesMatch(tempFile, new SimpleFileAttributes(tempFile));

        // File exists as a regular file
        tempFile.mkfile();
        assertAttributesMatch(tempFile, new SimpleFileAttributes(tempFile));

        // File exists as a directory
        tempFile.delete();
        tempFile.mkdir();
        assertAttributesMatch(tempFile, new SimpleFileAttributes(tempFile));
    }

    /**
     * Asserts that the attributes of the given AbstractFile and SimpleFileAttributes match.
     */
    private void assertAttributesMatch(AbstractFile file, SimpleFileAttributes attrs) {
        assertEquals(file.getAbsolutePath(), attrs.getPath());
        assertEquals(file.exists(), attrs.exists());
        assertEquals(file.getDate(), attrs.getDate());
        assertEquals(file.getSize(), attrs.getSize());
        assertEquals(file.isDirectory(), attrs.isDirectory());
        assertEquals(file.getPermissions(), attrs.getPermissions());
        assertEquals(file.getOwner(), attrs.getOwner());
        assertEquals(file.getGroup(), attrs.getGroup());
    }

    /**
     * Creates a SimpleFileAttributes instance with the no-arg constructor and ensures that the default values returned
     * by SimpleFileAttributes' getters are as specified by {@link FileAttributes}.
     */
    public void testDefaultValues() {
        SimpleFileAttributes attrs = new SimpleFileAttributes();
        assertEquals(null, attrs.getPath());
        assertEquals(false, attrs.exists());
        assertEquals(0, attrs.getDate());
        assertEquals(0, attrs.getSize());
        assertEquals(false, attrs.isDirectory());
        assertEquals(null, attrs.getPermissions());
        assertEquals(null, attrs.getOwner());
        assertEquals(null, attrs.getGroup());
    }
}
