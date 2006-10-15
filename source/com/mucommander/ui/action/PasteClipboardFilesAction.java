package com.mucommander.ui.action;

import com.mucommander.ui.MainFrame;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.ui.FileExistsDialog;
import com.mucommander.ui.dnd.ClipboardSupport;
import com.mucommander.ui.dnd.ClipboardNotifier;
import com.mucommander.text.Translator;
import com.mucommander.job.CopyJob;
import com.mucommander.file.FileSet;
import com.mucommander.file.AbstractFile;
import com.mucommander.PlatformManager;

/**
 * This action pastes the files contained by the system clipboard to the currently active folder.
 * Does nothing if the clipboard doesn't contain any file.
 *
 * <p>Under Java 1.5 and up, this action gets automatically enabled/disabled when files are present/not present
 * in the clipboard.
 *
 * @author Maxence Bernard
 */
public class PasteClipboardFilesAction extends MucoAction {

    public PasteClipboardFilesAction(MainFrame mainFrame) {
        super(mainFrame);

        // Allows this action to be dynamically enabled when the clipboard contains files, and disabled otherwise.
        // ClipboardNotifier requires Java 1.5 and does not work under Mac OS X (tested under Tiger with Java 1.5.0_06)
        if(PlatformManager.JAVA_VERSION >= PlatformManager.JAVA_1_5 && PlatformManager.OS_FAMILY!=PlatformManager.MAC_OS_X)
            new ClipboardNotifier(this);
    }

    public void performAction() {
        // Retrieve clipboard files
        FileSet clipboardFiles = ClipboardSupport.getClipboardFiles();
        if(clipboardFiles==null || clipboardFiles.isEmpty())
            return;

        // Start copying files
        ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
        AbstractFile destFolder = mainFrame.getLastActiveTable().getCurrentFolder();
        CopyJob job = new CopyJob(progressDialog, mainFrame, clipboardFiles, destFolder, null, CopyJob.COPY_MODE, FileExistsDialog.ASK_ACTION);
        progressDialog.start(job);
    }
}
