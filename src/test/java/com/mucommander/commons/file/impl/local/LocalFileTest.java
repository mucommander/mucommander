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

package com.mucommander.commons.file.impl.local;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AbstractFileTest;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileOperation;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;


/**
 * An {@link AbstractFileTest} implementation for {@link LocalFile}.
 *
 * @author Maxence Bernard
 */
@Test
public class LocalFileTest extends AbstractFileTest {

    /////////////////////////////////////
    // AbstractFileTest implementation //
    /////////////////////////////////////

    @Override
    public AbstractFile getTemporaryFile() throws IOException {
        return FileFactory.getTemporaryFile(getClass().getName(), false);
    }

    @Override
    public FileOperation[] getSupportedOperations() {
        return new FileOperation[] {
            FileOperation.READ_FILE,
            FileOperation.RANDOM_READ_FILE,
            FileOperation.WRITE_FILE,
            FileOperation.APPEND_FILE,
            FileOperation.RANDOM_WRITE_FILE,
            FileOperation.CREATE_DIRECTORY,
            FileOperation.LIST_CHILDREN,
            FileOperation.DELETE,
            FileOperation.RENAME,
            FileOperation.CHANGE_DATE,
            FileOperation.CHANGE_PERMISSION,
            FileOperation.GET_FREE_SPACE,
            FileOperation.GET_TOTAL_SPACE
        };
    }

    /////////////////////////////////////////
    // Additional LocalFile-specific tests //
    /////////////////////////////////////////

    /**
     * Asserts that a file can be renamed to a filename variation of the same file.
     *
     * @throws IOException should not normally happen
     * @throws NoSuchAlgorithmException should not happen
     */
    @Test
    public void testRenameToCaseVariation() throws IOException, NoSuchAlgorithmException {
        // First test with a regular file
        createFile(tempFile, 1);
        AbstractFile destFile = tempFile.getParent().getDirectChild(tempFile.getName().toUpperCase());
        deleteWhenFinished(destFile);

        tempFile.renameTo(destFile);
        assert !destFile.isSymlink();          // Leave me

        // Repeat the test with a directory
        destFile.delete();
        tempFile.mkdir();

        tempFile.renameTo(destFile);
        assert !destFile.isSymlink();          // Leave me
    }

    /**
     * Asserts that {@link com.mucommander.commons.file.impl.local.LocalFile#getUserHome()} returns a file that is not null,
     * is a directory, and exists, and that '~' can be resolved as the user home folder.
     *
     * @throws IOException should not happen 
     */
    @Test
    public void testUserHome() throws IOException {
        AbstractFile homeFolder = LocalFile.getUserHome();
        assert homeFolder != null;
        assert homeFolder.isDirectory();
        assert homeFolder.exists();

        assert homeFolder.equals(FileFactory.getFile("~"));
        assert homeFolder.getChild("blah").equals(FileFactory.getFile("~").getChild("blah"));
    }

    /**
     * Tests methods related to root drives (e.g. C:\).
     */
    @Test
    public void testRootDriveMethods() {
        // The following test simply assert that the method doesn't produce an uncaught exception.
        LocalFile.hasRootDrives();

        LocalFile localFile = (LocalFile)tempFile.getAncestor(LocalFile.class);
        localFile.guessRemovableDrive();
    }

    /**
     * Asserts that {@link com.mucommander.commons.file.impl.local.LocalFile#getVolumeInfo()} returns the same values as
     * {@link com.mucommander.commons.file.impl.local.LocalFile#getTotalSpace()}
     * and {@link com.mucommander.commons.file.impl.local.LocalFile#getFreeSpace()}.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testVolumeInfo() throws IOException {
        long volumeInfo[] = ((LocalFile)tempFile).getVolumeInfo();

        assert volumeInfo != null;
        assert volumeInfo[0] == tempFile.getTotalSpace();
        assert volumeInfo[1] == tempFile.getFreeSpace();
    }

    /**
     * Tests the volumes returned by {@link LocalFile#getVolumes()} by calling {@link #testVolume(AbstractFile)} for
     * each of them.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testVolumes() throws IOException {
        AbstractFile[] volumes = LocalFile.getVolumes();

        assert volumes != null;
        assert volumes.length>0;

        for (AbstractFile volume : volumes)
            testVolume(volume);
    }

    /**
     * Tests the regex pattern
     */
    @Test
    public void testDrivePattern() {
        Matcher matcher = LocalFile.DRIVE_ROOT_PATTERN.matcher("C:\\");
        assert matcher.matches();

        matcher = LocalFile.DRIVE_ROOT_PATTERN.matcher("C:");
        assert !matcher.matches();

        matcher = LocalFile.DRIVE_ROOT_PATTERN.matcher("C:\\blah");
        assert !matcher.matches();
        matcher.reset();
        assert matcher.find();

        matcher = LocalFile.DRIVE_ROOT_PATTERN.matcher("/blah/C:\\");
        assert !matcher.matches();
    }
}
