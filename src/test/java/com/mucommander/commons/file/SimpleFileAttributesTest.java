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

package com.mucommander.commons.file;

import com.mucommander.commons.file.impl.local.LocalFileTest;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * A test case for {@link com.mucommander.commons.file.SimpleFileAttributes}.
 *
 * @see com.mucommander.commons.file.SimpleFileAttributes
 * @author Maxence Bernard
 */
public class SimpleFileAttributesTest {

    /**
     * Creates a SimpleFileAttributes instance from an AbstractFile and ensures that the values returned by
     * SimpleFileAttributes' getters match those of AbstractFile.
     *
     * @throws IOException should not happen
     */
    @Test
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
        assert file.getAbsolutePath().equals(attrs.getPath());
        assert file.exists() == attrs.exists();
        assert file.getDate() == attrs.getDate();
        assert file.getSize() == attrs.getSize();
        assert file.isDirectory() == attrs.isDirectory();
        assert file.getPermissions() == attrs.getPermissions();
        assert file.getOwner() == null ? attrs.getOwner() == null : file.getOwner().equals(attrs.getOwner());
        assert file.getGroup() == null ? attrs.getGroup() == null : file.getGroup().equals(attrs.getGroup());
    }

    /**
     * Creates a SimpleFileAttributes instance with the no-arg constructor and ensures that the default values returned
     * by SimpleFileAttributes' getters are as specified by {@link FileAttributes}.
     */
    @Test
    public void testDefaultValues() {
        SimpleFileAttributes attrs = new SimpleFileAttributes();
        assert attrs.getPath() == null;
        assert !attrs.exists();
        assert 0 == attrs.getDate();
        assert 0 ==  attrs.getSize();
        assert !attrs.isDirectory();
        assert attrs.getPermissions() == null;
        assert attrs.getOwner() == null;
        assert attrs.getGroup() == null;
    }
}
