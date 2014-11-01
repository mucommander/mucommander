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

package com.mucommander.commons.file.util;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * This class is a JUnit test case for {@link com.mucommander.commons.file.util.PathUtils}.
 *
 * @author Maxence Bernard
 * @see com.mucommander.commons.file.util.PathUtils
 */
public class PathUtilsTest {

    /**
     * Calls {@link #testResolveDestination(AbstractFile)} with the system's temporary folder.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testResolveLocalDestination() throws IOException {
        testResolveDestination(FileFactory.getTemporaryFolder());
    }

    /**
     * Tests {@link com.mucommander.commons.file.util.PathUtils} by throwing at it a bunch of sample paths corresponding to all
     * possible situations.
     *
     * @param baseFolder the base folder, used for relative paths
     * @throws IOException should not happen
     */
    public void testResolveDestination(AbstractFile baseFolder) throws IOException {
        AbstractFile baseRoot = baseFolder.getRoot();
        AbstractFile baseParent = baseFolder.getParent();
        String separator = baseFolder.getSeparator();
        String nonExistentFilename = "non_existent_file";
        AbstractFile nonExistentFile = baseFolder.getDirectChild(nonExistentFilename);
        String existingFilename = "existing_file";
        AbstractFile existingFile = baseFolder.getDirectChild(existingFilename);
        existingFile.mkfile();
        String existingArchiveFilename = "existing_archive.zip";
        AbstractFile existingArchive = baseFolder.getDirectChild(existingArchiveFilename);
        existingArchive.mkfile();

        int expectedType;

        // Test a bunch of destination paths that denote an existing folder

        expectedType = PathUtils.ResolvedDestination.EXISTING_FOLDER;

        // Absolute paths
        assertResult(PathUtils.resolveDestination(baseFolder.getURL().toString(true), baseRoot), baseFolder, expectedType);
        assertResult(PathUtils.resolveDestination(baseFolder.getURL().toString(true), null), baseFolder, expectedType);
        assertResult(PathUtils.resolveDestination(baseRoot.getURL().toString(true), baseRoot), baseRoot, expectedType);
        // Relative paths
        assertResult(PathUtils.resolveDestination(".", baseFolder), baseFolder, expectedType);
        assertResult(PathUtils.resolveDestination("."+baseFolder.getSeparator(), baseFolder), baseFolder, expectedType);
        assertResult(PathUtils.resolveDestination(baseFolder.getName(), baseParent), baseFolder, expectedType);
        assertResult(PathUtils.resolveDestination(baseFolder.getName()+separator, baseParent), baseFolder, expectedType);
        assertResult(PathUtils.resolveDestination("."+separator+baseFolder.getName(), baseParent), baseFolder, expectedType);
        // Archive path as folder (with a trailing separator)
        assertResult(PathUtils.resolveDestination(existingArchive.getURL().toString(true)+separator, baseFolder), existingArchive, expectedType);
        assertResult(PathUtils.resolveDestination(existingArchiveFilename+separator, baseFolder), existingArchive, expectedType);
        assertResult(PathUtils.resolveDestination("."+separator+existingArchiveFilename+separator, baseFolder), existingArchive, expectedType);

        // Test a bunch of destination paths that denote an existing regular file

        expectedType = PathUtils.ResolvedDestination.EXISTING_FILE;

        // Absolute paths
        assertResult(PathUtils.resolveDestination(existingFile.getURL().toString(true), baseRoot), existingFile, expectedType);
        // Relative paths
        assertResult(PathUtils.resolveDestination(existingFilename, baseFolder), existingFile, expectedType);
        assertResult(PathUtils.resolveDestination("."+separator+existingFilename, baseFolder), existingFile, expectedType);
        // Archive path as regular file (without a trailing separator)
        assertResult(PathUtils.resolveDestination(existingArchive.getURL().toString(true), baseFolder), existingArchive, expectedType);
        assertResult(PathUtils.resolveDestination(existingArchiveFilename, baseFolder), existingArchive, expectedType);
        assertResult(PathUtils.resolveDestination("."+separator+existingArchiveFilename, baseFolder), existingArchive, expectedType);

        // Test a bunch of destination paths that denote a new/non-existing regular file

        expectedType = PathUtils.ResolvedDestination.NEW_FILE;

        // Absolute paths
        assertResult(PathUtils.resolveDestination(nonExistentFile.getURL().toString(true), baseRoot), nonExistentFile, expectedType);
        // Relative paths
        assertResult(PathUtils.resolveDestination(nonExistentFilename, baseFolder), nonExistentFile, expectedType);
        assertResult(PathUtils.resolveDestination("."+separator+nonExistentFilename, baseFolder), nonExistentFile, expectedType);

        // Test invalid destination paths

        // neither the file nor its parent exist
        assert PathUtils.resolveDestination(nonExistentFilename+separator+nonExistentFilename, baseFolder) == null;
        assert PathUtils.resolveDestination(nonExistentFilename, baseFolder.getChild(nonExistentFilename)) == null;
        // relative path and no base folder
        assert PathUtils.resolveDestination(nonExistentFilename, null) == null;

        // Delete the files we created when finished
        existingFile.delete();
        existingArchive.delete();
    }

