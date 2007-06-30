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
import com.mucommander.ui.FileCollisionDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;

import java.io.File;

/**
 * @author Nicolas Rinaudo
 */
public class TempCommandJob extends CopyJob {
    private Command      command;
    private AbstractFile tempFile;

    public TempCommandJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile fileToExecute, AbstractFile tempFile, Command command) {
        super(progressDialog, mainFrame, new FileSet(fileToExecute.getParent(), fileToExecute), tempFile.getParent(), tempFile.getName(), COPY_MODE, FileCollisionDialog.OVERWRITE_ACTION);
        this.tempFile = tempFile;
        this.command  = command;
    }

    protected void jobCompleted() {
        super.jobCompleted();

        // Make the temporary file read only
        new File(tempFile.getAbsolutePath()).setReadOnly();

        // Try to execute the command on the file.
        try {ProcessRunner.execute(command.getTokens(tempFile), tempFile);}
        catch(Exception e) {
            if(Debug.ON) {
                Debug.trace("Failed to execute command: " + command.getCommand());
                Debug.trace(e);
            }
        }
    }
}
