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

package com.mucommander.file.impl.local;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.AbstractFileTestCase;
import com.mucommander.file.FileFactory;

import java.io.IOException;

/**
 * An {@link com.mucommander.file.AbstractFileTestCase} implementation for {@link LocalFile}.
 *
 * @author Maxence Bernard
 */
public class LocalFileTest extends AbstractFileTestCase {

    /////////////////////////////
    // Additional test methods //
    /////////////////////////////

    /**
     * Asserts that a file can be renamed to a filename variation of the same file.
     *
     * @throws IOException should not normally happen
     */
    public void testMoveToCaseVariation() throws IOException {
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


    /////////////////////////////////////////
    // AbstractFileTestCase implementation //
    /////////////////////////////////////////

    protected AbstractFile getTemporaryFile() throws IOException {
        return FileFactory.getTemporaryFile(getClass().getName(), false);
    }
}
