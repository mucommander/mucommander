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

package com.mucommander.job;

import com.mucommander.Debug;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FilePermissions;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.IOException;

/**
 * This job copies a file or a set of files to a temporary folder and makes the temporary file(s) read-only.
 * The temporary files are deleted when the JVM terminates.
 *
 * @author Maxence Bernard
 */
public class TempCopyJob extends CopyJob {

    /** This list is populated with temporary files, as they are created by processFile() */
    protected FileSet tempFiles;

    /**
     * Creates a new <code>TempExecJob</code> that operates on a single file.
     *
     * @param progressDialog the ProgressDialog that monitors this job
     * @param mainFrame the MainFrame this job is attached to
     * @param fileToCopy the file to copy to a temporary location
     */
    public TempCopyJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile fileToCopy) {
        super(progressDialog, mainFrame, new FileSet(fileToCopy.getParentSilently(), fileToCopy), FileFactory.getTemporaryFolder(), getTemporaryFileName(fileToCopy), COPY_MODE, FileCollisionDialog.OVERWRITE_ACTION);
        tempFiles = new FileSet(baseDestFolder);
    }

    /**
     * Creates a new <code>TempExecJob</code> that operates on a single file.
     *
     * @param progressDialog the ProgressDialog that monitors this job
     * @param mainFrame the MainFrame this job is attached to
     * @param filesToCopy the file to copy to a temporary location
     */
    public TempCopyJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet filesToCopy) {
        super(progressDialog, mainFrame, filesToCopy, getTemporaryFolder(filesToCopy), null, COPY_MODE, FileCollisionDialog.OVERWRITE_ACTION);
        tempFiles = new FileSet(baseDestFolder);
    }


    protected static AbstractFile getTemporaryFolder(FileSet filesToExecute) {
        AbstractFile tempFolder;
        try {
            tempFolder = FileFactory.getTemporaryFile(filesToExecute.getBaseFolder().getName(), true);
            tempFolder.mkdir();
        }
        catch(IOException e) {
            tempFolder = FileFactory.getTemporaryFolder();
        }

        return tempFolder;
    }

    protected static String getTemporaryFileName(AbstractFile fileToExecute) {
        try {
            return FileFactory.getTemporaryFile(fileToExecute.getName(), true).getName();
        }
        catch(IOException e) {
            // Should never happen under normal circumstances.
            if(Debug.ON) Debug.trace("Caught exception instanciating temporary file, this should not happen!");
            return fileToExecute.getName();
        }
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected boolean processFile(AbstractFile file, Object recurseParams) {
        if(!super.processFile(file, recurseParams))
            return false;

        // Make the temporary file read only
        if(currentDestFile.canSetPermission(FilePermissions.USER_ACCESS, FilePermissions.WRITE_PERMISSION))
            currentDestFile.setPermission(FilePermissions.USER_ACCESS, FilePermissions.WRITE_PERMISSION, false);

        tempFiles.add(currentDestFile);

        return true;
    }
}
