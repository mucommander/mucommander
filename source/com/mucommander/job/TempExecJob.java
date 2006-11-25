package com.mucommander.job;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.ui.FileCollisionDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;

import java.io.File;

/**
 * This job copies a file to a temporary local file, makes the temporary file read-only and executes it
 * with native file associations.
 *
 * @author Maxence Bernard
 */
public class TempExecJob extends CopyJob {

    private AbstractFile tempFile;

    public TempExecJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile fileToExecute, AbstractFile tempFile) {
        super(progressDialog, mainFrame, new FileSet(fileToExecute.getParent(), fileToExecute), tempFile.getParent(), tempFile.getName(), COPY_MODE, FileCollisionDialog.OVERWRITE_ACTION);
        this.tempFile = tempFile;
    }

    protected void jobCompleted() {
        super.jobCompleted();

        // Make the temporary
        new File(tempFile.getAbsolutePath()).setReadOnly();

        // Tries to execute file with native file associations
        PlatformManager.open(tempFile);
    }
}