    /**
     * Asserts that the <code>ResolvedDestination</code> returned by {@link PathUtils#resolveDestination(String, com.mucommander.commons.file.AbstractFile)}
     * matches the expected destination file and type. This method asserts that the destination folder is not
     * <code>null</code> and consistent with the destination file.
     *
     * @param resolvedDestination the ResolvedDestination to test
     * @param expectedDestinationFile the expected destination file
     * @param expectedDestinationType the expected destination type
     */
    private void assertResult(PathUtils.ResolvedDestination resolvedDestination, AbstractFile expectedDestinationFile, int expectedDestinationType) {
        AbstractFile file = resolvedDestination.getDestinationFile();
        int type = resolvedDestination.getDestinationType();
        AbstractFile folder = resolvedDestination.getDestinationFolder();

        assert file != null;
        assert folder != null;

        assert expectedDestinationFile.equals(file);
        assert expectedDestinationType == type;

        if(type==PathUtils.ResolvedDestination.EXISTING_FOLDER)
            assert file.equals(folder);
        else
            assert file.getParent().equals(folder);
    }

    /**
     * Tests {@link PathUtils#removeLeadingFragments(String, String, int)}.
     */
    @Test
    public void testRemoveLeadingFragments() {
        assert "home/maxence/".equals(PathUtils.removeLeadingFragments("/home/maxence/", "/", 0));
        assert "maxence/".equals(PathUtils.removeLeadingFragments("/home/maxence/", "/", 1));
        assert "".equals(PathUtils.removeLeadingFragments("/home/maxence/", "/", 2));
        assert "".equals(PathUtils.removeLeadingFragments("/home/maxence/", "/", 3));
        assert "".equals(PathUtils.removeLeadingFragments("/home/maxence/", "\\", 1));
    }

    /**
     * Tests {@link PathUtils#getDepth(String, String)}.
     */
    @Test
    public void testGetDepth() {
        assert 0 == PathUtils.getDepth("/", "/");
        assert 0 == PathUtils.getDepth("", "/");
        assert 1 == PathUtils.getDepth("/home", "/");
        assert 1 == PathUtils.getDepth("/home/", "/");
        assert 2 == PathUtils.getDepth("/home/maxence", "/");
        assert 2 == PathUtils.getDepth("/home/maxence/", "/");

        assert 1 == PathUtils.getDepth("/home/maxence", "\\");
        assert 1 == PathUtils.getDepth("C:", "\\"); 
        assert 1 == PathUtils.getDepth("C:\\", "\\");
        assert 2 == PathUtils.getDepth("C:\\home", "\\");
        assert 2 == PathUtils.getDepth("C:\\home\\", "\\");
        assert 3 == PathUtils.getDepth("C:\\home\\maxence", "\\");
        assert 3 == PathUtils.getDepth("C:\\home\\maxence\\", "\\");
    }


