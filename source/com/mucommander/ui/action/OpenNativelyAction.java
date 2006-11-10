package com.mucommander.ui.action;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileFactory;
import com.mucommander.job.TempExecJob;
import com.mucommander.text.Translator;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;

/**
 * This action opens the currently selected file or folder with native file associations.
 *
 * @author Maxence Bernard
 */
public class OpenNativelyAction extends MucoAction {

    public OpenNativelyAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        AbstractFile selectedFile = mainFrame.getActiveTable().getSelectedFile(true);

        if(selectedFile==null)
            return;

        // Copy file to a temporary local file and execute it with native file associations if:
        // - file is not a directory
        // - file is not on a local filesystem or file is an archive entry
        if(!selectedFile.isDirectory()
                &&(!"file".equals(selectedFile.getURL().getProtocol()) || selectedFile.isArchiveEntry())) {

            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            TempExecJob job = new TempExecJob(progressDialog, mainFrame, selectedFile, FileFactory.getTemporaryFile(selectedFile.getName(), true));
            progressDialog.start(job);
        }
        else {
            // Tries to execute file with native file associations
            PlatformManager.open(selectedFile);
        }
    }
}
