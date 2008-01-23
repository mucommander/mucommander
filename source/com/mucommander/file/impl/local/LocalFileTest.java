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

package com.mucommander.file.impl.local;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractFileTestCase;
import com.mucommander.file.FileFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * An {@link com.mucommander.file.AbstractFileTestCase} implementation for {@link LocalFile}.
 *
 * @author Maxence Bernard
 */
public class LocalFileTest extends AbstractFileTestCase {


    /////////////////////////////////////////
    // AbstractFileTestCase implementation //
    /////////////////////////////////////////

    protected AbstractFile getTemporaryFile() throws IOException {
        return FileFactory.getTemporaryFile(getClass().getName(), false);
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
    public void testMoveToCaseVariation() throws IOException, NoSuchAlgorithmException {
        // First test with a regular file
        createFile(tempFile, 1);
        AbstractFile destFile = tempFile.getParent().getDirectChild(tempFile.getName().toUpperCase());
        deleteWhenFinished(destFile);

        assertTrue(tempFile.moveTo(destFile));

        // Repeat the test with a directory
        destFile.delete();
        tempFile.mkdir();

        assertTrue(tempFile.moveTo(destFile));
    }

    /**
     * Asserts that {@link com.mucommander.file.impl.local.LocalFile#getUserHome()} returns a file that is not null,
     * is a directory, and exists. 
     */
    public void testUserHome() {
        AbstractFile homeFolder = LocalFile.getUserHome();
        assertNotNull(homeFolder);
        assertTrue(homeFolder.isDirectory());
        assertTrue(homeFolder.exists());
    }

    /**
     * Tests methods related to root drives (e.g. C:\).
     */
    public void testRootDriveMethods() {
        // The following test simply assert that the method doesn't produce an uncaught exception.
        LocalFile.hasRootDrives();

        LocalFile localFile = (LocalFile)tempFile.getAncestor(LocalFile.class);
        localFile.guessFloppyDrive();
        localFile.guessRemovableDrive();
    }

    /**
     * Asserts that {@link com.mucommander.file.impl.local.LocalFile#getVolumeInfo()} returns the same values as
     * {@link com.mucommander.file.impl.local.LocalFile#getTotalSpace()}
     * and {@link com.mucommander.file.impl.local.LocalFile#getFreeSpace()}.
     */
    public void testVolumeInfo() {
        long volumeInfo[] = ((LocalFile)tempFile).getVolumeInfo();

        assertNotNull(volumeInfo);
        assertEquals(volumeInfo[0], tempFile.getTotalSpace());
        assertEquals(volumeInfo[1], tempFile.getFreeSpace());
    }
}
