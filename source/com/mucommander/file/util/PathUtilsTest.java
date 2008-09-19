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

package com.mucommander.file.util;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * This class is a JUnit test case for {@link com.mucommander.file.util.PathUtils}.
 *
 * @author Maxence Bernard
 * @see com.mucommander.file.util.PathUtils
 */
public class PathUtilsTest extends TestCase {

    /**
     * Tests {@link com.mucommander.file.util.PathUtils} by throwing a bunch of sample folders and paths corresponding
     * to all possible situations. 
     *
     * @throws IOException should not happen
     */
    public void testResolveDestination() throws IOException {
        AbstractFile folder = FileFactory.getTemporaryFolder();
        AbstractFile root = folder.getRoot();
        AbstractFile parent = folder.getParent();
        String separator = folder.getSeparator();

        // Test a bunch of destinations that don't contain a new filename in them

        // Absolute path
        assertResult(PathUtils.resolveDestination(folder.getAbsolutePath(), root), folder, null);
        assertResult(PathUtils.resolveDestination(parent.getAbsolutePath(), root), parent, null);
        // Relative path
        assertResult(PathUtils.resolveDestination(".", folder), folder, null);
        assertResult(PathUtils.resolveDestination("./", folder), folder, null);
        assertResult(PathUtils.resolveDestination(folder.getName(), parent), folder, null);
        assertResult(PathUtils.resolveDestination("."+separator+folder.getName(), parent), folder, null);

        // Test a bunch of destinations that have a new filename in them

        String nonExistentFolderName = "test_folder_that_cant_possibly_exist";
        // Absolute path
        assertResult(PathUtils.resolveDestination(folder.getAbsolutePath(true)+nonExistentFolderName, root), folder, nonExistentFolderName);
        // Relative path
        assertResult(PathUtils.resolveDestination(nonExistentFolderName, folder), folder, nonExistentFolderName);
        assertResult(PathUtils.resolveDestination("."+separator+nonExistentFolderName, folder), folder, nonExistentFolderName);
        assertResult(PathUtils.resolveDestination("."+separator+nonExistentFolderName, folder), folder, nonExistentFolderName);
    }

    /**
     * Assert that the results returned by {@link PathUtils#resolveDestination(String, com.mucommander.file.AbstractFile)}
     * in an Object array match the specified folder and new filename values. 
     *
     * @param resolvePathResult the Object array to test
     * @param folder should match the first array element
     * @param newFilename should match the second array element
     */
    private void assertResult(Object resolvePathResult[], AbstractFile folder, String newFilename) {
        assertTrue(folder==null?resolvePathResult[0]==null:folder.equals(resolvePathResult[0]));
        assertTrue(newFilename==null?resolvePathResult[1]==null:newFilename.equals(resolvePathResult[1]));
    }
}
