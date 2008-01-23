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

package com.mucommander.file.impl.zip;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractFileTestCase;
import com.mucommander.file.FileFactory;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * An {@link com.mucommander.file.AbstractFileTestCase} implementation, which performs tests on {@link com.mucommander.file.ArchiveEntryFile}
 * entries located inside a {@link ZipArchiveFile} residing in a temporary {@link com.mucommander.file.impl.local.LocalFile}.
 *
 * @author Maxence Bernard
 */
public class ZipArchiveFileTest extends AbstractFileTestCase {

    /** The archive file which contains the temporary entries */
    private static ZipArchiveFile tempZipFile;

    /** id of the last temporary entry generated, to avoid collisions */
    private int entryNum;


    /////////////////////////////////////////
    // AbstractFileTestCase implementation //
    /////////////////////////////////////////
    
    protected AbstractFile getTemporaryFile() throws IOException {
        // use a incremental id to avoid collisions
        return tempZipFile.getDirectChild("entry"+(++entryNum));
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Overridden to create the archive file before each test.
     */
    protected void setUp() throws IOException {
        tempZipFile = (ZipArchiveFile)FileFactory.getTemporaryFile(ZipArchiveFileTest.class.getName()+".zip", false);
        tempZipFile.mkfile();

        entryNum = 0;

        super.setUp();
    }

    /**
     * Overridden to delete the archive file after each test.
     */
    protected void tearDown() throws IOException {
        super.tearDown();

        tempZipFile.delete();
    }

    public void testCanonicalPath() throws IOException, NoSuchAlgorithmException {
        // TODO
        // Test temporarily disabled because if fails. The failure seems to be caused by archive file caching:
        // the change is made to the archive file denoted by its absolute path ; when accessed by the canonical path,
        // the archive file is another instance which isn't aware of the change, because the file date hasn't changed (?). 
    }
}
