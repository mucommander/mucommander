package com.mucommander.job;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.FileCollisionDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.command.Command;
import com.mucommander.process.ProcessRunner;

import java.io.File;

/**
 * This job copies a file to a temporary local file, makes the temporary file read-only and executes it
 * with a customisable command.
 *
 * @author Maxence Bernard, Nicolas Rinaudo
 */
public class TempOpenWithJob extends CopyJob {
    private AbstractFile tempFile;
    private Command      command;

    public TempOpenWithJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile fileToExecute, AbstractFile tempFile, Command command) {
        super(progressDialog, mainFrame, new FileSet(fileToExecute.getParent(), fileToExecute), tempFile.getParent(), tempFile.getName(), COPY_MODE, FileCollisionDialog.OVERWRITE_ACTION);
        this.tempFile = tempFile;
        this.command  = command;
    }

    protected void jobCompleted() {
        super.jobCompleted();

        // Make the temporary file read only
        new File(tempFile.getAbsolutePath()).setReadOnly();

        try {ProcessRunner.execute(command.getTokens(tempFile), tempFile);}
        catch(Exception e) {}
    }
}
