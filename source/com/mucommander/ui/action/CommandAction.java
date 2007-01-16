package com.mucommander.ui.action;

import com.mucommander.Debug;
import com.mucommander.file.FileFactory;
import com.mucommander.ui.MainFrame;
import com.mucommander.file.AbstractFile;
import com.mucommander.PlatformManager;
import com.mucommander.text.Translator;
import com.mucommander.command.Command;
import com.mucommander.process.ProcessRunner;
import com.mucommander.ui.table.FileTable;
import com.mucommander.job.TempCommandJob;
import com.mucommander.ui.ProgressDialog;
import com.mucommander.file.impl.local.FSFile;
import com.mucommander.file.FileProtocols;

import javax.swing.*;

/**
 * @author Nicolas Rinaudo
 */
public class CommandAction extends MucoAction {
    private Command command;

    public CommandAction(MainFrame mainFrame, Command command) {
        super(mainFrame, false);
        this.command = command;
        setLabel(command.getAlias());
    }


    public void performAction() {
        FileTable fileTable = mainFrame.getActiveTable();
        AbstractFile selectedFile = fileTable.getSelectedFile(true);

        if(selectedFile == null)
            return;

        if(selectedFile.getURL().getProtocol().equals(FileProtocols.FILE) && (selectedFile instanceof FSFile)) {
            try {ProcessRunner.execute(command.getTokens(selectedFile), selectedFile);}
            catch(Exception e) {
                if(Debug.ON) {
                    Debug.trace("Failed to execute command: " + command.getCommand());
                    Debug.trace(e);
                }
            }
        }
        else {
            ProgressDialog progressDialog = new ProgressDialog(mainFrame, Translator.get("copy_dialog.copying"));
            TempCommandJob job = new TempCommandJob(progressDialog, mainFrame, selectedFile, FileFactory.getTemporaryFile(selectedFile.getName(), true), command);
            progressDialog.start(job);
        }
    }
}
