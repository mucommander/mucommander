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
