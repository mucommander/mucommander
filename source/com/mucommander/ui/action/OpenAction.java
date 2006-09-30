package com.mucommander.ui.action;

import com.mucommander.PlatformManager;
import com.mucommander.job.TempExecJob;
import com.mucommander.text.Translator;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;
import com.mucommander.file.AbstractArchiveFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.table.FileTable;

/**
 * This action 'opens' the currently selected file or folder.
 *
 * @author Maxence Bernard
 */
public class OpenAction extends MucoAction {

    public OpenAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getLastActiveTable();
        AbstractFile selectedFile = fileTable.getSelectedFile(true);

        if(selectedFile==null)
            return;

        if(selectedFile.isBrowsable()) {
            fileTable.getFolderPanel().trySetCurrentFolder(selectedFile);
        }
        else if(selectedFile.getURL().getProtocol().equals("file") && (selectedFile instanceof FSFile)) {
            // Execute file with native file associations
            PlatformManager.open(selectedFile);
        }
        else {
//            // Tries to execute file
//            PlatformManager.open(selectedFile);
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            TempExecJob job = new TempExecJob(progressDialog, mainFrame, selectedFile, FileFactory.getTemporaryFile(selectedFile.getName(), true));
            progressDialog.start(job);
        }
    }
}