    /**
     * Tests {@link PathUtils#removeLeadingSeparator(String, String)}.
     */
    @Test
    public void testRemoveLeadingSeparator() {
        assert PathUtils.removeLeadingSeparator("/home/", "/").equals("home/");
        assert PathUtils.removeLeadingSeparator("/home/maxence", "/").equals("home/maxence");
        assert PathUtils.removeLeadingSeparator("home/", "/").equals("home/");
        assert PathUtils.removeLeadingSeparator("/home/", "\\").equals("/home/");
        assert PathUtils.removeLeadingSeparator("/", "/").equals("");

        assert PathUtils.removeLeadingSeparator("C:\\home\\", "\\").equals("C:\\home\\");
        assert PathUtils.removeLeadingSeparator("C:\\home\\", "/").equals("C:\\home\\");

        assert PathUtils.removeLeadingSeparator("--home--", "--").equals("home--");
        assert PathUtils.removeLeadingSeparator("--home--maxence", "--").equals("home--maxence");
        assert PathUtils.removeLeadingSeparator("home--", "--").equals("home--");
        assert PathUtils.removeLeadingSeparator("--home--", "/").equals("--home--");
        assert PathUtils.removeLeadingSeparator("--", "--").equals("");
    }

    /**
     * Tests {@link PathUtils#removeTrailingSeparator(String, String)}. 
     */
    @Test
    public void testRemoveTrailingSeparator() {
        assert PathUtils.removeTrailingSeparator("/home/", "/").equals("/home");
        assert PathUtils.removeTrailingSeparator("/home/maxence", "/").equals("/home/maxence");
        assert PathUtils.removeTrailingSeparator("/home/maxence/", "/").equals("/home/maxence");
        assert PathUtils.removeTrailingSeparator("/home/", "\\").equals("/home/");
        assert PathUtils.removeTrailingSeparator("/", "/").equals("");

        assert PathUtils.removeTrailingSeparator("C:\\home", "\\").equals("C:\\home");
        assert PathUtils.removeTrailingSeparator("C:\\home\\", "\\").equals("C:\\home");
        assert PathUtils.removeTrailingSeparator("C:\\home\\maxence", "\\").equals("C:\\home\\maxence");
        assert PathUtils.removeTrailingSeparator("C:\\home\\maxence", "\\").equals("C:\\home\\maxence");
        assert PathUtils.removeTrailingSeparator("C:\\home\\", "/").equals("C:\\home\\");

        assert PathUtils.removeTrailingSeparator("--home--", "--").equals("--home");
        assert PathUtils.removeTrailingSeparator("--home--maxence", "--").equals("--home--maxence");
        assert PathUtils.removeTrailingSeparator("--home--maxence--", "--").equals("--home--maxence");
        assert PathUtils.removeTrailingSeparator("--home--", "/").equals("--home--");
        assert PathUtils.removeTrailingSeparator("--", "--").equals("");
    }

    /**
     * Tests {@link PathUtils#pathEquals(String, String, String)}.
     */
    @Test
    public void testPathEquals() {
        assert PathUtils.pathEquals("/home/", "/home/", "/");
        assert PathUtils.pathEquals("/home", "/home", "/");
        assert PathUtils.pathEquals("/home/", "/home/", "\\");
        assert PathUtils.pathEquals("/home/", "/home", "/");
        assert PathUtils.pathEquals("/home", "/home/", "/");

        assert PathUtils.pathEquals("C:\\home\\", "C:\\home\\", "\\");
        assert PathUtils.pathEquals("C:\\home", "C:\\home", "\\");
        assert PathUtils.pathEquals("C:\\home\\", "C:\\home\\", "/");
        assert PathUtils.pathEquals("C:\\home\\", "C:\\home", "\\");
        assert PathUtils.pathEquals("C:\\home", "C:\\home\\", "\\");

        assert PathUtils.pathEquals("--home--", "--home--", "--");
        assert PathUtils.pathEquals("--home", "--home", "--");
        assert PathUtils.pathEquals("--home--", "--home--", "/");
        assert PathUtils.pathEquals("--home--", "--home", "--");
        assert PathUtils.pathEquals("--home", "--home--", "--");

        assert !(PathUtils.pathEquals("/", "/home", "/"));
        assert !(PathUtils.pathEquals("/home", "/home/", "\\"));
        assert !(PathUtils.pathEquals("/home/", "/home", "\\"));

        assert !(PathUtils.pathEquals("C:\\", "C:\\home", "\\"));
        assert !(PathUtils.pathEquals("C:\\home", "C:\\home\\", "/"));
        assert !(PathUtils.pathEquals("C:\\home\\", "C:\\home", "/"));

        assert !(PathUtils.pathEquals("--", "--home", "--"));
        assert !(PathUtils.pathEquals("--home", "--home--", "/"));
        assert !(PathUtils.pathEquals("--home--", "--home", "/"));
    }
}
