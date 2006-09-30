package com.mucommander.job;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileSet;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.FileExistsDialog;
import com.mucommander.PlatformManager;

/**
 * @author Maxence Bernard
 */
public class TempExecJob extends CopyJob {

    private AbstractFile tempFile;

    public TempExecJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile fileToExecute, AbstractFile tempFile) {
        super(progressDialog, mainFrame, new FileSet(fileToExecute.getParent(), fileToExecute), tempFile.getParent(), tempFile.getName(), COPY_MODE, FileExistsDialog.OVERWRITE_ACTION);
        this.tempFile = tempFile;
    }

    protected void jobCompleted() {
        super.jobCompleted();
        // Tries to execute file
        PlatformManager.open(tempFile);
    }
}
