package com.mucommander.ui.action;

import com.mucommander.PlatformManager;
import com.mucommander.job.TempExecJob;
import com.mucommander.text.Translator;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FSFile;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.table.FileTable;

/**
 * This action 'opens' the currently selected file or folder, which means different things depending on
 * the kind of file:
 * <ul>
 * <li>For Browsable files (directory, archive...): shows file contents
 * <li>For local file that are not an archive or archive entry: executes file with native file associations
 * <li>For any other file type, remote or local: copies file to a temporary local file and executes it with native file associations
 * </ul>
 *
 * @author Maxence Bernard
 */
public class OpenAction extends MucoAction {

    public OpenAction(MainFrame mainFrame) {
        super(mainFrame);
    }

    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();
        AbstractFile selectedFile = fileTable.getSelectedFile(true);

        if(selectedFile==null)
            return;

        // Browsable file: show file contents
        if(selectedFile.isBrowsable()) {
            fileTable.getFolderPanel().trySetCurrentFolder(selectedFile);
        }
        // Local file that is not an archive or archive entry: execute file with native file associations
        else if(selectedFile.getURL().getProtocol().equals("file") && (selectedFile instanceof FSFile)) {
            PlatformManager.open(selectedFile);
        }
        // Any other file remote or local: copy file to a temporary local file and execute it with native file associations
        else {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            TempExecJob job = new TempExecJob(progressDialog, mainFrame, selectedFile, FileFactory.getTemporaryFile(selectedFile.getName(), true));
            progressDialog.start(job);
        }
    }
}
