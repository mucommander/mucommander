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

package com.mucommander.job;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.PermissionAccesses;
import com.mucommander.commons.file.PermissionTypes;
import com.mucommander.commons.file.util.FileSet;
import com.mucommander.desktop.DesktopManager;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;
import com.mucommander.ui.main.quicklist.RecentExecutedFilesQL;

/**
 * This job copies a file or a set of files to a temporary folder, makes the temporary file(s) read-only and
 * executes each of them with native file associations. The temporary files are deleted when the JVM terminates.
 *
 * <p>It is important to understand that when this job operates on a set of files, a process is started for each file
 * to execute, so this operation should require confirmation by the user before being attempted.</p>
 *
 * @author Maxence Bernard
 */
public class TempExecJob extends TempCopyJob {
	private static final Logger LOGGER = LoggerFactory.getLogger(TempExecJob.class);
	
    /** Files to execute */
    private FileSet filesToExecute;

    /**
     * Creates a new <code>TempExecJob</code> that operates on a single file.
     *
     * @param progressDialog the ProgressDialog that monitors this job
     * @param mainFrame the MainFrame this job is attached to
     * @param fileToExecute the file to copy to a temporary location and execute
     */
    public TempExecJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile fileToExecute) {
        this(progressDialog, mainFrame, new FileSet(fileToExecute.getParent(), fileToExecute));
    }

    /**
     * Creates a new <code>TempExecJob</code> that operates on a set of files. Only a single command get executed, operating on
     * all files.
     *
     * @param progressDialog the ProgressDialog that monitors this job
     * @param mainFrame the MainFrame this job is attached to
     * @param filesToExecute the set of files to copy to a temporary location and execute
     */
    public TempExecJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet filesToExecute) {
        super(progressDialog, mainFrame, filesToExecute);

        this.filesToExecute = filesToExecute;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    @Override
    protected boolean processFile(AbstractFile file, Object recurseParams) {
        if(!super.processFile(file, recurseParams))
            return false;

        // TODO: temporary files seem to remain after the JVM quits under Mac OS X, even if the files permissions are unchanged

        // Execute the file, only if it is one of the top-level files
        if(filesToExecute.indexOf(file)!=-1) {
            if(!currentDestFile.isDirectory()) {        // Do not change directories' permissions
                try {
                    // Make the temporary file read only
                    if(currentDestFile.getChangeablePermissions().getBitValue(PermissionAccesses.USER_ACCESS, PermissionTypes.WRITE_PERMISSION))
                        currentDestFile.changePermission(PermissionAccesses.USER_ACCESS, PermissionTypes.WRITE_PERMISSION, false);
                }
                catch(IOException e) {
                    LOGGER.debug("Caught exeception while changing permissions of "+currentDestFile, e);
                    return false;
                }
            }

            // Try to open the file.
            try {
                DesktopManager.open(currentDestFile);
                RecentExecutedFilesQL.addFile(file);
            }
            catch(Exception e) {
                LOGGER.debug("Caught exeception while opening "+currentDestFile, e);
                return false;
            }
        }

        return true;
    }
}
