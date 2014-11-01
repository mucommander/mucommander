/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
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

package com.mucommander.io.backup;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Opens an input stream on a file that has been saved by {@link BackupOutputStream}.
 * <p>
 * This class' role is to choose which of the original or backup file should be read in order to ensure
 * that the data is not corrupt.
 * </p>
 * @see BackupOutputStream
 * @author Nicolas Rinaudo
 */
public class BackupInputStream extends FilterInputStream implements BackupConstants {
    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------
    /**
     * Opens a backup input stream on the specified file.
     * @param     file        file to open for reading.
     * @exception IOException thrown if any IO related error occurs.
     */
    public BackupInputStream(File file) throws IOException {super(getInputStream(FileFactory.getFile(file.getAbsolutePath())));}

    /**
     * Opens a backup input stream on the specified file.
     * @param     path        path to the file to open for reading.
     * @exception IOException thrown if any IO related error occurs.
     */
    public BackupInputStream(String path) throws IOException {super(getInputStream(FileFactory.getFile((new File(path)).getAbsolutePath())));}

    /**
     * Opens a backup input stream on the specified file.
     * @param     file        file to open for reading.
     * @exception IOException thrown if any IO related error occurs.
     */
    public BackupInputStream(AbstractFile file) throws IOException {super(getInputStream(file));}

    /**
     * Opens a stream on the right file.
     * <p>
     * If a backup file is found, and is bigger than the target file, then it will be used.
     * </p>
     * @param     file        file on which to open an input stream.
     * @return                a stream on the right file.
     * @exception IOException thrown if any IO related error occurs.
     */
    private static InputStream getInputStream(AbstractFile file) throws IOException {
        AbstractFile backup;
        FileURL test;

        test = (FileURL)file.getURL().clone();
        test.setPath(test.getPath() + BACKUP_SUFFIX);

        // Checks whether the backup file is a better choice than the target one.
        backup = FileFactory.getFile(test);
        if(backup != null && backup.exists() && (file.getSize() < backup.getSize()))
            return backup.getInputStream();

        // Opens a stream on the target file.
        return file.getInputStream();
    }
}
