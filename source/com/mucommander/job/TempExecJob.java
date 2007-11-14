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

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This job copies a file or a set of files to a temporary folder, makes the temporary file(s) read-only and
 * executes each of them with native file associations. The temporary files are deleted when the JVM terminates.
 *
 * <p>It is important to understand that when this job operates on a set of files a process is started for each file
 * to execute, so this operation should require confirmation by the user before being attempted.</p>
 *
 * @author Maxence Bernard
 */
public class TempExecJob extends TempCopyJob {

    /**
     * Creates a new <code>TempExecJob</code> that operates on a single file.
     *
     * @param progressDialog the ProgressDialog that monitors this job
     * @param mainFrame the MainFrame this job is attached to
     * @param fileToExecute the file to copy to a temporary location and execute
     */
    public TempExecJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile fileToExecute) {
        super(progressDialog, mainFrame, fileToExecute);
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
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected boolean processFile(AbstractFile file, Object recurseParams) {
        if(!super.processFile(file, recurseParams))
            return false;

        // Try to open the file.
        PlatformManager.open(destFile);

        return true;
    }
}
