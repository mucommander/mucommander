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
import com.mucommander.command.Command;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.process.ProcessRunner;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

/**
 * This job copies a file or a set of files to a temporary folder, makes the temporary file(s) read-only and
 * executes them with a specific command. The temporary files are deleted when the JVM terminates.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class TempOpenWithJob extends TempCopyJob {

    /** The commmand to execute, appended with the temporary file path(s) */
    private Command command;

    /**
     * Creates a new <code>TempOpenWithJob</code> that operates on a single file.
     *
     * @param progressDialog the ProgressDialog that monitors this job
     * @param mainFrame the MainFrame this job is attached to
     * @param fileToExecute the file to copy to a temporary location and execute
     * @param command the command used to execute the temporary file
     */
    public TempOpenWithJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile fileToExecute, Command command) {
        super(progressDialog, mainFrame, fileToExecute);
        this.command  = command;
    }

    /**
     * Creates a new <code>TempOpenWithJob</code> that operates on a set of files. Only a single command get executed, operating on
     * all files.
     *
     * @param progressDialog the ProgressDialog that monitors this job
     * @param mainFrame the MainFrame this job is attached to
     * @param filesToExecute the set of files to copy to a temporary location and execute
     * @param command the command used to execute the temporary file
     */
    public TempOpenWithJob(ProgressDialog progressDialog, MainFrame mainFrame, FileSet filesToExecute, Command command) {
        super(progressDialog, mainFrame, filesToExecute);
        this.command  = command;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected void jobCompleted() {
        super.jobCompleted();

        try {
            ProcessRunner.execute(command.getTokens(tempFiles), baseDestFolder);
        }
        catch(Exception e) {
            if(Debug.ON) Debug.trace("Caught exception executing "+command+" "+tempFiles);
        }
    }
}
